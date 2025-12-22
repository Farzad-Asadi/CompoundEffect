package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    override suspend fun insertCategory(vararg categoryEntity: CategoryEntity) =
        categoryDao.insertCategory(*categoryEntity)

    override suspend fun deleteCategory(categoryEntity: CategoryEntity) =
        categoryDao.deleteCategory(categoryEntity)


    override suspend fun updateCategory(categoryEntity: CategoryEntity) =
        categoryDao.updateCategory(categoryEntity)

    override suspend fun getAllCategory(): List<CategoryEntity> =
        categoryDao.getAllCategory()

    override suspend fun getCategoryById(categoryId: Int): CategoryEntity? =
        categoryDao.getCategoryById(categoryId)

    override suspend fun getCategoryWithchildren(): List<CategoryWithChildren> =
        categoryDao.getCategoryWithchildren()

    override suspend fun getCategoryWithChildrenById(categoryId: Int): CategoryWithChildren? =
            categoryDao.getCategoryWithChildrenById(categoryId)

    override fun observeCategories(): Flow<List<CategoryEntity>> =
        categoryDao.observeAll() // اگر نداری، تو DAO اضافه می‌کنیم

    override suspend fun addCategory(categoryEntity: CategoryEntity) {
        categoryDao.insertCategory(categoryEntity)
    }

}