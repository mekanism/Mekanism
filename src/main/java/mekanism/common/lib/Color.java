package mekanism.common.lib;

public class Color {

    public int r, g, b, a;

    protected Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    protected Color(double r, double g, double b, double a) {
        this((int) (r * 255D), (int) (g * 255D), (int) (b * 255D), (int) (a * 255D));
    }

    public int rgba() {
        return (r & 0xFF) << 24 | (g & 0xFF) << 16 | (b & 0xFF) << 8 | (a & 0xFF);
    }

    public int argb() {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public int rgb() {
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    /**
     * Blends this color into another, by a given scale.
     * @param to color to blend with
     * @param scale a scale (0 -> 1) defining the effect of the second color
     * @return blended color
     */
    public Color blend(Color to, double scale) {
        return rgba((int) (r + (to.r - r) * scale),
                    (int) (g + (to.g - g) * scale),
                    (int) (b + (to.b - b) * scale),
                    (int) (a + (to.a - a) * scale));
    }

    public Color darken(double amount) {
        return rgba((int) (r * (1 - amount)),
                    (int) (g * (1 - amount)),
                    (int) (b * (1 - amount)),
                    (int) (a * (1 - amount)));
    }

    public static Color rgba(int r, int g, int b, int a) {
        return new Color(r, g, b, a);
    }

    public static Color rgba(double r, double g, double b, double a) {
        return new Color(r, g, b, a);
    }

    public static Color rgba(int color) {
        return rgba((color >> 24) & 0xFF, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    }

    public static Color argb(int a, int r, int g, int b) {
        return rgba(r, g, b, a);
    }

    public static Color argb(double a, double r, double g, double b) {
        return rgba(r, g, b, a);
    }

    public static Color argb(int color) {
        return argb((color >> 24) & 0xFF, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    }

    public static Color rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    public static Color rgb(double r, double g, double b) {
        return rgba(r, g, b, 1D);
    }

    public static Color rgb(int color) {
        return rgb((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    }

    public static int packOpaque(int rgb) {
        return rgb | (0xFF << 24);
    }
}