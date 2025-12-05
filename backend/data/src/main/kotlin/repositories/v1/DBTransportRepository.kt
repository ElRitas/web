package repositories.v1

import TransportRepository
import errors.RepositoryException
import models.Transport
import DatabaseFactory.dbQuery
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class DBTransportRepository : TransportRepository {

    private val logger = LoggerFactory.getLogger(DBTransportRepository::class.java)

    override suspend fun addTransport(transport: Transport) {
        logger.info("Adding new transport: $transport")
        dbQuery { connection ->
            try {
                val query = "INSERT INTO TRANSPORT (id, number, model, color, insurance) VALUES (?, ?, ?, ?, ?)"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(transport.id))
                    statement.setString(2, transport.number)
                    statement.setString(3, transport.model)
                    statement.setString(4, transport.color)
                    statement.setBoolean(5, transport.insurance)
                    statement.executeUpdate()
                    logger.debug("Transport added successfully with id=${transport.id}")
                }
            } catch (e: SQLException) {
                logger.error("Failed to add transport: ${e.message}", e)
                if (e.sqlState == "23505") {
                    throw RepositoryException.AlreadyExistsException("Transport with id=${transport.id} already exists")
                }
                throw RepositoryException.NoDataAccessException("Database error while adding transport: ${e.message}")
            }
        }
    }

    override suspend fun deleteTransport(transport: Transport) {
        logger.info("Deleting transport with id=${transport.id}")
        dbQuery { connection ->
            try {
                val query = "DELETE FROM TRANSPORT WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(transport.id))
                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected == 0) {
                        logger.warn("Transport not found for deletion: ${transport.id}")
                        throw RepositoryException.NotFoundException("Transport with id=${transport.id} not found")
                    }
                    logger.debug("Transport deleted successfully: ${transport.id}")
                }
            } catch (e: SQLException) {
                logger.error("Error deleting transport: ${e.message}", e)
                throw RepositoryException.NoDataAccessException("Database error while deleting transport: ${e.message}")
            }
        }
    }

    override suspend fun getTransportByNumberAndModel(number: String, model: String): Transport? {
        logger.info("Retrieving transport by number=$number and model=$model")
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM TRANSPORT WHERE number = ? AND model = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, number)
                    statement.setString(2, model)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val transport = resultSet.toTransport()
                        logger.debug("Transport found: $transport")
                        transport
                    } else {
                        logger.warn("No transport found for number=$number and model=$model")
                        throw RepositoryException.NotFoundException(
                            "Transport with number=$number and model=$model not found")
                    }
                }
            } catch (e: SQLException) {
                logger.error("Error retrieving transport: ${e.message}", e)
                throw RepositoryException.NoDataAccessException("Database error while retrieving transport: ${e.message}")
            }
        }
    }

    override suspend fun getTransportById(id: String): Transport {
        logger.info("Retrieving transport by id=$id")
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM TRANSPORT WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(id))
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val transport = resultSet.toTransport()
                        logger.debug("Transport found: $transport")
                        transport
                    } else {
                        logger.warn("No transport found for id=$id")
                        throw RepositoryException.NotFoundException(
                            "Transport with id=$id")
                    }
                }
            } catch (e: SQLException) {
                logger.error("Error retrieving transport: ${e.message}", e)
                throw RepositoryException.NoDataAccessException("Database error while retrieving transport: ${e.message}")
            }
        }
    }

    private fun ResultSet.toTransport(): Transport {
        return Transport(
            id = getString("id"),
            number = getString("number"),
            model = getString("model"),
            color = getString("color"),
            insurance = getBoolean("insurance")
        )
    }
}
