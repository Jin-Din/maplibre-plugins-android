package org.maplibre.android.plugins.umap.tianditu;

import org.maplibre.android.style.layers.RasterLayer;
import org.maplibre.android.style.sources.RasterSource;
import org.maplibre.android.style.sources.TileSet;

import java.util.ArrayList;

public class TianDiTu {

//    public static final String TDT_W = "tdt_w";

    private static String TOKEN;

    private static TianDiTu instance;

//    public TianDiTu() {
//
//    }


    /**
     * 矢量图层
     */
    public static SourceAndLayer VEC_W;
    /**
     * 矢量注记·
     */
    public static SourceAndLayer CVA_W;

    /**
     * 影像
     */
    public static SourceAndLayer IMG_W;
    /**
     * 影像注记
     */
    public static SourceAndLayer CIA_W;

    /**
     * 地形
     */
    public static SourceAndLayer TER_W;
    /**
     * 地形注记
     */
    public static SourceAndLayer CTA_W;


    /**
     * 天地图 token。
     * <p>
     * <b>注意：</b>token 是 天地图的移动端类型的token，要和web端、server端区分开来
     * @param token
     */
    public static void init(String token) {
        TOKEN = token;

        IMG_W = builderSourceAndLayer("img", "w");
        CIA_W = builderSourceAndLayer("cia", "w");

        VEC_W = builderSourceAndLayer("vec", "w");
        CVA_W = builderSourceAndLayer("cva", "w");

        TER_W = builderSourceAndLayer("ter", "w");
        CTA_W = builderSourceAndLayer("cta", "w");

    }


    private static RasterSource buildSource(String id, String p) {
        String name = id + "_" + p;
        // 创建一个ArrayList，用于存储所有的Tile URL
        ArrayList<String> tileUrls = new ArrayList<>();
        for (int i = 0; i < 8; i++) {

            // http://t0.tianditu.gov.cn/img_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=您的密钥
            String tileUrl = "https://t" + i + ".tianditu.gov.cn/" + name + "/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=" + id + "&STYLE=default&TILEMATRIXSET=" + p + "&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}&tk=" + TOKEN;
            // 添加到ArrayList中
            tileUrls.add(tileUrl);
        }
        TileSet tileSet = new TileSet("2.1.0", tileUrls.toArray(new String[0]));
        tileSet.setMaxZoom(18);
        tileSet.setMinZoom(0);

        return new RasterSource(name, tileSet);
    }

    private static SourceAndLayer builderSourceAndLayer(String id, String p) {
        SourceAndLayer sourceAndLayer = new SourceAndLayer();
        sourceAndLayer.source = buildSource(id, p);
        sourceAndLayer.layer = new RasterLayer(sourceAndLayer.source.getId(), sourceAndLayer.source.getId());
        return sourceAndLayer;
    }

    public static String getToken() {
        return TOKEN;
    }


    public static class SourceAndLayer {
        public RasterSource source;
        public RasterLayer layer;

        public SourceAndLayer() {
        }

    }
}



