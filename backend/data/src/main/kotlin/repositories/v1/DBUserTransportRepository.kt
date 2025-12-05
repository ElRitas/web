package repositories.v1

import UserTransportRepository
import errors.RepositoryException
import models.UserTransport
import DatabaseFactory.dbQuery
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class DBUserTransportRepository : UserTransportRepository {

    private val logger = LoggerFactory.getLogger(DBUserTransportRepository::class.java)

    override suspend fun addUserTransport(userTransport: UserTransport) {
        logger.info("Adding user transport: $userTransport")
        dbQuery { connection ->
            try {
                val query = "INSERT INTO User_Transport(id, user_id, transport_id) VALUES(?, ?, ?)"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(userTransport.id))
                    statement.setObject(2, UUID.fromString(userTransport.userId))
                    statement.setObject(3, UUID.fromString(userTransport.transportId))
                    statement.executeUpdate()
                    logger.debug("User transport added successfully with id=${userTransport.id}")
                }
            } catch (e: SQLException) {
                logger.error("Failed to add user transport: ${e.message}", e)
                if (e.sqlState == "23505") {
                    throw RepositoryException.AlreadyExistsException(
                        "UserTransport with id=${userTransport.id} already exists")
                }
                throw RepositoryException.NoDataAccessException(
                    "Database error while adding user transport: ${e.message}")
            }
        }
    }

    override suspend fun deleteUserTransport(userTransport: UserTransport) {
        logger.info("Deleting user transport with id=${userTransport.id}")
        dbQuery { connection ->
            try {
                val query = "DELETE FROM User_Transport WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(userTransport.id))
                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected == 0) {
                        logger.warn("User transport not found for deletion: ${userTransport.id}")
                        throw RepositoryException.NotFoundException(
                            "UserTransport with id=${userTransport.id} not found")
                    }
                    logger.debug("User transport deleted successfully: ${userTransport.id}")
                }
            } catch (e: SQLException) {
                logger.error("Error deleting user transport: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while deleting user transport: ${e.message}")
            }
        }
    }

    override suspend fun getUserTransportByUserId(userId: String, transportId: String): UserTransport? {
        logger.info("Retrieving user transport by userId=$userId")
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM user_transport WHERE user_id = ? AND transport_id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(userId))
                    statement.setObject(2, UUID.fromString(transportId))
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val userTransport = resultSet.toUserTransport()
                        logger.debug("User transport found: $userTransport")
                        userTransport
                    } else {
                        logger.warn("No user transport found for userId=$userId")
                        null
                    }
                }
            } catch (e: SQLException) {
                logger.error("Error retrieving user transport: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while getting user transport: ${e.message}")
            }
        }
    }

    private fun ResultSet.toUserTransport(): UserTransport {
        return UserTransport(
            id = getString("id"),
            userId = getString("user_id"),
            transportId = getString("transport_id")
        )
    }
}
