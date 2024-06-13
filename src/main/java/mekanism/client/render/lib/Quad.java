package mekanism.client.render.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.Arrays;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.Color;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.joml.Vector3f;

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
        side = quad.getDirection();
        sprite = quad.getSprite();
        tintIndex = quad.getTintIndex();
        shade = quad.isShade();
        hasAmbientOcclusion = quad.hasAmbientOcclusion();
        BakedQuadUnpacker unpacker = new BakedQuadUnpacker();
        unpacker.putBulkData(new PoseStack().last(), quad, 1, 1, 1, 1, 0, OverlayTexture.NO_OVERLAY, true);
        vertices = unpacker.getVertices();
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
        QuadBakingVertexConsumer quadBaker = new QuadBakingVertexConsumer();
        quadBaker.setSprite(sprite);
        quadBaker.setDirection(side);
        quadBaker.setTintIndex(tintIndex);
        quadBaker.setShade(shade);
        quadBaker.setHasAmbientOcclusion(hasAmbientOcclusion);
        for (Vertex vertex : vertices) {
            vertex.write(quadBaker);
        }
        return quadBaker.bakeQuad();
    }

    public Quad copy() {
        Vertex[] newVertices = new Vertex[vertices.length];
        for (int i = 0; i < newVertices.length; i++) {
            newVertices[i] = vertices[i].copy(true);
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

    @NothingNullByDefault
    private static class BakedQuadUnpacker implements VertexConsumer {

        private final Vertex[] vertices = new Vertex[4];
        private boolean building = false;
        private int vertexIndex = 0;

        public Vertex[] getVertices() {
            if (!building || ++vertexIndex != 4) {
                throw new IllegalStateException("Not enough vertices available. Vertices in buffer: " + vertexIndex);
            }
            return vertices;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            if (building) {
                if (++vertexIndex > 4) {
                    throw new IllegalStateException("Expected quad export after fourth vertex");
                }
            }
            building = true;
            vertices[vertexIndex] = new Vertex().pos(new Vector3f(x, y, z));
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            vertices[vertexIndex].color(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            vertices[vertexIndex].texRaw(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            vertices[vertexIndex].overlay(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            vertices[vertexIndex].lightRaw(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            vertices[vertexIndex].normal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer misc(VertexFormatElement element, int... rawData) {
            vertices[vertexIndex].misc(element, Arrays.copyOf(rawData, rawData.length));
            return this;
        }
    }

    public static class Builder {

        private TextureAtlasSprite texture;
        private final Direction side;
        private Color color = Color.WHITE;

        private Vector3f vec1, vec2, vec3, vec4;

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

        public Builder pos(Vector3f tl, Vector3f bl, Vector3f br, Vector3f tr) {
            this.vec1 = tl;
            this.vec2 = bl;
            this.vec3 = br;
            this.vec4 = tr;
            return this;
        }

        public Builder rect(Vector3f start, float width, float height) {
            return rect(start, width, height, 1F / 16F); // default to 1/16 scale
        }

        // start = bottom left
        public Builder rect(Vector3f start, float width, float height, float scale) {
            start = start.mul(scale, scale, scale, new Vector3f());
            Vector3f end;
            if (side.getAxis().isHorizontal()) {
                Vec3i normal = side.getNormal();
                end = start.add(normal.getZ() * width * scale, 0, normal.getX() * width * scale, new Vector3f());
                if (side.getAxis() == Axis.X) {
                    //Wind vertices in a different order so that it faces the correct direction
                    return pos(start, start.add(0, height * scale, 0, new Vector3f()),
                          end.add(0, height * scale, 0, new Vector3f()), end);
                }
            } else {
                end = new Vector3f(start.x + width * scale, start.y, start.z);
            }
            return pos(start.add(0, height * scale, 0, new Vector3f()), start,
                  end, end.add(0, height * scale, 0, new Vector3f()));
        }

        public Quad build() {
            Vertex[] vertices = new Vertex[4];
            //Note: We don't need to create a new Vector3f for the cross multiplication, as it will just mutate the new one we used for the first subtraction
            Vector3f normal = vec3.sub(vec2, new Vector3f()).cross(vec1.sub(vec2,  new Vector3f())).normalize();
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
