package com.reuel.callsort.domain.usecase

import com.reuel.callsort.data.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class SyncResult {
    object Loading : SyncResult()
    object Success : SyncResult()
    data class Error(val exception: Throwable) : SyncResult()
}

class SyncContactsUseCase(
    private val repository: ContactRepository
) {
    operator fun invoke(): Flow<SyncResult> = flow {
        emit(SyncResult.Loading)
        try {
            repository.syncDeviceContacts()
            emit(SyncResult.Success)
        } catch (e: Exception) {
            emit(SyncResult.Error(e))
        }
    }
}
