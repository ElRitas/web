package servicesV1

import IAdminService
import ReservationRepository
import models.Reservation
import errors.RepositoryException
import SlotRepository
import TransportRepository
import models.ReservationStatus
import models.Transport
import org.slf4j.LoggerFactory

class AdminService(
    private val reservationRepo: ReservationRepository,
    private val slotRepo: SlotRepository,
    private val transportRepo: TransportRepository
) : IAdminService {

    private val logger = LoggerFactory.getLogger(AdminService::class.java)

    override suspend fun getAllReservations(): List<Reservation> {
        logger.info("Getting all reservations")
        try {
            val reservations = reservationRepo.getAll()
            logger.debug("Successfully fetched all reservations: ${reservations.size} records.")
            return reservations
        } catch (e: RepositoryException) {
            logger.error("Error fetching all reservations", e)
            throw e
        }
    }

    override suspend fun getReservationBySlot(residentialAddress: String, slotNumber: Int): Pair<Transport, ReservationStatus> {
        logger.info("Getting reservation for residential address: $residentialAddress, slot number: $slotNumber")
        try {
            val slot = try {
                slotRepo.getSlotByResidentialAddressAndNumber(residentialAddress, slotNumber)
            } catch (e: RepositoryException) {
                logger.error("Error fetching slot for $residentialAddress, slot number: $slotNumber", e)
                throw e
            }
            val reservation = reservationRepo.getReservationBySlot(slot.id)
            logger.debug("Found reservation for slot: {}, reservation details: {}", slot.id, reservation)
            val transport = transportRepo.getTransportById(reservation.transportId)
            return Pair(transport, reservation.status)
        } catch (e: RepositoryException) {
            logger.error("Error fetching reservation by slot for $residentialAddress, slot number: $slotNumber", e)
            throw e
        }
    }
}
