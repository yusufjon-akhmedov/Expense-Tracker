package com.yusufjonaxmedov.pennywise.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.ui.graphics.vector.ImageVector
import com.yusufjonaxmedov.pennywise.core.model.TransactionType
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

@Serializable
data object DashboardRoute

@Serializable
data object TransactionsRoute

@Serializable
data object BudgetsRoute

@Serializable
data object ReportsRoute

@Serializable
data object SettingsRoute

@Serializable
data class TransactionEditorRoute(
    val transactionId: Long? = null,
    val transactionType: TransactionType = TransactionType.EXPENSE,
)

@Serializable
data object CategoriesRoute

@Serializable
data object AccountsRoute

@Serializable
data object RecurringTemplatesRoute

data class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
    val route: Any,
)

val topLevelDestinations = listOf(
    TopLevelDestination("Home", Icons.Outlined.Home, DashboardRoute),
    TopLevelDestination("Transactions", Icons.AutoMirrored.Outlined.ReceiptLong, TransactionsRoute),
    TopLevelDestination("Budgets", Icons.Outlined.Savings, BudgetsRoute),
    TopLevelDestination("Reports", Icons.Outlined.Analytics, ReportsRoute),
    TopLevelDestination("Settings", Icons.Outlined.Settings, SettingsRoute),
)
