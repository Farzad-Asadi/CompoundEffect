package com.example.compoundeffectV1_01.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.compoundeffectV1_01.ui.categoryScreen.CategoryScreen
import com.example.compoundeffectV1_01.ui.scheduleScreen.ScheduleScreen2


@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = AppRoutes.SCHEDULE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppRoutes.CATEGORY) {
            CategoryScreen(
            )
        }
        composable(AppRoutes.SCHEDULE) {
            ScheduleScreen2(

            )
        }
    }
}








object AppRoutes {
    const val CATEGORY = "category"
    const val SCHEDULE = "schedule"
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

)