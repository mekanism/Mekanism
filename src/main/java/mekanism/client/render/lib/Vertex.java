package mekanism.client.render.lib;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import mekanism.common.lib.Color;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;

public class Vertex {

    private Vec3 pos;
    private Vector3f normal;

    //Store int representations of the colors so that we don't go between ints and doubles when unpacking and repacking a vertex
    private int red, green, blue, alpha;

    // 0 to 16
    private float texU, texV;
    // 0 to 0xF0
    private int overlayU, overlayV;
    // 0 to 0xF0
    private int lightU, lightV;

    public Vertex() {
    }

    public Vertex(Vec3 pos, Vector3f normal, Color color, float texU, float texV, int overlayU, int overlayV, int lightU, int lightV) {
        this(pos, normal, color.r(), color.g(), color.b(), color.a(), texU, texV, overlayU, overlayV, lightU, lightV);
    }

    public Vertex(Vec3 pos, Vector3f normal, int red, int green, int blue, int alpha, float texU, float texV, int overlayU, int overlayV, int lightU, int lightV) {
        this.pos = pos;
        this.normal = normal;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.texU = texU;
        this.texV = texV;
        this.overlayU = overlayU;
        this.overlayV = overlayV;
        this.lightU = lightU;
        this.lightV = lightV;
    }

    public static Vertex create(Vec3 pos, Vector3f normal, Color color, TextureAtlasSprite sprite, float texU, float texV, int overlayU, int overlayV, int lightU,
          int lightV) {
        return new Vertex(pos, normal, color, sprite.getU(texU), sprite.getV(texV), overlayU, overlayV, lightU, lightV);
    }

    public static Vertex create(Vec3 pos, Vector3f normal, Color color, TextureAtlasSprite sprite, float texU, float texV, int lightU, int lightV) {
        return create(pos, normal, color, sprite, texU, texV, OverlayTexture.NO_WHITE_U, OverlayTexture.WHITE_OVERLAY_V, lightU, lightV);
    }

    public static Vertex create(Vec3 pos, Vector3f normal, Color color, TextureAtlasSprite sprite, float u, float v) {
        return create(pos, normal, color, sprite, u, v, 0, 0);
    }

    public static Vertex create(Vec3 pos, Vector3f normal, TextureAtlasSprite sprite, float u, float v) {
        return create(pos, normal, Color.WHITE, sprite, u, v);
    }

    public Vec3 getPos() {
        return pos;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public Vec3 getNormalD() {
        return new Vec3(getNormal());
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
        return color(color.r(), color.g(), color.b(), color.a());
    }

    public Vertex color(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    public Vertex pos(Vec3 pos) {
        this.pos = pos;
        return this;
    }

    public Vertex normal(float x, float y, float z) {
        return normal(new Vector3f(x, y, z));
    }

    public Vertex normal(Vector3f normal) {
        this.normal = normal;
        return this;
    }

    public Vertex normal(Vec3 normal) {
        return normal(new Vector3f(normal));
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

    public Vertex flip() {
        return copy().normal(-normal.x(), -normal.y(), -normal.z());
    }

    public Vertex copy() {
        return new Vertex(pos, normal, red, green, blue, alpha, texU, texV, overlayU, overlayV, lightU, lightV);
    }

    public void write(VertexConsumer consumer) {
        consumer.vertex(pos.x, pos.y, pos.z);
        consumer.color(red, green, blue, alpha);
        consumer.uv(texU, texV);
        consumer.overlayCoords(overlayU, overlayV);
        consumer.uv2(lightU, lightV);
        consumer.normal(normal.x(), normal.y(), normal.z());
        consumer.endVertex();
    }
}
