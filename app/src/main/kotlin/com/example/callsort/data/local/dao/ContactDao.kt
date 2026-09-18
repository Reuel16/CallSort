package com.example.callsort.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.callsort.data.local.entities.ContactCategoryCrossRef
import com.example.callsort.data.local.entities.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    // Fix 1: @Upsert performs an UPDATE on existing primary keys instead of DELETE + INSERT.
    // This preserves foreign key relationship records in contact_category_cross_ref during sync.
    @Upsert
    suspend fun insertOrUpdateContacts(contacts: List<ContactEntity>)

    @Query("SELECT * FROM contacts ORDER BY displayName ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE contactId = :contactId")
    suspend fun getContactById(contactId: Long): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addContactToCategory(crossRef: ContactCategoryCrossRef)

    @Query("DELETE FROM contact_category_cross_ref WHERE contactId = :contactId AND categoryId = :categoryId")
    suspend fun removeContactFromCategory(contactId: Long, categoryId: Long)

    @Query("SELECT categoryId FROM contact_category_cross_ref WHERE contactId = :contactId")
    fun getCategoryIdsForContact(contactId: Long): Flow<List<Long>>

    /**
     * Retrieves distinct contacts matching any of the provided category IDs.
     */
    @Query(
        """
        SELECT DISTINCT c.* FROM contacts c
        INNER JOIN contact_category_cross_ref ref ON c.contactId = ref.contactId
        WHERE ref.categoryId IN (:categoryIds)
        ORDER BY c.displayName ASC
        """
    )
    fun getContactsInCategories(categoryIds: List<Long>): Flow<List<ContactEntity>>
}
