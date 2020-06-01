package mekanism.client.render.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import com.google.common.collect.Sets;
import mekanism.common.lib.Color;
import mekanism.common.lib.math.Quaternion;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public interface QuadTransformation {
    // down up north south west east
    static final Direction[][] ROTATION_MATRIX = new Direction[][] {{ Direction.SOUTH, Direction.NORTH, Direction.DOWN, Direction.DOWN, Direction.DOWN, Direction.DOWN },
                                                                    { Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.UP, Direction.UP, Direction.UP },
                                                                    { Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST },
                                                                    { Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST },
                                                                    { Direction.WEST, Direction.WEST, Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH },
                                                                    { Direction.EAST, Direction.EAST, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH }};

    static QuadTransformation identity = q -> {};

    static QuadTransformation fullbright = light(1);

    static QuadTransformation color(Color color) {
        return new ColorTransformation(color);
    }

    static QuadTransformation light(float light) {
        return new LightTransformation(light, light);
    }

    static QuadTransformation translate(Vec3d translation) {
        return new TranslationTransformation(translation);
    }

    static QuadTransformation rotate(Direction side) {
        if (side == null)
            return identity;
        switch (side) {
            case UP: return rotate(90, 0, 0);
            case DOWN: return rotate(-90, 0, 0);
            case WEST: return rotate(0, -90, 0);
            case EAST: return rotate(0, 90, 0);
            case NORTH: return rotate(0, 180, 0);
            default:
                return identity;
        }
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

    void transform(Quad quad);

    public static class SideTransformation implements QuadTransformation {

        private final Direction side;

        public SideTransformation(Direction side) {
            this.side = side;
        }

        @Override
        public void transform(Quad quad) {
            if (side == null)
                return;
            quad.setSide(ROTATION_MATRIX[quad.getSide().ordinal()][side.ordinal()]);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof SideTransformation && side == ((SideTransformation) other).side;
        }

        @Override
        public int hashCode() {
            return side != null ? side.hashCode() : -1;
        }
    }

    public static class ColorTransformation implements QuadTransformation {

        private final Color color;

        public ColorTransformation(Color color) {
            this.color = color;
        }

        @Override
        public void transform(Quad quad) {
            quad.transform(v -> v.color(color));
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ColorTransformation && color.equals(((ColorTransformation) other).color);
        }

        @Override
        public int hashCode() {
            return color.hashCode();
        }
    }

    public static class LightTransformation implements QuadTransformation {

        private final float lightU;
        private final float lightV;

        public LightTransformation(float lightU, float lightV) {
            this.lightU = lightU;
            this.lightV = lightV;
        }

        @Override
        public void transform(Quad quad) {
            quad.transform(v -> v.light(lightU, lightV));
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof LightTransformation && lightU == ((LightTransformation) other).lightU && lightV == ((LightTransformation) other).lightV;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lightU, lightV);
        }
    }

    public static class RotationTransformation implements QuadTransformation {

        private final Quaternion quaternion;

        public RotationTransformation(Quaternion quaternion) {
            this.quaternion = quaternion;
        }

        @Override
        public void transform(Quad quad) {
            quad.transform(v -> {
                v.pos(quaternion.rotate(v.getPos().subtract(0.5, 0.5, 0.5)).add(0.5, 0.5, 0.5));
                v.normal(quaternion.rotate(v.getNormal()).normalize());
            });
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof RotationTransformation && quaternion.equals(((RotationTransformation) other).quaternion);
        }

        @Override
        public int hashCode() {
            return quaternion.hashCode();
        }
    }

    public static class TranslationTransformation implements QuadTransformation {

        private Vec3d translation;

        public TranslationTransformation(Vec3d translation) {
            this.translation = translation;
        }

        @Override
        public void transform(Quad quad) {
            quad.transform(v -> v.pos(v.getPos().add(translation)));
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof TranslationTransformation && translation.equals(((TranslationTransformation) other).translation);
        }

        @Override
        public int hashCode() {
            return translation.hashCode();
        }
    }

    public static class TextureFilteredTransformation implements QuadTransformation {

        private QuadTransformation original;
        private Set<ResourceLocation> textures = new HashSet<>();
        private int hash;

        public TextureFilteredTransformation(QuadTransformation original, Set<ResourceLocation> textures) {
            this.original = original;
            this.textures = textures;
            hash = Objects.hash(original, textures);
        }

        public static TextureFilteredTransformation of(QuadTransformation original, ResourceLocation... textures) {
            return new TextureFilteredTransformation(original, Sets.newHashSet(textures));
        }

        @Override
        public void transform(Quad quad) {
            if (textures.contains(quad.getTexture().getName())) {
                quad.transform(v -> {
                    original.transform(quad);
                });
            }
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof TextureFilteredTransformation && textures.equals(((TextureFilteredTransformation) other).textures);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    public static class TransformationList implements QuadTransformation {

        private List<QuadTransformation> list = new ArrayList<>();
        private int hashCode;

        public TransformationList(List<QuadTransformation> list) {
            this.list = list;
            hashCode = list.hashCode();
        }

        public static TransformationList of(QuadTransformation... trans) {
            return new TransformationList(Arrays.asList(trans));
        }

        @Override
        public void transform(Quad quad) {
            list.forEach(transformation -> transformation.transform(quad));
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof TransformationList && list.equals(((TransformationList) other).list);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
