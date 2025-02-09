package com.jacekpietras.mapview.model

import com.jacekpietras.core.PointD

sealed class PaintHolder<T> {

    class Static<T>(val paint: T) : PaintHolder<T>()

    class Dynamic<T>(val block: (zoom: Double, position: PointD, screenWidthInPixels: Int) -> T) :
        PaintHolder<T>()
}
