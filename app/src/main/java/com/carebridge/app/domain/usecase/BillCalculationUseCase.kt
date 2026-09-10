package com.carebridge.app.domain.usecase

import com.carebridge.app.domain.model.BillDetails
import com.carebridge.app.domain.model.SettlementStatus
import com.carebridge.app.utils.Constants
import java.math.BigDecimal
import java.math.RoundingMode

object BillCalculationUseCase {

    /**
     * Calculates the itemized bill from check-in and check-out timestamps.
     * Subtotal = Logged Hours * Hourly Rate
     * Total = Subtotal + Flat Platform Fee (50.0)
     */
    fun calculate(
        checkInTimestamp: Long,
        checkOutTimestamp: Long,
        hourlyRate: Double,
        platformFee: Double = Constants.DEFAULT_PLATFORM_FEE
    ): BillDetails {
        val durationMillis = (checkOutTimestamp - checkInTimestamp).coerceAtLeast(0L)
        val rawHours = durationMillis.toDouble() / (1000.0 * 60 * 60)
        
        // Round hours to 2 decimal places with a minimum threshold of 0.1 hours (6 mins) for tests/short visits
        val loggedHours = BigDecimal(rawHours).setScale(2, RoundingMode.HALF_UP).toDouble().coerceAtLeast(0.01)
        val rawSubtotal = loggedHours * hourlyRate
        val subtotal = BigDecimal(rawSubtotal).setScale(2, RoundingMode.HALF_UP).toDouble()
        val total = BigDecimal(subtotal + platformFee).setScale(2, RoundingMode.HALF_UP).toDouble()

        return BillDetails(
            hourlyRate = hourlyRate,
            loggedHours = loggedHours,
            subtotal = subtotal,
            platformFee = platformFee,
            totalAmount = total,
            settlementStatus = SettlementStatus.UNSETTLED
        )
    }
}
