import models.Checkpoint
import models.CheckpointResponse
import models.Reservation
import models.ReservationResponse
import models.ReservationStatus
import models.Residential
import models.Slot
import models.SlotResponse
import models.SlotStatus
import models.Transport
import models.TransportResponse
import models.User
import models.UserTransport

// -------- version 1 ---------
interface UserRepository {
    suspend fun addUser(user: User)
    suspend fun deleteUser(user: User)
    suspend fun getUserByFIO(fio: String): User
    suspend fun getUserByEmailPassword(email: String, password: String): User
}

interface UserTransportRepository {
    suspend fun addUserTransport(userTransport: UserTransport)
    suspend fun deleteUserTransport(userTransport: UserTransport)
    suspend fun getUserTransportByUserId(userId: String, transportId: String): UserTransport?
}

interface TransportRepository {
    suspend fun addTransport(transport: Transport)
    suspend fun deleteTransport(transport: Transport)
    suspend fun getTransportByNumberAndModel(number: String, model: String): Transport?
    suspend fun getTransportById(id: String): Transport
}

interface ReservationRepository {
    suspend fun addReservation(reservation: Reservation)
    suspend fun deleteReservation(reservation: Reservation)
    suspend fun updateReservation(reservationId: String, status: ReservationStatus)
    suspend fun getAll(): List<Reservation>
    suspend fun getReservationByTransport(transportId: String): Reservation
    suspend fun getReservationBySlot(slotId: String): Reservation
}

interface CheckpointRepository {
    suspend fun addCheckpoint(checkpoint: Checkpoint)
    suspend fun deleteCheckpoint(checkpoint: Checkpoint)
    suspend fun updateCheckpoint(checkpoint: Checkpoint)
    suspend fun getCheckpointByResidentialIdAndNumber(residentialId: String, number: String): Checkpoint
}

interface SlotRepository {
    suspend fun addSlot(slot: Slot)
    suspend fun deleteSlot(slot: Slot)
    suspend fun updateSlotStatus(slotId: String, status: SlotStatus)
    suspend fun getSlotByResidentialAddressAndNumber(residentialAddress: String, number: Int): Slot
}

interface ResidentialRepository {
    suspend fun addResidential(residential: Residential)
    suspend fun deleteResidential(residential: Residential)
    suspend fun updateResidential(residential: Residential)
    suspend fun getResidentialIdByAddress(residentialAddress: String): String
    suspend fun getResidentialById(residentialId: String): Residential
}

// -------- version 2 ---------

interface SlotRepositoryV2 {
    suspend fun addSlot(slot: Slot)
    suspend fun updateSlotStatus(slotId: String, status: SlotStatus)
    suspend fun getSlots(resId: String, pageNum: Int = 1, count: Int = 5): SlotResponse
    suspend fun getSlotById(id: String): Slot
}

interface TransportRepositoryV2 {
    suspend fun addTransport(transport: Transport)
    suspend fun deleteTransport(id: String)
    suspend fun getTransportByModelAndNumber(model: String, number: String): Transport?
    suspend fun getAll(userId: String, pageNum: Int = 1, count: Int = 5): TransportResponse
    suspend fun getTransportById(id: String): Transport
}

interface CheckpointRepositoryV2 {
    suspend fun getById(id: String): Checkpoint
    suspend fun getAll(residentialId: String, pageNum: Int = 1, count: Int = 5): CheckpointResponse
}

interface ReservationRepositoryV2 {
    suspend fun addReservation(reservation: Reservation)
    suspend fun deleteReservation(id: String)
    suspend fun changeStatus(id: String, status: ReservationStatus)
    suspend fun getAll(userId: String, pageNum: Int = 1, count: Int = 5): ReservationResponse
    suspend fun getByIds(slotId: String, transportId: String): Reservation
}