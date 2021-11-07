package com.jacekpietras.mapview.ui


import android.content.Context
import android.graphics.Matrix
import android.graphics.Paint
import com.jacekpietras.core.PointD
import com.jacekpietras.core.RectD
import com.jacekpietras.mapview.BuildConfig
import com.jacekpietras.mapview.model.*
import com.jacekpietras.mapview.utils.pointsToDoubleArray
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

internal class MapViewLogic(
    private val context: Context,
    private val getCurrentHeight: () -> Int,
    private val getCurrentWidth: () -> Int,
    private val doAnimation: ((progress: Float, left: Float) -> Unit) -> Unit,
    private val invalidate: () -> Unit,
) {

    var setOnPointPlacedListener: ((PointD) -> Unit)? = null
    var maxZoom: Double = 10.0
    var minZoom: Double = 2.0
    var worldBounds: RectD = RectD()
        set(value) {
            field = value
            centerGpsCoordinate = PointD(value.centerX(), value.centerY())
            maxZoom = min(abs(value.width()), abs(value.height())) / 2
            minZoom = maxZoom / 10
            zoom = maxZoom / 5
        }
    private var _objectList: List<ObjectItem> = emptyList()
    internal var objectList: List<MapItem> = emptyList()
        set(value) {
            Timber.v("Content changed")
            field = value
            _objectList = value.toRenderItems()
            cutOutNotVisible()
        }

    internal var userPosition: PointD? = null
        set(value) {
            Timber.v("Position changed ${value?.x}")
            field = value
            if (centeringAtUser) {
                centerAtUserPosition()
            }
        }
    var terminalPoints: List<PointD> = emptyList()
        set(value) {
            field = value
            cutOutNotVisible()
        }
    internal var terminalPointsOnScreen: FloatArray? = null
    var userPositionOnScreen: FloatArray? = null
    var compass: Float = 0f
        set(value) {
            field = value
            if (centeringAtUser) {
                centerAtUserCompass()
            }
        }

    private val interesting: List<PointD> = listOf()
    internal var interestingOnScreen: FloatArray? = null

    internal var clickOnWorld: PointD? = null
        set(value) {
            field = value
            cutOutNotVisible()
        }
    var shortestPath: List<PointD> = emptyList()
        set(value) {
            field = value
            cutOutNotVisible()
        }
    internal var shortestPathOnScreen: FloatArray? = null
    internal var clickOnScreen: FloatArray? = null

    private lateinit var visibleGpsCoordinate: ViewCoordinates
    private var centerGpsCoordinate: PointD =
        PointD(worldBounds.centerX(), worldBounds.centerY())
    private var zoom: Double = 5.0
    private var zoomOnStart: Double = 5.0
    private var worldRotation: Float = 0f
    private var worldRotationOnStart: Float = 0f
    internal var renderList: List<RenderItem>? = null
    private var centeringAtUser = false

    fun centerAtPoint(desiredPosition: PointD) {
        centeringAtUser = false
        val previousPosition = centerGpsCoordinate

        doAnimation { progress, left ->
            centerGpsCoordinate = previousPosition * left + desiredPosition * progress
            cutOutNotVisible()
        }
    }

    fun centerAtUserPosition() {
        centeringAtUser = true
        val desiredPosition = userPosition ?: return
        val previousPosition = centerGpsCoordinate

        doAnimation { progress, left ->
            centerGpsCoordinate = previousPosition * left + desiredPosition * progress
            cutOutNotVisible()
        }
    }

    private fun centerAtUserCompass() {
        val diff = worldRotation - compass
        val previousPosition = when {
            diff > 180 -> worldRotation - 360
            diff < -180 -> worldRotation + 360
            else -> worldRotation
        }

        doAnimation { progress, left ->
            worldRotation = (previousPosition * left + compass * progress) % 360f
            cutOutNotVisible()
        }
    }

    fun onSizeChanged() {
        cutOutNotVisible(invalidate = false)
    }

    fun onScaleBegin() {
        zoomOnStart = zoom
    }

    fun onScale(scale: Float) {
        val zoomPoint = (maxZoom - minZoom) / 2
        zoom = (zoomOnStart + zoomPoint * (1 - scale)).coerceAtMost(maxZoom).coerceAtLeast(minZoom)
        cutOutNotVisible()
    }

    fun onRotateBegin() {
        worldRotationOnStart = worldRotation
    }

    fun onRotate(rotate: Float) {
        worldRotation = (rotate + worldRotationOnStart) % 360
    }

    fun onScroll(vX: Float, vY: Float) {
        centeringAtUser = false
        val radians = Math.toRadians(-worldRotation.toDouble())
        centerGpsCoordinate += PointD(
            (sin(radians) * vY + cos(radians) * vX) / visibleGpsCoordinate.horizontalScale,
            (-sin(radians) * vX + cos(radians) * vY) / visibleGpsCoordinate.verticalScale
        )
        cutOutNotVisible()
    }

    fun onClick(x: Float, y: Float) {
        val point = FloatArray(2)
        point[0] = x
        point[1] = y
        val matrix = Matrix()
            .apply {
                setRotate(
                    worldRotation,
                    getCurrentWidth() / 2.toFloat(),
                    getCurrentHeight() / 2.toFloat(),
                )
            }
        matrix.mapPoints(point)

        setOnPointPlacedListener?.invoke(visibleGpsCoordinate.deTransformPoint(point[0], point[1]))
    }

//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        renderList?.forEach { canvas.drawPath(it.shape, it.paint, it.close) }
//        userPositionOnScreen?.let {
//            canvas.drawCircle(it[0], it[1], 15f, userPositionPaint)
//        }
//        terminalPointsOnScreen?.let { array ->
//            for (i in array.indices step 2) {
//                canvas.drawCircle(array[i], array[i + 1], 5f, terminalPaint)
//            }
//        }
//        shortestPathOnScreen?.let { array ->
//            canvas.drawPath(array, shortestPaint, false)
//        }
//        interestingOnScreen?.let { array ->
//            for (i in array.indices step 2) {
//                canvas.drawCircle(array[i], array[i + 1], 5f, interestingPaint)
//            }
//        }
//        clickOnScreen?.let {
//            canvas.drawCircle(it[0], it[1], 15f, interestingPaint)
//        }
//
//        renderDebug(canvas)
//    }
//
//    private fun renderDebug(canvas: Canvas) {
//        if (BuildConfig.DEBUG && ::visibleGpsCoordinate.isInitialized) {
//            canvas.drawText("wrld: " + worldBounds.toShortString(), 10f, 40f, debugTextPaint)
//            canvas.drawText(
//                "curr: ${visibleGpsCoordinate.visibleRect.toShortString()}",
//                10f,
//                80f,
//                debugTextPaint
//            )
//
//            canvas.drawText(
//                "zoom: ${((zoom - minZoom) / (maxZoom - minZoom)).form()}",
//                10f,
//                120f,
//                debugTextPaint
//            )
//            canvas.drawText(
//                "rot:  [${worldRotation.toDouble().form()}]",
//                10f,
//                160f,
//                debugTextPaint
//            )
//            userPosition?.let {
//                canvas.drawText(
//                    "upos: [${it.x.form()},${it.y.form()}]",
//                    10f,
//                    200f,
//                    debugTextPaint
//                )
//            }
//        }
//    }

    private fun List<MapItem>.toRenderItems(): List<ObjectItem> {
        val innerPaints = mutableMapOf<MapPaint, PaintHolder>()
        val borderPaints = mutableMapOf<MapPaint, PaintHolder?>()

        return map { item ->
            val inner = innerPaints[item.paint]
                ?: item.paint.toCanvasPaint(context)
                    .also { innerPaints[item.paint] = it }
            val border = borderPaints[item.paint]
                ?: item.paint.toBorderCanvasPaint(context)
                    .also { borderPaints[item.paint] = it }

            when (item.shape) {
                is PathD -> ObjectItem(
                    pointsToDoubleArray(item.shape.vertices),
                    inner,
                    border,
                    close = false,
                )
                is PolygonD -> ObjectItem(
                    pointsToDoubleArray(item.shape.vertices),
                    inner,
                    border,
                    close = true,
                )
                else -> throw IllegalStateException("Unknown shape type ${item.shape}")
            }
        }
    }

    private fun preventedGoingOutsideWorld(): Boolean {
        val reversedV = worldBounds.top > worldBounds.bottom
        val reversedH = worldBounds.left > worldBounds.right

        val leftMargin =
            if (reversedH) {
                worldBounds.left - visibleGpsCoordinate.visibleRect.left
            } else {
                visibleGpsCoordinate.visibleRect.left - worldBounds.left
            }
        val rightMargin =
            if (reversedH) {
                visibleGpsCoordinate.visibleRect.right - worldBounds.right
            } else {
                worldBounds.right - visibleGpsCoordinate.visibleRect.right
            }
        val topMargin =
            if (reversedV) {
                worldBounds.top - visibleGpsCoordinate.visibleRect.top
            } else {
                visibleGpsCoordinate.visibleRect.top - worldBounds.top
            }
        val bottomMargin =
            if (reversedV) {
                visibleGpsCoordinate.visibleRect.bottom - worldBounds.bottom
            } else {
                worldBounds.bottom - visibleGpsCoordinate.visibleRect.bottom
            }
        var result = false

        when {
            leftMargin + rightMargin < 0 -> {
                zoom = zoom.times(.99f).coerceAtMost(maxZoom).coerceAtLeast(minZoom)
                centerGpsCoordinate = PointD(worldBounds.centerX(), centerGpsCoordinate.y)
                result = true
            }
            leftMargin < -0.00001 -> {
                if (reversedH) {
                    centerGpsCoordinate += PointD(leftMargin, 0.0)
                } else {
                    centerGpsCoordinate -= PointD(leftMargin, 0.0)
                }
                result = true
            }
            rightMargin < -0.00001 -> {
                if (reversedH) {
                    centerGpsCoordinate -= PointD(rightMargin, 0.0)
                } else {
                    centerGpsCoordinate += PointD(rightMargin, 0.0)
                }
                result = true
            }
        }

        when {
            topMargin + bottomMargin < 0 -> {
                zoom = zoom.times(.99f).coerceAtMost(maxZoom).coerceAtLeast(minZoom)
                centerGpsCoordinate = PointD(centerGpsCoordinate.x, worldBounds.centerY())
                result = true
            }
            topMargin < -0.00001 -> {
                if (reversedV) {
                    centerGpsCoordinate += PointD(0.0, topMargin)
                } else {
                    centerGpsCoordinate -= PointD(0.0, topMargin)
                }
                result = true
            }
            bottomMargin < -0.00001 -> {
                if (reversedV) {
                    centerGpsCoordinate -= PointD(0.0, bottomMargin)
                } else {
                    centerGpsCoordinate += PointD(0.0, bottomMargin)
                }
                result = true
            }
        }

        return result
    }

    private fun PaintHolder.takePaint(dynamicPaints: MutableMap<PaintHolder.Dynamic, Paint>): Paint {
        return when (this) {
            is PaintHolder.Static -> paint
            is PaintHolder.Dynamic -> {
                dynamicPaints[this]
                    ?: block(zoom, centerGpsCoordinate, getCurrentWidth())
                        .also { dynamicPaints[this] = it }
            }
        }
    }

    private fun cutOutNotVisible(invalidate: Boolean = true) {
        if (getCurrentWidth() == 0 || getCurrentHeight() == 0) return
        if (worldBounds.width() == 0.0 || worldBounds.height() == 0.0) return

        visibleGpsCoordinate = ViewCoordinates(centerGpsCoordinate, zoom, getCurrentWidth(), getCurrentHeight())

        if (preventedGoingOutsideWorld()) {
            cutOutNotVisible(false)
            return
        }

        val borders = mutableListOf<RenderItem>()
        val insides = mutableListOf<RenderItem>()
        val dynamicPaints = mutableMapOf<PaintHolder.Dynamic, Paint>()

        val matrix = Matrix()
            .apply {
                setRotate(
                    -worldRotation,
                    getCurrentWidth() / 2.toFloat(),
                    getCurrentHeight() / 2.toFloat(),
                )
            }

        _objectList.forEach { item ->
            if (item.close) {
                visibleGpsCoordinate
                    .transformPolygon(item.shape)
                    ?.withMatrix(matrix, worldRotation)
                    ?.let { polygon ->
                        item.addToRender(polygon, borders, insides, dynamicPaints)
                    }
            } else {
                visibleGpsCoordinate
                    .transformPath(item.shape)
                    .map { it.withMatrix(matrix, worldRotation) }
                    .forEach { path ->
                        item.addToRender(path, borders, insides, dynamicPaints)
                    }
            }
        }
        userPosition?.let {
            userPositionOnScreen = visibleGpsCoordinate
                .transformPoint(it)
                .withMatrix(matrix, worldRotation)
        }
        clickOnWorld?.let {
            clickOnScreen = visibleGpsCoordinate
                .transformPoint(it)
                .withMatrix(matrix, worldRotation)
        }
        if (terminalPoints.isNotEmpty()) {
            terminalPointsOnScreen = visibleGpsCoordinate
                .transformPoints(terminalPoints)
                .withMatrix(matrix, worldRotation)
        }
        if (shortestPath.isNotEmpty()) {
            shortestPathOnScreen = visibleGpsCoordinate
                .transformPoints(shortestPath)
                .withMatrix(matrix, worldRotation)
        }
        if (interesting.isNotEmpty()) {
            interestingOnScreen = visibleGpsCoordinate
                .transformPoints(interesting)
                .withMatrix(matrix, worldRotation)
        }

        renderList = borders + insides
        logVisibleShapes()
        if (invalidate) invalidate()
    }

    private fun FloatArray.withMatrix(matrix: Matrix, worldRotation: Float): FloatArray {
        if (worldRotation != 0f) {
            matrix.mapPoints(this)
        }
        return this
    }

    private fun ObjectItem.addToRender(
        array: FloatArray,
        borders: MutableList<RenderItem>,
        insides: MutableList<RenderItem>,
        dynamicPaints: MutableMap<PaintHolder.Dynamic, Paint>,
    ) {
        insides.add(
            RenderItem(
                array,
                paintHolder.takePaint(dynamicPaints),
                close
            )
        )
        if (outerPaintHolder != null) {
            borders.add(
                RenderItem(
                    array,
                    outerPaintHolder.takePaint(dynamicPaints),
                    close
                )
            )
        }
    }

    private fun logVisibleShapes() {
        if (BuildConfig.DEBUG) {
            val message = "Preparing render list: " +
                    renderList?.map { "Shape " + (it.shape.size shr 1) } +
                    " (${objectList.size})"
            Timber.v(message)
        }
    }

    private class ObjectItem(
        val shape: DoubleArray,
        val paintHolder: PaintHolder,
        val outerPaintHolder: PaintHolder? = null,
        val close: Boolean,
    )

    internal class RenderItem(
        val shape: FloatArray,
        val paint: Paint,
        val close: Boolean,
    )
}