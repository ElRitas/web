import models.Reservation
import java.util.*

enum class UserRoleDA {
    RESIDENT,
    GUEST,
    ADMIN
}

data class TransportDA(
    val id: String = UUID.randomUUID().toString(),
    val number: String,
    val model: String,
    val color: String,
    val insurance: Boolean
)

enum class ReservationStatusDA {
    ACTIVE,
    FROZEN
}


data class ReservationDA(
    val id: String = UUID.randomUUID().toString(),
    val slotId: String,
    val transportId: String,
    val status: ReservationStatusDA
)

enum class CheckpointStatusDA {
    OK,
    OUT_OF_ORDER
}


data class CheckpointDA(
    val id: String = UUID.randomUUID().toString(),
    val guardFio: String,
    val guardPhone: String,
    val residentialId: String,
    val number: String,
    val status: CheckpointStatusDA
)

enum class SlotStatusDA {
    FREE,
    BUSY
}


data class SlotDA(
    val id: String = UUID.randomUUID().toString(),
    val residential: String,
    val number: Int,
    val status: SlotStatusDA
)

enum class ParkingTypeDA {
    GROUND,
    UNDERGROUND,
    OVERGROUND
}


data class ResidentialDA(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val type: ParkingTypeDA,
    val slotsNumber: Int
)


// Version 2

data class PaginationDA(
    val totalPages: Int,
    val curPage: Int
)


data class SlotResponseDA(
    val slots: List<SlotDA>,
    val pagination: PaginationDA
)


data class TransportResponseDA(
    val transport: List<TransportDA>,
    val pagination: PaginationDA
)


data class CheckpointResponseDA(
    val checkpoints: List<CheckpointDA>,
    val pagination: PaginationDA
)


data class ReservationRepoResponseDA(
    val reservations: List<ReservationDA>,
    val pagination: PaginationDA
)
