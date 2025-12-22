package com.example.compoundeffectV1_01.ui.scheduleScreen


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.appSystemInfo.AppSystemInfoRepository
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.event.Event
import com.example.compoundeffectV1_01.data.dataBaseRoom.tables.event.EventRepository
import com.example.compoundeffectV1_01.utils.createTimeForSampleEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ScheduleScreenViewModel @Inject constructor(
    private val eventRepository: EventRepository,
) : ViewModel() {


    private val _dashboardUiState = MutableStateFlow(EventUiState())
    val dashboardUiState = _dashboardUiState.asStateFlow()

    init {
        initializeDashboardViewModel()
    }


    private fun initializeDashboardViewModel() {

        viewModelScope.launch {


            val job2 = async {

                val eventList = eventRepository.getAllEvents()
                _dashboardUiState.update { dashboardUiState ->
                    dashboardUiState.copy(
                        eventList = eventList,
                    )
                }
            }
            job2.await()
            if (job2.isCompleted) {
                val job3 = async {
                    _dashboardUiState.update { dashboardUiState ->
                        dashboardUiState.copy(
                            isDataLoaded = true
                        )
                    }
                }
                job3.await()
            }


        }
    }


    fun changeEventStartEndTimes(
        activeEventId: Int?,
        offsetY: Float,
        offsetX: Float,
        hourHeight: Float,
        changeStart: Boolean = false,
        changeEnd: Boolean = false,
        changeStartEnd: Boolean = false,
        endDrug: Boolean = false, // اضافه کردن پارامتر dragEnd
        taskPalletExpanded: Boolean = false
    ) {

        val oneMinutePx: Float = (hourHeight / 60)
        val totalMinute = ((((offsetY / oneMinutePx).toLong()) + 4) / 5) * 5


        val oldList = _dashboardUiState.value.eventList.filter { it.inSchedule }

        val activatedEvent = _dashboardUiState.value.eventList.first { it.id == activeEventId }
        if (!endDrug && _dashboardUiState.value.initialStartTime == null) {
            _dashboardUiState.update { dashboardUiState ->
                dashboardUiState.copy(
                    initialStartTime = activatedEvent.start
                )
            }
        }
        if (!endDrug && _dashboardUiState.value.initialEndTime == null) {
            _dashboardUiState.update { dashboardUiState ->
                dashboardUiState.copy(
                    initialEndTime = activatedEvent.end
                )
            }
        }

        val minimumTimeStartCanReach: LocalDateTime = LocalDateTime.of(
            activatedEvent.start.year,
            activatedEvent.start.month,
            activatedEvent.start.dayOfMonth,
            0,
            0
        )
        val maximumTimeStartCanReach: LocalDateTime = activatedEvent.end.minusMinutes(5)
        val minimumTimeEndCanReach: LocalDateTime = activatedEvent.start.plusMinutes(5)
        val maximumTimeEndCanReach: LocalDateTime = LocalDateTime.of(
            activatedEvent.end.year,
            activatedEvent.end.month,
            activatedEvent.end.dayOfMonth,
            23,
            55
        )


        if (changeStart) {

            var newStartTime =
                _dashboardUiState.value.initialStartTime?.plusMinutes(totalMinute)

            if (newStartTime != null) {
                newStartTime =
                    when {
                        newStartTime.isAfter(minimumTimeStartCanReach) && newStartTime.isBefore(
                            maximumTimeStartCanReach
                        ) ->
                            newStartTime

                        !newStartTime.isAfter(minimumTimeStartCanReach) && newStartTime.isBefore(
                            maximumTimeStartCanReach
                        ) ->
                            newStartTime.withHour(0).withMinute(0)
                                .withDayOfMonth(activatedEvent.start.dayOfMonth)

                        newStartTime.isAfter(minimumTimeStartCanReach) && !newStartTime.isBefore(
                            maximumTimeStartCanReach
                        ) ->
                            activatedEvent.end.minusMinutes(5)

                        else -> return
                    }

            }

            if (newStartTime != _dashboardUiState.value.initialStartTime) {
                val changedEvent = newStartTime?.let {
                    activatedEvent.copy(
                        start = it
                    )
                }
                if (!endDrug && _dashboardUiState.value.initialStartTime != null) {
                    val updatedList = _dashboardUiState.value.eventList.map { event ->
                        if (event.id == activeEventId && changedEvent != null) {
                            changedEvent // جایگزینی رویداد تغییر یافته
                        } else {
                            event // نگه داشتن رویداد بدون تغییر
                        }
                    }
                    _dashboardUiState.update { dashboardUiState ->
                        dashboardUiState.copy(
                            eventList = updatedList
                        )
                    }
                }
                if (endDrug) {
                    _dashboardUiState.update { dashboardUiState ->
                        dashboardUiState.copy(
                            initialStartTime = null,
                            initialEndTime = null
                        )
                    }
                    viewModelScope.launch {
                        if (changedEvent != null) {

                            eventRepository.updateEvent(changedEvent)
                        }
                    }

                }
            }

        }
        if (changeEnd) {

            var newEndTime =
                _dashboardUiState.value.initialEndTime?.plusMinutes(totalMinute)

            if (newEndTime != null) {
                newEndTime =
                    when {
                        newEndTime.isAfter(minimumTimeEndCanReach) && newEndTime.isBefore(
                            maximumTimeEndCanReach
                        ) ->
                            newEndTime

                        !newEndTime.isAfter(minimumTimeEndCanReach) && newEndTime.isBefore(
                            maximumTimeEndCanReach
                        ) ->
                            activatedEvent.start.plusMinutes(5)

                        newEndTime.isAfter(minimumTimeEndCanReach) && !newEndTime.isBefore(
                            maximumTimeEndCanReach
                        ) ->
                            newEndTime.withHour(23).withMinute(55)
                                .withDayOfMonth(activatedEvent.start.dayOfMonth)

                        else -> return
                    }

            }

            if (newEndTime != _dashboardUiState.value.initialEndTime) {
                val changedEvent = newEndTime?.let {
                    activatedEvent.copy(
                        end = it
                    )
                }
                if (!endDrug && _dashboardUiState.value.initialEndTime != null) {
                    val updatedList = _dashboardUiState.value.eventList.map { event ->
                        if (event.id == activeEventId && changedEvent != null) {
                            changedEvent // جایگزینی رویداد تغییر یافته
                        } else {
                            event // نگه داشتن رویداد بدون تغییر
                        }
                    }
                    _dashboardUiState.update { dashboardUiState ->
                        dashboardUiState.copy(
                            eventList = updatedList
                        )
                    }
                }
                if (endDrug) {
                    _dashboardUiState.update { dashboardUiState ->
                        dashboardUiState.copy(
                            initialStartTime = null,
                            initialEndTime = null
                        )
                    }
                    viewModelScope.launch {
                        if (changedEvent != null) {

                            eventRepository.updateEvent(changedEvent)
                        }
                    }

                }
            }

        }
        if (changeStartEnd) {

            var newStartTime =
                _dashboardUiState.value.initialStartTime?.plusMinutes(totalMinute)

            var newEndTime =
                _dashboardUiState.value.initialEndTime?.plusMinutes(totalMinute)

            if (newEndTime != null) {
                newEndTime =
                    when {
                        newEndTime.isBefore(maximumTimeEndCanReach) ->
                            newEndTime

                        !newEndTime.isBefore(maximumTimeEndCanReach) ->
                            newEndTime.withHour(23).withMinute(55)
                                .withDayOfMonth(activatedEvent.start.dayOfMonth)

                        else -> return
                    }
            }
            if (newStartTime != null) {
                newStartTime =
                    when {
                        newStartTime.isAfter(minimumTimeStartCanReach) ->
                            newStartTime

                        !newStartTime.isAfter(minimumTimeStartCanReach) ->
                            newStartTime.withHour(0).withMinute(0)
                                .withDayOfMonth(activatedEvent.start.dayOfMonth)

                        else -> return
                    }
            }


            if (
                newEndTime != null &&
                newStartTime != null &&
                newEndTime != _dashboardUiState.value.initialEndTime &&
                newStartTime != _dashboardUiState.value.initialStartTime
            ) {

                val changedEvent =
                    activatedEvent.copy(
                        end = newEndTime,
                        start = newStartTime,

                        )


                if (!endDrug && _dashboardUiState.value.initialStartTime != null && _dashboardUiState.value.initialEndTime != null) {
                    val updatedList = _dashboardUiState.value.eventList.map { event ->
                        if (event.id == activeEventId) {
                            changedEvent // جایگزینی رویداد تغییر یافته
                        } else {
                            event // نگه داشتن رویداد بدون تغییر
                        }
                    }
                    _dashboardUiState.update { dashboardUiState ->
                        dashboardUiState.copy(
                            eventList = updatedList
                        )
                    }
                }
                if (endDrug) {
                    _dashboardUiState.update { dashboardUiState ->
                        dashboardUiState.copy(
                            initialStartTime = null,
                            initialEndTime = null
                        )
                    }
                    viewModelScope.launch {
                        eventRepository.updateEvent(changedEvent)

                    }

                }
            }

        }

        if (offsetX > 200) {                     //movingEventToRight


            if (taskPalletExpanded) {
                if (!endDrug) {

                    if (offsetX > 210) {
                        val changedEvent =
                            activatedEvent.copy(
                                inSchedule = true,
                                inPallet = true
                            )

                        val updatedList = _dashboardUiState.value.eventList.map { event ->
                            if (event.id == activeEventId) {
                                changedEvent // جایگزینی رویداد تغییر یافته
                            } else {
                                event // نگه داشتن رویداد بدون تغییر
                            }
                        }
                        Log.i(
                            "TEST",
                            " _dashboardUiState.value.eventList=${_dashboardUiState.value.eventList}"
                        )
                        _dashboardUiState.update { dashboardUiState ->
                            dashboardUiState.copy(
                                eventList = updatedList
                            )
                        }
                    } else {
                        val changedEvent =
                            activatedEvent.copy(
                                inSchedule = true,
                                inPallet = false
                            )

                        val updatedList = _dashboardUiState.value.eventList.map { event ->
                            if (event.id == activeEventId) {
                                changedEvent // جایگزینی رویداد تغییر یافته
                            } else {
                                event // نگه داشتن رویداد بدون تغییر
                            }
                        }
                        _dashboardUiState.update { dashboardUiState ->
                            dashboardUiState.copy(
                                eventList = updatedList
                            )
                        }
                    }
                }
                if (endDrug) {
                    if (offsetX > 210) {
                        val changedEvent =
                            activatedEvent.copy(
                                inSchedule = false,
                                inPallet = true
                            )

                        val updatedList = _dashboardUiState.value.eventList.map { event ->
                            if (event.id == activeEventId) {
                                changedEvent // جایگزینی رویداد تغییر یافته
                            } else {
                                event // نگه داشتن رویداد بدون تغییر
                            }
                        }
                        _dashboardUiState.update { dashboardUiState ->
                            dashboardUiState.copy(
                                eventList = updatedList
                            )
                        }
                        viewModelScope.launch {
                            eventRepository.updateEvent(changedEvent)

                        }

                    } else {
                        val changedEvent =
                            activatedEvent.copy(
                                inSchedule = true,
                                inPallet = false
                            )

                        val updatedList = _dashboardUiState.value.eventList.map { event ->
                            if (event.id == activeEventId) {
                                changedEvent // جایگزینی رویداد تغییر یافته
                            } else {
                                event // نگه داشتن رویداد بدون تغییر
                            }
                        }
                        _dashboardUiState.update { dashboardUiState ->
                            dashboardUiState.copy(
                                eventList = updatedList
                            )
                        }
                    }
                }


            }
            if (!taskPalletExpanded) {


                val newStartTime =
                    _dashboardUiState.value.initialStartTime?.plusDays(1)

                val newEndTime =
                    _dashboardUiState.value.initialEndTime?.plusDays(1)

                if (
                    newEndTime != null &&
                    newStartTime != null
                ) {
                    val changedEvent =
                        activatedEvent.copy(
                            end = newEndTime,
                            start = newStartTime,

                            )
                    if (!endDrug && _dashboardUiState.value.initialStartTime != null && _dashboardUiState.value.initialEndTime != null) {
                        val updatedList = _dashboardUiState.value.eventList.map { event ->
                            if (event.id == activeEventId) {
                                changedEvent // جایگزینی رویداد تغییر یافته
                            } else {
                                event // نگه داشتن رویداد بدون تغییر
                            }
                        }
                        _dashboardUiState.update { dashboardUiState ->
                            dashboardUiState.copy(
                                eventList = updatedList
                            )
                        }

                    }
                    if (endDrug) {
                        _dashboardUiState.update { dashboardUiState ->
                            dashboardUiState.copy(
                                initialStartTime = null,
                                initialEndTime = null
                            )
                        }
                        viewModelScope.launch {
                            eventRepository.updateEvent(changedEvent)

                        }

                    }

                }
            }


        }
        if (offsetX < -200) {                        //movingEventToLeft


            var newStartTime =
                _dashboardUiState.value.initialStartTime?.minusDays(1)

            var newEndTime =
                _dashboardUiState.value.initialEndTime?.minusDays(1)



            if (newStartTime != null && newEndTime != null) {
                val currentTime: Calendar = Calendar.getInstance()
                val minimumTimeEventCanMove = createTimeForSampleEvents(currentTime, 0, 0, 0)

                newStartTime =
                    when {
                        newStartTime.isAfter(minimumTimeEventCanMove) ->
                            newStartTime

                        !newStartTime.isAfter(minimumTimeEventCanMove) ->
                            activatedEvent.start

                        else -> return
                    }
                newEndTime =
                    when {
                        newEndTime.isAfter(minimumTimeEventCanMove) ->
                            newEndTime

                        !newEndTime.isAfter(minimumTimeEventCanMove) ->
                            activatedEvent.end

                        else -> return
                    }
            }


            if (
                newEndTime != null &&
                newStartTime != null
            ) {
                val changedEvent =
                    activatedEvent.copy(
                        end = newEndTime,
                        start = newStartTime,

                        )


                if (!endDrug && _dashboardUiState.value.initialStartTime != null && _dashboardUiState.value.initialEndTime != null) {
                    val updatedList = _dashboardUiState.value.eventList.map { event ->
                        if (event.id == activeEventId) {
                            changedEvent // جایگزینی رویداد تغییر یافته
                        } else {
                            event // نگه داشتن رویداد بدون تغییر
                        }
                    }
                    _dashboardUiState.update { dashboardUiState ->
                        dashboardUiState.copy(
                            eventList = updatedList
                        )
                    }

                }
                if (endDrug) {

                    _dashboardUiState.update { dashboardUiState ->
                        dashboardUiState.copy(
                            initialStartTime = null,
                            initialEndTime = null
                        )
                    }
                    viewModelScope.launch {
                        eventRepository.updateEvent(changedEvent)

                    }
                }
            }


        }


        Log.i("TEST", "---------------")
    }


    fun onEventDrugFromPalletToSchedule(
        activeEventId: Int?,
        offsetX: Float,
        distanceYFromTopOfSideBar: Float,
        hourHeight: Float,
        visibleColumnDayOffset: Int,
        endDrug: Boolean = false,
    ) {
        val oneMinutePx: Float = (hourHeight / 60)
        val offsetXMargin = -200

        val eventDrugged = _dashboardUiState.value.eventList.first { it.id == activeEventId }
        val eventDuration = Duration.between(eventDrugged.start, eventDrugged.end).toMinutes()

        val oldList = _dashboardUiState.value.eventList

        val pointerTime = distanceYFromTopOfSideBar / oneMinutePx

        val newEventStartMinute = ((pointerTime.toLong() - (eventDuration / 2) + 4) / 5) * 5

        val currentTime: Calendar = Calendar.getInstance()
        val destinationTime = currentTime.clone() as Calendar // کپی از currentTime
        destinationTime.add(Calendar.DAY_OF_MONTH, visibleColumnDayOffset)


        val minimumTimeStartCanReach: LocalDateTime = LocalDateTime.of(
            currentTime.get(Calendar.YEAR),
            currentTime.get(Calendar.MONTH)+ 1,
            currentTime.get(Calendar.DAY_OF_MONTH),
            0,
            0
        )

        var newEventStart = createTimeForSampleEvents(destinationTime, 0, 0, 0).plusMinutes(
            newEventStartMinute
        )
        val newEventEnd = newEventStart.plusMinutes(eventDuration)

        if (newEventStart != null) {
            newEventStart =
                when {
                    newEventStart.isAfter(minimumTimeStartCanReach) ->
                        newEventStart

                    !newEventStart.isAfter(minimumTimeStartCanReach) ->
                        newEventStart.withHour(0).withMinute(0)
                            .withDayOfMonth(eventDrugged.start.dayOfMonth)

                    else -> return
                }
        }





        if (!endDrug) {
            if (offsetX < offsetXMargin) {
                val changedEvent =
                    eventDrugged.copy(
                        start = newEventStart,
                        end = newEventEnd,
                        inSchedule = true,
                        inPallet = true
                    )

                val updatedList = oldList.map { event ->
                    if (event.id == activeEventId) {
                        changedEvent // جایگزینی رویداد تغییر یافته
                    } else {
                        event // نگه داشتن رویداد بدون تغییر
                    }
                }
                _dashboardUiState.update { dashboardUiState ->
                    dashboardUiState.copy(
                        eventList = updatedList
                    )
                }
            } else {
                val changedEvent =
                    eventDrugged.copy(
                        start = newEventStart,
                        end = newEventEnd,
                        inSchedule = false,
                        inPallet = true
                    )

                val updatedList = oldList.map { event ->
                    if (event.id == activeEventId) {
                        changedEvent // جایگزینی رویداد تغییر یافته
                    } else {
                        event // نگه داشتن رویداد بدون تغییر
                    }
                }

                _dashboardUiState.update { dashboardUiState ->
                    dashboardUiState.copy(
                        eventList = updatedList
                    )
                }
            }
        }
        if (endDrug) {
            if (offsetX < offsetXMargin) {
                val changedEvent =
                    eventDrugged.copy(
                        inPallet = false
                    )


                _dashboardUiState.update { dashboardUiState ->

                    val updatedList = oldList.map { event ->
                        if (event.id == activeEventId) {
                            changedEvent // جایگزینی رویداد تغییر یافته
                        } else {
                            event // نگه داشتن رویداد بدون تغییر
                        }
                    }

                    dashboardUiState.copy(
                        eventList = updatedList
                    )
                }
                viewModelScope.launch {
                    eventRepository.updateEvent(changedEvent)

                }

            } else {
                val changedEvent =
                    eventDrugged.copy(
                        inPallet = true
                    )

                val updatedList = oldList.map { event ->
                    if (event.id == activeEventId) {
                        changedEvent // جایگزینی رویداد تغییر یافته
                    } else {
                        event // نگه داشتن رویداد بدون تغییر
                    }
                }
                _dashboardUiState.update { dashboardUiState ->
                    dashboardUiState.copy(
                        eventList = updatedList
                    )
                }
            }
        }


    }


}

// Data class for UI

data class EventUiState(
    val eventList: List<Event> = listOf(),
    var initialStartTime: LocalDateTime? = null,
    var initialEndTime: LocalDateTime? = null,
    val isDataLoaded: Boolean = false

)



