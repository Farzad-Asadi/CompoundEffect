package com.example.compoundeffectV1_01.ui.categoryScreen


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryEntity
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.category.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel2 @Inject constructor(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {



    // این دو تا برای سناریوی “درگ شروع شد/تمام شد”
    private val _dragCollapsedRestore = MutableStateFlow<Int?>(null)
    private val _collapsedIds = MutableStateFlow<Set<Int>>(emptySet())


    val uiState: StateFlow<CategoryUiState2> =
        combine(
            categoryRepository.observeAll(),
            _collapsedIds
        ) { categories, collapsed ->
            val flatten = flattenCategoryTreeWithLevelsAndVisibility(categories, collapsed)
            CategoryUiState2(
                isLoading = false,
                categories = categories,
                renderItems = flatten.items,
                levelById = flatten.levelById
            )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CategoryUiState2(isLoading = true, categories = emptyList())
            )

    private val _draft = MutableStateFlow(CategoryDraft2())
    val draft = _draft.asStateFlow()











    fun addCategoryQuick() {
        viewModelScope.launch {
            categoryRepository.insertCategory(
                CategoryEntity(
                    name = "New Category",
                    parentCategoryId = 1,
                    iconName = "QuestionMark",
                    color = "#000000",
                    description = "",
                    siblingIndex = 0
                )
            )
        }
    }

    fun canAddChildTo(parentId: Int): Boolean {
        val parentLevel = uiState.value.levelById[parentId] ?: 1
        return parentLevel < 4
    }


    fun setDraftParent(parentId: Int) {
        val parentLevel = uiState.value.levelById[parentId] ?: 1
        if (parentLevel >= 4) {
            // بعداً snackbar می‌زنیم؛ فعلاً ignore
            return
        }
        // set draft parent
    }

    fun setDraftName(value: String) {
        _draft.update { it.copy(name = value) }
    }

    fun trySetDraftParent(parentId: Int): Boolean {
        val parentLevel = uiState.value.levelById[parentId] ?: 1
        if (parentLevel >= 4) return false
        _draft.update { it.copy(parentId = parentId) }
        return true
    }

    fun createCategoryFromDraft(): Boolean {
        val d = _draft.value
        if (d.name.isBlank()) return false

        viewModelScope.launch {
            // siblingIndex = آخرین فرزندهای همین parent
            val current = uiState.value.categories
            val nextSiblingIndex = current.count { it.parentCategoryId == d.parentId }

            categoryRepository.insertCategory(
                CategoryEntity(
                    name = d.name.trim(),
                    parentCategoryId = d.parentId,
                    iconName = d.iconName,
                    color = d.color,
                    description = d.description,
                    siblingIndex = nextSiblingIndex
                )
            )
            resetDraft()
        }
        return true
    }

    fun resetDraft() {
        _draft.value = CategoryDraft2()
    }

    fun reorderWithinSameParent(parentId: Int?, orderedIds: List<Int>) {
        viewModelScope.launch {
            val current = uiState.value.categories.filter { it.parentCategoryId == parentId }
            val byId = current.associateBy { it.categoryId }

            orderedIds.forEachIndexed { index, id ->
                val entity = byId[id] ?: return@forEachIndexed
                categoryRepository.updateCategory(entity.copy(siblingIndex = index))
            }
        }
    }

    fun onDragStartMaybeCollapse(categoryId: Int) {
        val item = uiState.value.renderItems.firstOrNull { it.category.categoryId == categoryId } ?: return
        if (!item.hasChildren) return

        val isExpandedNow = item.isExpanded
        if (isExpandedNow) {
            _dragCollapsedRestore.value = categoryId
            _collapsedIds.update { it + categoryId }
            // Persist اختیاری:
            viewModelScope.launch {
                val current = uiState.value.categories.firstOrNull { it.categoryId == categoryId } ?: return@launch
                categoryRepository.updateCategory(current.copy(isExtended = false))
            }
        }
    }

    fun onDragEndRestoreExpand() {
        val id = _dragCollapsedRestore.value ?: return
        _dragCollapsedRestore.value = null

        _collapsedIds.update { it - id }

        // Persist اختیاری:
        viewModelScope.launch {
            val current = uiState.value.categories.firstOrNull { it.categoryId == id } ?: return@launch
            categoryRepository.updateCategory(current.copy(isExtended = true))
        }
    }

    fun toggleExpand(categoryId: Int) {
        viewModelScope.launch {
            val current = uiState.value.categories.firstOrNull { it.categoryId == categoryId } ?: return@launch
            val willCollapse = !_collapsedIds.value.contains(categoryId)

            _collapsedIds.update { set ->
                if (willCollapse) set + categoryId else set - categoryId
            }

            // Persist
            categoryRepository.updateCategory(current.copy(isExtended = !willCollapse))
        }
    }

    fun applyDragResult(
        draggedId: Int,
        oldParentId: Int?,
        newParentId: Int?,
        currentList: List<CategoryRenderItem>
    ) {
        viewModelScope.launch {
            val dragged = uiState.value.categories.firstOrNull { it.categoryId == draggedId } ?: return@launch

            val finalNewParent = newParentId ?: dragged.parentCategoryId

            // parent جدید باید سطحش < 4 باشد
            val parentLevel = uiState.value.levelById[finalNewParent ?: return@launch] ?: 1
            if (parentLevel >= 4) return@launch

            // آپدیت parent (اگر تغییر کرده)
            if (finalNewParent != dragged.parentCategoryId) {
                categoryRepository.updateCategory(dragged.copy(parentCategoryId = finalNewParent))
            }

            suspend fun reorderFor(parentId: Int?) {
                val orderedIds = currentList
                    .asSequence()
                    .filter { it.isVisible }
                    .filter { item ->
                        val id = item.category.categoryId ?: return@filter false
                        val p = if (id == draggedId) finalNewParent else item.category.parentCategoryId
                        p == parentId
                    }
                    .mapNotNull { it.category.categoryId }
                    .toList()

                val currentEntities = uiState.value.categories.filter { it.parentCategoryId == parentId }
                val byId = currentEntities.associateBy { it.categoryId }

                orderedIds.forEachIndexed { index, id ->
                    val e = byId[id] ?: return@forEachIndexed
                    categoryRepository.updateCategory(e.copy(siblingIndex = index))
                }
            }


            reorderFor(oldParentId)
            reorderFor(finalNewParent)
        }
    }



    private fun flattenCategoryTreeWithLevelsAndVisibility(
        all: List<CategoryEntity>,
        collapsedIds: Set<Int>,
        rootParentId: Int = -1,
        maxDepth: Int = 4
    ): FlattenResult {

        val byParent = all.groupBy { it.parentCategoryId }
        val items = mutableListOf<CategoryRenderItem>()
        val levelById = mutableMapOf<Int, Int>()

        fun dfs(parentId: Int?, level: Int, ancestorCollapsed: Boolean) {
            if (level > maxDepth) return

            val children = (byParent[parentId] ?: emptyList())
                .sortedBy { it.siblingIndex }

            for (child in children) {
                val id = child.categoryId
                val hasChildrenRaw = (byParent[id] ?: emptyList()).isNotEmpty()
                val hasChildren = (level < maxDepth) && hasChildrenRaw
                val isExpanded = id != null && !collapsedIds.contains(id)
                val selfCollapsed = id != null && collapsedIds.contains(id)

                if (id != null) levelById[id] = level

                val visible = !ancestorCollapsed

                items.add(
                    CategoryRenderItem(
                        category = child,
                        level = level,
                        hasChildren = hasChildren,
                        isExpanded = isExpanded,
                        isVisible = visible
                    )
                )

                // اگر خود این نود collapse شده، بچه‌هاش invisible می‌شن (ancestorCollapsed=true)
                dfs(child.categoryId, level + 1, ancestorCollapsed = ancestorCollapsed || selfCollapsed)
            }
        }

        dfs(rootParentId, level = 1, ancestorCollapsed = false)
        return FlattenResult(items, levelById)
    }










}










data class CategoryUiState2(
    val isLoading: Boolean = true,
    val categories: List<CategoryEntity> = emptyList(),
    val renderItems: List<CategoryRenderItem> = emptyList(),
    val levelById: Map<Int, Int> = emptyMap(),

    )

data class CategoryRenderItem(
    val category: CategoryEntity,
    val level: Int,
    val hasChildren: Boolean,
    val isExpanded: Boolean,
    val isVisible: Boolean
)

data class FlattenResult(
    val items: List<CategoryRenderItem>,
    val levelById: Map<Int, Int>
)

data class CategoryDraft2(
    val name: String = "",
    val parentId: Int = 1,
    val iconName: String = "QuestionMark",
    val color: String = "#000000",
    val description: String = ""
)
