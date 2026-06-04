package org.maplibre.android.plugins.umap.tool;

import android.content.Context;

import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;


/**
 * 缩放工具类
 *
 */
public class ZoomInOutTool {

    private final MapLibreMap maplibreMap;

    public ZoomInOutTool(MapView mapView, MapLibreMap maplibreMap) {
        this.maplibreMap = maplibreMap;
    }

    public void zoomIn() {
        if (maplibreMap != null)
            maplibreMap.animateCamera(CameraUpdateFactory.zoomIn());
    }

    public void zoomOut() {
        if (maplibreMap != null)
            maplibreMap.animateCamera(CameraUpdateFactory.zoomOut());
    }
}
