package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nullable;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.MekanismRenderer.Model3D.SpriteInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Adapted from Mantle's FluidRenderer and Tinker's SmelteryTankRenderer
 */
public class RenderResizableCuboid {

    private RenderResizableCuboid() {
    }

    /**
     * @implNote Based off of Tinker's
     */
    public static void renderCube(Model3D cube, MatrixStack matrix, IVertexBuilder buffer, int argb, int light, int overlay, boolean backFace) {
        //TODO - 10.1: Re-evaluate back face culling. It currently makes it so that the evap tower and other things don't show as rendering
        // when you are inside of them, and I believe it is the cause of why the mechanical pipe and stuff sometimes don't have parts rendering
        // when partially filled and vertical. Maybe we can somehow force this only when needed such as if the player is inside the area?
        //TODO - 10.1: Further attempt to fix z-fighting at larger distances if we make it not render the sides when it is in a solid block
        // that may improve performance some, but definitely would reduce/remove the majority of remaining z-fighting that is going on
        int xd = calculateD(cube.minX, cube.maxX);
        int yd = calculateD(cube.minY, cube.maxY);
        int zd = calculateD(cube.minZ, cube.maxZ);
        float[] xBounds = getBlockBounds(xd, cube.minX, cube.maxX);
        float[] yBounds = getBlockBounds(yd, cube.minY, cube.maxY);
        float[] zBounds = getBlockBounds(zd, cube.minZ, cube.maxZ);
        float red = MekanismRenderer.getRed(argb);
        float green = MekanismRenderer.getGreen(argb);
        float blue = MekanismRenderer.getBlue(argb);
        float alpha = MekanismRenderer.getAlpha(argb);
        MatrixStack.Entry lastMatrix = matrix.getLast();
        Matrix4f matrix4f = lastMatrix.getMatrix();
        Matrix3f normal = lastMatrix.getNormal();
        Vector3f from = new Vector3f();
        Vector3f to = new Vector3f();
        // render each side
        for (int y = 0; y <= yd; y++) {
            SpriteInfo upSprite = y == yd ? cube.getSpriteToRender(Direction.UP) : null;
            SpriteInfo downSprite = y == 0 ? cube.getSpriteToRender(Direction.DOWN) : null;
            from.setY(yBounds[y]);
            to.setY(yBounds[y + 1]);
            for (int z = 0; z <= zd; z++) {
                SpriteInfo northSprite = z == 0 ? cube.getSpriteToRender(Direction.NORTH) : null;
                SpriteInfo southSprite = z == zd ? cube.getSpriteToRender(Direction.SOUTH) : null;
                from.setZ(zBounds[z]);
                to.setZ(zBounds[z + 1]);
                for (int x = 0; x <= xd; x++) {
                    SpriteInfo westSprite = x == 0 ? cube.getSpriteToRender(Direction.WEST) : null;
                    SpriteInfo eastSprite = x == xd ? cube.getSpriteToRender(Direction.EAST) : null;
                    //Set bounds
                    from.setX(xBounds[x]);
                    to.setX(xBounds[x + 1]);
                    putTexturedQuad(buffer, matrix4f, normal, westSprite, from, to, Direction.WEST, red, green, blue, alpha, light, overlay, backFace);
                    putTexturedQuad(buffer, matrix4f, normal, eastSprite, from, to, Direction.EAST, red, green, blue, alpha, light, overlay, backFace);
                    putTexturedQuad(buffer, matrix4f, normal, northSprite, from, to, Direction.NORTH, red, green, blue, alpha, light, overlay, backFace);
                    putTexturedQuad(buffer, matrix4f, normal, southSprite, from, to, Direction.SOUTH, red, green, blue, alpha, light, overlay, backFace);
                    putTexturedQuad(buffer, matrix4f, normal, upSprite, from, to, Direction.UP, red, green, blue, alpha, light, overlay, backFace);
                    putTexturedQuad(buffer, matrix4f, normal, downSprite, from, to, Direction.DOWN, red, green, blue, alpha, light, overlay, backFace);
                }
            }
        }
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
    private static int calculateD(float min, float max) {
        //The texture can stretch over more blocks than the subtracted height is if min's decimal is bigger than max's decimal (causing UV over 1)
        // ignoring the decimals prevents this, as yd then equals exactly how many ints are between the two
        // for example, if max = 5.1 and min = 2.3, 2.8 (which rounds to 2), with the face array becoming 2.3, 3, 4, 5.1
        int d = (int) (max - (int) min);
        // except in the rare case of max perfectly aligned with the block, causing the top face to render multiple times
        // for example, if max = 3 and min = 1, the values of the face array become 1, 2, 3, 3 as we then have middle ints
        if (max % 1d == 0) {
            d--;
        }
        return d;
    }

    /**
     * @implNote From Mantle with some adjustments
     */
    private static void putTexturedQuad(IVertexBuilder buffer, Matrix4f matrix, Matrix3f normal, @Nullable SpriteInfo spriteInfo, Vector3f from, Vector3f to,
          Direction face, float red, float green, float blue, float alpha, int light, int overlay, boolean backFace) {
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
        // add quads
        switch (face) {
            case DOWN:
                drawFace(buffer, matrix, normal, red, green, blue, alpha, minU, maxU, minV, maxV, light, overlay, backFace,
                      x1, y1, z2,
                      x1, y1, z1,
                      x2, y1, z1,
                      x2, y1, z2);
                break;
            case UP:
                drawFace(buffer, matrix, normal, red, green, blue, alpha, minU, maxU, minV, maxV, light, overlay, backFace,
                      x1, y2, z1,
                      x1, y2, z2,
                      x2, y2, z2,
                      x2, y2, z1);
                break;
            case NORTH:
                drawFace(buffer, matrix, normal, red, green, blue, alpha, minU, maxU, minV, maxV, light, overlay, backFace,
                      x1, y1, z1,
                      x1, y2, z1,
                      x2, y2, z1,
                      x2, y1, z1);
                break;
            case SOUTH:
                drawFace(buffer, matrix, normal, red, green, blue, alpha, minU, maxU, minV, maxV, light, overlay, backFace,
                      x2, y1, z2,
                      x2, y2, z2,
                      x1, y2, z2,
                      x1, y1, z2);
                break;
            case WEST:
                drawFace(buffer, matrix, normal, red, green, blue, alpha, minU, maxU, minV, maxV, light, overlay, backFace,
                      x1, y1, z2,
                      x1, y2, z2,
                      x1, y2, z1,
                      x1, y1, z1);
                break;
            case EAST:
                drawFace(buffer, matrix, normal, red, green, blue, alpha, minU, maxU, minV, maxV, light, overlay, backFace,
                      x2, y1, z1,
                      x2, y2, z1,
                      x2, y2, z2,
                      x2, y1, z2);
                break;
        }
    }

    private static void drawFace(IVertexBuilder buffer, Matrix4f matrix, Matrix3f normal, float red, float green, float blue, float alpha, float minU, float maxU,
          float minV, float maxV, int light, int overlay, boolean backFace,
          float x1, float y1, float z1,
          float x2, float y2, float z2,
          float x3, float y3, float z3,
          float x4, float y4, float z4) {
        buffer.pos(matrix, x1, y1, z1).color(red, green, blue, alpha).tex(minU, maxV).overlay(overlay).lightmap(light).normal(normal, 0, 1, 0).endVertex();
        buffer.pos(matrix, x2, y2, z2).color(red, green, blue, alpha).tex(minU, minV).overlay(overlay).lightmap(light).normal(normal, 0, 1, 0).endVertex();
        buffer.pos(matrix, x3, y3, z3).color(red, green, blue, alpha).tex(maxU, minV).overlay(overlay).lightmap(light).normal(normal, 0, 1, 0).endVertex();
        buffer.pos(matrix, x4, y4, z4).color(red, green, blue, alpha).tex(maxU, maxV).overlay(overlay).lightmap(light).normal(normal, 0, 1, 0).endVertex();
        if (backFace) {
            //Draw the back face as well
            buffer.pos(matrix, x2, y2, z2).color(red, green, blue, alpha).tex(minU, minV).overlay(overlay).lightmap(light).normal(normal, 0, 1, 0).endVertex();
            buffer.pos(matrix, x1, y1, z1).color(red, green, blue, alpha).tex(minU, maxV).overlay(overlay).lightmap(light).normal(normal, 0, 1, 0).endVertex();
            buffer.pos(matrix, x4, y4, z4).color(red, green, blue, alpha).tex(maxU, maxV).overlay(overlay).lightmap(light).normal(normal, 0, 1, 0).endVertex();
            buffer.pos(matrix, x3, y3, z3).color(red, green, blue, alpha).tex(maxU, minV).overlay(overlay).lightmap(light).normal(normal, 0, 1, 0).endVertex();
        }
    }
}