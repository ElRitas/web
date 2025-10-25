import models.CheckpointResponse
import models.User
import models.Reservation
import models.ReservationResponse
import models.ReservationStatus
import models.Slot
import models.SlotResponse
import models.Transport
import models.TransportResponse

// --------- version 1 ---------

interface IUserService {
    suspend fun addGuest(user: User)
    suspend fun authorize(email: String, password: String): User?
    suspend fun register(user: User, address: String): User
}

interface ITransportService {
    suspend fun addTransport(userId: String, transport: Transport)
    suspend fun getTransportByNumberAndModel(number: String, model: String): Transport?
}

interface IAdminService {
    suspend fun getAllReservations(): List<Reservation>
    suspend fun getReservationBySlot(residentialAddress: String, slotNumber: Int):
            Pair<Transport, ReservationStatus>
}

interface IReservationService {
    suspend fun createReservation(residentialId: String, number: Int, transportNum: String, transportModel: String)
    suspend fun cancelReservation(transportNumber: String, transportModel: String)
}

interface ICheckpointPassService {
    suspend fun passCheckpoint(residentialAddress: String, checkpointNumber: String, userFIO: String): Boolean
}

// --------- version 2 ---------

interface ISlotService {
    suspend fun addSlot(resId: String, number: Int): String?
    suspend fun getById(id: String): Slot
    suspend fun getSlots(resId: String, pageNum: Int = 1, count: Int = 5): SlotResponse
}

interface ITransportServiceV2 {
    suspend fun addTransport(userId: String, number: String,
                             model: String, color: String, insurance: Boolean)
    suspend fun deleteTransport(id: String, fio: String, userId: String)
    suspend fun getAll(userId: String, pageNum: Int = 1, count: Int = 5): TransportResponse
    suspend fun getById(id: String): Transport
}

interface ICheckpointServiceV2 {
    suspend fun pass(id: String): Boolean
    suspend fun getAll(residentialId: String, pageNum: Int = 1, count: Int = 5): CheckpointResponse
}

interface IReservationServiceV2 {
    suspend fun add(slotId: String, transportId: String)
    suspend fun delete(id: String)
    suspend fun changeStatus(id: String, status: ReservationStatus)
    suspend fun getAll(userId: String, pageNum: Int = 1, count: Int = 5): ReservationResponse
}