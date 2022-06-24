package mekanism.client.render.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.common.lib.Color;
import mekanism.common.lib.math.Quaternion;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public interface QuadTransformation {

    // down up north south west east
    Direction[][] ROTATION_MATRIX = {{Direction.SOUTH, Direction.NORTH, Direction.DOWN, Direction.DOWN, Direction.DOWN, Direction.DOWN},
                                     {Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.UP, Direction.UP, Direction.UP},
                                     {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST},
                                     {Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST},
                                     {Direction.WEST, Direction.WEST, Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH},
                                     {Direction.EAST, Direction.EAST, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}};

    QuadTransformation identity = q -> {
    };

    QuadTransformation fullbright = light(1);
    QuadTransformation filtered_fullbright = TextureFilteredTransformation.of(fullbright, rl -> rl.getPath().contains("led"));

    static QuadTransformation color(Color color) {
        return new ColorTransformation(color);
    }

    static QuadTransformation light(float light) {
        return new LightTransformation(light, light);
    }

    static QuadTransformation translate(Vec3 translation) {
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

    static QuadTransformation list(QuadTransformation... transforms) {
        return TransformationList.of(transforms);
    }

    void transform(Quad quad);

    default QuadTransformation and(QuadTransformation other) {
        return list(this, other);
    }

    class SideTransformation implements QuadTransformation {

        private final Direction side;

        protected SideTransformation(Direction side) {
            this.side = side;
        }

        @Override
        public void transform(Quad quad) {
            if (side != null) {
                quad.setSide(ROTATION_MATRIX[quad.getSide().ordinal()][side.ordinal()]);
            }
        }

        @Override
        public boolean equals(Object o) {
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
        public void transform(Quad quad) {
            quad.vertexTransform(v -> v.color(color));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ColorTransformation other && color.equals(other.color);
        }

        @Override
        public int hashCode() {
            return color.hashCode();
        }
    }

    class LightTransformation implements QuadTransformation {

        private final float lightU;
        private final float lightV;

        public LightTransformation(float lightU, float lightV) {
            this.lightU = lightU;
            this.lightV = lightV;
        }

        @Override
        public void transform(Quad quad) {
            quad.vertexTransform(v -> v.light(lightU, lightV));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof LightTransformation other && lightU == other.lightU && lightV == other.lightV;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lightU, lightV);
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
        public void transform(Quad quad) {
            quad.vertexTransform(v -> {
                v.pos(round(quaternion.rotate(v.getPos().subtract(0.5, 0.5, 0.5)).add(0.5, 0.5, 0.5)));
                v.normal(round(quaternion.rotate(v.getNormal()).normalize()));
            });
        }

        private static Vec3 round(Vec3 vec) {
            return new Vec3(Math.round(vec.x * EPSILON) / EPSILON, Math.round(vec.y * EPSILON) / EPSILON, Math.round(vec.z * EPSILON) / EPSILON);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof RotationTransformation other && quaternion.equals(other.quaternion);
        }

        @Override
        public int hashCode() {
            return quaternion.hashCode();
        }
    }

    class TranslationTransformation implements QuadTransformation {

        private final Vec3 translation;

        protected TranslationTransformation(Vec3 translation) {
            this.translation = translation;
        }

        @Override
        public void transform(Quad quad) {
            quad.vertexTransform(v -> v.pos(v.getPos().add(translation)));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof TranslationTransformation other && translation.equals(other.translation);
        }

        @Override
        public int hashCode() {
            return translation.hashCode();
        }
    }

    class TextureTransformation implements QuadTransformation {

        private final TextureAtlasSprite texture;

        protected TextureTransformation(TextureAtlasSprite texture) {
            this.texture = texture;
        }

        @Override
        public void transform(Quad quad) {
            if (texture == null) {
                return;
            }
            QuadUtils.remapUVs(quad, texture);
            quad.setTexture(texture);
        }

        @Override
        public boolean equals(Object o) {
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
        public void transform(Quad quad) {
            if (verifier.test(quad.getTexture().getName())) {
                quad.transform(v -> original.transform(quad));
            }
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof TextureFilteredTransformation other && verifier.equals(other.verifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(original, verifier);
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
        public void transform(Quad quad) {
            list.forEach(transformation -> transformation.transform(quad));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof TransformationList other && list.equals(other.list);
        }

        @Override
        public QuadTransformation and(QuadTransformation other) {
            List<QuadTransformation> newList = new ArrayList<>(list);
            newList.add(other);
            return new TransformationList(newList);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
