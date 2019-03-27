package mekanism.common;

/**
 * Created by ben on 30/04/16.
 */
public class ColourRGBA {

    public byte valR;
    public byte valG;
    public byte valB;
    public byte valA;

    public ColourRGBA(double r, double g, double b, double a) {
        this((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
    }

    public ColourRGBA(int r, int g, int b, int a) {
        valR = (byte) r;
        valG = (byte) g;
        valB = (byte) b;
        valA = (byte) a;
    }

    public int rgba() {
        return (valR & 0xFF) << 24 | (valG & 0xFF) << 16 | (valB & 0xFF) << 8 | (valA & 0xFF);
    }

    public int argb() {
        return (valA & 0xFF) << 24 | (valR & 0xFF) << 16 | (valG & 0xFF) << 8 | (valB & 0xFF);
    }

    public void setRGBFromInt(int color) {
        valR = (byte) ((color >> 16) & 0xFF);
        valG = (byte) ((color >> 8) & 0xFF);
        valB = (byte) (color & 0xFF);
    }
}
