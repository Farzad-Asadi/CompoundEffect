package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.event


import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {


    override suspend fun insertEvent(vararg events: Event) =
        eventDao.insertEvent(*events)

    override suspend fun updateEvent(event: Event) =
        eventDao.updateEvent(event)

    override suspend fun getAllEvents(): List<Event> =
        eventDao.getAllEvents()

    override suspend fun changeEventStartTimeDuration(
        eventId: Int,
        changedStartDuration: LocalDateTime
    ) =
        eventDao.changeEventStartTimeDuration(eventId,changedStartDuration)

    override suspend fun changeSelectedEvent(eventId: Int, selected: Boolean) {
        eventDao.changeSelectedEvent(eventId, selected)
    }

    override suspend fun eventChangeHandling(eventId: Int, eventChangeState: Boolean) =
        eventDao.eventChangeHandling(eventId, eventChangeState)

    override fun getAllEventStream(): Flow<List<Event>> =
        eventDao.getAllEventsStream()

    //    override fun getAllAirportList(): Flow<List<Task>> =
//        taskDao.getAllAirportList()
//
//    override fun selectAirportByNameOrCode(searchedText: String): Flow<List<Task>> =
//        taskDao.selectAirportByNameOrCode(searchedText)
//
//    override fun selectAirportByIataCode(iataCode: String): Flow<Task> =
//        taskDao.selectAirportByIataCode(iataCode)
//
//    override fun getAllAirportListExceptOne(iataCode: String): Flow<List<Task>> =
//        taskDao.getAllAirportListExceptOne(iataCode)


    //    override fun getAllFavoriteList(): Flow<List<Favorite>> =
//        favoriteDao.getAllFavoriteList()
//
//    override fun findFavoriteBy(
//        selectedAirportIataCode: String,
//        arriveAirportIataCode: String
//    ): Flow<Favorite> =
//        favoriteDao.findFavoriteBy(selectedAirportIataCode,arriveAirportIataCode)
//
//    override suspend fun insertFavorite(favorite: Favorite) =
//        favoriteDao.insert(favorite)
//
//    override suspend fun deleteFavorite(departureCode :String,destinationCode :String) =
//        favoriteDao.deleteFavorite(departureCode,destinationCode)
}