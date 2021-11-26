package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import com.jacekpietras.zoo.domain.repository.GpsRepository
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ObserveVisitedRoadsUseCase(
    private val mapRepository: MapRepository,
    private val gpsRepository: GpsRepository,
    private val getSnapPathToRoadUseCase: GetSnapPathToRoadUseCase,
) {

    suspend fun run(): Flow<List<PathEntity>> {
        if (!mapRepository.areVisitedRoadsCalculated()) {
            coroutineScope {
                launch {
                    val historicalGpsData = gpsRepository.getAllPositionsNormalized().toPathEntity()
                    val snapped: List<VisitedRoadEdge> = getSnapPathToRoadUseCase.run(historicalGpsData)
                    mapRepository.updateVisitedRoads(snapped)
                }
            }
        }
        return mapRepository.observeVisitedRoads()
            .map { edges ->
                edges
                    .map { edge ->
                        val result = mutableListOf<List<PathEntity>>()
                        for (i in 0 until (edge.visited.size - 2) step 2) {
                            val diff = edge.to - edge.from
                            val moveStart = diff * edge.visited[i]
                            val moveEnd = diff * edge.visited[i + 1]

                            result.add(
                                listOf(
                                    PathEntity(listOf(edge.from + moveStart)),
                                    PathEntity(listOf(edge.from + moveEnd))
                                )
                            )
                        }
                        result
                    }
                    .flatten()
                    .flatten()
            }
    }

    private fun List<List<GpsHistoryEntity>>.toPathEntity() =
        map { list ->
            PathEntity(
                list.map { PointD(it.lon, it.lat) }
            )
        }
}