package mekanism.common.lib.effect;

import mekanism.common.lib.Color;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class CustomEffect {

    private final int GRID_SIZE;
    private final ResourceLocation texture;

    protected final RandomSource rand = RandomSource.create();

    private Vec3 pos = new Vec3(0, 0, 0);
    private Color color = Color.rgbai(255, 255, 255, 255);
    private float scale = 1F;

    protected int ticker;

    public CustomEffect(ResourceLocation texture) {
        this(texture, 4);
    }

    public CustomEffect(ResourceLocation texture, int gridSize) {
        this.texture = texture;
        this.GRID_SIZE = gridSize;
    }

    protected Vec3 randVec() {
        return new Vec3(rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5).normalize();
    }

    public boolean tick() {
        ticker++;
        return false;
    }

    public void setPos(Vec3 pos) {
        this.pos = pos;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public Vec3 getPos(float partialTick) {
        return pos;
    }

    public float getScale() {
        return scale;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public int getTextureGridSize() {
        return GRID_SIZE;
    }
}
