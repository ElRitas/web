package servicesV2

import IReservationServiceV2
import ReservationRepositoryV2
import SlotRepositoryV2
import TransportRepositoryV2
import errors.RepositoryException
import models.Reservation
import models.ReservationResponse
import models.ReservationStatus
import models.SlotStatus

class ReservationServiceV2(
    private val reservationRepo: ReservationRepositoryV2,
    private val transportRepo: TransportRepositoryV2,
    private val slotRepo: SlotRepositoryV2
) : IReservationServiceV2 {
    override suspend fun add(slotId: String, transportId: String) {
        try {
            try {
                reservationRepo.getByIds(slotId, transportId)
            } catch (_: RepositoryException.NotFoundException) {
                slotRepo.updateSlotStatus(slotId, SlotStatus.BUSY)
                reservationRepo.addReservation(
                    Reservation(
                        slotId = slotId,
                        transportId = transportId,
                        status = ReservationStatus.ACTIVE
                    )
                )
            }
        } catch (e: RepositoryException) {
            throw e
        }
    }

    override suspend fun delete(id: String) {
        try {
            reservationRepo.deleteReservation(id)
        } catch (e: RepositoryException) {
            throw e
        }
    }

    override suspend fun changeStatus(id: String, status: ReservationStatus) {
        try {
            reservationRepo.changeStatus(id, status)
        } catch (e: RepositoryException) {
            throw e
        }
    }

    override suspend fun getAll(
        userId: String,
        pageNum: Int,
        count: Int
    ): ReservationResponse {
        try {
            return reservationRepo.getAll(userId, pageNum, count)
        } catch (e: RepositoryException) {
            throw e
        }
    }
}