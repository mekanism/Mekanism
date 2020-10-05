package mekanism.client.render.lib;

import mekanism.common.lib.Color;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.vector.Vector3d;

public class Vertex {

    // I'm not sure why Forge packs light this way but w/e
    private static final float LIGHT_PACK_FACTOR = 240F / Short.MAX_VALUE;

    private Vector3d pos;
    private Vector3d normal;

    private Color color;

    // 0 to 16
    private float texU, texV;
    // 0 to 1
    private float lightU, lightV;

    public Vertex() {
    }

    public Vertex(Vector3d pos, Vector3d normal, Color color, float texU, float texV, float lightU, float lightV) {
        this.pos = pos;
        this.normal = normal;
        this.color = color;
        this.texU = texU;
        this.texV = texV;
        this.lightU = lightU;
        this.lightV = lightV;
    }

    public static Vertex create(Vector3d pos, Vector3d normal, Color color, TextureAtlasSprite sprite, float texU, float texV, float lightU, float lightV) {
        return new Vertex(pos, normal, color, sprite.getInterpolatedU(texU), sprite.getInterpolatedV(texV), lightU, lightV);
    }

    public static Vertex create(Vector3d pos, Vector3d normal, TextureAtlasSprite sprite, float u, float v) {
        return create(pos, normal, Color.WHITE, sprite, u, v, 0, 0);
    }

    public Vector3d getPos() {
        return pos;
    }

    public Vector3d getNormal() {
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

    public float getLightU() {
        return lightU;
    }

    public float getLightV() {
        return lightV;
    }

    public Vertex color(Color color) {
        this.color = color;
        return this;
    }

    public Vertex pos(Vector3d pos) {
        this.pos = pos;
        return this;
    }

    public Vertex normal(Vector3d normal) {
        this.normal = normal;
        return this;
    }

    public Vertex texRaw(float u, float v) {
        texU = u;
        texV = v;
        return this;
    }

    public Vertex lightRaw(float u, float v) {
        lightU = u;
        lightV = v;
        return this;
    }

    public Vertex light(float u, float v) {
        return lightRaw(u * LIGHT_PACK_FACTOR, v * LIGHT_PACK_FACTOR);
    }

    public Vertex copy() {
        return new Vertex(pos, normal, color, texU, texV, lightU, lightV);
    }

    public float[][] pack(VertexFormat format) {
        float[][] ret = new float[format.getElements().size()][4];
        for (int i = 0; i < format.getElements().size(); i++) {
            VertexFormatElement element = format.getElements().get(i);
            switch (element.getUsage()) {
                case POSITION:
                    ret[i][0] = (float) pos.getX();
                    ret[i][1] = (float) pos.getY();
                    ret[i][2] = (float) pos.getZ();
                    break;
                case NORMAL:
                    ret[i][0] = (float) normal.getX();
                    ret[i][1] = (float) normal.getY();
                    ret[i][2] = (float) normal.getZ();
                    break;
                case COLOR:
                    ret[i][0] = color.rf();
                    ret[i][1] = color.gf();
                    ret[i][2] = color.bf();
                    ret[i][3] = color.af();
                    break;
                case UV:
                    if (element.getIndex() == 0) {
                        ret[i][0] = texU;
                        ret[i][1] = texV;
                    } else if (element.getIndex() == 2) {
                        ret[i][0] = lightU;
                        ret[i][1] = lightV;
                    }
                    break;
                default:
                    break;
            }
        }
        return ret;
    }
}
