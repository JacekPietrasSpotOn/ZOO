package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.polygonContains
import com.jacekpietras.zoo.domain.model.MapItemEntity
import com.jacekpietras.zoo.domain.model.RegionId
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import com.jacekpietras.zoo.domain.repository.MapRepository

class IsRegionSeenUseCase(
    private val mapRepository: MapRepository,
) {

    suspend fun run(regionId: RegionId): Boolean {
        if (!mapRepository.areVisitedRoadsCalculated()) return false

        val alreadyVisited = checkNotNull(mapRepository.getVisitedRoads())
        val region = mapRepository.getCurrentRegions().first { it.first.id == regionId }.second

        alreadyVisited.forEach { edge ->
            edge.forEachPath {
                it.vertices.forEach { point ->
                    if (polygonContains(region.vertices, point)) return true
                }
            }
        }

        return false
    }

    private inline fun VisitedRoadEdge.forEachPath(block: (MapItemEntity.PathEntity) -> Unit) {
        when (this) {
            is VisitedRoadEdge.Fully -> {
                block(toPath())
            }
            is VisitedRoadEdge.Partially -> {
                toPath().forEach {
                    block(it)
                }
            }
        }
    }
}
