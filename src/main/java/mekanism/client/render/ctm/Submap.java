package mekanism.client.render.ctm;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class Submap implements ISubmap 
{
    private final float width, height;
    private final float xOffset, yOffset;

    private final SubmapNormalized normalized = new SubmapNormalized(this);
    
    public Submap(float w, float h, float x, float y)
    {
    	width = w;
    	height = h;
    	xOffset = x;
    	yOffset = y;
    }
    
    @Override
    public float getXOffset()
    {
    	return xOffset;
    }
    
    @Override
    public float getYOffset()
    {
    	return yOffset;
    }
    
    @Override
    public float getWidth()
    {
    	return width;
    }
    
    @Override
    public float getHeight()
    {
    	return height;
    }

    @Override
    public float getInterpolatedU(TextureAtlasSprite sprite, float u) {
        return sprite.getInterpolatedU(getXOffset() + u / getWidth());
    }

    @Override
    public float getInterpolatedV(TextureAtlasSprite sprite, float v) {
        return sprite.getInterpolatedV(getYOffset() + v / getWidth());
    }

    @Override
    public float[] toArray() {
        return new float[] { getXOffset(), getYOffset(), getXOffset() + getWidth(), getYOffset() + getHeight() };
    }

    @Override
    public SubmapNormalized normalize() {
        return normalized;
    }

    @Override
    public ISubmap relativize() {
        return this;
    }

    private static final float FACTOR = 16f;

    private static class SubmapNormalized implements ISubmap 
    {
        private final ISubmap parent;
        
        public SubmapNormalized(ISubmap submap)
        {
        	parent = submap;
        }

        @Override
        public float getXOffset() {
            return parent.getXOffset() / FACTOR;
        }

        @Override
        public float getYOffset() {
            return parent.getYOffset() / FACTOR;
        }

        @Override
        public float getWidth() {
            return parent.getWidth() / FACTOR;
        }

        @Override
        public float getHeight() {
            return parent.getHeight() / FACTOR;
        }

        @Override
        public ISubmap relativize() {
            return parent;
        }

        @Override
        public ISubmap normalize() {
            return this;
        }

        @Override
        public float getInterpolatedU(TextureAtlasSprite sprite, float u) {
            return parent.getInterpolatedU(sprite, u);
        }

        @Override
        public float getInterpolatedV(TextureAtlasSprite sprite, float v) {
            return parent.getInterpolatedV(sprite, v);
        }

        @Override
        public float[] toArray() {
            return parent.toArray();
        }
    }
}
