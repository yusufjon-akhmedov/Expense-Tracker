package com.yusufjonaxmedov.pennywise.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.yusufjonaxmedov.pennywise.feature.accounts.AccountsScreen
import com.yusufjonaxmedov.pennywise.feature.budgets.BudgetsScreen
import com.yusufjonaxmedov.pennywise.feature.categories.CategoriesScreen
import com.yusufjonaxmedov.pennywise.feature.dashboard.DashboardScreen
import com.yusufjonaxmedov.pennywise.feature.onboarding.OnboardingScreen
import com.yusufjonaxmedov.pennywise.feature.recurring.RecurringTemplatesScreen
import com.yusufjonaxmedov.pennywise.feature.reports.ReportsScreen
import com.yusufjonaxmedov.pennywise.feature.settings.SettingsScreen
import com.yusufjonaxmedov.pennywise.feature.transactions.TransactionEditorScreen
import com.yusufjonaxmedov.pennywise.feature.transactions.TransactionsScreen

@Composable
fun PennywiseNavHost(
    navController: NavHostController,
    onboardingCompleted: Boolean,
    onAppReady: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val destination = navBackStackEntry?.destination
    val showBottomBar = destination?.shouldShowBottomBar() == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { topLevelDestination ->
                        val selected = destination.isTopLevelSelected(topLevelDestination.route)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(topLevelDestination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(topLevelDestination.icon, contentDescription = topLevelDestination.label) },
                            label = { Text(topLevelDestination.label) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingCompleted) DashboardRoute else OnboardingRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<OnboardingRoute> {
                OnboardingScreen(
                    onFinished = {
                        onAppReady()
                        navController.navigate(DashboardRoute) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    },
                )
            }
            composable<DashboardRoute> {
                DashboardScreen(
                    onAddExpense = { navController.navigate(TransactionEditorRoute(transactionType = com.yusufjonaxmedov.pennywise.core.model.TransactionType.EXPENSE)) },
                    onAddIncome = { navController.navigate(TransactionEditorRoute(transactionType = com.yusufjonaxmedov.pennywise.core.model.TransactionType.INCOME)) },
                    onOpenBudgets = { navController.navigate(BudgetsRoute) },
                    onOpenReports = { navController.navigate(ReportsRoute) },
                    onOpenTransactions = { navController.navigate(TransactionsRoute) },
                    onOpenRecurring = { navController.navigate(RecurringTemplatesRoute) },
                )
            }
            composable<TransactionsRoute> {
                TransactionsScreen(
                    onAddExpense = { navController.navigate(TransactionEditorRoute(transactionType = com.yusufjonaxmedov.pennywise.core.model.TransactionType.EXPENSE)) },
                    onAddIncome = { navController.navigate(TransactionEditorRoute(transactionType = com.yusufjonaxmedov.pennywise.core.model.TransactionType.INCOME)) },
                    onEditTransaction = { navController.navigate(TransactionEditorRoute(transactionId = it)) },
                    onManageRecurring = { navController.navigate(RecurringTemplatesRoute) },
                )
            }
            composable<TransactionEditorRoute> {
                TransactionEditorScreen(onDone = { navController.popBackStack() })
            }
            composable<BudgetsRoute> {
                BudgetsScreen()
            }
            composable<ReportsRoute> {
                ReportsScreen()
            }
            composable<SettingsRoute> {
                SettingsScreen(
                    onManageCategories = { navController.navigate(CategoriesRoute) },
                    onManageAccounts = { navController.navigate(AccountsRoute) },
                    onManageRecurring = { navController.navigate(RecurringTemplatesRoute) },
                )
            }
            composable<CategoriesRoute> {
                CategoriesScreen(onBack = { navController.popBackStack() })
            }
            composable<AccountsRoute> {
                AccountsScreen(onBack = { navController.popBackStack() })
            }
            composable<RecurringTemplatesRoute> {
                RecurringTemplatesScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun NavDestination?.shouldShowBottomBar(): Boolean = this?.hierarchy?.any { destination ->
    destination.matchesRoute(DashboardRoute::class.qualifiedName) ||
        destination.matchesRoute(TransactionsRoute::class.qualifiedName) ||
        destination.matchesRoute(BudgetsRoute::class.qualifiedName) ||
        destination.matchesRoute(ReportsRoute::class.qualifiedName) ||
        destination.matchesRoute(SettingsRoute::class.qualifiedName)
} == true

private fun NavDestination?.isTopLevelSelected(route: Any): Boolean = this?.hierarchy?.any { destination ->
    when (route) {
        DashboardRoute -> destination.matchesRoute(DashboardRoute::class.qualifiedName)
        TransactionsRoute -> destination.matchesRoute(TransactionsRoute::class.qualifiedName)
        BudgetsRoute -> destination.matchesRoute(BudgetsRoute::class.qualifiedName)
        ReportsRoute -> destination.matchesRoute(ReportsRoute::class.qualifiedName)
        SettingsRoute -> destination.matchesRoute(SettingsRoute::class.qualifiedName)
        else -> false
    }
} == true

private fun NavDestination.matchesRoute(routeName: String?): Boolean = routeName != null && route?.contains(routeName) == true
