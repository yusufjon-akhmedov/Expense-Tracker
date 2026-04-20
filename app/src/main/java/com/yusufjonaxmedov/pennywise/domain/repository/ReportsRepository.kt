package com.yusufjonaxmedov.pennywise.domain.repository

import com.yusufjonaxmedov.pennywise.core.model.DateRange
import com.yusufjonaxmedov.pennywise.core.model.ReportSnapshot
import kotlinx.coroutines.flow.Flow

interface ReportsRepository {
    fun observeReport(range: DateRange): Flow<ReportSnapshot>
}
