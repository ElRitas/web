package models

import kotlinx.serialization.Serializable
import java.util.*

enum class UserRole {
    RESIDENT,
    GUEST,
    ADMIN
}

@Serializable
data class User(
    val id: String = UUID.randomUUID().toString(),
    val fio: String,
    val email: String,
    val password: String,
    val residentialId: String,
    val role: UserRole
)


data class UserTransport(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val transportId: String
)


data class Transport(
    val id: String = UUID.randomUUID().toString(),
    val number: String,
    val model: String,
    val color: String,
    val insurance: Boolean
)

enum class ReservationStatus {
    ACTIVE,
    FROZEN
}


data class Reservation(
    val id: String = UUID.randomUUID().toString(),
    val slotId: String,
    val transportId: String,
    val status: ReservationStatus
)

enum class CheckpointStatus {
    OK,
    OUT_OF_ORDER
}


data class Checkpoint(
    val id: String = UUID.randomUUID().toString(),
    val guardFio: String,
    val guardPhone: String,
    val residentialId: String,
    val number: String,
    val status: CheckpointStatus
)

enum class SlotStatus {
    FREE,
    BUSY
}


data class Slot(
    val id: String = UUID.randomUUID().toString(),
    val residential: String,
    val number: Int,
    val status: SlotStatus
)

enum class ParkingType {
    GROUND,
    UNDERGROUND,
    OVERGROUND
}


data class Residential(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val type: ParkingType,
    val slotsNumber: Int
)


// Version 2

data class Pagination(
    val totalPages: Int,
    val curPage: Int
)


data class SlotResponse(
    val slots: List<Slot>,
    val pagination: Pagination
)


data class TransportResponse(
    val transport: List<Transport>,
    val pagination: Pagination
)


data class CheckpointResponse(
    val checkpoints: List<Checkpoint>,
    val pagination: Pagination
)


data class ReservationResponse(
    val reservations: List<Reservation>,
    val pagination: Pagination
)
