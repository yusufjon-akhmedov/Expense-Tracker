package com.yusufjonaxmedov.pennywise.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.yusufjonaxmedov.pennywise.core.common.MoneyFormatter
import com.yusufjonaxmedov.pennywise.core.model.CategorySpend
import com.yusufjonaxmedov.pennywise.core.model.MonthlyTrendPoint

@Composable
fun CategorySpendChart(
    items: List<CategorySpend>,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val maxValue = items.maxOf { it.amountMinor }.coerceAtLeast(1L).toFloat()
    val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Spending by category", style = MaterialTheme.typography.titleMedium)
            items.take(5).forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("${item.category.emoji} ${item.category.name}")
                        Text(MoneyFormatter.format(item.amountMinor, currencyCode))
                    }
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                    ) {
                        drawRoundRect(
                            color = trackColor,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
                        )
                        drawRoundRect(
                            color = Color(android.graphics.Color.parseColor(item.category.colorHex)),
                            size = size.copy(width = size.width * (item.amountMinor / maxValue)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeExpenseTrendChart(
    items: List<MonthlyTrendPoint>,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val points = items.takeLast(6)
    val maxValue = points.maxOf { maxOf(it.incomeMinor, it.expenseMinor) }.coerceAtLeast(1L).toFloat()
    val incomeColor = MaterialTheme.colorScheme.secondary
    val expenseColor = MaterialTheme.colorScheme.error

    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Income vs expense", style = MaterialTheme.typography.titleMedium)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            ) {
                val stepX = size.width / (points.size.coerceAtLeast(2) - 1).coerceAtLeast(1)
                fun yFor(value: Long): Float = size.height - ((value / maxValue) * (size.height - 12f))

                val incomePath = Path()
                val expensePath = Path()
                points.forEachIndexed { index, point ->
                    val x = stepX * index
                    val incomeY = yFor(point.incomeMinor)
                    val expenseY = yFor(point.expenseMinor)
                    if (index == 0) {
                        incomePath.moveTo(x, incomeY)
                        expensePath.moveTo(x, expenseY)
                    } else {
                        incomePath.lineTo(x, incomeY)
                        expensePath.lineTo(x, expenseY)
                    }
                }

                drawPath(
                    path = incomePath,
                    color = incomeColor,
                    style = Stroke(width = 8f, cap = StrokeCap.Round),
                )
                drawPath(
                    path = expensePath,
                    color = expenseColor,
                    style = Stroke(width = 8f, cap = StrokeCap.Round),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                points.forEach { point ->
                    Text(
                        text = point.monthKey.takeLast(2),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(28.dp),
                    )
                }
            }
        }
    }
}
