package com.carebridge.app.domain.model

data class BillDetails(
    val hourlyRate: Double = 0.0,
    val loggedHours: Double = 0.0,
    val subtotal: Double = 0.0,
    val platformFee: Double = 50.0,
    val totalAmount: Double = 0.0,
    val settlementStatus: SettlementStatus = SettlementStatus.UNSETTLED
)
