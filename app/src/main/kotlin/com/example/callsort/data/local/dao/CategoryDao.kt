package com.example.callsort.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.callsort.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY name ASC")
    fun getRootCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY name ASC")
    fun getSubcategories(parentId: Long): Flow<List<CategoryEntity>>

    /**
     * Recursive CTE query to get a category ID and all descendant subcategory IDs.
     * Essential for upward inheritance filtering on the Board screen.
     */
    @Query(
        """
        WITH RECURSIVE CategoryTree AS (
            SELECT categoryId FROM categories WHERE categoryId = :categoryId
            UNION ALL
            SELECT c.categoryId FROM categories c
            INNER JOIN CategoryTree ct ON c.parentId = ct.categoryId
        )
        SELECT categoryId FROM CategoryTree
        """
    )
    suspend fun getSelfAndSubcategoryIds(categoryId: Long): List<Long>
}
