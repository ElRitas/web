package servicesV1

import ITransportService
import ITransportServiceV2
import UserTransportRepository
import TransportRepository
import models.UserTransport
import models.Transport
import errors.RepositoryException
import org.slf4j.LoggerFactory
import org.slf4j.Logger

class TransportService(
    private val userTransportRepository: UserTransportRepository,
    private val transportRepository: TransportRepository
) : ITransportService {

    private val logger: Logger = LoggerFactory.getLogger(TransportService::class.java)

    override suspend fun addTransport(userId: String, transport: Transport) {
        logger.info("Attempting to add transport: ${transport.number} ${transport.model} for user: $userId")

        try {
            try {
                transportRepository.getTransportByNumberAndModel(transport.number, transport.model)
            }
            catch (_: RepositoryException.NotFoundException) {
                transportRepository.addTransport(transport)
            }
            catch (e: RepositoryException) { throw e }

            userTransportRepository.addUserTransport(UserTransport(userId = userId, transportId = transport.id))
            logger.debug("Transport added successfully for user: $userId")
        } catch (e: RepositoryException) {
            logger.error("Error adding transport for user: $userId", e)
            throw e
        }
    }

    override suspend fun getTransportByNumberAndModel(number: String, model: String): Transport? {
        logger.info("Attempting to fetch transport with number: $number, model: $model")

        try {
            return transportRepository.getTransportByNumberAndModel(number, model)
        } catch (e: RepositoryException) {
            logger.error("Error fetching transport with number: $number, model: $model", e)
            throw e
        }
    }
}
