package com.example.callsort.data.repository

import com.example.callsort.data.contactprovider.ContactsFetcher
import com.example.callsort.data.local.dao.CategoryDao
import com.example.callsort.data.local.dao.ContactDao
import com.example.callsort.data.local.entities.CategoryEntity
import com.example.callsort.data.local.entities.ContactCategoryCrossRef
import com.example.callsort.data.local.entities.ContactEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class ContactRepository(
    private val contactDao: ContactDao,
    private val categoryDao: CategoryDao,
    private val contactsFetcher: ContactsFetcher
) {

    // Sync contacts from device ContactsContract into Room
    suspend fun syncDeviceContacts() {
        val fetchedContacts = contactsFetcher.fetchContacts()
        if (fetchedContacts.isNotEmpty()) {
            contactDao.insertOrUpdateContacts(fetchedContacts)
        }
    }

    // Contact queries
    fun getAllContacts(): Flow<List<ContactEntity>> = contactDao.getAllContacts()

    suspend fun getContactById(contactId: Long): ContactEntity? =
        contactDao.getContactById(contactId)

    // Upward inheritance fetching: gets contacts belonging to target category or any child category
    fun getContactsForCategoryWithInheritance(categoryId: Long): Flow<List<ContactEntity>> {
        return flow {
            val targetCategoryIds = categoryDao.getSelfAndSubcategoryIds(categoryId)
            
            // Fix 2: Guard against empty category lists to prevent SQLite IN () syntax errors
            if (targetCategoryIds.isEmpty()) {
                emit(emptyList())
            } else {
                contactDao.getContactsInCategories(targetCategoryIds).collect { contacts ->
                    emit(contacts)
                }
            }
        }
    }

    // Category assignment mappings
    suspend fun assignContactToCategory(contactId: Long, categoryId: Long) {
        contactDao.addContactToCategory(ContactCategoryCrossRef(contactId, categoryId))
    }

    suspend fun removeContactFromCategory(contactId: Long, categoryId: Long) {
        contactDao.removeContactFromCategory(contactId, categoryId)
    }

    fun getCategoryIdsForContact(contactId: Long): Flow<List<Long>> =
        contactDao.getCategoryIdsForContact(contactId)

    // Category CRUD operations
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getRootCategories(): Flow<List<CategoryEntity>> = categoryDao.getRootCategories()

    fun getSubcategories(parentId: Long): Flow<List<CategoryEntity>> =
        categoryDao.getSubcategories(parentId)

    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.deleteCategory(category)
}
