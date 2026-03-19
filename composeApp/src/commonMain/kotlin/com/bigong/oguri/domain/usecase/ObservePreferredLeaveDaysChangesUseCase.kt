package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.PreferredLeaveDaysChange
import com.bigong.oguri.domain.repository.MyPageRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
class ObservePreferredLeaveDaysChangesUseCase(
    private val myPageRepository: MyPageRepository,
) {
    operator fun invoke(): Flow<PreferredLeaveDaysChange> = myPageRepository.observePreferredLeaveDaysChanges()
}
