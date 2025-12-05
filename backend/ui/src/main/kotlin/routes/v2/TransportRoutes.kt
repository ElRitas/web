package routes.v2

import io.ktor.server.routing.*
import io.ktor.http.*
import ITransportServiceV2
import SlotAPI
import SlotResponseAPI
import TransportAPI
import TransportResponseAPI
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import models.TransportResponse
import paginationConvert


fun Route.transportRoutesV2(transportService: ITransportServiceV2) {
    route("/transport") {
        authenticate("auth-jwt") {
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.getClaim("role", String::class)
                    val id = principal?.getClaim("id", String::class) ?: ""
                    if (role != UserRoleAPI.RESIDENT.toString())
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "Access denied: only RESIDENT role allowed")
                        )
                    val requestBody = call.receive<Map<String, String>>()
                    val number = requestBody["number"] ?: return@post call.respondText(
                        "Missing id", status = HttpStatusCode.BadRequest
                    )
                    val model = requestBody["model"] ?: return@post call.respondText(
                        "Missing id", status = HttpStatusCode.BadRequest
                    )
                    val color = requestBody["color"] ?: return@post call.respondText(
                        "Missing id", status = HttpStatusCode.BadRequest
                    )
                    val insurance = requestBody["insurance"] ?: return@post call.respondText(
                        "Missing id", status = HttpStatusCode.BadRequest
                    )
                    transportService.addTransport(id, number, model, color, insurance.toBoolean())
                    call.respond("Transport added")
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError, "Registration failed: ${e.message}"
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
                            mapOf("error" to "Access denied: only RESIDENT role allowed")
                        )
                    val page = call.parameters["pagesNum"]?.toInt() ?: 0
                    val count = call.parameters["elemCount"]?.toInt() ?: 0
                    val response = transportService.getAll(id, page, count)
                    call.respond(convertResponse(response))
                } catch (e: Exception) {
                    return@get call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "$e")
                    )
                }
            }
        }
    }
    route("/transport/{id}") {
        authenticate("auth-jwt") {
            delete {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.getClaim("role", String::class)
                    val fio = principal?.getClaim("fio", String::class) ?: ""
                    val id = call.parameters["id"] ?: ""
                    if (role != UserRoleAPI.RESIDENT.toString())
                        return@delete call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "Access denied: only RESIDENT role allowed")
                        )
                    val transportId = call.parameters["id"] ?: ""
                    transportService.deleteTransport(transportId, fio, id)
                    return@delete call.respond(
                        HttpStatusCode.NoContent,
                        "Transport deleted"
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError, "Registration failed: ${e.message}"
                    )
                }
            }
        }
    }
}

private fun convertResponse(response: TransportResponse): TransportResponseAPI {
    val list = mutableListOf<TransportAPI>()
    for (transport in response.transport) {
        list.add(
            TransportAPI(
                id = transport.id,
                number = transport.number,
                model = transport.model,
                color = transport.color,
                insurance = transport.insurance
            )
        )
    }
    return TransportResponseAPI(list, paginationConvert(response.pagination))
}
