package mekanism.client.render.lib;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.common.lib.Color;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class Quad {

    private static final VertexFormat FORMAT = DefaultVertexFormat.BLOCK;
    private static final int SIZE = DefaultVertexFormat.BLOCK.getElements().size();

    private final Vertex[] vertices;
    private Direction side;
    private TextureAtlasSprite sprite;
    private int tintIndex = -1;
    private boolean applyDiffuseLighting;

    public Quad(TextureAtlasSprite sprite, Direction side, Vertex[] vertices) {
        this(sprite, side, vertices, -1, false);
    }

    public Quad(TextureAtlasSprite sprite, Direction side, Vertex[] vertices, int tintIndex, boolean applyDiffuseLighting) {
        this.sprite = sprite;
        this.side = side;
        this.vertices = vertices;
        this.tintIndex = tintIndex;
        this.applyDiffuseLighting = applyDiffuseLighting;
    }

    public Quad(BakedQuad quad) {
        vertices = new Vertex[4];
        quad.pipe(new BakedQuadUnpacker());
    }

    public TextureAtlasSprite getTexture() {
        return sprite;
    }

    public void setTexture(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    public void vertexTransform(Consumer<Vertex> transformation) {
        for (Vertex v : vertices) {
            transformation.accept(v);
        }
    }

    public Quad transform(QuadTransformation... transformations) {
        for (QuadTransformation transform : transformations) {
            transform.transform(this);
        }
        return this;
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

    public boolean getApplyDiffuseLighting() {
        return applyDiffuseLighting;
    }

    public void setApplyDiffuseLighting(boolean applyDiffuseLighting) {
        this.applyDiffuseLighting = applyDiffuseLighting;
    }

    public BakedQuad bake() {
        int[] ret = new int[FORMAT.getIntegerSize() * 4];
        for (int v = 0; v < vertices.length; v++) {
            float[][] packed = vertices[v].pack(FORMAT);
            for (int e = 0; e < SIZE; e++) {
                LightUtil.pack(packed[e], ret, DefaultVertexFormat.BLOCK, v, e);
            }
        }
        return new BakedQuad(ret, tintIndex, side, sprite, applyDiffuseLighting);
    }

    public Quad copy() {
        Vertex[] newVertices = new Vertex[4];
        for (int i = 0; i < 4; i++) {
            newVertices[i] = vertices[i].copy();
        }
        return new Quad(sprite, side, newVertices, tintIndex, applyDiffuseLighting);
    }

    public Quad flip() {
        Vertex[] flipped = new Vertex[4];
        flipped[3] = vertices[0].copy().normal(vertices[0].getNormal().scale(-1));
        flipped[2] = vertices[1].copy().normal(vertices[1].getNormal().scale(-1));
        flipped[1] = vertices[2].copy().normal(vertices[2].getNormal().scale(-1));
        flipped[0] = vertices[3].copy().normal(vertices[3].getNormal().scale(-1));
        return new Quad(sprite, side.getOpposite(), flipped, tintIndex, applyDiffuseLighting);
    }

    private class BakedQuadUnpacker implements IVertexConsumer {

        private Vertex vertex = new Vertex();
        private int vertexIndex = 0;

        @Nonnull
        @Override
        public VertexFormat getVertexFormat() {
            return FORMAT;
        }

        @Override
        public void setQuadTint(int tint) {
            tintIndex = tint;
        }

        @Override
        public void setQuadOrientation(@Nonnull Direction orientation) {
            side = orientation;
        }

        @Override
        public void setApplyDiffuseLighting(boolean diffuse) {
            applyDiffuseLighting = diffuse;
        }

        @Override
        public void setTexture(@Nonnull TextureAtlasSprite texture) {
            sprite = texture;
        }

        @Override
        public void put(int elementIndex, float... data) {
            VertexFormatElement element = FORMAT.getElements().get(elementIndex);
            float f0 = data.length >= 1 ? data[0] : 0;
            float f1 = data.length >= 2 ? data[1] : 0;
            float f2 = data.length >= 3 ? data[2] : 0;
            float f3 = data.length >= 4 ? data[3] : 0;
            switch (element.getUsage()) {
                case POSITION -> vertex.pos(new Vec3(f0, f1, f2));
                case NORMAL -> vertex.normal(new Vec3(f0, f1, f2));
                case COLOR -> vertex.color(Color.rgbad(f0, f1, f2, f3));
                case UV -> {
                    if (element.getIndex() == 0) {
                        vertex.texRaw(f0, f1);
                    } else if (element.getIndex() == 2) {
                        vertex.lightRaw(f0, f1);
                    }
                }
            }
            if (elementIndex == SIZE - 1) {
                vertices[vertexIndex++] = vertex;
                vertex = new Vertex();
            }
        }
    }

    public static class Builder {

        private TextureAtlasSprite texture;
        private final Direction side;

        private Vec3 vec1, vec2, vec3, vec4;

        private float minU, minV, maxU, maxV;
        private float lightU, lightV;

        private int tintIndex = -1;
        private boolean applyDiffuseLighting;
        private boolean contractUVs = true;

        public Builder(TextureAtlasSprite texture, Direction side) {
            this.texture = texture;
            this.side = side;
        }

        public Builder light(float u, float v) {
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

        public Builder applyDiffuseLighting(boolean applyDiffuseLighting) {
            this.applyDiffuseLighting = applyDiffuseLighting;
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
            return pos(start.add(0, height * scale, 0), start, start.add(width * scale, 0, 0), start.add(width * scale, height * scale, 0));
        }

        public Quad build() {
            Vertex[] vertices = new Vertex[4];
            Vec3 normal = vec3.subtract(vec2).cross(vec1.subtract(vec2)).normalize();
            vertices[0] = Vertex.create(vec1, normal, texture, minU, minV).light(lightU, lightV);
            vertices[1] = Vertex.create(vec2, normal, texture, minU, maxV).light(lightU, lightV);
            vertices[2] = Vertex.create(vec3, normal, texture, maxU, maxV).light(lightU, lightV);
            vertices[3] = Vertex.create(vec4, normal, texture, maxU, minV).light(lightU, lightV);
            Quad quad = new Quad(texture, side, vertices, tintIndex, applyDiffuseLighting);
            if (contractUVs) {
                QuadUtils.contractUVs(quad);
            }
            return quad;
        }
    }
}
