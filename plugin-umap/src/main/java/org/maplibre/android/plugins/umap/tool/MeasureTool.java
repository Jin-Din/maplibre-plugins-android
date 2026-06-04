package org.maplibre.android.plugins.umap.tool;

import android.annotation.SuppressLint;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.plugins.umap.utils.MeasureUtil;
import org.maplibre.android.style.layers.CircleLayer;
import org.maplibre.android.style.layers.FillLayer;
import org.maplibre.android.style.layers.Layer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.Property;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.layers.PropertyValue;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.maplibre.geojson.Polygon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MeasureTool implements MapLibreMap.OnMapClickListener {


    private MapView _mapView;
    private MapLibreMap _mapLibreMap;
    private boolean _isMeasuring = false;

    List<Point> _points = new ArrayList<>();
    private List<MeasurePoint> _measurePoints = new ArrayList<>();

    private final String _areaSourceLayerId = "c_measure_line_area_layer";
    private final String _pointSourceLayerId = "c_measure_line_point_layer";
    private final String _lineSourceLayerId = "c_measure_line_line_layer";
    private final String _labelSourceLayerId = "c_measure_line_label_layer";

    private MeasureType _measureType = MeasureType.DISTANCE;

    private MeasureLineOptions _options = new MeasureLineOptions();

    private static MeasureTool _instance;

    public static MeasureTool getInstance(MapView mapView, MapLibreMap mapLibreMap) {
        if (_instance == null) {
            _instance = new MeasureTool(mapView, mapLibreMap, null);
        }
        return _instance;
    }

    public MeasureTool(MapView mapView, MapLibreMap mapLibreMap) {
        this(mapView, mapLibreMap, null);
    }

    public MeasureTool(MapView mapView, MapLibreMap mapLibreMap, MeasureLineOptions options) {
        this._mapView = mapView;
        this._mapLibreMap = mapLibreMap;
        this._options = options == null ? new MeasureLineOptions() : options;
    }

    private void initLayers(Style mapStyle) {

        // 初始化面积图层
        GeoJsonSource areaSource = (GeoJsonSource) mapStyle.getSource(_areaSourceLayerId);
        GeoJsonSource pointSource = (GeoJsonSource) mapStyle.getSource(_pointSourceLayerId);
        GeoJsonSource lineSource = (GeoJsonSource) mapStyle.getSource(_lineSourceLayerId);
        GeoJsonSource labelSource = (GeoJsonSource) mapStyle.getSource(_labelSourceLayerId);

        if (areaSource == null) {
            mapStyle.addSource(new GeoJsonSource(_areaSourceLayerId, FeatureCollection.fromFeatures(new ArrayList<Feature>())));
        } else
            areaSource.setGeoJson(FeatureCollection.fromFeatures(new ArrayList<Feature>()));

        if (lineSource == null) {
            mapStyle.addSource(new GeoJsonSource(_lineSourceLayerId, FeatureCollection.fromFeatures(new ArrayList<Feature>())));
        } else
            lineSource.setGeoJson(FeatureCollection.fromFeatures(new ArrayList<Feature>()));

        if (pointSource == null) {
            mapStyle.addSource(new GeoJsonSource(_pointSourceLayerId, FeatureCollection.fromFeatures(new ArrayList<Feature>())));
        } else
            pointSource.setGeoJson(FeatureCollection.fromFeatures(new ArrayList<Feature>()));

        if (labelSource == null) {
            mapStyle.addSource(new GeoJsonSource(_labelSourceLayerId, FeatureCollection.fromFeatures(new ArrayList<Feature>())));
        } else
            labelSource.setGeoJson(FeatureCollection.fromFeatures(new ArrayList<Feature>()));


        // 初始化图层
        Layer areaLayer = mapStyle.getLayer(_areaSourceLayerId);
        Layer pointLayer = mapStyle.getLayer(_pointSourceLayerId);
        Layer lineLayer = mapStyle.getLayer(_lineSourceLayerId);
        Layer labelLayer = mapStyle.getLayer(_labelSourceLayerId);


        if (areaLayer == null) {
            areaLayer = new FillLayer(_areaSourceLayerId, _areaSourceLayerId);
            areaLayer.setProperties(new PropertyValue[]{
                    PropertyFactory.fillColor(_options.fillColor),
                    PropertyFactory.fillOpacity(_options.fillOpacity),
            });
            mapStyle.addLayer(areaLayer);
        }
        // 先加面、线，再加点
        if (lineLayer == null) {
            lineLayer = new LineLayer(_lineSourceLayerId, _lineSourceLayerId);
            lineLayer.setProperties(new PropertyValue[]{
                    PropertyFactory.lineWidth(_options.lineWidth),
                    PropertyFactory.lineColor(_options.lineColor),

            });
            mapStyle.addLayer(lineLayer);
        }
        if (pointLayer == null) {
            CircleLayer circleLayer = new CircleLayer(_pointSourceLayerId, _pointSourceLayerId);

            circleLayer.setProperties(new PropertyValue[]{
                    PropertyFactory.circleRadius(_options.circleRadius),
                    PropertyFactory.circleColor(_options.circleColor),
                    PropertyFactory.circleStrokeColor(Color.RED),
                    PropertyFactory.circleStrokeWidth(_options.circleStrokeWidth),
            });
            mapStyle.addLayer(circleLayer);
        }
        if (labelLayer == null) {
            SymbolLayer symbolLayer = new SymbolLayer(_labelSourceLayerId, _labelSourceLayerId);
            symbolLayer.setProperties(new PropertyValue[]{
                    // 显示文字
                    PropertyFactory.textField("{label}"),
                    PropertyFactory.textSize(_options.textSize),
                    PropertyFactory.textColor(_options.textColor),
                    PropertyFactory.textJustify(Property.TEXT_JUSTIFY_AUTO),
                    PropertyFactory.textAnchor(Property.TEXT_ANCHOR_BOTTOM)
            });
            mapStyle.addLayer(symbolLayer);
        }

        // 移除旧事件
        _mapLibreMap.removeOnMapClickListener(this);
        // 绑定事件
        _mapLibreMap.addOnMapClickListener(this);
    }

    private void init() {
        Style style = this._mapLibreMap.getStyle();
        assert style != null;
        initLayers(style);
    }

    private void clear() {
        _points.clear();
        _measurePoints.clear();

        //移除事件
        this._mapLibreMap.removeOnMapClickListener(this);

        Style mapStyle = this._mapLibreMap.getStyle();
        assert mapStyle != null;
        mapStyle.removeSource(this._pointSourceLayerId);
        mapStyle.removeSource(this._lineSourceLayerId);
        mapStyle.removeSource(this._labelSourceLayerId);
        mapStyle.removeSource(this._areaSourceLayerId);

        mapStyle.removeLayer(this._pointSourceLayerId);
        mapStyle.removeLayer(this._lineSourceLayerId);
        mapStyle.removeLayer(this._labelSourceLayerId);
        mapStyle.removeLayer(this._areaSourceLayerId);

    }

    public void start() {
        start(MeasureType.DISTANCE);
    }

    public void start(MeasureType measureType) {

        if (this._mapLibreMap == null)
            throw new IllegalArgumentException("MapLibreMap is null");

        this._measureType = measureType;

        //先清除旧数据
        this.stop();

        init();

        _isMeasuring = true;
    }

    public void stop() {
        _isMeasuring = false;
        this.clear();
    }


    @SuppressLint("DefaultLocale")
    @Override
    public boolean onMapClick(@NonNull LatLng latLng) {
        if (!_isMeasuring) {
            return false;
        }
        Point clickPointGeometry = Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude());
        _points.add(clickPointGeometry);



        if (this._measureType == MeasureType.DISTANCE) { // 测量距离

            MeasurePoint measurePoint = new MeasurePoint();
            measurePoint.point = clickPointGeometry;
            measurePoint.label = "起点";

            _measurePoints.add(measurePoint);

            updateLine(_points);
            updatePoint(_points);

            // 计算出与上一点的距离
            if(_measurePoints.size()>1){
                MeasurePoint lastPoint = _measurePoints.get(_measurePoints.size()-2);
                MeasurePoint currentPoint = _measurePoints.get(_measurePoints.size()-1);
                // 计算与上一点的距离
                double distance = MeasureUtil.getDistance(lastPoint.point.longitude(),lastPoint.point.latitude(),currentPoint.point.longitude(),currentPoint.point.latitude());
                if(distance > 1000){
                    currentPoint.label = String.format("%.2f km", distance / 1000.0D);
                }
                else{
                    currentPoint.label = String.format("%.2f m", distance);
                }
            }

            updateLabel(_measurePoints);

            return false;
        } else if (this._measureType == MeasureType.AREA) { // 测量面积

            updatePoint(_points);

            updatePolygon(_points);

            if(_points.size() >=3){

                double[][] coordinates = new double[_points.size()][2];
                for(int i = 0;i<_points.size();i++){
                    coordinates[i][0] = _points.get(i).longitude();
                    coordinates[i][1] = _points.get(i).latitude();
                }
                double[] centroid = MeasureUtil.computeCentroid(coordinates);
                double area = MeasureUtil.computeArea(coordinates);
                MeasurePoint measurePoint = new MeasurePoint();
                measurePoint.point = Point.fromLngLat(centroid[0], centroid[1]);
                if(area > 1000000){
                    measurePoint.label = String.format("%.2f km²", area / 1000000.0D);
                }
                else{
                    measurePoint.label = String.format("%.2f m²", area);
                }
                List<MeasurePoint> measurePoints = new ArrayList<>();
                measurePoints.add(measurePoint);

                updateLabel(measurePoints);
            }

//

            return false;
        }
        return false;
    }

    private void updateLabel(List<MeasurePoint> measurePoints) {

        List<Feature> labelFeatures = new ArrayList<>();
        Iterator var2 = measurePoints.iterator();
        while (var2.hasNext()) {
            MeasurePoint mPt = (MeasurePoint) var2.next();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("label", mPt.label);
            // 创建pointFeature
            Feature labelFeature =  Feature.fromGeometry(mPt.point, jsonObject);;
            labelFeatures.add(labelFeature);
        }
        // 刷新数据
        ((GeoJsonSource) _mapLibreMap.getStyle().getSource(_labelSourceLayerId)).setGeoJson(FeatureCollection.fromFeatures(labelFeatures));
    }

    private void updateLine(List<Point> points) {
        if (points.isEmpty()) {
            return;
        }
        LineString lineString = LineString.fromLngLats(points);
        // 创建lineFeature
        Feature lineFeature = Feature.fromGeometry(lineString);

        // 刷新数据
        ((GeoJsonSource) _mapLibreMap.getStyle().getSource(_lineSourceLayerId)).setGeoJson(lineFeature);
    }

    private void updatePoint(List<Point> points) {
        if (points.isEmpty()) {
            return;
        }

        List<Feature> circleLayerFeatureList = new ArrayList();
        Iterator var2 = points.iterator();
        while (var2.hasNext()) {
            Point p = (Point) var2.next();
            circleLayerFeatureList.add(Feature.fromGeometry(p));
        }
        FeatureCollection pointFeatureCollection = FeatureCollection.fromFeatures(circleLayerFeatureList);

        // 刷新数据
        ((GeoJsonSource) _mapLibreMap.getStyle().getSource(_pointSourceLayerId)).setGeoJson(pointFeatureCollection);

    }

    private void updatePolygon(List<Point> points) {
        if (points.isEmpty()) {
            return;
        }

        List<Feature> poygonLayerFeatureList = new ArrayList<>();
        List<Point> temp_point = new ArrayList<>((points));
        // 关闭多边形
        if (!points.isEmpty()) {
            temp_point.add(points.get(0));
        }
        List<List<Point>> pointsList = new ArrayList<>();
        pointsList.add(temp_point);
        Polygon poy = Polygon.fromLngLats(pointsList);

        poygonLayerFeatureList.add(Feature.fromGeometry(poy));

        FeatureCollection areaFeatureCollection = FeatureCollection.fromFeatures(poygonLayerFeatureList);
        // 刷新数据
        ((GeoJsonSource) _mapLibreMap.getStyle().getSource(_areaSourceLayerId)).setGeoJson(areaFeatureCollection);


        // 刷新lineSource
        // 这里面积量算构建的线 和 line 公用一个source
        // 当点的数量大于等于3个时，面 构建成功，所以直接传入 areaFeatureCollection即可
        // 当点的数量小于3个时，不能构建成面，则用线代替， lineFeatureCollection

        if (points.size() >= 3) {
            ((GeoJsonSource) _mapLibreMap.getStyle().getSource(_lineSourceLayerId)).setGeoJson(areaFeatureCollection);
        } else {
            LineString lineString = LineString.fromLngLats(points);
            // 创建lineFeature
            Feature lineFeature = Feature.fromGeometry(lineString);

            // 刷新数据
            ((GeoJsonSource) _mapLibreMap.getStyle().getSource(_lineSourceLayerId)).setGeoJson(lineFeature);
        }

    }

    public class MeasureLineOptions {

        public float circleRadius = 4.0F;
        public int circleColor = Color.parseColor("#fffd38");
        public float circleStrokeWidth = 1.0F;

        public float lineWidth = 2.0F;
        public int lineColor = Color.parseColor("#d71345");

        public int fillColor = Color.parseColor("#fffd38");
        public float fillOpacity = 0.5f;
        public float textSize = 12.0F;
        public int textColor = Color.parseColor("#45b97c");

        public MeasureLineOptions() {
        }

    }

    public enum MeasureType {
        DISTANCE,
        AREA
    }

    private class MeasurePoint {
        public Point point;
        public String label;
       public MeasurePoint(){}
    }

}

