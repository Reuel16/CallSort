package com.example.callsort.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.callsort.data.local.dao.CategoryDao
import com.example.callsort.data.local.dao.ContactDao
import com.example.callsort.data.local.entities.CategoryEntity
import com.example.callsort.data.local.entities.ContactCategoryCrossRef
import com.example.callsort.data.local.entities.ContactEntity

@Database(
    entities = [
        ContactEntity::class,
        CategoryEntity::class,
        ContactCategoryCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CallSortDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: CallSortDatabase? = null

        fun getDatabase(context: Context): CallSortDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallSortDatabase::class.java,
                    "callsort_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
