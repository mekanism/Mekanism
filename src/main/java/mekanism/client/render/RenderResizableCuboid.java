package mekanism.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.util.EnumUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ILightReader;

/*
 * Adapted from BuildCraft
 */
public class RenderResizableCuboid {

    public static final Vec3d VEC_ONE = vec3(1);
    public static final Vec3d VEC_ZERO = vec3(0);
    public static final Vec3d VEC_HALF = vec3(0.5);
    public static final RenderResizableCuboid INSTANCE = new RenderResizableCuboid();
    /**
     * The AO map assumes that each direction in the world has a different amount of light going towards it.
     */
    private static final Map<Direction, Vec3d> aoMap = new EnumMap<>(Direction.class);
    private static final int U_MIN = 0;
    private static final int U_MAX = 1;
    private static final int V_MIN = 2;
    private static final int V_MAX = 3;

    static {
        // Static constants taken directly from minecraft's block renderer
        // ( net.minecraft.client.renderer.BlockModelRenderer.EnumNeighborInfo )
        aoMap.put(Direction.UP, vec3(1));
        aoMap.put(Direction.DOWN, vec3(0.5));
        aoMap.put(Direction.NORTH, vec3(0.8));
        aoMap.put(Direction.SOUTH, vec3(0.8));
        aoMap.put(Direction.EAST, vec3(0.6));
        aoMap.put(Direction.WEST, vec3(0.6));
    }

    protected EntityRendererManager manager = Minecraft.getInstance().getRenderManager();

    public static Vec3d withValue(Vec3d vector, Axis axis, double value) {
        if (axis == Axis.X) {
            return new Vec3d(value, vector.y, vector.z);
        } else if (axis == Axis.Y) {
            return new Vec3d(vector.x, value, vector.z);
        } else if (axis == Axis.Z) {
            return new Vec3d(vector.x, vector.y, value);
        }
        throw new RuntimeException("Was given a null axis! That was probably not intentional, consider this a bug! (Vector = " + vector + ")");
    }

    public static BlockPos convertFloor(Vec3d vec) {
        return new BlockPos(vec.x, vec.y, vec.z);
    }

    public static Vec3d convert(Vec3i vec3i) {
        return new Vec3d(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static Vec3d convert(Direction face) {
        if (face == null) {
            return VEC_ZERO;
        }
        return new Vec3d(face.getXOffset(), face.getYOffset(), face.getZOffset());
    }

    public static Direction[] getNeighbours(Direction face) {
        Direction[] faces = new Direction[4];
        int ordinal = 0;
        for (Direction next : EnumUtils.DIRECTIONS) {
            if (next.getAxis() != face.getAxis()) {
                faces[ordinal] = next;
                ordinal++;
            }
        }
        return faces;
    }

    public static void setWorldRendererRGB(IVertexBuilder wr, Vec3d color) {
        wr.color((float) color.x, (float) color.y, (float) color.z, 1F);
    }

    public static Vec3d vec3(double value) {
        return new Vec3d(value, value, value);
    }

    public static Vec3d multiply(Vec3d vec, double multiple) {
        return new Vec3d(vec.x * multiple, vec.y * multiple, vec.z * multiple);
    }

    public static Vec3d convertMiddle(Vec3i vec3i) {
        return convert(vec3i).add(VEC_HALF);
    }

    public static double getValue(Vec3d vector, Axis axis) {
        if (axis == Axis.X) {
            return vector.x;
        } else if (axis == Axis.Y) {
            return vector.y;
        } else if (axis == Axis.Z) {
            return vector.z;
        }
        throw new RuntimeException("Was given a null axis! That was probably not intentional, consider this a bug! (Vector = " + vector + ")");
    }

    /**
     * This will render a cuboid from its middle.
     */
    public void renderCubeFromCentre(Model3D cuboid, MatrixStack matrix, IRenderTypeBuffer renderer, RenderType.State.Builder stateBuilder, int argb) {
        matrix.push();
        matrix.translate(-cuboid.sizeX() / 2D, -cuboid.sizeY() / 2D, -cuboid.sizeZ() / 2D);
        renderCube(cuboid, matrix, renderer, stateBuilder, argb, EnumShadeArgument.NONE, null, null, null);
        matrix.pop();
    }

    public void renderCube(Model3D cuboid, MatrixStack matrix, IRenderTypeBuffer renderer, RenderType.State.Builder stateBuilder, int argb) {
        renderCube(cuboid, matrix, renderer, stateBuilder, argb, EnumShadeArgument.NONE, null, null, null);
    }

    public void renderCube(Model3D cube, MatrixStack matrix, IRenderTypeBuffer renderer, RenderType.State.Builder stateBuilder, int argb, EnumShadeArgument shadeTypes,
          IBlockLocation formula, IFacingLocation faceFormula, ILightReader world) {
        if (faceFormula == null) {
            faceFormula = DefaultFacingLocation.INSTANCE;
        }
        TextureAtlasSprite[] sprites = cube.textures;
        int[] flips = cube.textureFlips;
        if (flips == null) {
            flips = new int[6];
        }
        Vec3d textureStart = new Vec3d(cube.textureStartX / 16D, cube.textureStartY / 16D, cube.textureStartZ / 16D);
        Vec3d textureSize = new Vec3d(cube.textureSizeX / 16D, cube.textureSizeY / 16D, cube.textureSizeZ / 16D);
        Vec3d textureOffset = new Vec3d(cube.textureOffsetX / 16D, cube.textureOffsetY / 16D, cube.textureOffsetZ / 16D);
        Vec3d size = new Vec3d(cube.sizeX(), cube.sizeY(), cube.sizeZ());
        IVertexBuilder builder = renderer.getBuffer(MekanismRenderType.resizableCuboid(stateBuilder, shadeTypes.vertexFormat));
        for (Direction face : EnumUtils.DIRECTIONS) {
            if (cube.shouldSideRender(face)) {
                renderCuboidFace(matrix, builder, argb, face, sprites, flips, textureStart, textureSize, size, textureOffset, shadeTypes, formula, faceFormula, world);
            }
        }
    }

    private void renderCuboidFace(MatrixStack matrix, IVertexBuilder builder, int argb, Direction face, TextureAtlasSprite[] sprites, int[] flips, Vec3d textureStart,
          Vec3d textureSize, Vec3d size, Vec3d textureOffset, EnumShadeArgument shadeTypes, IBlockLocation locationFormula, IFacingLocation faceFormula, ILightReader access) {
        int ordinal = face.ordinal();
        if (sprites[ordinal] == null) {
            return;
        }

        Vec3d textureEnd = textureStart.add(textureSize);
        float[] uv = getUVArray(sprites[ordinal], flips[ordinal], face, textureStart, textureEnd);
        List<RenderInfo> renderInfoList = getRenderInfos(uv, face, size, textureSize, textureOffset);

        Axis u = face.getAxis() == Axis.X ? Axis.Z : Axis.X;
        Axis v = face.getAxis() == Axis.Y ? Axis.Z : Axis.Y;
        double other = face.getAxisDirection() == AxisDirection.POSITIVE ? getValue(size, face.getAxis()) : 0;

        /* Swap the face if this is positive: the renderer returns indexes that ALWAYS are for the negative face, so
         * light it properly this way */
        face = face.getAxisDirection() == AxisDirection.NEGATIVE ? face : face.getOpposite();

        float red = MekanismRenderer.getRed(argb);
        float green = MekanismRenderer.getGreen(argb);
        float blue = MekanismRenderer.getBlue(argb);
        float alpha = MekanismRenderer.getAlpha(argb);
        Direction opposite = face.getOpposite();
        Matrix4f matrix4f = matrix.getLast().getMatrix();
        for (RenderInfo ri : renderInfoList) {
            renderPoint(matrix4f, builder, face, u, v, other, ri, true, false, locationFormula, faceFormula, access, shadeTypes, red, green, blue, alpha);
            renderPoint(matrix4f, builder, face, u, v, other, ri, true, true, locationFormula, faceFormula, access, shadeTypes, red, green, blue, alpha);
            renderPoint(matrix4f, builder, face, u, v, other, ri, false, true, locationFormula, faceFormula, access, shadeTypes, red, green, blue, alpha);
            renderPoint(matrix4f, builder, face, u, v, other, ri, false, false, locationFormula, faceFormula, access, shadeTypes, red, green, blue, alpha);

            renderPoint(matrix4f, builder, opposite, u, v, other, ri, false, false, locationFormula, faceFormula, access, shadeTypes, red, green, blue, alpha);
            renderPoint(matrix4f, builder, opposite, u, v, other, ri, false, true, locationFormula, faceFormula, access, shadeTypes, red, green, blue, alpha);
            renderPoint(matrix4f, builder, opposite, u, v, other, ri, true, true, locationFormula, faceFormula, access, shadeTypes, red, green, blue, alpha);
            renderPoint(matrix4f, builder, opposite, u, v, other, ri, true, false, locationFormula, faceFormula, access, shadeTypes, red, green, blue, alpha);
        }
    }

    private void renderPoint(Matrix4f matrix4f, IVertexBuilder builder, Direction face, Axis u, Axis v, double other, RenderInfo ri, boolean minU, boolean minV,
          IBlockLocation locationFormula, IFacingLocation faceFormula, ILightReader access, EnumShadeArgument shadeTypes, float red, float green, float blue, float alpha) {
        int U_ARRAY = minU ? U_MIN : U_MAX;
        int V_ARRAY = minV ? V_MIN : V_MAX;

        Vec3d vertex = withValue(VEC_ZERO, u, ri.xyz[U_ARRAY]);
        vertex = withValue(vertex, v, ri.xyz[V_ARRAY]);
        vertex = withValue(vertex, face.getAxis(), other);

        builder.pos(matrix4f, (float) vertex.x, (float) vertex.y, (float) vertex.z)
              .tex(ri.uv[U_ARRAY], ri.uv[V_ARRAY])
              .color(red, green, blue, alpha);
        if (shadeTypes.isEnabled(EnumShadeType.FACE)) {
            setWorldRendererRGB(builder, aoMap.get(faceFormula.transformToWorld(face)));
        }
        if (shadeTypes.isEnabled(EnumShadeType.AMBIENT_OCCLUSION)) {
            applyLocalAO(builder, faceFormula.transformToWorld(face), locationFormula, access, shadeTypes, vertex);
        } else if (shadeTypes.isEnabled(EnumShadeType.LIGHT)) {
            Vec3d transVertex = locationFormula.transformToWorld(vertex);
            BlockPos pos = convertFloor(transVertex);
            int combinedLight = WorldRenderer.getCombinedLight(access, pos);
            builder.lightmap(combinedLight >> 16 & 65535, combinedLight & 65535);
        }
        builder.endVertex();
    }

    private void applyLocalAO(@Nonnull IVertexBuilder builder, Direction face, IBlockLocation locationFormula, ILightReader access, EnumShadeArgument shadeTypes, Vec3d vertex) {
        // This doesn't work. At all.
        boolean allAround = false;

        int numPositions = allAround ? 7 : 5;
        int[] skyLight = new int[numPositions];
        int[] blockLight = new int[numPositions];
        float[] colorMultiplier = new float[numPositions];
        double[] distances = new double[numPositions];
        double totalDist = 0;
        Vec3d transVertex = locationFormula.transformToWorld(vertex);
        BlockPos pos = convertFloor(transVertex);
        BlockState blockState = access.getBlockState(pos);
        int combinedLight = WorldRenderer.getCombinedLight(access, pos);

        skyLight[0] = combinedLight / 0x10000;
        blockLight[0] = combinedLight % 0x10000;
        colorMultiplier[0] = blockState.getAmbientOcclusionLightValue(access, pos);
        distances[0] = transVertex.distanceTo(convertMiddle(pos));

        int index = 0;
        Direction[] testArray = allAround ? EnumUtils.DIRECTIONS : getNeighbours(face);
        for (Direction otherFace : testArray) {
            Vec3d nearestOther = vertex.add(convert(otherFace));
            pos = convertFloor(locationFormula.transformToWorld(nearestOther));
            blockState = access.getBlockState(pos);
            combinedLight = WorldRenderer.getCombinedLight(access, pos);

            index++;

            skyLight[index] = combinedLight / 0x10000;
            blockLight[index] = combinedLight % 0x10000;
            colorMultiplier[index] = blockState.getAmbientOcclusionLightValue(access, pos);
            // The extra 0.1 is to stop any 1 divided by 0 errors
            distances[index] = 1 / (transVertex.distanceTo(convertMiddle(pos)) + 0.1);
            totalDist += distances[index];
        }

        double avgBlockLight = 0;
        double avgSkyLight = 0;
        double avgColorMultiplier = 0;
        for (int i = 0; i < numPositions; i++) {
            double part = distances[i] / totalDist;
            avgBlockLight += blockLight[i] * part;
            avgSkyLight += skyLight[i] * part;
            avgColorMultiplier += colorMultiplier[i] * part;
        }

        if (shadeTypes.isEnabled(EnumShadeType.LIGHT)) {
            int capBlockLight = (int) avgBlockLight;
            int capSkyLight = (int) avgSkyLight;
            builder.lightmap(capBlockLight, capSkyLight);
        }

        Vec3d color;
        if (shadeTypes.isEnabled(EnumShadeType.FACE)) {
            color = aoMap.get(face);
        } else {
            color = VEC_ONE;
        }
        color = multiply(color, avgColorMultiplier);
        setWorldRendererRGB(builder, color);
    }

    /**
     * Note that this method DOES take into account its position. But not its rotation. (Open an issue on github if you need rotation, and a second method will be made
     * that does all the trig required)
     */
    public void renderCubeStatic(List<BakedQuad> quads, Model3D cuboid) {
        TextureAtlasSprite[] sprites = cuboid.textures;

        int[] flips = cuboid.textureFlips;
        if (flips == null) {
            flips = new int[6];
        }

        double textureStartX = cuboid.textureStartX / 16D;
        double textureStartY = cuboid.textureStartY / 16D;
        double textureStartZ = cuboid.textureStartZ / 16D;

        double textureSizeX = cuboid.textureSizeX / 16D;
        double textureSizeY = cuboid.textureSizeY / 16D;
        double textureSizeZ = cuboid.textureSizeZ / 16D;

        double textureEndX = textureSizeX + textureStartX;
        double textureEndY = textureSizeY + textureStartY;
        double textureEndZ = textureSizeZ + textureStartZ;

        double textureOffsetX = cuboid.textureOffsetX / 16D;
        double textureOffsetY = cuboid.textureOffsetY / 16D;
        double textureOffsetZ = cuboid.textureOffsetZ / 16D;

        double sizeX = cuboid.sizeX();
        double sizeY = cuboid.sizeY();
        double sizeZ = cuboid.sizeZ();

        if (sprites[0] != null) {
            // Down
            float[] uv = getUVArray(sprites[0], flips[0], textureStartX, textureEndX, textureStartZ, textureEndZ);
            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeZ, textureSizeX, textureSizeZ, textureOffsetX, textureOffsetZ)) {
                ri = ri.offset(cuboid, Axis.Y);
                double[][] arr = new double[4][];
                arr[0] = new double[]{ri.xyz[U_MAX], cuboid.posY, ri.xyz[V_MIN], -1, ri.uv[U_MAX], ri.uv[V_MIN], 0};
                arr[1] = new double[]{ri.xyz[U_MAX], cuboid.posY, ri.xyz[V_MAX], -1, ri.uv[U_MAX], ri.uv[V_MAX], 0};
                arr[2] = new double[]{ri.xyz[U_MIN], cuboid.posY, ri.xyz[V_MAX], -1, ri.uv[U_MIN], ri.uv[V_MAX], 0};
                arr[3] = new double[]{ri.xyz[U_MIN], cuboid.posY, ri.xyz[V_MIN], -1, ri.uv[U_MIN], ri.uv[V_MIN], 0};
                convertToDoubleQuads(quads, arr, Direction.DOWN, sprites[0]);
            }
        }

        if (sprites[1] != null) {
            // Up
            float[] uv = getUVArray(sprites[1], flips[1], textureStartX, textureEndX, textureStartZ, textureEndZ);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeZ, textureSizeX, textureSizeZ, textureOffsetX, textureOffsetZ)) {
                ri = ri.offset(cuboid, Axis.Y);
                double[][] arr = new double[4][];
                arr[0] = new double[]{ri.xyz[U_MAX], sizeY + cuboid.posY, ri.xyz[V_MIN], -1, ri.uv[U_MAX], ri.uv[V_MIN], 0};
                arr[1] = new double[]{ri.xyz[U_MAX], sizeY + cuboid.posY, ri.xyz[V_MAX], -1, ri.uv[U_MAX], ri.uv[V_MAX], 0};
                arr[2] = new double[]{ri.xyz[U_MIN], sizeY + cuboid.posY, ri.xyz[V_MAX], -1, ri.uv[U_MIN], ri.uv[V_MAX], 0};
                arr[3] = new double[]{ri.xyz[U_MIN], sizeY + cuboid.posY, ri.xyz[V_MIN], -1, ri.uv[U_MIN], ri.uv[V_MIN], 0};
                convertToDoubleQuads(quads, arr, Direction.UP, sprites[1]);
            }
        }

        if (sprites[2] != null) {
            // North (-Z)
            float[] uv = getUVArray(sprites[2], flips[2], textureStartX, textureEndX, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeY, textureSizeX, textureSizeY, textureOffsetX, textureOffsetY)) {
                ri = ri.offset(cuboid, Axis.Z);
                double[][] arr = new double[4][];
                arr[0] = new double[]{ri.xyz[U_MAX], ri.xyz[V_MIN], cuboid.posZ, -1, ri.uv[U_MAX], ri.uv[V_MIN], 0};
                arr[1] = new double[]{ri.xyz[U_MAX], ri.xyz[V_MAX], cuboid.posZ, -1, ri.uv[U_MAX], ri.uv[V_MAX], 0};
                arr[2] = new double[]{ri.xyz[U_MIN], ri.xyz[V_MAX], cuboid.posZ, -1, ri.uv[U_MIN], ri.uv[V_MAX], 0};
                arr[3] = new double[]{ri.xyz[U_MIN], ri.xyz[V_MIN], cuboid.posZ, -1, ri.uv[U_MIN], ri.uv[V_MIN], 0};
                convertToDoubleQuads(quads, arr, Direction.NORTH, sprites[2]);
            }
        }

        if (sprites[3] != null) {
            // South (+Z)
            float[] uv = getUVArray(sprites[3], flips[3], textureStartX, textureEndX, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeX, sizeY, textureSizeX, textureSizeY, textureOffsetX, textureOffsetY)) {
                ri = ri.offset(cuboid, Axis.Z);
                double[][] arr = new double[4][];
                arr[0] = new double[]{ri.xyz[U_MAX], ri.xyz[V_MIN], cuboid.posZ + sizeZ, -1, ri.uv[U_MAX], ri.uv[V_MIN], 0};
                arr[1] = new double[]{ri.xyz[U_MAX], ri.xyz[V_MAX], cuboid.posZ + sizeZ, -1, ri.uv[U_MAX], ri.uv[V_MAX], 0};
                arr[2] = new double[]{ri.xyz[U_MIN], ri.xyz[V_MAX], cuboid.posZ + sizeZ, -1, ri.uv[U_MIN], ri.uv[V_MAX], 0};
                arr[3] = new double[]{ri.xyz[U_MIN], ri.xyz[V_MIN], cuboid.posZ + sizeZ, -1, ri.uv[U_MIN], ri.uv[V_MIN], 0};
                convertToDoubleQuads(quads, arr, Direction.SOUTH, sprites[3]);
            }
        }

        if (sprites[4] != null) {
            // West (-X)
            float[] uv = getUVArray(sprites[4], flips[4], textureStartZ, textureEndZ, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeZ, sizeY, textureSizeZ, textureSizeY, textureOffsetZ, textureOffsetY)) {
                ri = ri.offset(cuboid, Axis.X);
                double[][] arr = new double[4][];
                arr[0] = new double[]{cuboid.posX, ri.xyz[V_MIN], ri.xyz[U_MAX], -1, ri.uv[U_MAX], ri.uv[V_MIN], 0};
                arr[1] = new double[]{cuboid.posX, ri.xyz[V_MAX], ri.xyz[U_MAX], -1, ri.uv[U_MAX], ri.uv[V_MAX], 0};
                arr[2] = new double[]{cuboid.posX, ri.xyz[V_MAX], ri.xyz[U_MIN], -1, ri.uv[U_MIN], ri.uv[V_MAX], 0};
                arr[3] = new double[]{cuboid.posX, ri.xyz[V_MIN], ri.xyz[U_MIN], -1, ri.uv[U_MIN], ri.uv[V_MIN], 0};
                convertToDoubleQuads(quads, arr, Direction.WEST, sprites[4]);
            }
        }

        if (sprites[5] != null) {
            // East (+X)
            float[] uv = getUVArray(sprites[5], flips[5], textureStartZ, textureEndZ, textureStartY, textureEndY);

            for (RenderInfo ri : getRenderInfos(uv, sizeZ, sizeY, textureSizeZ, textureSizeY, textureOffsetZ, textureOffsetY)) {
                ri = ri.offset(cuboid, Axis.X);
                double[][] arr = new double[4][];
                arr[0] = new double[]{cuboid.posX + sizeX, ri.xyz[V_MIN], ri.xyz[U_MAX], -1, ri.uv[U_MAX], ri.uv[V_MIN], 0};
                arr[1] = new double[]{cuboid.posX + sizeX, ri.xyz[V_MAX], ri.xyz[U_MAX], -1, ri.uv[U_MAX], ri.uv[V_MAX], 0};
                arr[2] = new double[]{cuboid.posX + sizeX, ri.xyz[V_MAX], ri.xyz[U_MIN], -1, ri.uv[U_MIN], ri.uv[V_MAX], 0};
                arr[3] = new double[]{cuboid.posX + sizeX, ri.xyz[V_MIN], ri.xyz[U_MIN], -1, ri.uv[U_MIN], ri.uv[V_MIN], 0};
                convertToDoubleQuads(quads, arr, Direction.EAST, sprites[5]);
            }
        }
    }

    private void convertToDoubleQuads(List<BakedQuad> quads, double[][] points, Direction face, TextureAtlasSprite sprite) {
        BakedQuad quad = convertToQuad(points, face, sprite);
        quads.add(quad);

        double[][] otherPoints = new double[][]{points[3], points[2], points[1], points[0]};
        quad = convertToQuad(otherPoints, face, sprite);
        quads.add(quad);
    }

    private BakedQuad convertToQuad(double[][] points, Direction face, TextureAtlasSprite sprite) {
        int[] list = new int[points.length * points[0].length];
        for (int i = 0; i < points.length; i++) {
            double[] arr = points[i];
            for (int j = 0; j < arr.length; j++) {
                double d = arr[j];
                int used;
                if (j == 3 || j == 6) {// Shade or unused
                    used = (int) d;
                } else {
                    used = Float.floatToRawIntBits((float) d);
                }
                list[i * arr.length + j] = used;
            }
        }
        return new BakedQuad(list, -1, face, sprite, true);
    }

    /**
     * Returns an array containing [uMin, uMax, vMin, vMax]. start* and end* must be doubles between 0 and 1
     */
    private float[] getUVArray(TextureAtlasSprite sprite, int flips, double startU, double endU, double startV, double endV) {
        float minU = sprite.getInterpolatedU(startU * 16);
        float maxU = sprite.getInterpolatedU(endU * 16);
        float minV = sprite.getInterpolatedV(startV * 16);
        float maxV = sprite.getInterpolatedV(endV * 16);
        float[] uvarray = new float[]{minU, maxU, minV, maxV};
        if (flips % 2 == 1) {
            float holder = uvarray[0];
            uvarray[0] = uvarray[1];
            uvarray[1] = holder;
        }
        if (flips >> 1 % 2 == 1) {
            float holder = uvarray[2];
            uvarray[2] = uvarray[3];
            uvarray[3] = holder;
        }
        return uvarray;
    }

    private float[] getUVArray(TextureAtlasSprite sprite, int flips, Direction face, Vec3d start, Vec3d end) {
        Axis u = face.getAxis() == Axis.X ? Axis.Z : Axis.X;
        Axis v = face.getAxis() == Axis.Y ? Axis.Z : Axis.Y;

        float minU = sprite.getInterpolatedU(getValue(start, u) * 16);
        float maxU = sprite.getInterpolatedU(getValue(end, u) * 16);
        float minV = sprite.getInterpolatedV(getValue(start, v) * 16);
        float maxV = sprite.getInterpolatedV(getValue(end, v) * 16);

        float[] uvarray = new float[]{minU, maxU, minV, maxV};
        if (flips % 2 == 1) {
            float holder = uvarray[0];
            uvarray[0] = uvarray[1];
            uvarray[1] = holder;
        }
        if (flips >> 1 % 2 == 1) {
            float holder = uvarray[2];
            uvarray[2] = uvarray[3];
            uvarray[3] = holder;
        }
        return uvarray;
    }

    private List<RenderInfo> getRenderInfos(float[] uv, Direction face, Vec3d size, Vec3d texSize, Vec3d texOffset) {
        Axis u = face.getAxis() == Axis.X ? Axis.Z : Axis.X;
        Axis v = face.getAxis() == Axis.Y ? Axis.Z : Axis.Y;

        double sizeU = getValue(size, u);
        double sizeV = getValue(size, v);
        double textureSizeU = getValue(texSize, u);
        double textureSizeV = getValue(texSize, v);
        double textureOffsetU = getValue(texOffset, u);
        double textureOffsetV = getValue(texOffset, v);

        return getRenderInfos(uv, sizeU, sizeV, textureSizeU, textureSizeV, textureOffsetU, textureOffsetV);
    }

    /**
     * A way to automatically generate the different positions given the same arguments.
     */
    private List<RenderInfo> getRenderInfos(float[] uv, double sizeU, double sizeV, double textureSizeU, double textureSizeV, double textureOffsetU, double textureOffsetV) {
        List<RenderInfo> infos = new ArrayList<>();
        boolean firstU = true;
        for (double u = 0; u < sizeU; u += textureSizeU) {
            float[] uvCu = Arrays.copyOf(uv, 4);
            double addU = textureSizeU;
            boolean lowerU = false;

            // If there is an offset then make sure the texture positions are changed properly
            if (firstU && textureOffsetU != 0) {
                uvCu[U_MIN] = uvCu[U_MIN] + (uvCu[U_MAX] - uvCu[U_MIN]) * (float) textureOffsetU;
                addU -= textureOffsetU;
                // addU = 1 - textureOffsetU;
                lowerU = true;
            }

            // If the size of the texture is greater than the cuboid goes on for then make sure the texture
            // positions are lowered
            if (u + addU > sizeU) {
                addU = sizeU - u;
                if (firstU && textureOffsetU != 0) {
                    uvCu[U_MAX] = uvCu[U_MIN] + (uvCu[U_MAX] - uvCu[U_MIN]) * (float) (addU / (textureSizeU - textureOffsetU));
                } else {
                    uvCu[U_MAX] = uvCu[U_MIN] + (uvCu[U_MAX] - uvCu[U_MIN]) * (float) (addU / textureSizeU);
                }
            }
            firstU = false;
            boolean firstV = true;
            for (double v = 0; v < sizeV; v += textureSizeV) {
                float[] uvCv = Arrays.copyOf(uvCu, 4);

                double addV = textureSizeV;

                boolean lowerV = false;

                if (firstV && textureOffsetV != 0) {
                    uvCv[V_MIN] = uvCv[V_MIN] + (uvCv[V_MAX] - uvCv[V_MIN]) * (float) textureOffsetV;
                    addV -= textureOffsetV;
                    lowerV = true;
                }
                if (v + addV > sizeV) {
                    addV = sizeV - v;
                    if (firstV && textureOffsetV != 0) {
                        uvCv[V_MAX] = uvCv[V_MIN] + (uvCv[V_MAX] - uvCv[V_MIN]) * (float) (addV / (textureSizeV - textureOffsetV));
                    } else {
                        uvCv[V_MAX] = uvCv[V_MIN] + (uvCv[V_MAX] - uvCv[V_MIN]) * (float) (addV / textureSizeV);
                    }
                }

                double[] xyz = new double[4];
                xyz[U_MIN] = u;
                xyz[U_MAX] = u + addU;
                xyz[V_MIN] = v;
                xyz[V_MAX] = v + addV;
                infos.add(new RenderInfo(uvCv, xyz));

                if (lowerV) {
                    v -= textureOffsetV;
                }
                firstV = false;
            }
            // If we lowered the U because the cuboid started on an offset, reset it back to what was actually
            // rendered, not what the for loop assumes
            if (lowerU) {
                u -= textureOffsetU;
            }
        }
        return infos;
    }

    public enum DefaultFacingLocation implements IFacingLocation {
        INSTANCE;

        @Override
        public Direction transformToWorld(Direction face) {
            return face;
        }
    }

    public enum EnumShadeType {
        FACE(DefaultVertexFormats.COLOR_4UB),
        LIGHT(DefaultVertexFormats.TEX_2S),
        AMBIENT_OCCLUSION(DefaultVertexFormats.COLOR_4UB);

        private final VertexFormatElement element;

        EnumShadeType(VertexFormatElement element) {
            this.element = element;
        }
    }

    public enum EnumShadeArgument {
        NONE,
        FACE(EnumShadeType.FACE),
        FACE_LIGHT(EnumShadeType.FACE, EnumShadeType.LIGHT),
        FACE_OCCLUDE(EnumShadeType.FACE, EnumShadeType.AMBIENT_OCCLUSION),
        FACE_LIGHT_OCCLUDE(EnumShadeType.FACE, EnumShadeType.LIGHT, EnumShadeType.AMBIENT_OCCLUSION),
        LIGHT(EnumShadeType.LIGHT),
        LIGHT_OCCLUDE(EnumShadeType.LIGHT, EnumShadeType.AMBIENT_OCCLUSION),
        OCCLUDE(EnumShadeType.AMBIENT_OCCLUSION);

        public final ImmutableSet<EnumShadeType> types;
        final VertexFormat vertexFormat;

        EnumShadeArgument(EnumShadeType... types) {
            List<VertexFormatElement> elements = new ArrayList<>();
            elements.add(DefaultVertexFormats.POSITION_3F);
            elements.add(DefaultVertexFormats.TEX_2F);
            elements.add(DefaultVertexFormats.COLOR_4UB);
            for (EnumShadeType type : types) {
                if (!elements.contains(type.element)) {
                    elements.add(type.element);
                }
            }
            this.vertexFormat = new VertexFormat(ImmutableList.copyOf(elements));
            this.types = ImmutableSet.copyOf(types);
        }

        public boolean isEnabled(EnumShadeType type) {
            return types.contains(type);
        }
    }

    public interface IBlockLocation {

        Vec3d transformToWorld(Vec3d vec);
    }

    public interface IFacingLocation {

        Direction transformToWorld(Direction face);
    }

    private static final class RenderInfo {

        private final float[] uv;
        private final double[] xyz;

        public RenderInfo(float[] uv, double[] xyz) {
            this.uv = uv;
            this.xyz = xyz;
        }

        public RenderInfo offset(Model3D ent, Axis axis) {
            switch (axis) {
                case X:
                    return new RenderInfo(uv, new double[]{xyz[0] + ent.posZ, xyz[1] + ent.posZ, xyz[2] + ent.posY, xyz[3] + ent.posY});
                case Y:
                    return new RenderInfo(uv, new double[]{xyz[0] + ent.posX, xyz[1] + ent.posX, xyz[2] + ent.posZ, xyz[3] + ent.posZ});
                case Z:
                    return new RenderInfo(uv, new double[]{xyz[0] + ent.posX, xyz[1] + ent.posX, xyz[2] + ent.posY, xyz[3] + ent.posY});
            }
            return new RenderInfo(uv, xyz);
        }
    }
}