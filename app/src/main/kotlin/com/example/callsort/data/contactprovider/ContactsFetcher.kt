package com.example.callsort.data.contactprovider

import android.content.Context
import android.provider.ContactsContract
import com.example.callsort.data.local.entities.ContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsFetcher(private val context: Context) {

    suspend fun fetchContacts(): List<ContactEntity> = withContext(Dispatchers.IO) {
        val contactsList = mutableListOf<ContactEntity>()
        val contentResolver = context.contentResolver

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
        )

        cursor?.use { c ->
            val idIndex = c.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameIndex = c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val photoIndex = c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI)
            val hasPhoneIndex = c.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            while (c.moveToNext()) {
                val contactId = c.getLong(idIndex)
                val displayName = c.getString(nameIndex) ?: "Unknown"
                val photoUri = c.getString(photoIndex)
                val hasPhoneNumber = c.getInt(hasPhoneIndex) > 0

                var phoneNumber: String? = null
                if (hasPhoneNumber) {
                    phoneNumber = fetchPrimaryPhoneNumber(contactId)
                }

                contactsList.add(
                    ContactEntity(
                        contactId = contactId,
                        displayName = displayName,
                        phoneNumber = phoneNumber,
                        photoUri = photoUri
                    )
                )
            }
        }
        contactsList
    }

    private fun fetchPrimaryPhoneNumber(contactId: Long): String? {
        val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val phoneCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            phoneProjection,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )

        phoneCursor?.use { pc ->
            if (pc.moveToFirst()) {
                val numberIndex = pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                return pc.getString(numberIndex)
            }
        }
        return null
    }
}
