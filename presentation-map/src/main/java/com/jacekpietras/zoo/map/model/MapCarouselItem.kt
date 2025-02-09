package com.jacekpietras.zoo.map.model

import com.jacekpietras.zoo.core.text.Text
import com.jacekpietras.zoo.domain.model.AnimalId
import com.jacekpietras.zoo.domain.model.RegionId

sealed class MapCarouselItem(
    open val name: Text,
) {

    data class Animal(
        val id: AnimalId,
        override val name: Text,
        val photoUrl: String?,
    ) : MapCarouselItem(name)

    data class Region(
        val id: RegionId,
        override val name: Text,
        val photoUrlLeftTop: String?,
        val photoUrlRightTop: String?,
        val photoUrlLeftBottom: String?,
        val photoUrlRightBottom: String?,
    ) : MapCarouselItem(name)
}
