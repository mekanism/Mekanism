package mekanism.client.render.lib;

import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.common.lib.Color;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;

public class Vertex {

    private Vec3 pos;
    private Vec3 normal;

    private Color color;

    // 0 to 16
    private float texU, texV;
    // 0 to 0xF0
    private int overlayU, overlayV;
    // 0 to 0xF0
    private int lightU, lightV;

    public Vertex() {
    }

    public Vertex(Vec3 pos, Vec3 normal, Color color, float texU, float texV, int overlayU, int overlayV, int lightU, int lightV) {
        this.pos = pos;
        this.normal = normal;
        this.color = color;
        this.texU = texU;
        this.texV = texV;
        this.overlayU = overlayU;
        this.overlayV = overlayV;
        this.lightU = lightU;
        this.lightV = lightV;
    }

    public static Vertex create(Vec3 pos, Vec3 normal, Color color, TextureAtlasSprite sprite, float texU, float texV, int overlayU, int overlayV, int lightU,
          int lightV) {
        return new Vertex(pos, normal, color, sprite.getU(texU), sprite.getV(texV), overlayU, overlayV, lightU, lightV);
    }

    public static Vertex create(Vec3 pos, Vec3 normal, Color color, TextureAtlasSprite sprite, float texU, float texV, int lightU, int lightV) {
        return create(pos, normal, color, sprite, texU, texV, OverlayTexture.NO_WHITE_U, OverlayTexture.WHITE_OVERLAY_V, lightU, lightV);
    }

    public static Vertex create(Vec3 pos, Vec3 normal, TextureAtlasSprite sprite, float u, float v) {
        return create(pos, normal, Color.WHITE, sprite, u, v, 0, 0);
    }

    public Vec3 getPos() {
        return pos;
    }

    public Vec3 getNormal() {
        return normal;
    }

    public Color getColor() {
        return color;
    }

    public float getTexU() {
        return texU;
    }

    public float getTexV() {
        return texV;
    }

    public int getOverlayU() {
        return overlayU;
    }

    public int getOverlayV() {
        return overlayV;
    }

    public int getRawLightU() {
        return lightU;
    }

    public int getRawLightV() {
        return lightV;
    }

    public Vertex color(Color color) {
        this.color = color;
        return this;
    }

    public Vertex pos(Vec3 pos) {
        this.pos = pos;
        return this;
    }

    public Vertex normal(Vec3 normal) {
        this.normal = normal;
        return this;
    }

    public Vertex texRaw(float u, float v) {
        texU = u;
        texV = v;
        return this;
    }

    public Vertex overlay(int u, int v) {
        overlayU = u;
        overlayV = v;
        return this;
    }

    public Vertex lightRaw(int u, int v) {
        lightU = u;
        lightV = v;
        return this;
    }

    public Vertex light(int u, int v) {
        return lightRaw(u << 4, v << 4);
    }

    public Vertex copy() {
        return new Vertex(pos, normal, color, texU, texV, overlayU, overlayV, lightU, lightV);
    }

    public void write(VertexConsumer consumer) {
        consumer.vertex(pos.x, pos.y, pos.z);
        consumer.color(color.rf(), color.gf(), color.bf(), color.af());
        consumer.uv(texU, texV);
        consumer.overlayCoords(overlayU, overlayV);
        consumer.uv2(lightU, lightV);
        consumer.normal((float) normal.x, (float) normal.y, (float) normal.z);
        consumer.endVertex();
    }
}
