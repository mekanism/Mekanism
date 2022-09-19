package mekanism.client.render.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.function.Consumer;
import mekanism.common.lib.Color;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.NotNull;

public class Quad {

    private final Vertex[] vertices;
    private Direction side;
    private TextureAtlasSprite sprite;
    private int tintIndex;
    private boolean shade;
    private boolean hasAmbientOcclusion;

    public Quad(TextureAtlasSprite sprite, Direction side, Vertex[] vertices) {
        this(sprite, side, vertices, -1, false, true);
    }

    public Quad(TextureAtlasSprite sprite, Direction side, Vertex[] vertices, int tintIndex, boolean shade, boolean hasAmbientOcclusion) {
        this.sprite = sprite;
        this.side = side;
        this.vertices = vertices;
        this.tintIndex = tintIndex;
        this.shade = shade;
        this.hasAmbientOcclusion = hasAmbientOcclusion;
    }

    public Quad(BakedQuad quad) {
        vertices = new Vertex[4];
        side = quad.getDirection();
        sprite = quad.getSprite();
        tintIndex = quad.getTintIndex();
        shade = quad.isShade();
        hasAmbientOcclusion = quad.hasAmbientOcclusion();
        new BakedQuadUnpacker().putBulkData(new PoseStack().last(), quad, 1, 1, 1, 1, 0, OverlayTexture.NO_OVERLAY, true);
    }

    public TextureAtlasSprite getTexture() {
        return sprite;
    }

    public void setTexture(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    public int getTint() {
        return tintIndex;
    }

    public void setTint(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    public void vertexTransform(Consumer<Vertex> transformation) {
        for (Vertex v : vertices) {
            transformation.accept(v);
        }
    }

    public boolean transform(QuadTransformation... transformations) {
        boolean transformed = false;
        for (QuadTransformation transform : transformations) {
            transformed |= transform.transform(this);
        }
        return transformed;
    }

    public Vertex[] getVertices() {
        return vertices;
    }

    public void setSide(Direction side) {
        this.side = side;
    }

    public Direction getSide() {
        return side;
    }

    public boolean isShade() {
        return shade;
    }

    public void setShade(boolean shade) {
        this.shade = shade;
    }

    public boolean hasAmbientOcclusion() {
        return hasAmbientOcclusion;
    }

    public void setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
        this.hasAmbientOcclusion = hasAmbientOcclusion;
    }

    public BakedQuad bake() {
        BakedQuad[] quads = new BakedQuad[1];
        QuadBakingVertexConsumer quadBaker = new QuadBakingVertexConsumer(q -> quads[0] = q);
        quadBaker.setSprite(sprite);
        quadBaker.setDirection(side);
        quadBaker.setTintIndex(tintIndex);
        quadBaker.setShade(shade);
        quadBaker.setHasAmbientOcclusion(hasAmbientOcclusion);
        for (Vertex vertex : vertices) {
            vertex.write(quadBaker);
        }
        return quads[0];
    }

    public Quad copy() {
        Vertex[] newVertices = new Vertex[vertices.length];
        for (int i = 0; i < newVertices.length; i++) {
            newVertices[i] = vertices[i].copy();
        }
        return new Quad(sprite, side, newVertices, tintIndex, shade, hasAmbientOcclusion);
    }

    public Quad flip() {
        Vertex[] flipped = new Vertex[vertices.length];
        for (int i = 0; i < flipped.length; i++) {
            flipped[i] = vertices[i].flip();
        }
        return new Quad(sprite, side.getOpposite(), flipped, tintIndex, shade, hasAmbientOcclusion);
    }

    private class BakedQuadUnpacker implements VertexConsumer {

        private Vertex vertex = new Vertex();
        private int vertexIndex = 0;

        @NotNull
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            vertex.pos(new Vec3(x, y, z));
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            vertex.color(red, green, blue, alpha);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer uv(float u, float v) {
            vertex.texRaw(u, v);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            vertex.overlay(u, v);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer uv2(int u, int v) {
            vertex.lightRaw(u, v);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer normal(float x, float y, float z) {
            vertex.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            if (vertexIndex != vertices.length) {
                vertices[vertexIndex++] = vertex;
                vertex = new Vertex();
            }
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            //We don't support having a default color
        }

        @Override
        public void unsetDefaultColor() {
            //We don't support having a default color
        }
    }

    public static class Builder {

        private TextureAtlasSprite texture;
        private final Direction side;
        private Color color = Color.WHITE;

        private Vec3 vec1, vec2, vec3, vec4;

        private float minU, minV, maxU, maxV;
        private int lightU, lightV;

        private int tintIndex = -1;
        private boolean shade;
        private boolean hasAmbientOcclusion = true;
        private boolean contractUVs = true;

        public Builder(TextureAtlasSprite texture, Direction side) {
            this.texture = texture;
            this.side = side;
        }

        public Builder light(int light) {
            return light(LightTexture.block(light), LightTexture.sky(light));
        }

        public Builder light(int u, int v) {
            this.lightU = u;
            this.lightV = v;
            return this;
        }

        public Builder uv(float minU, float minV, float maxU, float maxV) {
            this.minU = minU;
            this.minV = minV;
            this.maxU = maxU;
            this.maxV = maxV;
            return this;
        }

        public Builder tex(TextureAtlasSprite texture) {
            this.texture = texture;
            return this;
        }

        public Builder tint(int tintIndex) {
            this.tintIndex = tintIndex;
            return this;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public Builder setShade(boolean shade) {
            this.shade = shade;
            return this;
        }

        public Builder setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
            this.hasAmbientOcclusion = hasAmbientOcclusion;
            return this;
        }

        public Builder contractUVs(boolean contractUVs) {
            this.contractUVs = contractUVs;
            return this;
        }

        public Builder pos(Vec3 tl, Vec3 bl, Vec3 br, Vec3 tr) {
            this.vec1 = tl;
            this.vec2 = bl;
            this.vec3 = br;
            this.vec4 = tr;
            return this;
        }

        public Builder rect(Vec3 start, double width, double height) {
            return rect(start, width, height, 1F / 16F); // default to 1/16 scale
        }

        // start = bottom left
        public Builder rect(Vec3 start, double width, double height, double scale) {
            start = start.scale(scale);
            Vec3 end;
            if (side.getAxis().isHorizontal()) {
                Vec3i normal = side.getNormal();
                end = start.add(normal.getZ() * width * scale, 0, normal.getX() * width * scale);
                if (side.getAxis() == Axis.X) {
                    //Wind vertices in a different order so that it faces the correct direction
                    return pos(start, start.add(0, height * scale, 0), end.add(0, height * scale, 0), end);
                }
            } else {
                end = start.add(width * scale, 0, 0);
            }
            return pos(start.add(0, height * scale, 0), start, end, end.add(0, height * scale, 0));
        }

        public Quad build() {
            Vertex[] vertices = new Vertex[4];
            Vector3f normal = new Vector3f(vec3.subtract(vec2).cross(vec1.subtract(vec2)).normalize());
            vertices[0] = Vertex.create(vec1, normal, color, texture, minU, minV).light(lightU, lightV);
            vertices[1] = Vertex.create(vec2, normal, color, texture, minU, maxV).light(lightU, lightV);
            vertices[2] = Vertex.create(vec3, normal, color, texture, maxU, maxV).light(lightU, lightV);
            vertices[3] = Vertex.create(vec4, normal, color, texture, maxU, minV).light(lightU, lightV);
            Quad quad = new Quad(texture, side, vertices, tintIndex, shade, hasAmbientOcclusion);
            if (contractUVs) {
                QuadUtils.contractUVs(quad);
            }
            return quad;
        }
    }
}
