package repositories.v1

import CheckpointRepository
import errors.RepositoryException
import models.Checkpoint
import DatabaseFactory.dbQuery
import models.CheckpointStatus
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class DBCheckpointRepository : CheckpointRepository {

    private val logger = LoggerFactory.getLogger(DBCheckpointRepository::class.java)

    override suspend fun addCheckpoint(checkpoint: Checkpoint) {
        logger.info("Adding checkpoint: $checkpoint")
        dbQuery { connection ->
            try {
                val query = "INSERT INTO checkpoints (id, guard_fio, guard_phone, residential_id, number, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?)"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(checkpoint.id))
                    statement.setString(2, checkpoint.guardFio)
                    statement.setString(3, checkpoint.guardPhone)
                    statement.setObject(4, UUID.fromString(checkpoint.residentialId))
                    statement.setString(5, checkpoint.number)
                    statement.setString(6, checkpoint.status.toString())
                    statement.executeUpdate()
                    logger.debug("Checkpoint successfully added: ${checkpoint.id}")
                }
            } catch (e: SQLException) {
                logger.error("Failed to add checkpoint: ${e.message}", e)
                if (e.sqlState == "23505") {
                    throw RepositoryException.AlreadyExistsException(
                        "Checkpoint with id=${checkpoint.id} already exists")
                }
                throw RepositoryException.NoDataAccessException("Database error while adding checkpoint: ${e.message}")
            }
        }
    }

    override suspend fun deleteCheckpoint(checkpoint: Checkpoint) {
        logger.info("Deleting checkpoint: ${checkpoint.id}")
        dbQuery { connection ->
            try {
                val query = "DELETE FROM checkpoints WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(checkpoint.id))
                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected == 0) {
                        logger.warn("Checkpoint not found for deletion: ${checkpoint.id}")
                        throw RepositoryException.NotFoundException("Checkpoint with id=${checkpoint.id} not found")
                    }
                    logger.debug("Checkpoint deleted: ${checkpoint.id}")
                }
            } catch (e: SQLException) {
                logger.error("Failed to delete checkpoint: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while deleting checkpoint: ${e.message}")
            }
        }
    }

    override suspend fun updateCheckpoint(checkpoint: Checkpoint) {
        logger.info("Updating checkpoint: ${checkpoint.id}")
        dbQuery { connection ->
            try {
                val query =
                    "UPDATE checkpoints SET guard_fio = ?, guard_phone = ?, residential_id = ?, number = ?, status = ? " +
                            "WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, checkpoint.guardFio)
                    statement.setString(2, checkpoint.guardPhone)
                    statement.setObject(3, UUID.fromString(checkpoint.residentialId))
                    statement.setString(4, checkpoint.number)
                    statement.setString(5, checkpoint.status.toString())
                    statement.setObject(6, UUID.fromString(checkpoint.id))
                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected == 0) {
                        logger.warn("Checkpoint not found for update: ${checkpoint.id}")
                        throw RepositoryException.NotFoundException(
                            "Checkpoint with id=${checkpoint.id} not found")
                    }
                    logger.debug("Checkpoint updated: ${checkpoint.id}")
                }
            } catch (e: SQLException) {
                logger.error("Failed to update checkpoint: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while updating checkpoint: ${e.message}")
            }
        }
    }

    override suspend fun getCheckpointByResidentialIdAndNumber(residentialId: String, number: String): Checkpoint {
        logger.info("Retrieving checkpoint for residentialId=$residentialId, number=$number")
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM checkpoints WHERE residential_id = ? AND number = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(residentialId))
                    statement.setString(2, number)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val checkpoint = resultSet.toCheckpoint()
                        logger.debug("Checkpoint retrieved: $checkpoint")
                        checkpoint
                    } else {
                        logger.warn("Checkpoint not found for residentialId=$residentialId, number=$number")
                        throw RepositoryException.NotFoundException(
                            "Checkpoint not found for residentialId=$residentialId and number=$number")
                    }
                }
            } catch (e: SQLException) {
                logger.error("Failed to retrieve checkpoint: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while retrieving checkpoint: ${e.message}")
            }
        }
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
}
