package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.zoo.domain.model.RectD
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow

class GetWorldSpaceUseCase(
    private val mapRepository: MapRepository,
) {

    operator fun invoke(): Flow<RectD> =
        mapRepository.getWorldSpace()
}