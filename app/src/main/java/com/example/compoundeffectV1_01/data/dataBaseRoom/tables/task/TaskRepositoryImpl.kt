package com.example.compoundeffectV1_01.data.dataBaseRoom.tables.task


import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override suspend fun insertTask(vararg task: Task) =
        taskDao.insertTask(*task)

    override suspend fun insertTaskAndReturnId(task: Task): Int =
        taskDao.insertTaskAndReturnId(task).toInt()

    override suspend fun updateTask(task: Task) =
        taskDao.updateTask(task)

    override suspend fun deleteTask(task: Task) =
        taskDao.deleteTask(task)

    override fun observeTasksByCategory(categoryId: Int): Flow<List<Task>> =
        taskDao.observeTasksByCategory(categoryId)

    override suspend fun getTasksByCategory(categoryId: Int): List<Task> =
        taskDao.getTasksByCategory(categoryId)

    override suspend fun updateSiblingIndex(id: Int, siblingIndex: Int) =
        taskDao.updateSiblingIndex(id, siblingIndex)

    override suspend fun updateTaskParent(id: Int, parentTaskId: Int?) =
        taskDao.updateTaskParent(id, parentTaskId)

    override suspend fun getTaskById(id: Int): Task? =
        taskDao.getTaskById(id)

    override fun observeTasksWithScheduleByCategory(categoryId: Int): Flow<List<TaskWithSchedule>> =
        taskDao.observeTasksWithScheduleByCategory(categoryId)

    override fun observeScheduledCountByCategory(categoryId: Int): Flow<Int> =
        taskDao.observeScheduledCountByCategory(categoryId)

    override fun observeAllScheduledTasksWithSchedule(): Flow<List<TaskWithSchedule>> =
        taskDao.observeAllScheduledTasksWithSchedule()

    override suspend fun getTasksByCategoryOrdered(categoryId: Int): List<Task> =
        taskDao.getTasksByCategoryOrdered(categoryId)


    override suspend fun countChildren(taskId: Int): Int =
        taskDao.countChildren(taskId)


    override suspend fun setCompletedForIds(ids: List<Int>, done: Boolean) {
        if (ids.isEmpty()) return
        taskDao.setCompletedForIds(ids, done)
    }

    override suspend fun getSiblings(categoryId: Int, parentId: Int): List<Task> =
        taskDao.getSiblings(categoryId, parentId)

    override suspend fun shiftSiblingsDown(categoryId: Int, parentId: Int) =
        taskDao.shiftSiblingsDown(categoryId, parentId)

    override suspend fun normalizeNullParentsToRoot() =
        taskDao.normalizeNullParentsToRoot()

    override suspend fun completeAllInCategory(categoryId: Int) =
        taskDao.completeAllInCategory(categoryId)

    override suspend fun uncompleteAllInCategory(categoryId: Int) =
        taskDao.uncompleteAllInCategory(categoryId)

    override suspend fun deleteCompletedInCategory(categoryId: Int) =
        taskDao.deleteCompletedInCategory(categoryId)

    override suspend fun deleteAllInCategory(categoryId: Int) =
        taskDao.deleteAllInCategory(categoryId)

    override suspend fun incrementPomodoroDoneUnits(taskId: Int) =
        taskDao.incrementPomodoroDoneUnits(taskId)


}