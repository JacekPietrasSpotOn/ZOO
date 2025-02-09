package com.jacekpietras.zoo.map.model

import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.model.MapItem

internal class MapWorldViewState(
    val worldBounds: RectD = RectD(0.0, 0.0, 0.0, 0.0),
    val mapData: List<MapItem> = emptyList(),
)
