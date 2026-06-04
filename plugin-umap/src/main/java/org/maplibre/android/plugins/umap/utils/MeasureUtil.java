package org.maplibre.android.plugins.umap.utils;

/**
 *
 */
public class MeasureUtil {

    /**
     * 地球半径（米），用于计算弧长
     */
    private static final double EARTH_RADIUS = 6378137.0;

    /**
     * 将角度转换为弧度
     */
    private static double toRadians(double degree) {
        return degree * Math.PI / 180.0;
    }

    /**
     * 计算两点之间的距离，单位:米
     * @param lng1
     * @param lat1
     * @param lng2
     * @param lat2
     * @return
     */
    public static double getDistance(Double lng1, Double lat1, Double lng2, Double lat2) {
        double radLat1 = lat1 * 0.017453292519943295D;
        double radLat2 = lat2 * 0.017453292519943295D;
        double a = radLat1 - radLat2;
        double b = (lng1 - lng2) * 0.017453292519943295D;
        double s = 2.0D * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2.0D), 2.0D) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2.0D), 2.0D)));
        s *= 6378137.0D;
        s = (double) Math.round(s * 10000.0D) / 10000.0D;
        return s;
    }


    /**
     * 计算多边形的面积（平方米）
     *
     * @param coordinates 坐标数组，格式: [[lng, lat], [lng, lat], ...]
     * @return 面积（平方米）
     */
    public static double computeArea(double[][] coordinates) {
        validateCoordinates(coordinates);

        int n = coordinates.length;
        double[] lons = extractLongitudes(coordinates);
        double[] lats = extractLatitudes(coordinates);

        // 计算原始经纬度下的有向面积（度为单位）
        double areaDeg = 0.0;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            areaDeg += (lons[i] * lats[j] - lons[j] * lats[i]);
        }
        areaDeg = Math.abs(areaDeg) / 2.0;

        // 获取多边形中心点（用于转换系数计算）
        double[] centroidDeg = calculateCentroidDegrees(lons, lats, areaDeg);

        // 将经纬度面积转换为平方米
        double latRad = toRadians(centroidDeg[1]);
        double meterPerDegLon = Math.cos(latRad) * EARTH_RADIUS * Math.PI / 180.0;
        double meterPerDegLat = EARTH_RADIUS * Math.PI / 180.0;

        return areaDeg * meterPerDegLon * meterPerDegLat;
    }

    /**
     * 计算多边形的质心坐标（经纬度）
     *
     * @param coordinates 坐标数组，格式: [[lng, lat], [lng, lat], ...]
     * @return double[2] -> {质心经度, 质心纬度}
     */
    public static double[] computeCentroid(double[][] coordinates) {
        validateCoordinates(coordinates);

        int n = coordinates.length;
        double[] lons = extractLongitudes(coordinates);
        double[] lats = extractLatitudes(coordinates);

        // 计算原始经纬度下的有向面积（度为单位）
        double areaDeg = 0.0;
        double cx = 0.0;
        double cy = 0.0;

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            double cross = lons[i] * lats[j] - lons[j] * lats[i];
            areaDeg += cross;
            cx += (lons[i] + lons[j]) * cross;
            cy += (lats[i] + lats[j]) * cross;
        }

        areaDeg = Math.abs(areaDeg) / 2.0;

        // 计算粗略质心（度坐标）
        double factor = 1.0 / (6.0 * areaDeg);
        double roughCentroidLon = cx * factor;
        double roughCentroidLat = cy * factor;

        // 二次更精确质心计算（用米坐标）
        return preciseCentroidInMeters(lons, lats, roughCentroidLon, roughCentroidLat);
    }

    /**
     * 验证坐标数组格式
     */
    private static void validateCoordinates(double[][] coordinates) {
        if (coordinates == null || coordinates.length < 3) {
            throw new IllegalArgumentException("至少需要3个点构成多边形");
        }

        for (int i = 0; i < coordinates.length; i++) {
            if (coordinates[i] == null || coordinates[i].length < 2) {
                throw new IllegalArgumentException("第 " + i + " 个坐标点格式错误，应为 [经度, 纬度]");
            }
        }
    }

    /**
     * 提取经度数组
     */
    private static double[] extractLongitudes(double[][] coordinates) {
        double[] lons = new double[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            lons[i] = coordinates[i][0];
        }
        return lons;
    }

    /**
     * 提取纬度数组
     */
    private static double[] extractLatitudes(double[][] coordinates) {
        double[] lats = new double[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            lats[i] = coordinates[i][1];
        }
        return lats;
    }

    /**
     * 计算粗略质心（度坐标）
     */
    private static double[] calculateCentroidDegrees(double[] lons, double[] lats, double areaDeg) {
        int n = lons.length;
        double cx = 0.0;
        double cy = 0.0;

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            double cross = lons[i] * lats[j] - lons[j] * lats[i];
            cx += (lons[i] + lons[j]) * cross;
            cy += (lats[i] + lats[j]) * cross;
        }

        double factor = 1.0 / (6.0 * areaDeg);
        return new double[]{cx * factor, cy * factor};
    }

    /**
     * 通过将经纬度转换为局部米坐标（等矩形投影）来计算更精确的质心
     */
    private static double[] preciseCentroidInMeters(double[] lons, double[] lats,
                                                    double refLon, double refLat) {
        int n = lons.length;
        double refLonRad = toRadians(refLon);
        double refLatRad = toRadians(refLat);
        double cosLat = Math.cos(refLatRad);

        // 转换为米坐标（X, Y）
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            double dx = toRadians(lons[i] - refLon) * EARTH_RADIUS * cosLat;
            double dy = toRadians(lats[i] - refLat) * EARTH_RADIUS;
            x[i] = dx;
            y[i] = dy;
        }

        // 计算米坐标下的质心
        double signedArea = 0.0;
        double cx = 0.0;
        double cy = 0.0;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            double cross = x[i] * y[j] - x[j] * y[i];
            signedArea += cross;
            cx += (x[i] + x[j]) * cross;
            cy += (y[i] + y[j]) * cross;
        }

        signedArea /= 2.0;
        double factor = 1.0 / (6.0 * signedArea);
        double centerX = cx * factor;
        double centerY = cy * factor;

        // 转换回经纬度
        double centroidLon = refLon + Math.toDegrees(centerX / (EARTH_RADIUS * cosLat));
        double centroidLat = refLat + Math.toDegrees(centerY / EARTH_RADIUS);

        return new double[]{centroidLon, centroidLat};
    }

    // ==================== 测试示例 ====================

//    public static void main(String[] args) {
//        // 示例：一个正方形（近似 100m x 100m）
//        double[][] coordinates = {
//                {116.397428, 39.908798},
//                {116.398428, 39.908798},
//                {116.398428, 39.909798},
//                {116.397428, 39.909798}
//        };
//
//        // 单独计算面积
//        double area = computeArea(coordinates);
//        System.out.printf("面积: %.2f 平方米\n", area);
//
//        // 单独计算质心
//        double[] centroid = computeCentroid(coordinates);
//        System.out.printf("质心经度: %.6f\n", centroid[0]);
//        System.out.printf("质心纬度: %.6f\n", centroid[1]);
//    }
}