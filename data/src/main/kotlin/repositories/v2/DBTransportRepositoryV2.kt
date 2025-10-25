package repositories.v2


import errors.RepositoryException
import models.Transport
import DatabaseFactory.dbQuery
import SlotDA
import TransportDA
import TransportRepositoryV2
import models.Pagination
import models.Slot
import models.TransportResponse
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import kotlin.math.min

class DBTransportRepositoryV2 : TransportRepositoryV2 {
    override suspend fun addTransport(transport: Transport) {
        val transportDA = convert(transport)
        dbQuery { connection ->
            try {
                val query = "INSERT INTO TRANSPORT (id, number, model, color, insurance) VALUES (?, ?, ?, ?, ?)"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(transportDA.id))
                    statement.setString(2, transportDA.number)
                    statement.setString(3, transportDA.model)
                    statement.setString(4, transportDA.color)
                    statement.setBoolean(5, transportDA.insurance)
                    statement.executeUpdate()
                }
            } catch (e: SQLException) {
                if (e.sqlState == "23505") {
                    throw RepositoryException.AlreadyExistsException("Transport with id=${transport.id} already exists")
                }
                throw RepositoryException.NoDataAccessException("Database error while adding transport: ${e.message}")
            }
        }
    }

    override suspend fun deleteTransport(id: String) {
        dbQuery { connection ->
            try {
                val query = "DELETE FROM TRANSPORT WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(id))
                    val rowsAffected = statement.executeUpdate()
                    if (rowsAffected == 0) {
                        throw RepositoryException.NotFoundException("Transport with id=${id} not found")
                    }
                }
            } catch (e: SQLException) {
                throw RepositoryException.NoDataAccessException("Database error while deleting transport: ${e.message}")
            }
        }
    }

    override suspend fun getTransportByModelAndNumber(model: String, number: String): Transport? {
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM TRANSPORT WHERE number = ? AND model = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, number)
                    statement.setString(2, model)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val transport = resultSet.toTransport()
                        transport
                    } else {
                        throw RepositoryException.NotFoundException(
                            "Transport with number=$number and model=$model not found")
                    }
                }
            } catch (e: SQLException) {
                throw RepositoryException.NoDataAccessException("Database error while retrieving transport: ${e.message}")
            }
        }
    }

    override suspend fun getAll(userId: String, pageNum: Int, count: Int): TransportResponse {
        val slots = dbQuery { connection ->
            try {
                val query = """
                    SELECT t.* FROM transport t
                    JOIN user_transport ut ON ut.transport_id = t.id
                    WHERE ut.user_id = ?
                    LIMIT ? OFFSET ?
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(userId))
                    statement.setInt(2, count)
                    statement.setInt(3, ((pageNum - 1) * count))
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val transport = resultSet.toTransportList()
                        transport
                    } else {
                        throw RepositoryException.NotFoundException(
                            "Transport not found, $statement")
                    }
                }
            } catch (e: SQLException) {
                throw RepositoryException.NoDataAccessException("Database error while retrieving slot: ${e.message}")
            }
        }
        val total = dbQuery { connection ->
            try {
                val query = """
                    SELECT COUNT(t.*) FROM transport t
                    JOIN user_transport ut ON ut.transport_id = t.id
                    WHERE ut.user_id = ?
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(userId))
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
        return TransportResponse(slots, Pagination(total, pageNum))
    }

    override suspend fun getTransportById(id: String): Transport {
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM TRANSPORT WHERE id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(id))
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val transport = resultSet.toTransport()
                        transport
                    } else {
                        throw RepositoryException.NotFoundException(
                            "Transport with id=$id not found")
                    }
                }
            } catch (e: SQLException) {
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

    private fun ResultSet.toTransportList(): List<Transport> {
        val list = mutableListOf<Transport>()
        do {
            list.add(toTransport())
        } while (next())
        return list
    }

    private fun convert(transport: Transport): TransportDA {
        return TransportDA(
            transport.id,
            transport.number,
            transport.model,
            transport.color,
            transport.insurance
        )
    }
}