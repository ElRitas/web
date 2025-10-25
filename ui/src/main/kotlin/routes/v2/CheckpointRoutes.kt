package routes.v2

import CheckpointAPI
import CheckpointResponseAPI
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ICheckpointServiceV2
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import UserRoleDA
import models.CheckpointResponse
import paginationConvert

fun Route.checkpointRoutesV2(checkpointService: ICheckpointServiceV2) {
    route("/checkpoints") {
        authenticate("auth-jwt") {
            get {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.getClaim("role", String::class)
                    val resId = principal?.getClaim("residentialId", String::class)
                        ?: ""
                    if (role == UserRoleDA.GUEST.toString())
                        return@get call.respond(
                            HttpStatusCode.Forbidden,
                            mapOf("error" to "Access denied: GUEST role not allowed")
                        )
                    val page = call.parameters["pagesNum"]?.toInt() ?: 1
                    val count = call.parameters["elemCount"]?.toInt() ?: 5
                    val response = checkpointService.getAll(resId, page, count)
                    call.respond(convert(response))
                } catch (e: Exception) {
                    return@get call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "$e")
                    )
                }
            }

            route("/{id}/pass") {
                post {
                    try {
                        val id = call.parameters["id"] ?: ""
                        if (id.isEmpty())
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf("error" to "Wrong id")
                            )
                        val response = checkpointService.pass(id)
                        return@post call.respond(hashMapOf("passed" to response))
                    } catch (e: Exception) {
                        return@post call.respond(
                            HttpStatusCode.InternalServerError,
                            mapOf("error" to "$e")
                        )
                    }
                }
            }
        }
    }
}

private fun convert(response: CheckpointResponse): CheckpointResponseAPI {
    val list = mutableListOf<CheckpointAPI>()
    for (checkpoint in response.checkpoints)
        list.add(CheckpointAPI(
            checkpoint.id,
            checkpoint.guardFio,
            checkpoint.guardPhone,
            checkpoint.number,
            CheckpointStatusAPI.valueOf(checkpoint.status.toString())
        ))
    return CheckpointResponseAPI(
        list,
        paginationConvert(response.pagination)
    )
}
