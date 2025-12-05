import kotlinx.serialization.Serializable
import models.Pagination
import java.util.*

@Serializable
enum class UserRoleAPI {
    RESIDENT,
    GUEST,
    ADMIN
}

@Serializable
data class UserAPI(
    val id: String = UUID.randomUUID().toString(),
    val fio: String,
    val email: String,
    val password: String,
    val residentialId: String,
    val role: UserRoleAPI
)

@Serializable
data class UserTransportAPI(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val transportId: String
)

@Serializable
data class TransportAPI(
    val id: String = UUID.randomUUID().toString(),
    val number: String,
    val model: String,
    val color: String,
    val insurance: Boolean
)

enum class ReservationStatusAPI {
    ACTIVE,
    FROZEN
}

@Serializable
data class ReservationAPI(
    val id: String = UUID.randomUUID().toString(),
    val slotId: String,
    val transportId: String,
    val status: ReservationStatusAPI
)

enum class CheckpointStatusAPI {
    OK,
    OUT_OF_ORDER
}

@Serializable
data class CheckpointAPI(
    val id: String = UUID.randomUUID().toString(),
    val guardFio: String,
    val guardPhone: String,
    val number: String,
    val status: CheckpointStatusAPI
)

enum class SlotStatusAPI {
    FREE,
    BUSY
}

@Serializable
data class Slot(
    val id: String = UUID.randomUUID().toString(),
    val residential: String,
    val number: Int,
    val status: SlotStatusAPI
)

enum class ParkingTypeAPI {
    GROUND,
    UNDERGROUND,
    OVERGROUND
}

@Serializable
data class Residential(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val type: ParkingTypeAPI,
    val slotsNumber: Int
)


// Version 2
@Serializable
data class PaginationAPI(
    val totalPages: Int,
    val curPage: Int
)

fun paginationConvert(pagination: Pagination): PaginationAPI {
    return PaginationAPI(pagination.totalPages, pagination.curPage)
}

@Serializable
data class SlotResponseAPI(
    val slots: List<SlotAPI>,
    val pagination: PaginationAPI
)

@Serializable
data class SlotAPI(
    val id: String = UUID.randomUUID().toString(),
    val number: Int,
    val status: SlotStatusAPI
)

@Serializable
data class TransportResponseAPI(
    val transport: List<TransportAPI>,
    val pagination: PaginationAPI
)

@Serializable
data class CheckpointResponseAPI(
    val checkpoints: List<CheckpointAPI>,
    val pagination: PaginationAPI
)

@Serializable
data class ReservationResponseAPI(
    val reservations: List<ReservationInfoAPI>,
    val pagination: PaginationAPI
)

@Serializable
data class ReservationInfoAPI(
    val id: String = UUID.randomUUID().toString(),
    val number: Int,
    val transportNumber: String,
    val transportModel: String
)