package com.jacekpietras.zoo.map.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.google.android.material.composethemeadapter.MdcTheme
import com.jacekpietras.mapview.ui.ComposableMapView
import com.jacekpietras.mapview.ui.ComposablePaintBaker
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.core.extensions.observe
import com.jacekpietras.zoo.map.model.MapEffect.*
import com.jacekpietras.zoo.map.viewmodel.MapViewModel
import com.jacekpietras.zoo.tracking.GpsPermissionRequester
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ComposableMapFragment : Fragment() {

    private val args: ComposableMapFragmentArgs? by navArgs()
    private val viewModel by viewModel<MapViewModel> {
        parametersOf(args?.animalId, args?.regionId)
    }
    private val paintBaker by lazy { ComposablePaintBaker(requireActivity()) }
    private val permissionChecker = GpsPermissionRequester(fragment = this)
    private val mapLogic = MapViewLogic(
        doAnimation = { it(1f, 0f) },
        invalidate = { mapUpdates.value = "update " + System.currentTimeMillis() },
        bakeCanvasPaint = { paintBaker.bakeCanvasPaint(it) },
        bakeBorderCanvasPaint = { paintBaker.bakeBorderCanvasPaint(it) },
        setOnPointPlacedListener = { point -> viewModel.onPointPlaced(point) },
    )
    private val mapUpdates = MutableLiveData("init")

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View = ComposeView(requireContext()).apply {
        setContent {
            MdcTheme {
                ComposableMapView(
                    mapData = mapLogic,
                    onScroll = mapLogic::onScroll,
                    onSizeChanged = mapLogic::onSizeChanged,
                    onClick = mapLogic::onClick,
                    onRotate = mapLogic::onRotate,
                    onScale = mapLogic::onScale,
                    state = mapUpdates.observeAsState(),
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
    }

    private fun setObservers() = with(viewModel) {
        mapViewState.observe(viewLifecycleOwner) {
            with(it) {
                mapLogic.worldData = MapViewLogic.WorldData(
                    bounds = worldBounds,
                    objectList = mapData,
                    terminalPoints = terminalPoints,
                )
            }
        }
        volatileViewState.observe(viewLifecycleOwner) {
            with(it) {
                mapLogic.userData = MapViewLogic.UserData(
                    userPosition = userPosition,
                    compass = compass,
                    clickOnWorld = snappedPoint,
                    shortestPath = shortestPath,
                )
            }
        }

        effect.observe(viewLifecycleOwner) {
            when (it) {
                is ShowToast -> toast(it.text.toString(requireContext()))
                is CenterAtUser -> mapLogic.centerAtUserPosition()
                is CenterAtPoint -> mapLogic.centerAtPoint(it.point)
            }
        }
    }

    private fun toast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}
