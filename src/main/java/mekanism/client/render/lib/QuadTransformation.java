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

public interface QuadTransformation {

    static QuadTransformation identity = q -> {};

    static QuadTransformation fullbright = light(1);

    static QuadTransformation color(Color color) {
        return new ColorTransformation(color);
    }

    static QuadTransformation light(float light) {
        return new LightTransformation(light, light);
    }

    static QuadTransformation rotate(Direction side) {
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

    void transform(Quad quad);

    public class ColorTransformation implements QuadTransformation {

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

    public class LightTransformation implements QuadTransformation {

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

    public class RotationTransformation implements QuadTransformation {

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

    public class TextureFilteredTransformation implements QuadTransformation {

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
