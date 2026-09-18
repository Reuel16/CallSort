package com.reuel.callsort.domain.usecase

import com.reuel.callsort.data.local.entities.ContactEntity
import com.reuel.callsort.data.repository.ContactRepository
import kotlinx.coroutines.flow.Flow

class GetCategoryContactsUseCase(
    private val repository: ContactRepository
) {
    /**
     * @param categoryId Pass null to fetch all contacts, or pass a category ID to fetch contacts
     *                   assigned to that category or any of its subcategories (upward inheritance).
     */
    operator fun invoke(categoryId: Long? = null): Flow<List<ContactEntity>> {
        return if (categoryId == null) {
            repository.getAllContacts()
        } else {
            repository.getContactsForCategoryWithInheritance(categoryId)
        }
    }
}
