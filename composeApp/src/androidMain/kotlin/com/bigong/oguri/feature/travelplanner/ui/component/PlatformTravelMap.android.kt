package com.bigong.oguri.feature.travelplanner.ui.component

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bigong.oguri.domain.model.TravelCoordinates
import com.bigong.oguri.feature.travelplanner.ui.model.TravelMapMarker
import com.bigong.oguri.feature.travelplanner.ui.model.TravelMapScene
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.delay

@Composable
internal actual fun PlatformTravelMap(
    scene: TravelMapScene,
    viewportRequest: Int,
    focusedVisitId: String?,
    onMarkerClick: (String) -> Unit,
    onUnavailable: () -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val available =
        remember(context) {
            @Suppress("DEPRECATION")
            val key =
                context.packageManager
                    .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                    .metaData
                    ?.get("com.google.android.geo.API_KEY")
                    ?.toString()
            !key.isNullOrBlank() &&
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        }
    if (!available) {
        LaunchedEffect(Unit) { onUnavailable() }
        return
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentClick by rememberUpdatedState(onMarkerClick)
    val unavailable by rememberUpdatedState(onUnavailable)
    val mapView = remember(context, lifecycle) { MapView(context) }
    var map by remember(mapView) { mutableStateOf<GoogleMap?>(null) }
    var tilesLoaded by remember(mapView) { mutableStateOf(false) }
    var mapSize by remember(mapView) { mutableStateOf(IntSize.Zero) }
    val density = context.resources.displayMetrics.density

    DisposableEffect(mapView) {
        mapView.onCreate(Bundle())
        mapView.getMapAsync { googleMap ->
            googleMap.setOnMarkerClickListener { marker ->
                (marker.tag as? String)?.let(currentClick)
                true
            }
            googleMap.setOnMapLoadedCallback { tilesLoaded = true }
            map = googleMap
        }
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    else -> Unit
                }
            }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) mapView.onPause()
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) mapView.onStop()
            map?.setOnMarkerClickListener(null)
            map?.setOnMapLoadedCallback(null)
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView, tilesLoaded) {
        if (!tilesLoaded) {
            delay(15_000)
            unavailable()
        }
    }
    val renderedMap = map
    if (renderedMap != null) {
        LaunchedEffect(renderedMap, scene) {
            renderedMap.clear()
            scene.lines.forEach { line ->
                renderedMap.addPolyline(
                    PolylineOptions()
                        .addAll(line.points.map { it.toLatLng() })
                        .color(MAP_LINE_COLOR)
                        .width(3 * density)
                        .geodesic(true),
                )
            }
            scene.markers.forEach { marker ->
                renderedMap
                    .addMarker(
                        MarkerOptions()
                            .position(marker.coordinates.toLatLng())
                            .anchor(0.5f, 0.5f)
                            .zIndex(if (marker.selected) 1f else 0f)
                            .icon(BitmapDescriptorFactory.fromBitmap(marker.bitmap(density))),
                    )?.tag = marker.id
            }
        }

        LaunchedEffect(renderedMap, viewportRequest, scene.markers.isEmpty(), focusedVisitId, mapSize) {
            val focus = scene.markers.firstOrNull { it.id == focusedVisitId }
            if (focus != null) {
                renderedMap.moveCamera(CameraUpdateFactory.newLatLngZoom(focus.coordinates.toLatLng(), 15f))
            } else {
                val coordinates = scene.markers.map { it.coordinates.toLatLng() }.distinct()
                when (coordinates.size) {
                    0 ->
                        renderedMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                scene.cityCenter?.toLatLng() ?: LatLng(0.0, 0.0),
                                if (scene.cityCenter ==
                                    null
                                ) {
                                    1f
                                } else {
                                    11f
                                },
                            ),
                        )
                    1 -> renderedMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates.single(), 14f))
                    else -> {
                        val bounds = LatLngBounds.builder().apply { coordinates.forEach(::include) }.build()
                        if (mapSize.width > 0 && mapSize.height > 0) {
                            val padding = (28 * density).toInt().coerceAtMost(minOf(mapSize.width, mapSize.height) / 4)
                            renderedMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapSize.width, mapSize.height, padding))
                        } else {
                            renderedMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 10f))
                        }
                    }
                }
            }
        }
    }
    AndroidView(factory = { mapView }, modifier = modifier.onSizeChanged { mapSize = it })
}

private fun TravelCoordinates.toLatLng(): LatLng = LatLng(latitude, longitude)

private fun TravelMapMarker.bitmap(density: Float): Bitmap {
    val size = (34 * density).toInt().coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val middle = size / 2f
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(middle, middle, middle, paint)
    paint.color = if (selected) MAP_SELECTED_COLOR else MAP_LINE_COLOR
    canvas.drawCircle(middle, middle, middle * 0.86f, paint)
    label?.let {
        paint.color = android.graphics.Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = middle * 0.84f
        canvas.drawText(it, middle, middle - (paint.ascent() + paint.descent()) / 2, paint)
    }
    return bitmap
}

private const val MAP_LINE_COLOR: Int = 0xff007d69.toInt()
private const val MAP_SELECTED_COLOR: Int = 0xff244da5.toInt()
