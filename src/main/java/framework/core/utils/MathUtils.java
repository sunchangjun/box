package framework.core.utils;

import java.math.BigDecimal;

/**
 * 计算工具类
 */
public class MathUtils {

    // ================================================================
    // Constants
    // ================================================================

    // ================================================================
    // Fields
    // ================================================================

    // ================================================================
    // Constructors
    // ================================================================

    // ================================================================
    // Methods from/for super Interfaces or SuperClass
    // ================================================================

    // ================================================================
    // Public or Protected Methods
    // ================================================================

    /**
     * 计算百分比
     */
    public static int toPercent(long n, long d) {
        return Math.round((100 * n) / d);
    }

    /**
     * 计算百分比
     */
    public static int toPercent(double n, double d) {
        return (int) Math.round((100 * n) / d);
    }

    /**
     * data保留scale位小数
     */
    public static double round(double data, int scale, int mode) {
        BigDecimal bd = new BigDecimal(data);
        bd = bd.setScale(scale, mode);
        return bd.doubleValue();
    }

    /**
     * data保留scale位小数，采用四舍五入策略
     */
    public static double round(double data, int scale) {
        return round(data, scale, BigDecimal.ROUND_HALF_UP);
    }

    // ================================================================
    // Getter & Setter
    // ================================================================

    // ================================================================
    // Private Methods
    // ================================================================

    // ================================================================
    // Inner or Anonymous Class
    // ================================================================

    // ================================================================
    // Test Methods
    // ================================================================
}
