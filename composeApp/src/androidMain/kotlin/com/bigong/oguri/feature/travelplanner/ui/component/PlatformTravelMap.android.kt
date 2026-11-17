package com.bigong.oguri.feature.travelplanner.ui.component

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import com.bigong.oguri.core.designsystem.LightOguriColorPalette
import com.bigong.oguri.core.designsystem.OguriTheme
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
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.MapColorScheme
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
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
    val mapColorScheme = if (OguriTheme.isDarkTheme) MapColorScheme.DARK else MapColorScheme.LIGHT
    val backgroundArgb = OguriTheme.colors.surfaceCard.toArgb()
    val mapView = remember(context, lifecycle) {
        MapView(context, GoogleMapOptions().mapColorScheme(mapColorScheme).backgroundColor(backgroundArgb))
    }
    var map by remember(mapView) { mutableStateOf<GoogleMap?>(null) }
    var tilesLoaded by remember(mapView) { mutableStateOf(false) }
    var mapSize by remember(mapView) { mutableStateOf(IntSize.Zero) }
    val density = context.resources.displayMetrics.density
    val labelStyle = OguriTheme.typography.labelMedium
    val selectedLabelStyle = OguriTheme.typography.labelLarge
    val labelSize = with(LocalDensity.current) { labelStyle.fontSize.toPx() }
    val selectedLabelSize = with(LocalDensity.current) { selectedLabelStyle.fontSize.toPx() }
    val outline = LightOguriColorPalette.surfaceCard.toArgb()
    val fontResolver = LocalFontFamilyResolver.current
    val labelTypeface = fontResolver.resolve(labelStyle.fontFamily, labelStyle.fontWeight ?: FontWeight.Normal, labelStyle.fontStyle ?: FontStyle.Normal).value as Typeface
    val selectedLabelTypeface = fontResolver.resolve(selectedLabelStyle.fontFamily, selectedLabelStyle.fontWeight ?: FontWeight.Normal, selectedLabelStyle.fontStyle ?: FontStyle.Normal).value as Typeface

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
        LaunchedEffect(renderedMap, mapColorScheme) {
            renderedMap.setMapColorScheme(mapColorScheme)
        }
        LaunchedEffect(renderedMap, scene, mapColorScheme, labelSize, selectedLabelSize, labelTypeface, selectedLabelTypeface) {
            renderedMap.clear()
            scene.lines.forEach { line ->
                renderedMap.addPolyline(
                    PolylineOptions()
                        .addAll(line.points.map { it.toLatLng() })
                        .color(line.colorArgb)
                        .width(2 * density)
                        .pattern(listOf(Dot(), Gap(6 * density)))
                        .geodesic(true),
                )
            }
            scene.markers.forEach { marker ->
                renderedMap
                    .addMarker(
                        MarkerOptions()
                            .position(marker.coordinates.toLatLng())
                            .title(marker.title ?: marker.label)
                            .anchor(0.5f, 0.5f)
                            .zIndex(if (marker.selected) 1f else 0f)
                            .icon(BitmapDescriptorFactory.fromBitmap(marker.bitmap(density, outline, labelSize, selectedLabelSize, labelTypeface, selectedLabelTypeface))),
                    )?.tag = marker.id
            }
        }

        var appliedViewport by remember(renderedMap) { mutableStateOf<Int?>(null) }
        var fittedMarkers by remember(renderedMap, viewportRequest) { mutableStateOf(false) }
        LaunchedEffect(renderedMap, viewportRequest, scene.markers.isNotEmpty(), mapSize) {
            if (mapSize.width == 0 || mapSize.height == 0) return@LaunchedEffect
            val hasMarkers = scene.markers.isNotEmpty()
            if (appliedViewport == viewportRequest && (fittedMarkers || !hasMarkers)) return@LaunchedEffect
            appliedViewport = viewportRequest
            fittedMarkers = hasMarkers
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
                        val padding = (28 * density).toInt().coerceAtMost(minOf(mapSize.width, mapSize.height) / 4)
                        renderedMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapSize.width, mapSize.height, padding))
                    }
                }
            }
        }
    }
    AndroidView(factory = { mapView }, modifier = modifier.onSizeChanged { mapSize = it })
}

private fun TravelCoordinates.toLatLng(): LatLng = LatLng(latitude, longitude)

private fun TravelMapMarker.bitmap(
    density: Float,
    outline: Int,
    labelSize: Float,
    selectedLabelSize: Float,
    labelTypeface: Typeface,
    selectedLabelTypeface: Typeface,
): Bitmap {
    val size = ((if (selected) 44 else 26) * density).toInt().coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val middle = size / 2f
    if (selected) {
        paint.color = (colorArgb and 0x00ffffff) or 0x47000000
        canvas.drawCircle(middle, middle, middle, paint)
    }
    val radius = (if (selected) 16 else 13) * density
    paint.color = outline
    canvas.drawCircle(middle, middle, radius, paint)
    paint.color = colorArgb
    canvas.drawCircle(middle, middle, radius - 2 * density, paint)
    label?.let {
        paint.color = labelColorArgb
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = if (selected) selectedLabelSize else labelSize
        paint.typeface = if (selected) selectedLabelTypeface else labelTypeface
        canvas.drawText(it, middle, middle - (paint.ascent() + paint.descent()) / 2, paint)
    }
    return bitmap
}
