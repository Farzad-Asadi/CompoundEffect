package com.example.compoundeffectV1_01.ui.categoryScreen

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundeffectV1_01.data.room.appSystemInfo.AppSystemInfoRepository
import com.example.compoundeffectV1_01.data.room.category.CategoryEntity
import com.example.compoundeffectV1_01.data.room.category.CategoryRepository
import com.example.compoundeffectV1_01.data.room.event.EventRepository
import com.example.compoundeffectV1_01.utils.colorToString
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryScreenViewModel(
    private val categoryRepository: CategoryRepository,
    private val eventRepository: EventRepository,
    private val appSystemInfoRepository: AppSystemInfoRepository
) : ViewModel() {


    // region init

    private val _categoryUiState = MutableStateFlow(CategoryUiState())
    val categoryUiState = _categoryUiState.asStateFlow()


    init {
        initializeCategoryScreenViewModel()
    }


    /**
     * بارگذاری اولیه ویومدل.
     */
    private fun initializeCategoryScreenViewModel() {

        viewModelScope.launch {
            val job1 = async {
                fetchAndUpdateCategory()
            }
            job1.await()

            val job2 = async {

                createNewCategory()
            }
            job2.await()

            val job3 = async {
                _categoryUiState.update { categoryUiState ->
                    categoryUiState.copy(
                        isDataLoaded = true
                    )
                }
            }
            job3.await()
        }
    }


    // endregion


    // region unPrivet


    /**
     * کارهایی که با زدن روی دکمه تایید یا برگشت در صفحه ایجاد دسته بندی اتفاق می افتد
     */
    fun handleAddCategory(
        name: String = "",
        parentId: Int? = null,
        icon: ImageVector? = null,
        color: Color? = null,
        siblingPositionTop: Boolean? = null,
        back: Boolean = false,
        confirm: Boolean = false
    ) {
        val siblingListOfCategory =
            _categoryUiState.value.sortedCategoryEntityWhitGeneration.keys.filter { it.parentCategoryId == _categoryUiState.value.newCategoryEntity?.parentCategoryId }


        val siblingIndex: Int =
            if (siblingListOfCategory.isNotEmpty()
            ) {
                if (siblingPositionTop == true){
                    0
                }else{
                    siblingListOfCategory.count()
                }

            }else{
                0
            }

//        Log.i("TEST", "siblingPositionTop=${siblingPositionTop}")
//        Log.i("TEST", "siblingIndex=${siblingIndex}")
//
//        Log.i("TEST", "siblingListOfCategory=${siblingListOfCategory}")
//        Log.i("TEST", "---------------------------------------")


        if (_categoryUiState.value.newCategoryEntity != null) {
            val updateNewCategory = _categoryUiState.value.newCategoryEntity!!.copy(
                name = if (name != "") name else _categoryUiState.value.newCategoryEntity!!.name,
                parentCategoryId = parentId
                    ?: _categoryUiState.value.newCategoryEntity!!.parentCategoryId,
                icon = icon ?: _categoryUiState.value.newCategoryEntity!!.icon,
                color = color?.colorToString() ?: _categoryUiState.value.newCategoryEntity!!.color,
                siblingIndex = siblingIndex ?: _categoryUiState.value.newCategoryEntity!!.siblingIndex
            )
            Log.i("TEST", "updateNewCategory.siblingIndex=${updateNewCategory.siblingIndex}")
            _categoryUiState.update { categoryUiState ->
                categoryUiState.copy(
                    newCategoryEntity = updateNewCategory
                )
            }

            if (confirm ) {
                val newSiblingListOfCategoryEntity= mutableListOf<CategoryEntity>()

                if (updateNewCategory.siblingIndex == 0){
                    Log.i("TEST", "in if")
                    newSiblingListOfCategoryEntity.addAll(changeSiblingPositionOfSibling(siblingListOfCategory,updateNewCategory,moveCategoryToTopOfList = true))


                }

                viewModelScope.launch {

                    val job0 = async {
                        newSiblingListOfCategoryEntity.forEach {
                            categoryRepository.updateCategory(it)
                        }
                    }
                    job0.await()

                    val job1 = async {
                        categoryRepository.insertCategory(updateNewCategory)
                    }
                    job1.await()

                    val job2 = async {
                        fetchAndUpdateCategory()
                    }
                    job2.await()
                }
                createNewCategory()
            }

            if (back) {
                createNewCategory()
            }

        }

    }

    private fun changeSiblingPositionOfSibling(
        siblingListOfCategoryEntity: List<CategoryEntity>,
        categoryEntity: CategoryEntity,
        moveCategoryToTopOfList:Boolean =false,
        moveCategoryUp:Boolean =false,
        moveCategoryDown:Boolean =false,
    ) : List<CategoryEntity>{
        val newSiblingListOfCategoryEntity= mutableListOf<CategoryEntity>()

        if (moveCategoryToTopOfList){
            siblingListOfCategoryEntity.forEach {
                newSiblingListOfCategoryEntity.add(it.copy(siblingIndex = it.siblingIndex+1))
            }
        }
        Log.i("TEST", "newSiblingListOfCategory=${newSiblingListOfCategoryEntity}")
//        if (moveCategoryUp ){
//
//            val topSiblingOfCategory=siblingListOfCategory.firstOrNull { it.siblingIndex == siblingIndex-1 }
//            val bottomSiblingOfCategory : List<Category> =siblingListOfCategory.filter { it.siblingIndex == siblingIndex+1 }
//
//            if (topSiblingOfCategory !=null){
//                newSiblingListOfCategory.add(topSiblingOfCategory.copy(siblingIndex = topSiblingOfCategory.siblingIndex+1))
//            }
//        }
//        if (moveCategoryDown ){
//            val bottomSiblingOfCategory=siblingListOfCategory.firstOrNull { it.siblingIndex == siblingIndex+1 }
//
//            if (bottomSiblingOfCategory !=null){
//                newSiblingListOfCategory.add(bottomSiblingOfCategory.copy(siblingIndex = bottomSiblingOfCategory.siblingIndex-1))
//            }
//        }


        return newSiblingListOfCategoryEntity
    }


    /**
     * کارهایی که با درگ دسته بندی در صفحه دسته بندی اتفاق می افتد.
     */
    fun drugCategoryInCategoryContent(
        categoryEntity: CategoryEntity,
        offsetX: Float,
        offsetY: Float,
        endDrag: Boolean = false
    ) {
        val categoryOffset: Map<Int?, Pair<Float, Float>> =
            mutableStateMapOf(categoryEntity.categoryId to Pair(offsetX, offsetY))
        _categoryUiState.update { categoryUiState ->
            categoryUiState.copy(
                categoryOffset = categoryOffset
            )
        }


        val categoryList = _categoryUiState.value.categoryEntityList
        var sortedCategoryWhitGeneration = _categoryUiState.value.sortedCategoryEntityWhitGeneration

        var changedCategory = categoryList.first { it.categoryId == categoryEntity.categoryId }

        val parentCategory =
            // ریشه اصلی       for firstBranch
            categoryList.first { it.categoryId == changedCategory.parentCategoryId }

        val siblingCategoryList =
            sortedCategoryWhitGeneration.keys.filter { it.parentCategoryId == changedCategory.parentCategoryId }

        val indexOfCategoryInSiblingCategory: Int = siblingCategoryList.indexOf(changedCategory)

        val topCategoryOfSelectedCategoryEntity: CategoryEntity =
            when {
                indexOfCategoryInSiblingCategory == 0 -> parentCategory
                indexOfCategoryInSiblingCategory > 0 -> siblingCategoryList[indexOfCategoryInSiblingCategory - 1]
                else -> parentCategory
            }


        if (offsetX - _categoryUiState.value.lastParentChangeOffsetX >= 50) {
            if ((sortedCategoryWhitGeneration[changedCategory] ?: 5) <= 5) {
                topCategoryOfSelectedCategoryEntity.let {
                    changedCategory = categoryEntity.copy(parentCategoryId = it.categoryId)
                    _categoryUiState.value.lastParentChangeOffsetX = offsetX // مقدار جدید ذخیره شود
                }
            }
        }
        if (offsetX - _categoryUiState.value.lastParentChangeOffsetX <= -50) {
            if ((sortedCategoryWhitGeneration[changedCategory] ?: 2) >= 3) {
                changedCategory = categoryEntity.copy(parentCategoryId = parentCategory.parentCategoryId)
                _categoryUiState.value.lastParentChangeOffsetX = offsetX // مقدار جدید ذخیره شود
            }
        }

        if (offsetY - _categoryUiState.value.lastParentChangeOffsetY >= 50) {


            //پدر دارد
            if (changedCategory.parentCategoryId!! > 1) {
                Log.i("TEST", "پدر دارد")


                if (siblingCategoryList.count() == 1) {
                    Log.i("TEST", "خواهر و برادر ندارد")


                }
                if (siblingCategoryList.count() > 1) {
                    Log.i("TEST", "خواهر و برادر دارد")
                    if (indexOfCategoryInSiblingCategory == 0) {
                        Log.i("TEST", "بالای لیست است")

                    }

                    if (indexOfCategoryInSiblingCategory == (siblingCategoryList.count()) - 1) {
                        Log.i("TEST", "انتهای لیست است")

                    }
                    if (
                        indexOfCategoryInSiblingCategory != 0 &&
                        indexOfCategoryInSiblingCategory != (siblingCategoryList.count()) - 1
                    ) {
                        Log.i("TEST", "میانه لیست است است")

                    }
                }


            }


            //پدر ندارد
            if (changedCategory.parentCategoryId == 1) {
                Log.i("TEST", "پدر ندارد")


                //خواهر و برادر دارد
                if (siblingCategoryList.count() > 1) {
                    Log.i("TEST", "خواهر و برادر دارد")

                    //بالای لیست
                    if (indexOfCategoryInSiblingCategory == 0) {

                        Log.i("TEST", "بالای لیست است")


                        val parentIdOfBelowSibling =
                            siblingCategoryList[1].categoryId
                        changedCategory =
                            categoryEntity.copy(
                                isExtended = false,
                                parentCategoryId = parentIdOfBelowSibling
                            )
                        val allChildren = getAllChildren(categoryList.toSet(), categoryEntity)

                        sortedCategoryWhitGeneration = sortedCategoryWhitGeneration.mapKeys {
                            if (it.key in allChildren) {
                                it.key.copy(visible = false)
                            } else {
                                it.key

                            }
                        }

                    }


                    //میانه لیست
                    if (
                        indexOfCategoryInSiblingCategory != 0 &&
                        indexOfCategoryInSiblingCategory != (siblingCategoryList.count()) - 1
                    ) {

                        Log.i("TEST", "میانه لیست است است")

                    }


                    //انتهای لیست
                    if (indexOfCategoryInSiblingCategory == (siblingCategoryList.count()) - 1) {

                        Log.i("TEST", "انتهای لیست است")
                        //نیاز به هیچ کاری نیست

                    }
                }


                //خواهر و برادر ندارد
                if (siblingCategoryList.count() == 1) {


                    Log.i("TEST", "خواهر و برادر ندارد")
                    //نیاز به هیچ کاری نیست
                }


            }






            if (siblingCategoryList.lastIndex >= indexOfCategoryInSiblingCategory + 1) {
                val parentIdOfBelowSibling =
                    siblingCategoryList[indexOfCategoryInSiblingCategory + 1].categoryId

                if (categoryEntity.expandable) {

                    changedCategory =
                        categoryEntity.copy(isExtended = false, parentCategoryId = parentIdOfBelowSibling)
                    val allChildren = getAllChildren(categoryList.toSet(), categoryEntity)

                    sortedCategoryWhitGeneration = sortedCategoryWhitGeneration.mapKeys {
                        if (it.key in allChildren) {
                            it.key.copy(visible = false)
                        } else {
                            it.key

                        }
                    }

                    _categoryUiState.value.lastParentChangeOffsetY = offsetY // مقدار جدید ذخیره شود
                }
            }

        }


        val updatedList = categoryList.map { categoryInMap ->
            if (categoryInMap.categoryId == categoryEntity.categoryId) {
                changedCategory
            } else {
                categoryInMap
            }
        }
        val expandableCheckedCategoryList = expandableCheckedCategory(updatedList)

        sortedCategoryWhitGeneration = sortedCategoryWhitGeneration.mapKeys { entry ->
            expandableCheckedCategoryList.find { it == entry.key } ?: entry.key
        }
        _categoryUiState.update { categoryUiState ->
            categoryUiState.copy(
                sortedCategoryEntityWhitGeneration = sortedCategoryWhitGeneration,
            )
        }

        if (endDrag) {


            _categoryUiState.value.lastParentChangeOffsetX = 0f // ریست مقدار ذخیره‌شده
            _categoryUiState.value.lastParentChangeOffsetY = 0f // ریست مقدار ذخیره‌شده
            val newCategoryOffset: Map<Int?, Pair<Float, Float>> =
                mutableStateMapOf(categoryEntity.categoryId to Pair(0f, 0f))

            _categoryUiState.update { categoryUiState ->
                categoryUiState.copy(
                    categoryOffset = newCategoryOffset,
                    resetKey = !_categoryUiState.value.resetKey
                )
            }
            viewModelScope.launch {
                val job0 = async {
                    categoryRepository.updateCategory(changedCategory)
                }
                job0.await()

                val job1 = async {

                    fetchAndUpdateCategory()

                }
                job1.await()
            }
        }


        Log.i("TEST", "---------------")

    }


    /**
     *  کارهایی که با لمس expand در صفحه دسته بندی اتفاق می افتد.
     */
    fun onClickExpandInCategoryListInCategoryScreen(categoryId: Int, isExtended: Boolean) {
        val category =
            _categoryUiState.value.sortedCategoryEntityWhitGeneration.keys.firstOrNull { it.categoryId == categoryId }
                ?: return


        if (isExtended) {
            val updatedCategoryList = _categoryUiState.value.categoryEntityList.map { categoryInMap ->
                if (categoryInMap.categoryId == categoryId) {
                    categoryInMap.copy(isExtended = false)
                } else {
                    categoryInMap
                }
            }
            val allChildrenOfUpdatedCategoryList =
                getAllChildren(updatedCategoryList.toSet(), category)
            val updateCategoryList = updatedCategoryList.map { categoryInMap ->
                if (categoryInMap in allChildrenOfUpdatedCategoryList) {
                    categoryInMap.copy(visible = false)
                } else {
                    categoryInMap
                }
            }
            _categoryUiState.update { categoryUiState ->
                categoryUiState.copy(
                    categoryEntityList = updateCategoryList,
                )
            }
            saveCategoryToDatabase(updateCategoryList)


            val updatedSortedCategoryWhitGeneration =
                _categoryUiState.value.sortedCategoryEntityWhitGeneration.mapKeys {
                    if (it.key.categoryId == categoryId) {
                        it.key.copy(isExtended = false)
                    } else {
                        it.key
                    }
                }
            val allChildrenOfSortedCategoryWhitGeneration =
                getAllChildren(updatedSortedCategoryWhitGeneration.keys.toSet(), category)

            val updateSortedCategoryWhitGeneration = updatedSortedCategoryWhitGeneration.mapKeys {
                if (it.key in allChildrenOfSortedCategoryWhitGeneration) {
                    it.key.copy(visible = false)
                } else {
                    it.key
                }
            }
            _categoryUiState.update { categoryUiState ->
                categoryUiState.copy(
                    sortedCategoryEntityWhitGeneration = updateSortedCategoryWhitGeneration,
                )
            }


        } else {


            val updatedCategoryList = _categoryUiState.value.categoryEntityList.map { categoryInMap ->
                if (categoryInMap.categoryId == categoryId) {
                    categoryInMap.copy(isExtended = true)
                } else {
                    categoryInMap
                }
            }
            val allChildrenOfUpdatedCategoryList =
                getAllChildren(updatedCategoryList.toSet(), category)
            val updateCategoryList = updatedCategoryList.map { categoryInMap ->
                if (categoryInMap in allChildrenOfUpdatedCategoryList) {
                    categoryInMap.copy(visible = true)
                } else {
                    categoryInMap
                }
            }
            _categoryUiState.update { categoryUiState ->
                categoryUiState.copy(
                    categoryEntityList = updateCategoryList,
                )
            }
            saveCategoryToDatabase(updateCategoryList)


            val updatedSortedCategoryWhitGeneration =
                _categoryUiState.value.sortedCategoryEntityWhitGeneration.mapKeys {
                    if (it.key.categoryId == categoryId) {
                        it.key.copy(isExtended = true)
                    } else {
                        it.key
                    }
                }
            val allChildrenOfSortedCategoryWhitGeneration =
                getAllChildren(updatedSortedCategoryWhitGeneration.keys.toSet(), category)
            val updateSortedCategoryWhitGeneration = updatedSortedCategoryWhitGeneration.mapKeys {
                if (it.key in allChildrenOfSortedCategoryWhitGeneration) {
                    it.key.copy(visible = true)
                } else {
                    it.key
                }
            }
            _categoryUiState.update { categoryUiState ->
                categoryUiState.copy(
                    sortedCategoryEntityWhitGeneration = updateSortedCategoryWhitGeneration,
                )
            }
        }


    }


    // endregion


    // region Privet


    /**
     * دریافت دسته بندی از دیتابیس و آپدیت categoryList و sortedCategoryWhitGeneration در دیتاکلاس ویومدل.
     */
    private suspend fun fetchAndUpdateCategory() {

        val categoryList = categoryRepository.getAllCategory()
        val expandableCheckedCategory = expandableCheckedCategory(categoryList)
        val sortCategoriesWithGenerations = sortCategoriesWithGenerations(expandableCheckedCategory)

        _categoryUiState.update { categoryUiState ->
            categoryUiState.copy(
                categoryEntityList = categoryList,
                sortedCategoryEntityWhitGeneration = sortCategoriesWithGenerations
            )
        }

    }

    /**
     *چک کردن دسته بندی که آیا فرزندی دارد که پارامتر expandable  را ترو کند.
     * @return لیست دسته بندی که پارامتر expandable آن به روز است
     */
    private fun expandableCheckedCategory(categoryEntityList: List<CategoryEntity>): List<CategoryEntity> {
        val categoryMap = categoryEntityList.groupBy { it.parentCategoryId }
        val expandableCheckedCategoryList = categoryEntityList.map { category ->
            if (categoryMap.keys.contains(category.categoryId)) {
                category.copy(expandable = true)
            } else {
                category.copy(expandable = false)
            }
        }
//        _categoryUiState.update { categoryUiState ->
//            categoryUiState.copy(
//                categoryList = expandableCheckedCategoryList,
//            )
//        }
        return expandableCheckedCategoryList
    }

    /**
     * ساخت دسته بندی جدید در دیتاکلاس ویومدل.
     */
    private fun createNewCategory() {
        val newCategoryEntity = CategoryEntity(
            name = "",
            parentCategoryId = 1,
            icon = Icons.Filled.QuestionMark,
            color = Color(0xFF000000).colorToString(),
            description = "",
            siblingIndex = 0
        )
        _categoryUiState.update { categoryUiState ->
            categoryUiState.copy(
                newCategoryEntity = newCategoryEntity
            )
        }
    }

    /**
     *ذخیره دسته بندی در دیتابیس.
     */
    private fun saveCategoryToDatabase(categoryEntityList: List<CategoryEntity>) {
        viewModelScope.launch {
            categoryEntityList.forEach { category ->
                categoryRepository.updateCategory(category)
            }
        }
    }

    /**
     *ایجاد یک مپ از دسته بندی ها وGeneration که sort شده اند .
     * @return یک مپ که دسته بندی ها وGeneration در آن مرتب شده اند
     */
    private fun sortCategoriesWithGenerations(categoryEntityList: List<CategoryEntity>): Map<CategoryEntity, Int> {

        val categoryMap = categoryEntityList.groupBy { it.parentCategoryId }

        val rootCategories = categoryEntityList.filter { it.parentCategoryId == -1 }
        val generationMap = mutableMapOf<CategoryEntity, Int>()
        fun traverseTree(categoryEntity: CategoryEntity, generation: Int): List<CategoryEntity> {
            val sortedList = mutableListOf<CategoryEntity>()

            generationMap[categoryEntity] = generation
            sortedList.add(categoryEntity)

            val children =
                categoryMap[categoryEntity.categoryId]?.sortedBy { it.siblingIndex } ?: emptyList()
            for (child in children) {
                sortedList.addAll(traverseTree(child, generation + 1)) // پیمایش فرزند با نسل +1
            }
            return sortedList
        }

        val sortedCategories = rootCategories.flatMap { traverseTree(it, 1) }

        val sortedCategoriesMinesRootCategories =
            sortedCategories.filterNot { it.parentCategoryId == -1 }
        val newSortCategoriesWithGeneration = sortedCategoriesMinesRootCategories
            .associateWithTo(LinkedHashMap()) { generationMap[it]!! }
        return newSortCategoriesWithGeneration
    }

    /**
     *پیدا کردن لیست فرزندان.
     * @return لیست فرزندان
     */
    private fun getAllChildren(allCategories: Set<CategoryEntity>, parent: CategoryEntity): List<CategoryEntity> {
        val directChildren = allCategories.filter { it.parentCategoryId == parent.categoryId }

        // پیدا کردن فرزندهای این فرزندان
        return directChildren + directChildren.flatMap { getAllChildren(allCategories.toSet(), it) }
    }

    /**
     *پاک کردن دسته بندی
     */
    fun onClickDeleteCategory(categoryEntity: CategoryEntity) {
        viewModelScope.launch {
            val job1 = async {
                categoryRepository.deleteCategory(categoryEntity)
            }
            job1.await()
            val job2 = async {
                fetchAndUpdateCategory()
            }
            job2.await()
        }
    }


    // endregion


}


/**
 *  دیتاکلاس ویو مدل.
 *
 * @property newCategoryEntity دسته بندی جدید.
 */
data class CategoryUiState(
    val categoryEntityList: List<CategoryEntity> = listOf(),
    val sortedCategoryEntityWhitGeneration: Map<CategoryEntity, Int> = mutableStateMapOf(),
    val newCategoryEntity: CategoryEntity? = null,
    val categoryOffset: Map<Int?, Pair<Float, Float>> = mutableStateMapOf(),
    var lastParentChangeOffsetX: Float = 0f,
    var lastParentChangeOffsetY: Float = 0f,
    val resetKey: Boolean = false,
    val isDataLoaded: Boolean = false

)