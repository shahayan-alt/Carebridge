package com.carebridge.app.di

import com.carebridge.app.data.repository.AdminRepositoryImpl
import com.carebridge.app.data.repository.AuthRepositoryImpl
import com.carebridge.app.data.repository.BookingRepositoryImpl
import com.carebridge.app.data.repository.CaregiverRepositoryImpl
import com.carebridge.app.data.repository.ReviewRepositoryImpl
import com.carebridge.app.domain.repository.AdminRepository
import com.carebridge.app.domain.repository.AuthRepository
import com.carebridge.app.domain.repository.BookingRepository
import com.carebridge.app.domain.repository.CaregiverRepository
import com.carebridge.app.domain.repository.ReviewRepository
import com.carebridge.app.domain.usecase.CheckInCaregiverUseCase
import com.carebridge.app.domain.usecase.CheckOutCaregiverUseCase
import com.carebridge.app.domain.usecase.CreateBookingUseCase
import com.carebridge.app.domain.usecase.GetCurrentUserUseCase
import com.carebridge.app.domain.usecase.GetCaregiverProfileUseCase
import com.carebridge.app.domain.usecase.LoginUseCase
import com.carebridge.app.domain.usecase.LogoutUseCase
import com.carebridge.app.domain.usecase.ObserveAllUsersUseCase
import com.carebridge.app.domain.usecase.ObserveCaregiverBookingsUseCase
import com.carebridge.app.domain.usecase.ObserveCaregiverReviewsUseCase
import com.carebridge.app.domain.usecase.ObserveFamilyBookingsUseCase
import com.carebridge.app.domain.usecase.ObservePendingCaregiversUseCase
import com.carebridge.app.domain.usecase.RegisterUseCase
import com.carebridge.app.domain.usecase.SearchCaregiversUseCase
import com.carebridge.app.domain.usecase.SubmitReviewUseCase
import com.carebridge.app.domain.usecase.UpdateBookingStatusUseCase
import com.carebridge.app.domain.usecase.UpdateCaregiverProfileUseCase
import com.carebridge.app.domain.usecase.UpdateCaregiverVerificationUseCase
import com.carebridge.app.domain.usecase.UpdateSettlementStatusUseCase
import com.carebridge.app.domain.usecase.UpdateUserActiveStateUseCase
import com.carebridge.app.domain.usecase.UploadKycDocumentUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object ServiceLocator {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(firebaseAuth, firestore)
    }

    val caregiverRepository: CaregiverRepository by lazy {
        CaregiverRepositoryImpl(firestore, storage)
    }

    val bookingRepository: BookingRepository by lazy {
        BookingRepositoryImpl(firestore)
    }

    val reviewRepository: ReviewRepository by lazy {
        ReviewRepositoryImpl(firestore)
    }

    val adminRepository: AdminRepository by lazy {
        AdminRepositoryImpl(firestore)
    }

    // Auth Use Cases
    val loginUseCase by lazy { LoginUseCase(authRepository) }
    val registerUseCase by lazy { RegisterUseCase(authRepository) }
    val getCurrentUserUseCase by lazy { GetCurrentUserUseCase(authRepository) }
    val logoutUseCase by lazy { LogoutUseCase(authRepository) }

    // Caregiver Use Cases
    val getCaregiverProfileUseCase by lazy { GetCaregiverProfileUseCase(caregiverRepository) }
    val updateCaregiverProfileUseCase by lazy { UpdateCaregiverProfileUseCase(caregiverRepository) }
    val searchCaregiversUseCase by lazy { SearchCaregiversUseCase(caregiverRepository) }
    val uploadKycDocumentUseCase by lazy { UploadKycDocumentUseCase(caregiverRepository) }

    // Booking Use Cases
    val createBookingUseCase by lazy { CreateBookingUseCase(bookingRepository) }
    val observeFamilyBookingsUseCase by lazy { ObserveFamilyBookingsUseCase(bookingRepository) }
    val observeCaregiverBookingsUseCase by lazy { ObserveCaregiverBookingsUseCase(bookingRepository) }
    val updateBookingStatusUseCase by lazy { UpdateBookingStatusUseCase(bookingRepository) }
    val checkInCaregiverUseCase by lazy { CheckInCaregiverUseCase(bookingRepository) }
    val checkOutCaregiverUseCase by lazy { CheckOutCaregiverUseCase(bookingRepository) }
    val updateSettlementStatusUseCase by lazy { UpdateSettlementStatusUseCase(bookingRepository) }

    // Review Use Cases
    val submitReviewUseCase by lazy { SubmitReviewUseCase(reviewRepository) }
    val observeCaregiverReviewsUseCase by lazy { ObserveCaregiverReviewsUseCase(reviewRepository) }

    // Admin Use Cases
    val observePendingCaregiversUseCase by lazy { ObservePendingCaregiversUseCase(adminRepository) }
    val observeAllUsersUseCase by lazy { ObserveAllUsersUseCase(adminRepository) }
    val updateCaregiverVerificationUseCase by lazy { UpdateCaregiverVerificationUseCase(adminRepository) }
    val updateUserActiveStateUseCase by lazy { UpdateUserActiveStateUseCase(adminRepository) }
}
