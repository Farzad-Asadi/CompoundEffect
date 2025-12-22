package com.example.compoundeffectV1_01.data.room.category

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "category")
data class CategoryEntity(

    @PrimaryKey(autoGenerate = true)
    val categoryId : Int?=null,

    val name: String,
    val parentCategoryId: Int?=null,
    val icon: ImageVector ,
    val color: String ,
    val description: String,
    val expandable:Boolean = false,
    val isExtended: Boolean = true,
    val visible: Boolean = true,
    val siblingIndex: Int = 0
)

data class CategoryWithChildren(
    @Embedded val categoryEntity: CategoryEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "parentCategoryId"
    )val categoryEntityChildren: List<CategoryEntity>?
)




