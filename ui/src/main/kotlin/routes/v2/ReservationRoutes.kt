package routes.v2

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import IReservationServiceV2
import ISlotService
import ITransportServiceV2
import ReservationInfoAPI
import ReservationResponseAPI
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import models.ReservationStatus
import paginationConvert

fun Route.reservationRoutesV2(
    reservationService: IReservationServiceV2,
    transportService: ITransportServiceV2,
    slotService: ISlotService
) {
    route("/reservations") {
        authenticate("auth-jwt") {
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.getClaim("role", String::class)
                    if (role == UserRoleAPI.GUEST.toString())
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "Access denied: GUEST role not allowed")
                        )
                    val requestBody = call.receive<Map<String, String>>()
                    val slotId = requestBody["slotId"] ?: return@post call.respondText(
                        "Missing id", status = HttpStatusCode.BadRequest
                    )
                    val transportId = requestBody["transportId"] ?: return@post call.respondText(
                        "Missing number", status = HttpStatusCode.BadRequest
                    )
                    reservationService.add(slotId, transportId)
                    return@post call.respond(message = "Reservation created")
                } catch (e: Exception) {
                    return@post call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "$e")
                    )
                }
            }

            get {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.getClaim("role", String::class)
                    val id = principal?.getClaim("id", String::class)
                        ?: ""
                    if (role != UserRoleAPI.RESIDENT.toString())
                        return@get call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "Access denied: GUEST role not allowed")
                        )
                    val page = call.parameters["pagesNum"]?.toInt() ?: 1
                    val count = call.parameters["elemCount"]?.toInt() ?: 5
                    val res = reservationService.getAll(id, page, count)
                    val resList = mutableListOf<ReservationInfoAPI>()
                    for (reservation in res.reservations) {
                        val transport = transportService.getById(reservation.transportId)
                        resList.add(ReservationInfoAPI(
                            id = reservation.id,
                            number = slotService.getById(reservation.slotId).number,
                            transportNumber = transport.number,
                            transportModel = transport.model
                        ))
                    }
                    call.respond(ReservationResponseAPI(
                        resList,
                        paginationConvert(res.pagination)
                    ))
                } catch (e: Exception) {
                    return@get call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "${e.message}")
                    )
                }
            }
            route("/{id}") {
                delete {
                    try {
                        val id = call.parameters["id"] ?: ""
                        if (id.isEmpty())
                            return@delete call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Wrong id")
                            )
                        val principal = call.principal<JWTPrincipal>()
                        val role = principal?.getClaim("role", String::class)
                        if (role != UserRoleAPI.RESIDENT.toString())
                            return@delete call.respond(
                                HttpStatusCode.Forbidden,
                                mapOf("error" to "Access denied: GUEST role not allowed")
                            )
                        reservationService.delete(id)
                        return@delete call.respond("Reservation cancelled")
                    } catch (e: Exception) {
                        return@delete call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "$e")
                        )
                    }
                }
            }
            route("/{id}/status") {
                patch {
                    try {
                        val id = call.parameters["id"] ?: ""
                        if (id.isEmpty())
                            return@patch call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Wrong id")
                            )
                        val principal = call.principal<JWTPrincipal>()
                        val role = principal?.getClaim("role", String::class)
                        if (role != UserRoleAPI.RESIDENT.toString())
                            return@patch call.respond(
                                HttpStatusCode.Forbidden,
                                mapOf("error" to "Access denied: GUEST role not allowed")
                            )
                        val requestBody = call.receive<Map<String, String>>()
                        val status = requestBody["status"] ?: return@patch call.respondText(
                            "Missing status", status = HttpStatusCode.BadRequest
                        )
                        reservationService.changeStatus(id, ReservationStatus.valueOf(status))
                        return@patch call.respond("Reservation cancelled")
                    } catch (e: Exception) {
                        return@patch call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "$e")
                        )
                    }
                }
            }
        }
    }
}
