package servicesV1

import IReservationService
import ReservationRepository
import ResidentialRepository
import SlotRepository
import TransportRepository
import errors.RepositoryException
import models.Reservation
import models.ReservationStatus
import models.SlotStatus
import org.slf4j.LoggerFactory
import org.slf4j.Logger

class ReservationService(
    private val reservationRepo: ReservationRepository,
    private val slotRepo: SlotRepository,
    private val transportRepo: TransportRepository,
    private val resRepo: ResidentialRepository
) : IReservationService {

    private val logger: Logger = LoggerFactory.getLogger(ReservationService::class.java)

    override suspend fun createReservation(
        residentialId: String, number: Int, transportNum: String, transportModel: String) {
        logger.info("Attempting to create reservation")

        try {
            val res = resRepo.getResidentialById(residentialId)
            val slot = slotRepo.getSlotByResidentialAddressAndNumber(res.address, number)
            val transport = transportRepo.getTransportByNumberAndModel(transportNum, transportModel)
            if (transport != null) {
                if (slot.status == SlotStatus.BUSY) {
                    logger.error("Slot ${slot.id} is already busy")
                    throw RepositoryException.AlreadyExistsException("Slot is not free")
                }
                reservationRepo.addReservation(
                    Reservation(
                        transportId = transport.id,
                        slotId = slot.id,
                        status = ReservationStatus.ACTIVE
                    )
                )
                slotRepo.updateSlotStatus(slot.id, SlotStatus.BUSY)
                logger.info("Reservation created successfully for transport" +
                        "${transport.id} on slot ${slot.id}")
            } else {
                throw RepositoryException.NotFoundException("Transport not found")
            }
        } catch (e: RepositoryException) {
            logger.error("Error creating reservation", e)
            throw e
        }
    }

    override suspend fun cancelReservation(transportNumber: String, transportModel: String) {
        logger.info("Attempting to cancel reservation for transport: $transportNumber, $transportModel")

        try {
            val transportId = try {
                transportRepo.getTransportByNumberAndModel(transportNumber, transportModel)?.id
                    ?: throw RepositoryException.NotFoundException("Transport not found")
            } catch (e: RepositoryException.NoDataAccessException) { throw e }

            val reservation = try {
                reservationRepo.getReservationByTransport(transportId)
            } catch (e: RepositoryException) { throw e }

            slotRepo.updateSlotStatus(reservation.slotId, SlotStatus.FREE)
            reservationRepo.deleteReservation(reservation)
            logger.info("Reservation canceled successfully for transport $transportNumber, $transportModel")
        } catch (e: RepositoryException) {
            logger.error("Error canceling reservation for transport $transportNumber, $transportModel", e)
            throw e
        }
    }
}
