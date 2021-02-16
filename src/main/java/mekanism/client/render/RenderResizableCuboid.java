package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.Arrays;
import javax.annotation.Nullable;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.MekanismRenderer.Model3D.SpriteInfo;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Adapted from Mantle's FluidRenderer and Tinker's SmelteryTankRenderer
 */
public class RenderResizableCuboid {

    /**
     * Used to not need to create multiple arrays when we just want to fill it differently at times, and given rendering TERs is not multithreaded it is perfectly safe to
     * just use one backing "temporary" array.
     */
    private static final int[] combinedARGB = new int[EnumUtils.DIRECTIONS.length];
    private static final Vector3f NORMAL = new Vector3f(1, 1, 1);
    static {
        NORMAL.normalize();
    }

    private RenderResizableCuboid() {
    }

    public static void renderCube(Model3D cube, MatrixStack matrix, IVertexBuilder buffer, int argb, int light, int overlay, FaceDisplay faceDisplay,
          boolean fakeDisableDiffuse) {
        Arrays.fill(combinedARGB, argb);
        renderCube(cube, matrix, buffer, combinedARGB, light, overlay, faceDisplay, fakeDisableDiffuse);
    }

    /**
     * @implNote Based off of Tinker's
     */
    public static void renderCube(Model3D cube, MatrixStack matrix, IVertexBuilder buffer, int[] colors, int light, int overlay, FaceDisplay faceDisplay,
          boolean fakeDisableDiffuse) {
        //TODO - 10.1: Further attempt to fix z-fighting at larger distances if we make it not render the sides when it is in a solid block
        // that may improve performance some, but definitely would reduce/remove the majority of remaining z-fighting that is going on
        //Shift it so that the min values are all greater than or equal to zero as the various drawing code
        // has some issues when it comes to handling negative numbers
        float xShift = MathHelper.floor(cube.minX);
        float yShift = MathHelper.floor(cube.minY);
        float zShift = MathHelper.floor(cube.minZ);
        matrix.push();
        matrix.translate(xShift, yShift, zShift);
        float minX = cube.minX - xShift;
        float minY = cube.minY - yShift;
        float minZ = cube.minZ - zShift;
        float maxX = cube.maxX - xShift;
        float maxY = cube.maxY - yShift;
        float maxZ = cube.maxZ - zShift;
        int xDelta = calculateDelta(minX, maxX);
        int yDelta = calculateDelta(minY, maxY);
        int zDelta = calculateDelta(minZ, maxZ);
        float[] xBounds = getBlockBounds(xDelta, minX, maxX);
        float[] yBounds = getBlockBounds(yDelta, minY, maxY);
        float[] zBounds = getBlockBounds(zDelta, minZ, maxZ);
        MatrixStack.Entry lastMatrix = matrix.getLast();
        Matrix4f matrix4f = lastMatrix.getMatrix();
        Matrix3f normalMatrix = lastMatrix.getNormal();
        Vector3f normal = fakeDisableDiffuse ? NORMAL : Vector3f.YP;
        Vector3f from = new Vector3f();
        Vector3f to = new Vector3f();
        // render each side
        for (int y = 0; y <= yDelta; y++) {
            SpriteInfo upSprite = y == yDelta ? cube.getSpriteToRender(Direction.UP) : null;
            SpriteInfo downSprite = y == 0 ? cube.getSpriteToRender(Direction.DOWN) : null;
            from.setY(yBounds[y]);
            to.setY(yBounds[y + 1]);
            for (int z = 0; z <= zDelta; z++) {
                SpriteInfo northSprite = z == 0 ? cube.getSpriteToRender(Direction.NORTH) : null;
                SpriteInfo southSprite = z == zDelta ? cube.getSpriteToRender(Direction.SOUTH) : null;
                from.setZ(zBounds[z]);
                to.setZ(zBounds[z + 1]);
                for (int x = 0; x <= xDelta; x++) {
                    SpriteInfo westSprite = x == 0 ? cube.getSpriteToRender(Direction.WEST) : null;
                    SpriteInfo eastSprite = x == xDelta ? cube.getSpriteToRender(Direction.EAST) : null;
                    //Set bounds
                    from.setX(xBounds[x]);
                    to.setX(xBounds[x + 1]);
                    putTexturedQuad(buffer, matrix4f, normalMatrix, westSprite, from, to, Direction.WEST, colors, light, overlay, faceDisplay, normal);
                    putTexturedQuad(buffer, matrix4f, normalMatrix, eastSprite, from, to, Direction.EAST, colors, light, overlay, faceDisplay, normal);
                    putTexturedQuad(buffer, matrix4f, normalMatrix, northSprite, from, to, Direction.NORTH, colors, light, overlay, faceDisplay, normal);
                    putTexturedQuad(buffer, matrix4f, normalMatrix, southSprite, from, to, Direction.SOUTH, colors, light, overlay, faceDisplay, normal);
                    putTexturedQuad(buffer, matrix4f, normalMatrix, upSprite, from, to, Direction.UP, colors, light, overlay, faceDisplay, normal);
                    putTexturedQuad(buffer, matrix4f, normalMatrix, downSprite, from, to, Direction.DOWN, colors, light, overlay, faceDisplay, normal);
                }
            }
        }
        matrix.pop();
    }

    /**
     * @implNote From Tinker's
     */
    private static float[] getBlockBounds(int delta, float start, float end) {
        float[] bounds = new float[2 + delta];
        bounds[0] = start;
        int offset = (int) start;
        for (int i = 1; i <= delta; i++) {
            bounds[i] = i + offset;
        }
        bounds[delta + 1] = end;
        return bounds;
    }

    /**
     * @implNote From Tinker's
     */
    private static int calculateDelta(float min, float max) {
        //The texture can stretch over more blocks than the subtracted height is if min's decimal is bigger than max's decimal (causing UV over 1)
        // ignoring the decimals prevents this, as yd then equals exactly how many ints are between the two
        // for example, if max = 5.1 and min = 2.3, 2.8 (which rounds to 2), with the face array becoming 2.3, 3, 4, 5.1
        int delta = (int) (max - (int) min);
        // except in the rare case of max perfectly aligned with the block, causing the top face to render multiple times
        // for example, if max = 3 and min = 1, the values of the face array become 1, 2, 3, 3 as we then have middle ints
        if (max % 1d == 0) {
            delta--;
        }
        return delta;
    }

    /**
     * @implNote From Mantle with some adjustments
     */
    private static void putTexturedQuad(IVertexBuilder buffer, Matrix4f matrix, Matrix3f normalMatrix, @Nullable SpriteInfo spriteInfo, Vector3f from, Vector3f to,
          Direction face, int[] colors, int light, int overlay, FaceDisplay faceDisplay, Vector3f normal) {
        if (spriteInfo == null) {
            return;
        }
        // start with texture coordinates
        float x1 = from.getX(), y1 = from.getY(), z1 = from.getZ();
        float x2 = to.getX(), y2 = to.getY(), z2 = to.getZ();
        // choose UV based on opposite two axis
        float u1, u2, v1, v2;
        switch (face.getAxis()) {
            default:
            case Y:
                u1 = x1;
                u2 = x2;
                v1 = z2;
                v2 = z1;
                break;
            case Z:
                u1 = x2;
                u2 = x1;
                v1 = y1;
                v2 = y2;
                break;
            case X:
                u1 = z2;
                u2 = z1;
                v1 = y1;
                v2 = y2;
                break;
        }

        // wrap UV to be between 0 and 1, assumes none of the positions lie outside the 0,0,0 to 1,1,1 range
        // however, one of them might be exactly on the 1.0 bound, that one should be set to 1 instead of left at 0
        boolean bigger = u1 > u2;
        u1 = u1 % 1;
        u2 = u2 % 1;
        if (bigger) {
            if (u1 == 0) {
                u1 = 1;
            }
        } else if (u2 == 0) {
            u2 = 1;
        }
        bigger = v1 > v2;
        v1 = v1 % 1;
        v2 = v2 % 1;
        if (bigger) {
            if (v1 == 0) {
                v1 = 1;
            }
        } else if (v2 == 0) {
            v2 = 1;
        }

        //Flip V
        float temp = v1;
        v1 = 1f - v2;
        v2 = 1f - temp;

        float minU = spriteInfo.sprite.getInterpolatedU(u1 * spriteInfo.size);
        float maxU = spriteInfo.sprite.getInterpolatedU(u2 * spriteInfo.size);
        float minV = spriteInfo.sprite.getInterpolatedV(v1 * spriteInfo.size);
        float maxV = spriteInfo.sprite.getInterpolatedV(v2 * spriteInfo.size);
        int argb = colors[face.ordinal()];
        float red = MekanismRenderer.getRed(argb);
        float green = MekanismRenderer.getGreen(argb);
        float blue = MekanismRenderer.getBlue(argb);
        float alpha = MekanismRenderer.getAlpha(argb);
        // add quads
        switch (face) {
            case DOWN:
                drawFace(buffer, matrix, normalMatrix, red, green, blue, alpha, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal,
                      x1, y1, z2,
                      x1, y1, z1,
                      x2, y1, z1,
                      x2, y1, z2);
                break;
            case UP:
                drawFace(buffer, matrix, normalMatrix, red, green, blue, alpha, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal,
                      x1, y2, z1,
                      x1, y2, z2,
                      x2, y2, z2,
                      x2, y2, z1);
                break;
            case NORTH:
                drawFace(buffer, matrix, normalMatrix, red, green, blue, alpha, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal,
                      x1, y1, z1,
                      x1, y2, z1,
                      x2, y2, z1,
                      x2, y1, z1);
                break;
            case SOUTH:
                drawFace(buffer, matrix, normalMatrix, red, green, blue, alpha, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal,
                      x2, y1, z2,
                      x2, y2, z2,
                      x1, y2, z2,
                      x1, y1, z2);
                break;
            case WEST:
                drawFace(buffer, matrix, normalMatrix, red, green, blue, alpha, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal,
                      x1, y1, z2,
                      x1, y2, z2,
                      x1, y2, z1,
                      x1, y1, z1);
                break;
            case EAST:
                drawFace(buffer, matrix, normalMatrix, red, green, blue, alpha, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal,
                      x2, y1, z1,
                      x2, y2, z1,
                      x2, y2, z2,
                      x2, y1, z2);
                break;
        }
    }

    private static void drawFace(IVertexBuilder buffer, Matrix4f matrix, Matrix3f normalMatrix, float red, float green, float blue, float alpha, float minU, float maxU,
          float minV, float maxV, int light, int overlay, FaceDisplay faceDisplay, Vector3f normal,
          float x1, float y1, float z1,
          float x2, float y2, float z2,
          float x3, float y3, float z3,
          float x4, float y4, float z4) {
        if (faceDisplay.front) {
            buffer.pos(matrix, x1, y1, z1).color(red, green, blue, alpha).tex(minU, maxV).overlay(overlay).lightmap(light).normal(normalMatrix, normal.getX(), normal.getY(), normal.getZ()).endVertex();
            buffer.pos(matrix, x2, y2, z2).color(red, green, blue, alpha).tex(minU, minV).overlay(overlay).lightmap(light).normal(normalMatrix, normal.getX(), normal.getY(), normal.getZ()).endVertex();
            buffer.pos(matrix, x3, y3, z3).color(red, green, blue, alpha).tex(maxU, minV).overlay(overlay).lightmap(light).normal(normalMatrix, normal.getX(), normal.getY(), normal.getZ()).endVertex();
            buffer.pos(matrix, x4, y4, z4).color(red, green, blue, alpha).tex(maxU, maxV).overlay(overlay).lightmap(light).normal(normalMatrix, normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
        if (faceDisplay.back) {
            buffer.pos(matrix, x4, y4, z4).color(red, green, blue, alpha).tex(maxU, maxV).overlay(overlay).lightmap(light).normal(normalMatrix, 0, -1, 0).endVertex();
            buffer.pos(matrix, x3, y3, z3).color(red, green, blue, alpha).tex(maxU, minV).overlay(overlay).lightmap(light).normal(normalMatrix, 0, -1, 0).endVertex();
            buffer.pos(matrix, x2, y2, z2).color(red, green, blue, alpha).tex(minU, minV).overlay(overlay).lightmap(light).normal(normalMatrix, 0, -1, 0).endVertex();
            buffer.pos(matrix, x1, y1, z1).color(red, green, blue, alpha).tex(minU, maxV).overlay(overlay).lightmap(light).normal(normalMatrix, 0, -1, 0).endVertex();
        }
    }

    public enum FaceDisplay {
        FRONT(true, false),
        BACK(false, true),
        BOTH(true, true);

        private final boolean front;
        private final boolean back;

        FaceDisplay(boolean front, boolean back) {
            this.front = front;
            this.back = back;
        }
    }
}