package servicesV2

import CheckpointRepositoryV2
import ICheckpointServiceV2
import errors.RepositoryException
import models.CheckpointResponse
import models.CheckpointStatus

class CheckpointServiceV2(
    private val checkpointRepository: CheckpointRepositoryV2
) : ICheckpointServiceV2 {
    override suspend fun pass(id: String): Boolean {
        try {
            val checkpoint = checkpointRepository.getById(id)
            return checkpoint.status == CheckpointStatus.OK
        } catch (e: RepositoryException) {
            throw e
        }
    }

    override suspend fun getAll(residentialId: String, pageNum: Int, count: Int): CheckpointResponse {
        try {
            return checkpointRepository.getAll(residentialId, pageNum, count)
        } catch (e: RepositoryException) {
            throw e
        }
    }

}