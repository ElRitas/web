package repositories.v2

import DatabaseFactory.dbQuery
import ReservationDA
import ReservationRepositoryV2
import errors.RepositoryException
import models.Pagination
import models.Reservation
import models.ReservationResponse
import models.ReservationStatus
import java.sql.ResultSet
import java.sql.SQLException
import java.util.UUID
import kotlin.math.min
import kotlin.use

class DBReservationRepositoryV2 : ReservationRepositoryV2 {
    override suspend fun addReservation(reservation: Reservation) {
        val reservationDA = convert(reservation)
        dbQuery { connection ->
            try {
                val query = "INSERT INTO reservations (id, slot_id, transport_id, status) VALUES (?, ?, ?, ?)"
                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setObject(1, UUID.fromString(reservationDA.id))
                    preparedStatement.setObject(2, UUID.fromString(reservationDA.slotId))
                    preparedStatement.setObject(3, UUID.fromString(reservationDA.transportId))
                    preparedStatement.setString(4, reservationDA.status.toString())
                    preparedStatement.executeUpdate()
                }
            } catch (e: SQLException) {
                if (e.sqlState == "23505") {
                    throw RepositoryException.AlreadyExistsException(
                        "Reservation with id=${reservation.id} already exists")
                }
                throw RepositoryException.NoDataAccessException(
                    "Database error while adding reservation: ${e.message}")
            }
        }
    }

    override suspend fun deleteReservation(id: String) {
        dbQuery { connection ->
            try {
                val query = "DELETE FROM reservations WHERE id = ?"
                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setObject(1, UUID.fromString(id))
                    val rowsAffected = preparedStatement.executeUpdate()
                    if (rowsAffected == 0) {
                        throw RepositoryException.NotFoundException("Reservation with id=${id} not found")
                    }
                }
            } catch (e: SQLException) {
                throw RepositoryException.NoDataAccessException(
                    "Database error while deleting reservation: ${e.message}")
            }
        }
    }

    override suspend fun changeStatus(id: String, status: ReservationStatus) {
        dbQuery { connection ->
            try {
                val query = "UPDATE reservations SET status = ? WHERE id = ?"
                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setString(1, status.toString())
                    preparedStatement.setObject(2, UUID.fromString(id))
                    val rowsAffected = preparedStatement.executeUpdate()
                    if (rowsAffected == 0) {
                        throw RepositoryException.NotFoundException(
                            "Reservation with id=$id not found"
                        )
                    }
                }
            } catch (e: SQLException) {
                throw RepositoryException.NoDataAccessException(
                    "Database error while updating reservation: ${e.message}")
            }
        }
    }

    override suspend fun getAll(
        userId: String,
        pageNum: Int,
        count: Int
    ): ReservationResponse {
        val reservations =  dbQuery { connection ->
            try {
                val query = """
                    SELECT r.* FROM reservations r
                    JOIN user_transport ut on ut.transport_id = r.transport_id
                    WHERE ut.user_id = ?
                    LIMIT ? OFFSET ?
                """.trimIndent()
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(userId))
                    statement.setInt(2, count)
                    statement.setInt(3, ((pageNum - 1) * count))
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val list = resultSet.toReservationList()
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
                    SELECT COUNT(r.*) FROM reservations r
                    JOIN user_transport ut on ut.transport_id = r.transport_id
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
        return ReservationResponse(reservations, Pagination(total, pageNum))
    }

    override suspend fun getByIds(slotId: String, transportId: String): Reservation {
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM reservations WHERE slot_id = ? AND transport_id = ?"
                connection.prepareStatement(query).use { statement ->
                    statement.setObject(1, UUID.fromString(slotId))
                    statement.setObject(2, UUID.fromString(transportId))
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val transport = resultSet.toReservation()
                        transport
                    } else {
                        throw RepositoryException.NotFoundException(
                            "not found")
                    }
                }
            } catch (e: SQLException) {
                throw RepositoryException.NoDataAccessException("Database error while retrieving transport: ${e.message}")
            }
        }
    }

    private fun ResultSet.toReservation(): Reservation {
        return Reservation(
            id = getString("id"),
            slotId = getString("slot_id"),
            transportId = getString("transport_id"),
            status = ReservationStatus.valueOf(getString("status"))
        )
    }

    private fun ResultSet.toReservationList(): List<Reservation> {
        val reservationList = mutableListOf<Reservation>()
        do {
            reservationList.add(toReservation())
        } while (next())
        return reservationList
    }

    private fun convert(reservation: Reservation): ReservationDA {
        return ReservationDA(
            reservation.id,
            reservation.slotId,
            reservation.transportId,
            ReservationStatusDA.valueOf(reservation.status.toString())
        )
    }
}