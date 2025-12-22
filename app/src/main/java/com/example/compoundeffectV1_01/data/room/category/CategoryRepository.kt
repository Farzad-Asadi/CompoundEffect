package com.example.compoundeffectV1_01.data.room.category

import kotlinx.coroutines.flow.Flow


interface CategoryRepository {

    suspend fun insertCategory(vararg categoryEntity: CategoryEntity)

    suspend fun deleteCategory(categoryEntity: CategoryEntity)

    suspend fun updateCategory(categoryEntity: CategoryEntity)


    suspend fun getAllCategory() : List<CategoryEntity>

    suspend fun getCategoryById(categoryId:Int) : CategoryEntity?


    suspend fun getCategoryWithchildren(): List<CategoryWithChildren>

    suspend fun getCategoryWithChildrenById(categoryId: Int): CategoryWithChildren?

    fun observeCategories(): Flow<List<CategoryEntity>>
    suspend fun addCategory(categoryEntity: CategoryEntity)
}