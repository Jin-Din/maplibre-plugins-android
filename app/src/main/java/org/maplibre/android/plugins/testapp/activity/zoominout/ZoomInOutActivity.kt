package org.maplibre.android.plugins.testapp.activity.zoominout

import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.testapp.databinding.ActivityZoominoutBinding
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.turf.TurfConstants
import org.maplibre.turf.TurfMeasurement


import org.maplibre.android.plugins.testapp.Utils
import org.maplibre.android.plugins.umap.UMapStyles
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.geojson.Feature

import org.maplibre.android.plugins.umap.tool.ZoomInOutView
import org.maplibre.android.plugins.umap.tool.MeasureTool
import org.maplibre.android.plugins.umap.tool.ZoomInOutTool

/**
 * Activity showing a scalebar used on a MapView.
 */
class ZoomInOutActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var binding: ActivityZoominoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityZoominoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { maplibreMap ->
            // TestStyles.BRIGHT.url
            maplibreMap.setStyle(UMapStyles.getInstance(null).DEFAULT_STYLE) {

                binding.fabStyles.setOnClickListener {
                    maplibreMap.setStyle(Utils.nextStyle)
                }

                binding.btnMeasureLine.setOnClickListener {
                    MeasureTool.getInstance(mapView,maplibreMap)
                    .start()
                }
                binding.btnMeasureArea.setOnClickListener {
                    MeasureTool.getInstance(mapView,maplibreMap)
                        .start(MeasureTool.MeasureType.AREA)
                }
                binding.btnMeasureStop.setOnClickListener {
                    MeasureTool.getInstance(mapView,maplibreMap)
                        .stop()
                }


                maplibreMap.uiSettings.apply {
                    isLogoEnabled = false
                    isAttributionEnabled = false

                }

                maplibreMap.cameraPosition = CameraPosition.Builder()
                    .tilt(45.0)
                    .bearing(0.0)
                    .zoom(9.0)
                    .target(LatLng(34.263, 108.953)).build()

//                addZoomInOutView(maplibreMap);
                addPoint(it);

            }
        }
    }

    private fun addZoomInOutView(maplibreMap: MapLibreMap) {
        val zoomInOutView = ZoomInOutView(this)
        zoomInOutView.bindingTool(ZoomInOutTool(mapView, maplibreMap))
        mapView.addView(zoomInOutView, zoomInOutView.topEnd)
        zoomInOutView.setTag("zoomInOutView")

        val layoutParams: FrameLayout.LayoutParams =
            zoomInOutView.getLayoutParams() as FrameLayout.LayoutParams
        layoutParams.topMargin = 100
        zoomInOutView.setLayoutParams(layoutParams)

    }


    private fun addPoint(style: Style) {

        // 自定义 geojson 点
        val pointJsonSource = GeoJsonSource(
            "point", Feature.fromJson(
                """
                {
                    "type": "Feature",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [108.953, 34.263]
                    },
                    "properties": {
                        "name": "我是一个点"
                    }
                }
 """.trimIndent()
            )
        );



        style.addSource(pointJsonSource)
        style.addLayer(
            CircleLayer("point-symbol", "point").withProperties(
                PropertyFactory.circleColor(Color.RED),
                PropertyFactory.circleRadius(10f)

//                PropertyFactory.iconImage("marker-icon"),
//                PropertyFactory.iconSize(1.0f),
            )
        )

    }

    private fun setWidgetGravity(view: android.view.View, gravity: Int) {
        val layoutParams: FrameLayout.LayoutParams =
            view.getLayoutParams() as FrameLayout.LayoutParams
        layoutParams.gravity = gravity
        view.setLayoutParams(layoutParams)
    }

    private fun addScalebar(maplibreMap: MapLibreMap) {
//        val scaleBarPlugin =
//            ScaleBarPlugin(mapView, maplibreMap)
//        val scaleBarOptions = ScaleBarOptions(this)
//        scaleBarOptions
//            .setTextColor(R.color.black)
//            .setTextSize(40f)
//            .setBarHeight(5f)
//            .setBorderWidth(2f)
//            .setRefreshInterval(15)
//            .setMarginTop(15f)
//            .setMarginLeft(16f)
//            .setTextBarMargin(15f)
//            .setMaxWidthRatio(0.5f)
//            .setShowTextBorder(true)
//            .setTextBorderWidth(5f)
//
//        scaleBarPlugin.create(scaleBarOptions)
//        binding.fabScaleWidget.setOnClickListener {
//            scaleBarPlugin.isEnabled = !scaleBarPlugin.isEnabled
//        }
    }

    private fun setupTestLine(style: Style) {
        val source = GeoJsonSource("source-id")
        val lineLayer = LineLayer("layer-id", source.id)
        val startPoint: Point = Point.fromLngLat(-122.447244, 37.769145)
        val endPoint: Point =
            TurfMeasurement.destination(startPoint, 200.0, 90.0, TurfConstants.UNIT_METERS)
        val pointList: List<Point> = listOf(startPoint, endPoint)
        source.setGeoJson(LineString.fromLngLats(pointList))
        style.addSource(source)
        style.addLayer(lineLayer)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
