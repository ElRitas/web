package repositories.v1

import ReservationRepository
import errors.RepositoryException
import models.Reservation
import models.ReservationStatus
import DatabaseFactory.dbQuery
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class DBReservationRepository : ReservationRepository {

    private val logger = LoggerFactory.getLogger(DBReservationRepository::class.java)

    override suspend fun addReservation(reservation: Reservation) {
        logger.info("Adding reservation: $reservation")
        dbQuery { connection ->
            try {
                val query = "INSERT INTO reservations (id, slot_id, transport_id, status) VALUES (?, ?, ?, ?)"
                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setObject(1, UUID.fromString(reservation.id))
                    preparedStatement.setObject(2, UUID.fromString(reservation.slotId))
                    preparedStatement.setObject(3, UUID.fromString(reservation.transportId))
                    preparedStatement.setString(4, reservation.status.toString())
                    preparedStatement.executeUpdate()
                    logger.debug("Reservation added successfully: ${reservation.id}")
                }
            } catch (e: SQLException) {
                logger.error("Failed to add reservation: ${e.message}", e)
                if (e.sqlState == "23505") {
                    throw RepositoryException.AlreadyExistsException(
                        "Reservation with id=${reservation.id} already exists")
                }
                throw RepositoryException.NoDataAccessException(
                    "Database error while adding reservation: ${e.message}")
            }
        }
    }

    override suspend fun deleteReservation(reservation: Reservation) {
        logger.info("Deleting reservation: ${reservation.id}")
        dbQuery { connection ->
            try {
                val query = "DELETE FROM reservations WHERE id = ?"
                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setObject(1, UUID.fromString(reservation.id))
                    val rowsAffected = preparedStatement.executeUpdate()
                    if (rowsAffected == 0) {
                        logger.warn("Reservation not found for deletion: ${reservation.id}")
                        throw RepositoryException.NotFoundException("Reservation with id=${reservation.id} not found")
                    }
                    logger.debug("Reservation deleted: ${reservation.id}")
                }
            } catch (e: SQLException) {
                logger.error("Failed to delete reservation: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while deleting reservation: ${e.message}")
            }
        }
    }

    override suspend fun updateReservation(reservationId: String, status: ReservationStatus) {
        logger.info("Updating reservation status: id=$reservationId, status=$status")
        dbQuery { connection ->
            try {
                val query = "UPDATE reservations SET status = ? WHERE id = ?"
                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setString(1, status.toString())
                    preparedStatement.setObject(2, UUID.fromString(reservationId))
                    val rowsAffected = preparedStatement.executeUpdate()
                    if (rowsAffected == 0) {
                        logger.warn("Reservation not found for update: $reservationId")
                        throw RepositoryException.NotFoundException("Reservation with id=$reservationId not found")
                    }
                    logger.debug("Reservation updated: $reservationId to status=$status")
                }
            } catch (e: SQLException) {
                logger.error("Failed to update reservation: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while updating reservation: ${e.message}")
            }
        }
    }

    override suspend fun getReservationByTransport(transportId: String): Reservation {
        logger.info("Retrieving reservation for transportId=$transportId")
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM reservations WHERE transport_id = ?"
                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setObject(1, UUID.fromString(transportId))
                    val resultSet = preparedStatement.executeQuery()
                    if (resultSet.next()) {
                        val reservation = resultSet.toReservation()
                        logger.debug("Reservation retrieved: $reservation")
                        reservation
                    } else {
                        logger.warn("No reservation found for transportId=$transportId")
                        throw RepositoryException.NotFoundException(
                            "No reservation found for transportId=$transportId")
                    }
                }
            } catch (e: SQLException) {
                logger.error("Failed to retrieve reservation: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while retrieving reservation: ${e.message}")
            }
        }
    }

    override suspend fun getReservationBySlot(slotId: String): Reservation {
        logger.info("Retrieving reservation for slotId=$slotId")
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM reservations WHERE slot_id = ?"
                connection.prepareStatement(query).use { preparedStatement ->
                    preparedStatement.setObject(1, UUID.fromString(slotId))
                    val resultSet = preparedStatement.executeQuery()
                    if (resultSet.next()) {
                        val reservation = resultSet.toReservation()
                        logger.debug("Reservation retrieved: $reservation")
                        reservation
                    } else {
                        logger.warn("No reservation found for slotId=$slotId")
                        throw RepositoryException.NotFoundException("No reservation found for slotId=$slotId")
                    }
                }
            } catch (e: SQLException) {
                logger.error("Failed to retrieve reservation: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while retrieving reservation: ${e.message}")
            }
        }
    }

    override suspend fun getAll(): List<Reservation> {
        logger.info("Retrieving all reservations")
        return dbQuery { connection ->
            try {
                val query = "SELECT * FROM reservations"
                connection.prepareStatement(query).use { preparedStatement ->
                    val result = preparedStatement.executeQuery().toReservationList()
                    logger.debug("Total reservations retrieved: ${result.size}")
                    result
                }
            } catch (e: SQLException) {
                logger.error("Failed to retrieve all reservations: ${e.message}", e)
                throw RepositoryException.NoDataAccessException(
                    "Database error while retrieving all reservations: ${e.message}")
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
        while (next()) {
            reservationList.add(toReservation())
        }
        return reservationList
    }
}
