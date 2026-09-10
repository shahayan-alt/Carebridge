package com.carebridge.app.domain.usecase

import com.carebridge.app.domain.model.SettlementStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class BillCalculationUseCaseTest {

    @Test
    fun calculate_workedExampleFromReadme_returnsCorrectAmounts() {
        // From README:
        // Check-In:   09:00:00
        // Check-Out:  13:30:00 (4.5 hours)
        // Rate:       250 / hour
        // Subtotal:   4.5 * 250 = 1125.0
        // Fee:        50.0
        // Total:      1175.0
        val checkIn = 1728550800000L // 09:00:00
        val checkOut = checkIn + (4.5 * 60 * 60 * 1000).toLong() // 13:30:00
        val hourlyRate = 250.0

        val bill = BillCalculationUseCase.calculate(checkIn, checkOut, hourlyRate)

        assertEquals(4.5, bill.loggedHours, 0.001)
        assertEquals(250.0, bill.hourlyRate, 0.001)
        assertEquals(1125.0, bill.subtotal, 0.001)
        assertEquals(50.0, bill.platformFee, 0.001)
        assertEquals(1175.0, bill.totalAmount, 0.001)
        assertEquals(SettlementStatus.UNSETTLED, bill.settlementStatus)
    }

    @Test
    fun calculate_twoHoursAt300_returnsCorrectTotal() {
        val checkIn = 1000000L
        val checkOut = checkIn + (2 * 60 * 60 * 1000L) // 2 hours
        val hourlyRate = 300.0

        val bill = BillCalculationUseCase.calculate(checkIn, checkOut, hourlyRate)

        assertEquals(2.0, bill.loggedHours, 0.001)
        assertEquals(600.0, bill.subtotal, 0.001)
        assertEquals(50.0, bill.platformFee, 0.001)
        assertEquals(650.0, bill.totalAmount, 0.001)
    }
}
