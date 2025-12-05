package repositories.v1

import SlotRepository
import errors.RepositoryException
import models.Slot
import models.SlotStatus
import DatabaseFactory.dbQuery
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class DBSlotRepository : SlotRepository {

    private val logger = LoggerFactory.getLogger(DBSlotRepository::class.java)

    override suspend fun addSlot(slot: Slot) {
        logger.info("Adding new slot: $slot")
        dbQuery { connection ->
            try {
                val query = "INSERT INTO slots(id, residential_id, number, status) VALUES (?, ?, ?, ?)"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(slot.id))
                    statement.setObject(2, UUID.fromString(slot.residential))
                    statement.setInt(3, slot.number)
                    statement.setString(4, slot.status.toString())
                    statement.executeUpdate()
                    logger.debug("Slot added successfully with id=${slot.id}")
                }
            } catch (e: SQLException) {
                logger.error("Failed to add slot: ${e.message}", e)
                if (e.sqlState == "23505") {
                    throw RepositoryException.AlreadyExistsException("Slot with id=${slot.id} already exists")
                }
                throw RepositoryException.NoDataAccessException("Database error while adding slot: ${e.message}")
            }
        }
    }

    override suspend fun deleteSlot(slot: Slot) {
        logger.info("Deleting slot with id=${slot.id}")
        dbQuery { connection ->
            try {
                val query = "DELETE FROM slots WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(slot.id))
                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected == 0) {
                        logger.warn("Slot not found for deletion: ${slot.id}")
                        throw RepositoryException.NotFoundException("Slot with id=${slot.id} not found")
                    }
                    logger.debug("Slot deleted successfully: ${slot.id}")
                }
            } catch (e: SQLException) {
                logger.error("Error deleting slot: ${e.message}", e)
                throw RepositoryException.NoDataAccessException("Database error while deleting slot: ${e.message}")
            }
        }
    }

    override suspend fun updateSlotStatus(slotId: String, status: SlotStatus) {
        logger.info("Updating slot status. Slot ID=$slotId, New Status=$status")
        dbQuery { connection ->
            try {
                val query = "UPDATE slots SET status = ? WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, status.toString())
                    statement.setObject(2, UUID.fromString(slotId))
                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected == 0) {
                        logger.warn("Slot not found for status update: $slotId")
                        throw RepositoryException.NotFoundException("Slot with id=$slotId not found")
                    }
                    logger.debug("Slot status updated for id=$slotId to $status")
                }
            } catch (e: SQLException) {
                logger.error("Error updating slot status: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while updating slot status: ${e.message}")
            }
        }
    }

    override suspend fun getSlotByResidentialAddressAndNumber(residentialAddress: String, number: Int): Slot {
        logger.info("Retrieving slot by address=$residentialAddress and number=$number")
        return dbQuery { connection ->
            try {
                val query = """
                    SELECT s.* FROM slots s
                    JOIN residentials r ON s.residential_id = r.id
                    WHERE r.address = ? AND s.number = ?
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, residentialAddress)
                    statement.setInt(2, number)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val slot = resultSet.toSlot()
                        logger.debug("Slot found: $slot")
                        slot
                    } else {
                        logger.warn("No slot found for address=$residentialAddress and number=$number")
                        throw RepositoryException.NotFoundException(
                            "Slot with residentialAddress=$residentialAddress and number=$number not found")
                    }
                }
            } catch (e: SQLException) {
                logger.error("Error retrieving slot: ${e.message}", e)
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
}
