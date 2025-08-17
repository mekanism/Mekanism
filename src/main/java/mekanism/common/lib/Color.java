package mekanism.common.lib;

import mekanism.common.util.StatUtils;

public class Color {

    public static final Color WHITE = rgbad(1F, 1F, 1F, 1F);
    public static final Color BLACK = rgbad(0F, 0F, 0F, 1F);

    private final double r, g, b, a;

    private Color(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public int r() {
        return (int) Math.round(r * 255D);
    }

    public int g() {
        return (int) Math.round(g * 255D);
    }

    public int b() {
        return (int) Math.round(b * 255D);
    }

    public int a() {
        return (int) Math.round(a * 255D);
    }

    public float rf() {
        return (float) r;
    }

    public float gf() {
        return (float) g;
    }

    public float bf() {
        return (float) b;
    }

    public float af() {
        return (float) a;
    }

    public double rd() {
        return r;
    }

    public double gd() {
        return g;
    }

    public double bd() {
        return b;
    }

    public double ad() {
        return a;
    }

    public Color alpha(double alpha) {
        return new Color(r, g, b, alpha);
    }

    public int rgba() {
        return (r() & 0xFF) << 24 | (g() & 0xFF) << 16 | (b() & 0xFF) << 8 | (a() & 0xFF);
    }

    public int argb() {
        return (a() & 0xFF) << 24 | rgb();
    }

    public int rgb() {
        return (r() & 0xFF) << 16 | (g() & 0xFF) << 8 | (b() & 0xFF);
    }

    public int[] rgbaArray() {
        return new int[]{r(), g(), b(), a()};
    }

    public int[] argbArray() {
        return new int[]{a(), r(), g(), b()};
    }

    public int[] rgbArray() {
        return new int[]{r(), g(), b()};
    }

    /**
     * Blends this color into another, by a given scale.
     *
     * @param to    color to blend with
     * @param scale a scale (0 -> 1) defining the effect of the second color
     *
     * @return blended color
     */
    public Color blend(Color to, double scale) {
        return rgbad(r + (to.r - r) * scale,
              g + (to.g - g) * scale,
              b + (to.b - b) * scale,
              a + (to.a - a) * scale);
    }

    public Color blendOnto(Color baseColor) {
        double sR = rd(), sG = gd(), sB = bd(), sA = ad();
        double dR = baseColor.rd(), dG = baseColor.gd(), dB = baseColor.bd(), dA = baseColor.ad();

        double rR = sR * sA + dR * (1 - sA);
        double rG = sG * sA + dG * (1 - sA);
        double rB = sB * sA + dB * (1 - sA);
        double rA = dA + sA * (1 - dA);//dA * 1D + sA * (1 - dA);
        return rgbad(rR, rG, rB, rA);
    }

    public Color darken(double amount) {
        double scale = 1 - amount;
        return rgbad(r * scale, g * scale, b * scale, a);
    }

    public Color toTint() {
        //Calculate actual tint from alpha in the same way we do in the shader. Ideally we would expose this somehow for resource packs
        // like is done for the shader but oh well
        double aDif = 1.0 - a;
        return rgbd(r * a + aDif,
              g * a + aDif,
              b * a + aDif
        );
    }

    public static Color blend(Color src, Color dest) {
        return src.blendOnto(dest);
    }

    public static Color rgbai(int r, int g, int b, int a) {
        return new Color(r / 255D, g / 255D, b / 255D, a / 255D);
    }

    public static Color rgbad(double r, double g, double b, double a) {
        return new Color(r, g, b, a);
    }

    public static Color rgba(int color) {
        return rgbai((color >> 24) & 0xFF, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    }

    public static Color rgba(int[] color) {
        return rgbai(color[0], color[1], color[2], color[3]);
    }

    public static Color argbi(int a, int r, int g, int b) {
        return rgbai(r, g, b, a);
    }

    public static Color argbd(double a, double r, double g, double b) {
        return rgbad(r, g, b, a);
    }

    public static Color argb(int color) {
        return argbi((color >> 24) & 0xFF, (color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    }

    public static Color argb(int[] color) {
        return argbi(color[0], color[1], color[2], color[3]);
    }

    public static Color rgbi(int r, int g, int b) {
        return rgbai(r, g, b, 255);
    }

    public static Color rgbd(double r, double g, double b) {
        return rgbad(r, g, b, 1D);
    }

    public static Color rgb(int color) {
        return rgbi((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    }

    public static Color rgb(int[] color) {
        return rgbi(color[0], color[1], color[2]);
    }

    public static Color hsv(double h, double s, double v) {
        double hueIndex = (h % 360) / 60D;
        int i = (int) hueIndex;
        double diff = hueIndex - i;
        double p = v * (1.0 - s);
        double q = v * (1.0 - (s * diff));
        double t = v * (1.0 - (s * (1.0 - diff)));

        return switch (i) {
            case 0 -> rgbd(v, t, p);
            case 1 -> rgbd(q, v, p);
            case 2 -> rgbd(p, v, t);
            case 3 -> rgbd(p, q, v);
            case 4 -> rgbd(t, p, v);
            default -> rgbd(v, p, q);
        };
    }

    //Works both directions
    public static int argbToFromABGR(int argb) {
        int red = argb >> 16 & 0xFF;
        int blue = argb & 0xFF;
        return argb & 0xFF00FF00 | blue << 16 | red;
    }

    public double[] hsvArray() {
        double min = StatUtils.min(r, g, b), max = StatUtils.max(r, g, b);
        double delta = max - min;
        double[] ret = new double[3];
        if (delta == 0) {
            ret[0] = 0;
        } else if (max == r) {
            ret[0] = ((g - b) / delta) % 6;
        } else if (max == g) {
            ret[0] = ((b - r) / delta) + 2;
        } else {
            ret[0] = ((r - g) / delta) + 4;
        }
        ret[0] *= 60;
        if (ret[0] < 0) {
            ret[0] += 360;
        }
        ret[1] = max == 0 ? 0 : delta / max;
        ret[2] = max;
        return ret;
    }

    public static int packOpaque(int rgb) {
        return rgb | 0xFF000000;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj instanceof Color other && r == other.r && g == other.g && b == other.b && a == other.a;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(r);
        result = 31 * result + Double.hashCode(g);
        result = 31 * result + Double.hashCode(b);
        result = 31 * result + Double.hashCode(a);
        return result;
    }

    @Override
    public String toString() {
        return "[Color: " + r + ", " + g + ", " + b + ", " + a + "]";
    }

    public interface ColorFunction {

        ColorFunction HEAT = level -> rgbai((int) Math.min(200, 400 * level), (int) Math.max(0, 200 - Math.max(0, -200 + 400 * level)), 0, 255);

        static ColorFunction scale(Color from, Color to) {
            return level -> from.blend(to, level);
        }

        Color getColor(float level);
    }
}