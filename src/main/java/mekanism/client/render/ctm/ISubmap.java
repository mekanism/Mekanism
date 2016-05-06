package mekanism.client.render.ctm;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface ISubmap {

    float getYOffset();

    float getXOffset();

    float getWidth();

    float getHeight();

    float getInterpolatedU(TextureAtlasSprite sprite, float u);

    float getInterpolatedV(TextureAtlasSprite sprite, float v);

    float[] toArray();

    ISubmap normalize();

    ISubmap relativize();
}
