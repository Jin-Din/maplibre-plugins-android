package org.maplibre.android.plugins.umap;

public class UMapStyles {

    public String DEFAULT_STYLE = "http://210.74.129.84:8520/styles/vector_style_3857_proxy.json";

    private static final String TAG = "UMapStyles";
    private  UMapStyleOption _uMapStyleOption;

    private static UMapStyles _instance;
    public static UMapStyles getInstance(UMapStyleOption options) {
        if(_instance == null)
            _instance = new UMapStyles(options);
        return _instance;
    }

    private UMapStyles(UMapStyleOption options) {
        _uMapStyleOption = options != null ? options : new UMapStyleOption();
        freshStyle();
    }

    private  void freshStyle(){
        // 刷新样式,把token注入到地址中

        this.DEFAULT_STYLE = "http://210.74.129.84:8520/styles/vector_style_3857_proxy.json";
    }


    public class UMapStyleOption{

        public UMapStyleOption(){}
    }


}
