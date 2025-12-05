package repositories.v1

import ResidentialRepository
import errors.RepositoryException
import models.Residential
import models.ParkingType
import DatabaseFactory.dbQuery
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class DBResidentialRepository : ResidentialRepository {

    private val logger = LoggerFactory.getLogger(DBResidentialRepository::class.java)

    override suspend fun addResidential(residential: Residential) {
        logger.info("Adding residential: $residential")
        dbQuery { connection ->
            try {
                val query = "INSERT INTO residentials (id, name, address, parking_type, slots_num) VALUES (?, ?, ?, ?, ?)"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(residential.id))
                    statement.setString(2, residential.name)
                    statement.setString(3, residential.address)
                    statement.setString(4, residential.type.name)
                    statement.setInt(5, residential.slotsNumber)
                    statement.executeUpdate()
                    logger.debug("Residential added successfully: ${residential.id}")
                }
            } catch (e: SQLException) {
                logger.error("Error adding residential: ${e.message}", e)
                if (e.sqlState == "23505") {
                    throw RepositoryException.AlreadyExistsException(
                        "Residential with id=${residential.id} already exists")
                }
                throw RepositoryException.NoDataAccessException(
                    "Database error while adding residential: ${e.message}")
            }
        }
    }

    override suspend fun deleteResidential(residential: Residential) {
        logger.info("Deleting residential: ${residential.id}")
        dbQuery { connection ->
            try {
                val query = "DELETE FROM residentials WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, residential.id)
                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected == 0) {
                        logger.warn("Residential not found for deletion: ${residential.id}")
                        throw RepositoryException.NotFoundException("Residential with id=${residential.id} not found")
                    }
                    logger.debug("Residential deleted: ${residential.id}")
                }
            } catch (e: SQLException) {
                logger.error("Error deleting residential: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while deleting residential: ${e.message}")
            }
        }
    }

    override suspend fun updateResidential(residential: Residential) {
        logger.info("Updating residential: ${residential.id}")
        dbQuery { connection ->
            try {
                val query = "UPDATE residentials SET name = ?, address = ?, parking_type = ?, slots_num = ? WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, residential.name)
                    statement.setString(2, residential.address)
                    statement.setString(3, residential.type.name)
                    statement.setInt(4, residential.slotsNumber)
                    statement.setObject(5, UUID.fromString(residential.id))
                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected == 0) {
                        logger.warn("Residential not found for update: ${residential.id}")
                        throw RepositoryException.NotFoundException("Residential with id=${residential.id} not found")
                    }
                    logger.debug("Residential updated: ${residential.id}")
                }
            } catch (e: SQLException) {
                logger.error("Error updating residential: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while updating residential: ${e.message}")
            }
        }
    }

    override suspend fun getResidentialIdByAddress(residentialAddress: String): String {
        logger.info("Retrieving residential ID by address: $residentialAddress")
        return dbQuery { connection ->
            try {
                val query = "SELECT id FROM residentials WHERE address = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, residentialAddress)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val id = resultSet.getString("id")
                        logger.debug("Residential ID found: $id")
                        id
                    } else {
                        logger.warn("No residential found for address=$residentialAddress")
                        throw RepositoryException.NotFoundException(
                            "Residential with address=$residentialAddress not found")
                    }
                }
            } catch (e: SQLException) {
                logger.error("Error retrieving residential by address: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while retrieving residential ID: ${e.message}")
            }
        }
    }

    override suspend fun getResidentialById(residentialId: String): Residential {
        logger.info("Retrieving residential ID: $residentialId")
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM residentials WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(residentialId))
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val result = resultSet.toResidential()
                        logger.debug("Residential found: ${result.address}")
                        result
                    } else {
                        logger.warn("No residential found for id=$residentialId")
                        throw RepositoryException.NotFoundException(
                            "Residential with address=$residentialId not found")
                    }
                }
            } catch (e: SQLException) {
                logger.error("Error retrieving residential by address: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while retrieving residential ID: ${e.message}")
            }
        }
    }

    private fun ResultSet.toResidential(): Residential {
        return Residential(
            id = getString("id"),
            name = getString("name"),
            address = getString("address"),
            type = ParkingType.valueOf(getString("parking_type")),
            slotsNumber = getInt("slots_num")
        )
    }
}
