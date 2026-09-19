package com.reuel.callsort.data.repository

import android.content.Context
import com.reuel.callsort.data.local.CallSortDatabase
import com.reuel.callsort.data.local.entities.CategoryEntity
import com.reuel.callsort.data.local.entities.ContactCategoryCrossRef
import com.reuel.callsort.data.local.entities.ContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BackupRepository(
    private val context: Context,
    private val database: CallSortDatabase
) {
    suspend fun exportData(): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val contacts = database.contactDao().getAllContactsList()
            val categories = database.categoryDao().getAllCategoriesList()
            val crossRefs = database.contactCategoryCrossRefDao().getAllCrossRefsList()

            val rootJson = JSONObject().apply {
                put("contacts", JSONArray().apply {
                    contacts.forEach { contact ->
                        put(JSONObject().apply {
                            put("contactId", contact.id)
                            put("displayName", contact.displayName)
                            put("phoneNumber", contact.phoneNumber)
                        })
                    }
                })
                put("categories", JSONArray().apply {
                    categories.forEach { category ->
                        put(JSONObject().apply {
                            put("categoryId", category.id)
                            put("name", category.name)
                            put("colorHex", category.colorHex)
                        })
                    }
                })
                put("crossRefs", JSONArray().apply {
                    crossRefs.forEach { ref ->
                        put(JSONObject().apply {
                            put("contactId", ref.contactId)
                            put("categoryId", ref.categoryId)
                        })
                    }
                })
            }

            val appDir = context.getExternalFilesDir(null) ?: context.filesDir
            val backupFile = File(appDir, "callsort_backup.json")
            backupFile.writeText(rootJson.toString(2))
            backupFile
        }
    }

    suspend fun importData(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val appDir = context.getExternalFilesDir(null) ?: context.filesDir
            val backupFile = File(appDir, "callsort_backup.json")
            if (!backupFile.exists()) {
                throw Exception("Backup file 'callsort_backup.json' not found in app storage.")
            }

            val jsonString = backupFile.readText()
            val rootJson = JSONObject(jsonString)

            val contactsJson = rootJson.optJSONArray("contacts") ?: JSONArray()
            val contactsList = mutableListOf<ContactEntity>()
            for (i in 0 until contactsJson.length()) {
                val obj = contactsJson.getJSONObject(i)
                contactsList.add(
                    ContactEntity(
                        id = obj.getString("contactId"),
                        displayName = obj.getString("displayName"),
                        phoneNumber = obj.optString("phoneNumber", "")
                    )
                )
            }

            val categoriesJson = rootJson.optJSONArray("categories") ?: JSONArray()
            val categoriesList = mutableListOf<CategoryEntity>()
            for (i in 0 until categoriesJson.length()) {
                val obj = categoriesJson.getJSONObject(i)
                categoriesList.add(
                    CategoryEntity(
                        id = obj.getString("categoryId"),
                        name = obj.getString("name"),
                        colorHex = obj.optString("colorHex", "#000000")
                    )
                )
            }

            val crossRefsJson = rootJson.optJSONArray("crossRefs") ?: JSONArray()
            val crossRefsList = mutableListOf<ContactCategoryCrossRef>()
            for (i in 0 until crossRefsJson.length()) {
                val obj = crossRefsJson.getJSONObject(i)
                crossRefsList.add(
                    ContactCategoryCrossRef(
                        contactId = obj.getString("contactId"),
                        categoryId = obj.getString("categoryId")
                    )
                )
            }

            database.contactDao().insertContacts(contactsList)
            database.categoryDao().insertCategories(categoriesList)
            database.contactCategoryCrossRefDao().insertCrossRefs(crossRefsList)
        }
    }
}
