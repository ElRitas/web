package repositories.v2

import CheckpointRepositoryV2
import DatabaseFactory.dbQuery
import errors.RepositoryException
import models.Checkpoint
import models.CheckpointResponse
import models.CheckpointStatus
import models.Pagination
import java.sql.ResultSet
import java.sql.SQLException
import java.util.UUID
import kotlin.math.min
import kotlin.use

class DBCheckpointRepositoryV2 : CheckpointRepositoryV2 {
    override suspend fun getById(id: String): Checkpoint {
        return dbQuery { connection ->
            try {
                val query = """
                    SELECT * FROM checkpoints
                    WHERE id = ?
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(id))
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val slot = resultSet.toCheckpoint()
                        slot
                    } else {
                        throw RepositoryException.NotFoundException(
                            "Checkpoint with id=$id not found")
                    }
                }
            } catch (e: SQLException) {
                throw RepositoryException.NoDataAccessException("Database error while retrieving slot: ${e.message}")
            }
        }
    }

    override suspend fun getAll(residentialId: String, pageNum: Int, count: Int): CheckpointResponse {
        val checkpoints =  dbQuery { connection ->
            try {
                val query = """
                    SELECT * FROM checkpoints
                    WHERE residential_id = ?
                    LIMIT ? OFFSET ?
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(residentialId))
                    statement.setInt(2, count)
                    statement.setInt(3, ((pageNum - 1) * count))
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val list = resultSet.toCheckpointList()
                        list
                    } else {
                        throw RepositoryException.NotFoundException(
                            "Checkpoints not found")
                    }
                }
            } catch (e: SQLException) {
                throw RepositoryException.NoDataAccessException("Database error while retrieving slot: ${e.message}")
            }
        }
        val total = dbQuery { connection ->
            try {
                val query = """
                    SELECT COUNT(*) FROM checkpoints
                    WHERE residential_id = ?
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(residentialId))
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val total = resultSet.getString("count").toInt()
                        (total + min(total, count) - 1) / min(total, count)
                    } else {
                        throw RepositoryException.NotFoundException(
                            "No slots found"
                        )
                    }
                }
            } catch (e: SQLException) {
                throw RepositoryException.NoDataAccessException("Database error while retrieving slot: ${e.message}")
            }
        }
        return CheckpointResponse(checkpoints, Pagination(total, pageNum))
    }

    private fun ResultSet.toCheckpoint(): Checkpoint {
        return Checkpoint(
            id = getString("id"),
            guardFio = getString("guard_fio"),
            guardPhone = getString("guard_phone"),
            residentialId = getString("residential_id"),
            number = getString("number"),
            status = CheckpointStatus.valueOf(getString("status"))
        )
    }

    private fun ResultSet.toCheckpointList(): List<Checkpoint> {
        val checkList = mutableListOf<Checkpoint>()
        do {
            checkList.add(toCheckpoint())
        } while (next())
        return checkList
    }
}