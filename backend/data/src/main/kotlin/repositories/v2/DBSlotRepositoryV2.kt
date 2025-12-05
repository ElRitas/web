package repositories.v2

import DatabaseFactory.dbQuery
import SlotDA
import SlotRepositoryV2
import models.Slot
import models.SlotStatus
import models.Pagination
import errors.RepositoryException
import models.SlotResponse
import java.sql.ResultSet
import java.sql.SQLException
import java.util.UUID
import kotlin.math.min
import kotlin.use

class DBSlotRepositoryV2 : SlotRepositoryV2 {
    override suspend fun addSlot(slot: Slot) {
        val slotDA = convert(slot)
        dbQuery { connection ->
            try {
                val query = "INSERT INTO slots(id, residential_id, number, status) VALUES (?, ?, ?, ?)"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(slotDA.id))
                    statement.setObject(2, UUID.fromString(slotDA.residential))
                    statement.setInt(3, slotDA.number)
                    statement.setString(4, slotDA.status.toString())
                    statement.executeUpdate()
                }
            } catch (e: SQLException) {
                if (e.sqlState == "23505") {
                    throw RepositoryException.AlreadyExistsException("Slot with id=${slot.id} already exists")
                }
                throw RepositoryException.NoDataAccessException("Database error while adding slot: ${e.message}")
            }
        }
    }

    override suspend fun updateSlotStatus(slotId: String, status: SlotStatus) {
        dbQuery { connection ->
            try {
                val query = "UPDATE slots SET status = ? WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, status.toString())
                    statement.setObject(2, UUID.fromString(slotId))
                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected == 0) {
                        throw RepositoryException.NotFoundException("Slot with id=$slotId not found")
                    }
                }
            } catch (e: SQLException) {
                throw RepositoryException.NoDataAccessException(
                    "Database error while updating slot status: ${e.message}")
            }
        }
    }

    override suspend fun getSlots(resId: String, pageNum: Int, count: Int): SlotResponse {
        val slots =  dbQuery { connection ->
            try {
                val query = """
                    SELECT * FROM slots
                    WHERE residential_id = ?
                    LIMIT ? OFFSET ?
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(resId))
                    statement.setInt(2, count)
                    statement.setInt(3, ((pageNum - 1) * count))
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val slot = resultSet.toSlotsList()
                        slot
                    } else {
                        throw RepositoryException.NotFoundException(
                            "Slots not found")
                    }
                }
            } catch (e: SQLException) {
                throw RepositoryException.NoDataAccessException("Database error while retrieving slot: ${e.message}")
            }
        }
        val total = dbQuery { connection ->
            try {
                val query = """
                    SELECT COUNT(*) FROM slots
                    WHERE residential_id = ?
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(resId))
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
        return SlotResponse(slots, Pagination(total, pageNum))
    }

    override suspend fun getSlotById(id: String): Slot {
        return dbQuery { connection ->
            try {
                val query = """
                    SELECT * FROM slots
                    WHERE id = ?
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(id))
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val slot = resultSet.toSlot()
                        slot
                    } else {
                        throw RepositoryException.NotFoundException(
                            "Slot with id=$id not found")
                    }
                }
            } catch (e: SQLException) {
                throw RepositoryException.NoDataAccessException("Database error while retrieving slot: ${e.message}")
            }
        }
    }

    private fun ResultSet.toSlot(): Slot {
        return Slot(
            id = getString("id"),
            residential = getString("residential_id"),
            number = getInt("number"),
            status = SlotStatus.valueOf(getString("status"))
        )
    }

    private fun ResultSet.toSlotsList(): List<Slot> {
        val slotList = mutableListOf<Slot>()
        do {
            slotList.add(toSlot())
        } while (next())
        return slotList
    }

    private fun convert(slot: Slot): SlotDA {
        return SlotDA(
            slot.id,
            slot.residential,
            slot.number,
            SlotStatusDA.valueOf(slot.status.toString())
        )
    }
}