package mekanism.client.render.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.common.lib.Color;
import mekanism.common.lib.math.Quaternion;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public interface QuadTransformation {

    // down up north south west east
    Direction[][] ROTATION_MATRIX = {{Direction.SOUTH, Direction.NORTH, Direction.DOWN, Direction.DOWN, Direction.DOWN, Direction.DOWN},
                                     {Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.UP, Direction.UP, Direction.UP},
                                     {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST},
                                     {Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST},
                                     {Direction.WEST, Direction.WEST, Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH},
                                     {Direction.EAST, Direction.EAST, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}};

    QuadTransformation identity = q -> false;

    QuadTransformation fullbright = packedLight(LightTexture.FULL_BRIGHT);
    QuadTransformation filtered_fullbright = TextureFilteredTransformation.of(fullbright, rl -> rl.getPath().contains("led"));

    static QuadTransformation color(Color color) {
        return new ColorTransformation(color);
    }

    static QuadTransformation light(int light) {
        return new LightTransformation(light, light);
    }

    static QuadTransformation packedLight(int light) {
        return new LightTransformation(LightTexture.block(light), LightTexture.sky(light));
    }

    static QuadTransformation ambientShade(boolean ambientOcclusion, boolean shade) {
        return new AmbientShadeTransformation(ambientOcclusion, shade);
    }

    static QuadTransformation translate(float xTranslation, float yTranslation, float zTranslation) {
        return translate(new Vector3f(xTranslation, yTranslation, zTranslation));
    }

    static QuadTransformation translate(Vector3f translation) {
        return new TranslationTransformation(translation);
    }

    static QuadTransformation rotate(Direction side) {
        if (side == null) {
            return identity;
        }
        return switch (side) {
            case UP -> rotate(90, 0, 0);
            case DOWN -> rotate(-90, 0, 0);
            case WEST -> rotate(0, 90, 0);
            case EAST -> rotate(0, -90, 0);
            case SOUTH -> rotate(0, 180, 0);
            default -> identity;
        };
    }

    static QuadTransformation rotateY(double degrees) {
        return rotate(0, degrees, 0);
    }

    static QuadTransformation rotate(double rotationX, double rotationY, double rotationZ) {
        return rotate(new Quaternion(rotationX, rotationY, rotationZ, true));
    }

    static QuadTransformation rotate(Quaternion quat) {
        return new RotationTransformation(quat);
    }

    static QuadTransformation sideRotate(Direction side) {
        return new SideTransformation(side);
    }

    static QuadTransformation texture(TextureAtlasSprite texture) {
        return new TextureTransformation(texture);
    }

    //Bounds 0 to 16
    static QuadTransformation uvShift(float uShift, float vShift) {
        return new UVTransformation(uShift, vShift);
    }

    static QuadTransformation list(QuadTransformation... transforms) {
        return TransformationList.of(transforms);
    }

    /**
     * Transforms the given quad.
     *
     * @param quad Quad to transform.
     *
     * @return {@code true} if the quad was changed.
     */
    boolean transform(Quad quad);

    default QuadTransformation and(QuadTransformation other) {
        return list(this, other);
    }

    class SideTransformation implements QuadTransformation {

        private final Direction side;

        protected SideTransformation(Direction side) {
            this.side = side;
        }

        @Override
        public boolean transform(Quad quad) {
            if (side != null) {
                Direction newSide = ROTATION_MATRIX[quad.getSide().ordinal()][side.ordinal()];
                if (newSide != quad.getSide()) {
                    quad.setSide(newSide);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof SideTransformation other && side == other.side;
        }

        @Override
        public int hashCode() {
            return side == null ? -1 : side.hashCode();
        }
    }

    class ColorTransformation implements QuadTransformation {

        private final Color color;

        protected ColorTransformation(Color color) {
            this.color = color;
        }

        @Override
        public boolean transform(Quad quad) {
            for (Vertex v : quad.getVertices()) {
                v.color(color);
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof ColorTransformation other && color.equals(other.color);
        }

        @Override
        public int hashCode() {
            return color.hashCode();
        }
    }

    class LightTransformation implements QuadTransformation {

        private final int lightU;
        private final int lightV;

        public LightTransformation(int lightU, int lightV) {
            this.lightU = lightU;
            this.lightV = lightV;
        }

        @Override
        public boolean transform(Quad quad) {
            for (Vertex v : quad.getVertices()) {
                v.light(lightU, lightV);
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof LightTransformation other && lightU == other.lightU && lightV == other.lightV;
        }

        @Override
        public int hashCode() {
            int result = lightU;
            result = 31 * result + lightV;
            return result;
        }
    }

    class AmbientShadeTransformation implements QuadTransformation {

        private final boolean ambientOcclusion;
        private final boolean shade;

        public AmbientShadeTransformation(boolean ambientOcclusion, boolean shade) {
            this.ambientOcclusion = ambientOcclusion;
            this.shade = shade;
        }

        @Override
        public boolean transform(Quad quad) {
            quad.setHasAmbientOcclusion(ambientOcclusion);
            quad.setShade(shade);
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof AmbientShadeTransformation other && ambientOcclusion == other.ambientOcclusion && shade == other.shade;
        }

        @Override
        public int hashCode() {
            return 31 * Boolean.hashCode(ambientOcclusion) + Boolean.hashCode(shade);
        }
    }

    class RotationTransformation implements QuadTransformation {

        // quaternion math isn't exact- we round to nearest ten-thousandth
        private static final double EPSILON = 10_000;

        private final Quaternion quaternion;

        protected RotationTransformation(Quaternion quaternion) {
            this.quaternion = quaternion;
        }

        @Override
        public boolean transform(Quad quad) {
            for (Vertex v : quad.getVertices()) {
                v.pos(round(quaternion.rotate(v.getPosD().subtract(0.5, 0.5, 0.5)).add(0.5, 0.5, 0.5)));
                v.normal(round(quaternion.rotate(v.getNormalD()).normalize()));
            }
            return true;
        }

        private static Vec3 round(Vec3 vec) {
            return new Vec3(Math.round(vec.x * EPSILON) / EPSILON, Math.round(vec.y * EPSILON) / EPSILON, Math.round(vec.z * EPSILON) / EPSILON);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof RotationTransformation other && quaternion.equals(other.quaternion);
        }

        @Override
        public int hashCode() {
            return quaternion.hashCode();
        }
    }

    class TranslationTransformation implements QuadTransformation {

        private final Vector3f translation;

        protected TranslationTransformation(Vector3f translation) {
            this.translation = translation;
        }

        @Override
        public boolean transform(Quad quad) {
            for (Vertex v : quad.getVertices()) {
                v.getPos().add(translation);
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof TranslationTransformation other && translation.equals(other.translation);
        }

        @Override
        public int hashCode() {
            return translation.hashCode();
        }
    }

    class UVTransformation implements QuadTransformation {

        private final float uShift;
        private final float vShift;

        protected UVTransformation(float uShift, float vShift) {
            this.uShift = uShift;
            this.vShift = vShift;
        }

        @Override
        public boolean transform(Quad quad) {
            //TODO: At some point we may want to add in some form of validation here about bounds and stuff
            TextureAtlasSprite texture = quad.getTexture();
            float uMin = texture.getU0(), uMax = texture.getU1();
            float vMin = texture.getV0(), vMax = texture.getV1();
            //Calculate how much of a shift it is based on the texture's scale
            float uShift = this.uShift * (uMax - uMin);
            float vShift = this.vShift * (vMax - vMin);
            for (Vertex v : quad.getVertices()) {
                v.texRaw(v.getTexU() + uShift, v.getTexV() + vShift);
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof UVTransformation other && Float.compare(other.uShift, uShift) == 0 && Float.compare(other.vShift, vShift) == 0;
        }

        @Override
        public int hashCode() {
            int result = Float.hashCode(uShift);
            result = 31 * result + Float.hashCode(vShift);
            return result;
        }
    }

    class TextureTransformation implements QuadTransformation {

        private final TextureAtlasSprite texture;

        protected TextureTransformation(TextureAtlasSprite texture) {
            this.texture = texture;
        }

        @Override
        public boolean transform(Quad quad) {
            if (texture != null && quad.getTexture() != texture) {
                QuadUtils.remapUVs(quad, texture);
                quad.setTexture(texture);
                return true;
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof TextureTransformation other && texture == other.texture;
        }

        @Override
        public int hashCode() {
            return texture == null ? -1 : texture.hashCode();
        }
    }

    class TextureFilteredTransformation implements QuadTransformation {

        private final QuadTransformation original;
        private final Predicate<ResourceLocation> verifier;

        protected TextureFilteredTransformation(QuadTransformation original, Predicate<ResourceLocation> verifier) {
            this.original = original;
            this.verifier = verifier;
        }

        public static TextureFilteredTransformation of(QuadTransformation original, Predicate<ResourceLocation> verifier) {
            return new TextureFilteredTransformation(original, verifier);
        }

        @Override
        public boolean transform(Quad quad) {
            return verifier.test(quad.getTexture().contents().name()) && original.transform(quad);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof TextureFilteredTransformation other && verifier.equals(other.verifier);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(original);
            result = 31 * result + Objects.hashCode(verifier);
            return result;
        }
    }

    class TransformationList implements QuadTransformation {

        private final List<QuadTransformation> list;
        private final int hashCode;

        protected TransformationList(List<QuadTransformation> list) {
            this.list = list;
            hashCode = list.hashCode();
        }

        public static TransformationList of(QuadTransformation... trans) {
            return new TransformationList(List.of(trans));
        }

        @Override
        public boolean transform(Quad quad) {
            boolean transformed = false;
            for (QuadTransformation transformation : list) {
                transformed |= transformation.transform(quad);
            }
            return transformed;
        }

        @Override
        public QuadTransformation and(QuadTransformation other) {
            List<QuadTransformation> newList = new ArrayList<>(list);
            newList.add(other);
            return new TransformationList(newList);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            return o instanceof TransformationList other && list.equals(other.list);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
