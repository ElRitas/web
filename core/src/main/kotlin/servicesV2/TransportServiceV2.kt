package servicesV2

import ITransportServiceV2
import TransportRepositoryV2
import UserRepository
import UserTransportRepository
import errors.RepositoryException
import models.Transport
import models.TransportResponse
import models.UserTransport

class TransportServiceV2(
    private val transportRepository: TransportRepositoryV2,
    private val userTransportRepository: UserTransportRepository,
    private val userRepository: UserRepository
) : ITransportServiceV2 {
    override suspend fun addTransport(
        userId: String,
        number: String,
        model: String,
        color: String,
        insurance: Boolean
    ) {
        try {
            val transport = Transport(
                number = number,
                model = model,
                color = color,
                insurance = insurance
            )
            transportRepository.addTransport(transport)
            val userTransport = UserTransport(
                userId = userId,
                transportId = transport.id
            )
            userTransportRepository.addUserTransport(userTransport)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deleteTransport(id: String, fio: String, userId: String) {
        try {
            val user = userRepository.getUserByFIO(fio)
            val userTransport = userTransportRepository.getUserTransportByUserId(user.id, id)
                ?: UserTransport("", "", "")
            userTransportRepository.deleteUserTransport(userTransport)
            transportRepository.deleteTransport(id)
        } catch (e: RepositoryException) {
            throw e
        }
    }

    override suspend fun getAll(userId: String, pageNum: Int, count: Int): TransportResponse {
        try {
            return transportRepository.getAll(userId, pageNum, count)
        } catch (e: RepositoryException) {
            throw e
        }
    }

    override suspend fun getById(id: String): Transport {
        try {
            return transportRepository.getTransportById(id)
        } catch (e: RepositoryException) {
            throw e
        }
    }
}