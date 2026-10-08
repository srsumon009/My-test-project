package com.zakattracker.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Centralised route definitions so every NavController.navigate() call stays type-checked
 * against a single source of truth instead of hand-typed strings scattered around the UI. */
sealed class Screen(val route: String) {
    object PinLock : Screen("pin_lock")

    object ChangePin : Screen("change_pin/{forced}") {
        fun createRoute(forced: Boolean) = "change_pin/$forced"
    }

    object Dashboard : Screen("dashboard")
    object Years : Screen("years")

    object YearEdit : Screen("year_edit?yearId={yearId}") {
        fun createRoute(yearId: Long? = null) = "year_edit?yearId=${yearId ?: -1L}"
    }

    object Payments : Screen("payments/{yearId}") {
        fun createRoute(yearId: Long) = "payments/$yearId"
    }

    object PaymentEdit : Screen("payment_edit/{yearId}?paymentId={paymentId}") {
        fun createRoute(yearId: Long, paymentId: Long? = null) =
            "payment_edit/$yearId?paymentId=${paymentId ?: -1L}"
    }

    object Categories : Screen("categories")
    object Reports : Screen("reports")
    object Settings : Screen("settings")

    companion object {
        /** Routes that show the bottom navigation bar. */
        val bottomNavRoutes = listOf(Dashboard.route, Years.route, Categories.route, Reports.route, Settings.route)
    }
}

data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Home", Icons.Filled.Home),
    BottomNavItem(Screen.Years, "Years", Icons.Filled.CalendarMonth),
    BottomNavItem(Screen.Categories, "Categories", Icons.Filled.Category),
    BottomNavItem(Screen.Reports, "Reports", Icons.Filled.Assessment),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings)
)
