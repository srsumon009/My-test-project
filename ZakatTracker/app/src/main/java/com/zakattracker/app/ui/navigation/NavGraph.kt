package com.zakattracker.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zakattracker.app.ui.categories.CategoriesScreen
import com.zakattracker.app.ui.changepin.ChangePinScreen
import com.zakattracker.app.ui.dashboard.DashboardScreen
import com.zakattracker.app.ui.payments.PaymentEditScreen
import com.zakattracker.app.ui.payments.PaymentsScreen
import com.zakattracker.app.ui.reports.ReportsScreen
import com.zakattracker.app.ui.settings.SettingsScreen
import com.zakattracker.app.ui.years.ZakatYearEditScreen
import com.zakattracker.app.ui.years.ZakatYearsScreen

/** The main, unlocked part of the app: a NavHost with a bottom navigation bar that is only
 * shown on the five top-level tabs, hidden on edit/detail screens. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            if (currentRoute in Screen.bottomNavRoutes) {
                val title = if (currentRoute == Screen.Dashboard.route) {
                    "Zakat Tracker"
                } else {
                    bottomNavItems.firstOrNull { it.screen.route == currentRoute }?.label ?: "Zakat Tracker"
                }
                TopAppBar(title = { Text(title) })
            }
        },
        bottomBar = {
            if (currentRoute in Screen.bottomNavRoutes) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onAddPayment = { yearId -> navController.navigate(Screen.PaymentEdit.createRoute(yearId)) },
                    onCreateFirstYear = { navController.navigate(Screen.YearEdit.createRoute()) },
                    onViewAllPayments = { yearId -> navController.navigate(Screen.Payments.createRoute(yearId)) }
                )
            }

            composable(Screen.Years.route) {
                ZakatYearsScreen(
                    onAddYear = { navController.navigate(Screen.YearEdit.createRoute()) },
                    onEditYear = { yearId -> navController.navigate(Screen.YearEdit.createRoute(yearId)) },
                    onOpenPayments = { yearId -> navController.navigate(Screen.Payments.createRoute(yearId)) }
                )
            }

            composable(
                route = Screen.YearEdit.route,
                arguments = listOf(navArgument("yearId") { type = NavType.LongType; defaultValue = -1L })
            ) { entry ->
                val yearIdArg = entry.arguments?.getLong("yearId") ?: -1L
                ZakatYearEditScreen(
                    yearId = if (yearIdArg > 0) yearIdArg else null,
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Payments.route,
                arguments = listOf(navArgument("yearId") { type = NavType.LongType })
            ) { entry ->
                val yearId = entry.arguments?.getLong("yearId") ?: 0L
                PaymentsScreen(
                    yearId = yearId,
                    onBack = { navController.popBackStack() },
                    onAddPayment = { navController.navigate(Screen.PaymentEdit.createRoute(yearId)) },
                    onEditPayment = { paymentId -> navController.navigate(Screen.PaymentEdit.createRoute(yearId, paymentId)) }
                )
            }

            composable(
                route = Screen.PaymentEdit.route,
                arguments = listOf(
                    navArgument("yearId") { type = NavType.LongType },
                    navArgument("paymentId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { entry ->
                val yearId = entry.arguments?.getLong("yearId") ?: 0L
                val paymentIdArg = entry.arguments?.getLong("paymentId") ?: -1L
                PaymentEditScreen(
                    yearId = yearId,
                    paymentId = if (paymentIdArg > 0) paymentIdArg else null,
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Categories.route) { CategoriesScreen() }

            composable(Screen.Reports.route) { ReportsScreen() }

            composable(Screen.Settings.route) {
                SettingsScreen(onChangePin = { navController.navigate(Screen.ChangePin.createRoute(false)) })
            }

            composable(
                route = Screen.ChangePin.route,
                arguments = listOf(navArgument("forced") { type = NavType.BoolType; defaultValue = false })
            ) { entry ->
                val forced = entry.arguments?.getBoolean("forced") ?: false
                ChangePinScreen(
                    forced = forced,
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}
