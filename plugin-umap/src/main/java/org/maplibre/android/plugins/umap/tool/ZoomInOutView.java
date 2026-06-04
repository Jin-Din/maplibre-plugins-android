package org.maplibre.android.plugins.umap.tool;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.jetbrains.annotations.Nullable;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.plugins.umap.R;


public class ZoomInOutView extends LinearLayout {



    private ZoomInOutTool zoomInOutTool;

//    private MapView mapView;
//    private MapLibreMap maplibreMap;
//


    private Button btnZoomIn;
    private Button btnZoomOut;


//    public ZoomInOutView(Context context, MapView mapView, MapLibreMap maplibreMap) {
//        super(context);
//        this.mapView = mapView;
//        this.maplibreMap = maplibreMap;
//        init(context, null);
//    }

    // 标准构造函数 - 必须提供
    public ZoomInOutView(Context context) {
        super(context);
        init(context, null);
    }
    // 标准构造函数 - 必须提供（XML 布局使用）
    public ZoomInOutView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    // 标准构造函数 - 必须提供
    public ZoomInOutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     *
     * 可手动绑定ZoomInOutTool
     * @param zoomInOutTool
     */
    public void bindingTool(ZoomInOutTool zoomInOutTool) {
        this.zoomInOutTool = zoomInOutTool;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // 获取父容器
        ViewParent parent = getParent();

        if (parent instanceof MapView) {
            MapView mapView = (MapView) parent;
            // 现在可以操作父容器了
            mapView.getMapAsync(maplibreMap -> {
                this.zoomInOutTool = new ZoomInOutTool(mapView, maplibreMap);
            });
        }
    }

    // 初始化布局
    private void init(Context context, AttributeSet attrs) {

        // 设置垂直排列
        setOrientation(VERTICAL);
        setElevation(4f);
        //设置内边距，避免按钮紧贴圆角（可根据视觉效果调整，这里设0让按钮填满）
        int padding = (int) context.getResources().getDimension(R.dimen.maplibre_four_dp);
        setPadding(padding, padding, padding, padding);

        // 加载按钮布局
        LayoutInflater.from(context).inflate(R.layout.maplibre_zoom_in_out_view, this, true);

        // 应用圆角阴影背景
        setBackgroundResource(R.drawable.zoom_in_out_rounded_layout_bg);


        btnZoomIn = findViewById(R.id.btn_zoom_in);
        btnZoomOut = findViewById(R.id.btn_zoom_out);

        btnZoomIn.setOnClickListener(v -> {
            if (zoomInOutTool != null)
                zoomInOutTool.zoomIn();
        });

        btnZoomOut.setOnClickListener(v -> {
            if (zoomInOutTool != null)
                zoomInOutTool.zoomOut();
        });
    }

    public FrameLayout.LayoutParams getBottomEnd() {
        return getViewLayoutParams(Gravity.BOTTOM | Gravity.END);
    }

    public FrameLayout.LayoutParams getBottomStart() {
        return getViewLayoutParams(Gravity.BOTTOM | Gravity.START);
    }

    public FrameLayout.LayoutParams getTopEnd() {
        return getViewLayoutParams(Gravity.TOP | Gravity.END);
    }

    public FrameLayout.LayoutParams getTopStart() {
        return getViewLayoutParams(Gravity.TOP | Gravity.START);
    }


    private FrameLayout.LayoutParams getViewLayoutParams(int gravity) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );

        int left = 25, top = 25, right = 25, bottom = 25;
        layoutParams.setMargins(left, top, right, bottom);
        // support RTL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            layoutParams.setMarginStart(left);
            layoutParams.setMarginEnd(right);
        }

        layoutParams.gravity = gravity;

        return layoutParams;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            setAlpha(1.0f);
            setVisibility(View.VISIBLE);

        } else {
            setAlpha(0.0f);
            setVisibility(View.INVISIBLE);
        }
    }
}