package servicesV1

import UserRepository
import CheckpointRepository
import ICheckpointPassService
import errors.RepositoryException
import models.CheckpointStatus
import ResidentialRepository
import org.slf4j.LoggerFactory
import org.slf4j.Logger

class CheckpointPassService(
    private val userRepo: UserRepository,
    private val checkpointRepo: CheckpointRepository,
    private val residentialRepo: ResidentialRepository
) : ICheckpointPassService {


    private val logger: Logger = LoggerFactory.getLogger(CheckpointPassService::class.java)

    override suspend fun passCheckpoint(residentialAddress: String, checkpointNumber: String, userFIO: String): Boolean {
        logger.info("Attempting to pass checkpoint for user: $userFIO at address: $residentialAddress, checkpoint: $checkpointNumber")

        if (!Validator.validateFIO(userFIO)) {
            logger.error("Invalid FIO provided: $userFIO")
            throw IllegalArgumentException("Invalid FIO")
        }

        try {
            val residentialId = try {
                logger.debug("Fetching residential ID for address: $residentialAddress")
                residentialRepo.getResidentialIdByAddress(residentialAddress)
            } catch (e: RepositoryException) {
                logger.error("Error fetching residential ID for address: $residentialAddress", e)
                throw e
            }

            val checkpoint = try {
                logger.debug("Fetching checkpoint for residential ID: $residentialId and checkpoint number: $checkpointNumber")
                checkpointRepo.getCheckpointByResidentialIdAndNumber(residentialId, checkpointNumber)
            } catch (e: RepositoryException) {
                logger.error("Error fetching checkpoint for residential ID: $residentialId, checkpoint number: $checkpointNumber", e)
                throw e
            }

            try {
                logger.debug("Fetching user with FIO: $userFIO")
                userRepo.getUserByFIO(userFIO)
            } catch (e: RepositoryException) {
                logger.error("Error fetching user with FIO: $userFIO", e)
                throw e
            }

            val isPassed = checkpoint.status == CheckpointStatus.OK
            logger.info("Checkpoint pass check result for user $userFIO: $isPassed")
            return isPassed

        } catch (e: RepositoryException) {
            logger.error("Error during checkpoint pass process for user $userFIO", e)
            throw e
        }
    }
}
