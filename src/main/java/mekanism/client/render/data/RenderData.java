package mekanism.client.render.data;

import mekanism.api.Coord4D;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class RenderData {

    public Coord4D location;

    public int height;
    public int length;
    public int width;

    public abstract TextureAtlasSprite getTexture();

    public abstract boolean isGaseous();

    public abstract int getColorARGB(float scale);

    public int calculateGlowLight(int light) {
        return light;
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + location.hashCode();
        code = 31 * code + height;
        code = 31 * code + length;
        code = 31 * code + width;
        return code;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof RenderData) {
            RenderData data = (RenderData) object;
            return data.height == height && data.length == length && data.width == width;
        }
        return false;
    }
}