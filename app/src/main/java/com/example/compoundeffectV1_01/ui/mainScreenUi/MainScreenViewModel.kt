package com.example.compoundeffectV1_01.ui.mainScreenUi

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.appSystemInfo.AppSystemInfoRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.event.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val appSystemInfoRepository: AppSystemInfoRepository
) : ViewModel() {


    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState = _mainUiState.asStateFlow()

    init {
        initializeMainScreenViewModel()
    }


    private fun initializeMainScreenViewModel() {

        viewModelScope.launch {
            val job1 = async {

                val itemsForFloatingActionButtonInSchedule = listOf(
                    ItemsForFloatingActionButton(
                        name = null,
                        imageVector1 = Icons.Filled.AddBox,
                        imageVector2 = null
                    ),
                    ItemsForFloatingActionButton(
                        name = null,
                        imageVector1 = Icons.Filled.AddBox,
                        imageVector2 = null
                    ),
                    ItemsForFloatingActionButton(
                        name = null,
                        imageVector1 = Icons.Filled.AddBox,
                        imageVector2 = null
                    )
                )
                val itemsForFloatingActionButtonInCategory = listOf(

                    ItemsForFloatingActionButton(
                        name = "گروه بندی",
                        imageVector1 = Icons.Filled.LibraryAdd,
                        imageVector2 = null
                    )
                )
                _mainUiState.update { mainUiState ->
                    mainUiState.copy(
                        itemsForFloatingActionButtonInSchedule = itemsForFloatingActionButtonInSchedule,
                        itemsForFloatingActionButtonInCategory = itemsForFloatingActionButtonInCategory
                    )
                }
            }
            job1.await()

            val job2 = async {

                val eventList = eventRepository.getAllEvents()
                _mainUiState.update { mainUiState ->
                    mainUiState.copy(
                    )
                }
            }
            job2.await()

            val job3 = async {
                _mainUiState.update { mainUiState ->
                    mainUiState.copy(
                        isDataLoaded = true
                    )
                }
            }
            job3.await()


        }
    }


}

// Data class for UI

data class MainUiState(
    val isDataLoaded: Boolean = false,
    val itemsForFloatingActionButtonInSchedule: List<ItemsForFloatingActionButton> = listOf(),
    val itemsForFloatingActionButtonInCategory: List<ItemsForFloatingActionButton> = listOf()

)

data class ItemsForFloatingActionButton(
    val name: String? = null,
    val imageVector1: ImageVector,
    val imageVector2: ImageVector? = null,

    )