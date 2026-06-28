package com.example.compoundeffectV1_01.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.compoundeffectV1_01.ui.backupRestore.CompoundBackupRestoreScreen
import com.example.compoundeffectV1_01.ui.categoryScreen.CategoryScreen
import com.example.compoundeffectV1_01.ui.scheduleScreen.ScheduleScreen
import com.example.compoundeffectV1_01.ui.settingsScreen.SettingsScreen
import com.example.compoundeffectV1_01.ui.taskScreen.TaskScreen


@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = AppRoutes.CATEGORY
) {
    NavHost(
        navController = navController,
        startDestination = AppGraphRoutes.ROOT,
        modifier = modifier
    ) {
        navigation(
            route = AppGraphRoutes.ROOT,
            startDestination = startDestination
        ) {
            composable(AppRoutes.CATEGORY) {
                CategoryScreen(navController = navController)
            }
            composable(AppRoutes.SCHEDULE) {
                ScheduleScreen(navController = navController)
            }
            composable(
                route = AppRoutes.TASK_ROUTE,
                arguments = listOf(
                    navArgument(AppRoutes.ARG_CATEGORY_ID) {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument(AppRoutes.ARG_TASK_ID) {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) {
                TaskScreen(
                    onClickBack = { navController.popBackStack() },
                    onOpenTask = { taskId ->
                        navController.navigate(AppRoutes.taskEdit(taskId))
                    }
                )
            }
            composable(AppRoutes.BACKUP_RESTORE) {
                CompoundBackupRestoreScreen(
                    onClose = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.SETTINGS) {
                SettingsScreen(
                    onOpenBackupRestore = {
                        navController.navigate(AppRoutes.BACKUP_RESTORE)
                    }
                )
            }
        }
    }
}









object AppRoutes {
    const val CATEGORY = "category"
    const val SCHEDULE = "schedule"
    const val TASK = "task"

    const val ARG_CATEGORY_ID = "categoryId"
    const val ARG_TASK_ID = "taskId"
    const val BACKUP_RESTORE = "backup_restore"

    const val SETTINGS = "settings"

    // route template
    const val TASK_ROUTE = "task?$ARG_CATEGORY_ID={$ARG_CATEGORY_ID}&$ARG_TASK_ID={$ARG_TASK_ID}"

    fun taskAdd(categoryId: Int): String =
        "task?$ARG_CATEGORY_ID=$categoryId&$ARG_TASK_ID=-1"

    fun taskEdit(taskId: Int): String =
        "task?$ARG_CATEGORY_ID=-1&$ARG_TASK_ID=$taskId"
}

object AppGraphRoutes {
    const val ROOT = "root"
}






//برای آیتم‌های باتم‌بار
data class AppBottomBarDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)
val bottomBarDestinations = listOf(
    AppBottomBarDestination(
        route = AppRoutes.SCHEDULE,
        label = "Schedule",
        icon = Icons.Filled.Schedule
    ),
    AppBottomBarDestination(
        route = AppRoutes.CATEGORY,
        label = "Category",
        icon = Icons.Filled.Category
    ),
    AppBottomBarDestination(
        route = AppRoutes.SETTINGS,
        label = "Settings",
        icon = Icons.Filled.Settings
    )

)