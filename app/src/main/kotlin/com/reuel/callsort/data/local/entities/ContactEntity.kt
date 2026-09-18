package com.reuel.callsort.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val contactId: Long, // Unique ID from system ContactsContract
    val displayName: String,
    val phoneNumber: String?,
    val photoUri: String?
)
