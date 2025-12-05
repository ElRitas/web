package servicesV2

import ISlotService
import SlotRepositoryV2
import models.SlotResponse
import errors.RepositoryException
import models.Slot
import models.SlotStatus

class SlotService(
    private val slotRepo: SlotRepositoryV2
) : ISlotService {
    override suspend fun addSlot(resId: String, number: Int): String? {
        try {
            val slot = Slot(
                residential = resId,
                number = number,
                status = SlotStatus.FREE
            )
            slotRepo.addSlot(slot)
            return slot.id
        } catch (e: RepositoryException) {
            throw e
        }
    }

    override suspend fun getById(id: String): Slot {
        try {
            return slotRepo.getSlotById(id)
        } catch (e: RepositoryException) {
            throw e
        }
    }

    override suspend fun getSlots(resId: String, pageNum: Int, count: Int): SlotResponse {
        try {
            return slotRepo.getSlots(resId, pageNum, count)
        } catch (e: RepositoryException) {
            throw e
        }
    }
}