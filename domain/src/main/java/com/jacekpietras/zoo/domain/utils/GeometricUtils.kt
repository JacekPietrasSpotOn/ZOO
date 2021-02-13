package com.jacekpietras.zoo.domain.utils

import android.graphics.PointF
import com.jacekpietras.zoo.domain.model.PointD
import com.jacekpietras.zoo.domain.model.RectD
import kotlin.math.*

internal fun contains(list: List<PointD>, point: PointD): Boolean {
    // ray casting algorithm http://rosettacode.org/wiki/Ray-casting_algorithm
    var crossings = 0

    // for each edge
    for (i in list.indices) {
        val a: PointD = list[i]
        var j = i + 1
        //to close the last edge, you have to take the first point of your polygon
        if (j >= list.size) {
            j = 0
        }
        val b: PointD = list[j]
        if (rayCrossesSegment(point, a, b)) {
            crossings++
        }
    }

    // odd number of crossings?
    return crossings % 2 == 1
}

internal fun contains(list: List<PointF>, point: PointF): Boolean {
    // ray casting algorithm http://rosettacode.org/wiki/Ray-casting_algorithm
    var crossings = 0

    // for each edge
    for (i in list.indices) {
        val a: PointF = list[i]
        var j = i + 1
        //to close the last edge, you have to take the first point of your polygon
        if (j >= list.size) {
            j = 0
        }
        val b: PointF = list[j]
        if (rayCrossesSegment(point, a, b)) {
            crossings++
        }
    }

    // odd number of crossings?
    return crossings % 2 == 1
}

private fun rayCrossesSegment(point: PointD, a: PointD, b: PointD): Boolean {
    // Ray Casting algorithm checks, for each segment, if the point is
    // 1) to the left of the segment and
    // 2) not above nor below the segment. If these two conditions are met, it returns true
    var px: Double = point.x
    var py: Double = point.y
    var ax: Double = a.x
    var ay: Double = a.y
    var bx: Double = b.x
    var by: Double = b.y
    if (ay > by) {
        ax = b.x
        ay = b.y
        bx = a.x
        by = a.y
    }
    // alter longitude to cater for 180 degree crossings
    if (px < 0 || ax < 0 || bx < 0) {
        px += 360.0
        ax += 360.0
        bx += 360.0
    }
    // if the point has the same latitude as a or b, increase slightly py
    if (py == ay || py == by) py += 0.00000001


    // if the point is above, below or to the right of the segment, it returns false
    return if (py > by || py < ay || px > ax.coerceAtLeast(bx)) {
        false
    } else if (px < ax.coerceAtMost(bx)) {
        true
    } else {
        val red = if (ax != bx) (by - ay) / (bx - ax) else java.lang.Double.POSITIVE_INFINITY
        val blue = if (ax != px) (py - ay) / (px - ax) else java.lang.Double.POSITIVE_INFINITY
        blue >= red
    }
}

private fun rayCrossesSegment(point: PointF, a: PointF, b: PointF): Boolean {
    // Ray Casting algorithm checks, for each segment, if the point is
    // 1) to the left of the segment and
    // 2) not above nor below the segment. If these two conditions are met, it returns true
    var px: Float = point.x
    var py: Float = point.y
    var ax: Float = a.x
    var ay: Float = a.y
    var bx: Float = b.x
    var by: Float = b.y
    if (ay > by) {
        ax = b.x
        ay = b.y
        bx = a.x
        by = a.y
    }
    // alter longitude to cater for 180 degree crossings
    if (px < 0 || ax < 0 || bx < 0) {
        px += 360f
        ax += 360f
        bx += 360f
    }
    // if the point has the same latitude as a or b, increase slightly py
    if (py == ay || py == by) py += 0.00000001f


    // if the point is above, below or to the right of the segment, it returns false
    return if (py > by || py < ay || px > ax.coerceAtLeast(bx)) {
        false
    } else if (px < ax.coerceAtMost(bx)) {
        true
    } else {
        val red = if (ax != bx) (by - ay) / (bx - ax) else java.lang.Float.POSITIVE_INFINITY
        val blue = if (ax != px) (py - ay) / (px - ax) else java.lang.Float.POSITIVE_INFINITY
        blue >= red
    }
}