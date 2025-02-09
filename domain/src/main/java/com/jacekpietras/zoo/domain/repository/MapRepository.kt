package com.jacekpietras.zoo.domain.repository

import com.jacekpietras.core.RectD
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PolygonEntity
import com.jacekpietras.zoo.domain.model.Region
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import kotlinx.coroutines.flow.Flow

interface MapRepository {

    suspend fun loadMap()

    fun isMapLoaded(): Boolean

    fun observeWorldBounds(): Flow<RectD>

    fun getWorldBounds(): RectD

    suspend fun getCurrentRegions(): List<Pair<Region, PolygonEntity>>

    fun observeBuildings(): Flow<List<PolygonEntity>>

    fun observeAviary(): Flow<List<PolygonEntity>>

    fun observeRoads(): Flow<List<PathEntity>>

    suspend fun getRoads(): List<PathEntity>

    fun observeVisitedRoads(): Flow<List<VisitedRoadEdge>>

    fun getVisitedRoads(): List<VisitedRoadEdge>?

    fun updateVisitedRoads(list: List<VisitedRoadEdge>)

    fun areVisitedRoadsCalculated(): Boolean

    fun observeTechnicalRoads(): Flow<List<PathEntity>>

    suspend fun getTechnicalRoads(): List<PathEntity>

    fun observeLines(): Flow<List<PathEntity>>
}