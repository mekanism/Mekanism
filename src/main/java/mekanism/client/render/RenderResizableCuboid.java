package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Adapted from Mantle's FluidRenderer and Tinker's SmelteryTankRenderer
 */
public class RenderResizableCuboid {

    /**
     * Used to not need to create multiple arrays when we just want to fill it differently at times, and given rendering TERs is not multithreaded it is perfectly safe to
     * just use one backing "temporary" array.
     */
    private static final int[] combinedARGB = new int[EnumUtils.DIRECTIONS.length];
    private static final Vector3f NORMAL = new Vector3f(1, 1, 1).normalize();
    private static final int X_AXIS_MASK = 1 << Axis.X.ordinal();
    private static final int Y_AXIS_MASK = 1 << Axis.Y.ordinal();
    private static final int Z_AXIS_MASK = 1 << Axis.Z.ordinal();

    private RenderResizableCuboid() {
    }

    public static void renderCube(Model3D cube, PoseStack matrix, VertexConsumer buffer, int argb, int light, int overlay, FaceDisplay faceDisplay, Camera camera,
          @Nullable Vec3 renderPos) {
        Arrays.fill(combinedARGB, argb);
        renderCube(cube, matrix, buffer, combinedARGB, light, overlay, faceDisplay, camera, renderPos);
    }

    /**
     * @implNote Based off of Tinker's
     */
    public static void renderCube(Model3D cube, PoseStack matrix, VertexConsumer buffer, int[] colors, int light, int overlay, FaceDisplay faceDisplay, Camera camera,
          @Nullable Vec3 renderPos) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[6];
        int axisToRender = 0;
        //TODO: Eventually try not rendering faces that are covered by things? At the very least for things like multiblocks
        // when one face is entirely casing and not glass
        if (renderPos != null && faceDisplay != FaceDisplay.BOTH) {
            //If we know the position this model is based around in the world, and we aren't displaying both faces
            // then calculate to see if we can skip rendering any faces due to the camera not facing them
            Vec3 camPos = camera.getPosition();
            Vec3 minPos = renderPos.add(cube.minX, cube.minY, cube.minZ);
            Vec3 maxPos = renderPos.add(cube.maxX, cube.maxY, cube.maxZ);
            for (Direction direction : EnumUtils.DIRECTIONS) {
                TextureAtlasSprite sprite = cube.getSpriteToRender(direction);
                if (sprite != null) {
                    Axis axis = direction.getAxis();
                    AxisDirection axisDirection = direction.getAxisDirection();
                    double planeLocation = switch (axisDirection) {
                        case POSITIVE -> axis.choose(maxPos.x, maxPos.y, maxPos.z);
                        case NEGATIVE -> axis.choose(minPos.x, minPos.y, minPos.z);
                    };
                    double cameraPosition = axis.choose(camPos.x, camPos.y, camPos.z);
                    //Check whether the camera's position is past the side that it can render on for the face
                    // that we want to be rendering
                    if (faceDisplay.front == (axisDirection == AxisDirection.POSITIVE)) {
                        if (cameraPosition >= planeLocation) {
                            sprites[direction.ordinal()] = sprite;
                            axisToRender |= 1 << axis.ordinal();
                        }
                    } else if (cameraPosition <= planeLocation) {
                        sprites[direction.ordinal()] = sprite;
                        axisToRender |= 1 << axis.ordinal();
                    }
                }
            }
        } else {
            for (Direction direction : EnumUtils.DIRECTIONS) {
                TextureAtlasSprite sprite = cube.getSpriteToRender(direction);
                if (sprite != null) {
                    sprites[direction.ordinal()] = sprite;
                    axisToRender |= 1 << direction.getAxis().ordinal();
                }
            }
        }
        if (axisToRender == 0) {
            //Skip rendering if no sides are meant to be rendered
            return;
        }
        //TODO: Further attempt to fix z-fighting at larger distances if we make it not render the sides when it is in a solid block
        // that may improve performance some, but definitely would reduce/remove the majority of remaining z-fighting that is going on
        //Shift it so that the min values are all greater than or equal to zero as the various drawing code
        // has some issues when it comes to handling negative numbers
        int xShift = Mth.floor(cube.minX);
        int yShift = Mth.floor(cube.minY);
        int zShift = Mth.floor(cube.minZ);
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

        matrix.pushPose();
        matrix.translate(xShift, yShift, zShift);
        PoseStack.Pose lastMatrix = matrix.last();
        Matrix4f matrix4f = lastMatrix.pose();
        NormalData normal = new NormalData(lastMatrix.normal(), NORMAL, faceDisplay);

        if ((axisToRender & X_AXIS_MASK) != 0) {
            renderSideXAxis(buffer, colors, light, overlay, faceDisplay, xDelta, yDelta, zDelta, sprites, yBounds, zBounds, xBounds, matrix4f, normal);
        }
        if ((axisToRender & Y_AXIS_MASK) != 0) {
            renderSideYAxis(buffer, colors, light, overlay, faceDisplay, xDelta, yDelta, zDelta, sprites, yBounds, zBounds, xBounds, matrix4f, normal);
        }
        if ((axisToRender & Z_AXIS_MASK) != 0) {
            renderSideZAxis(buffer, colors, light, overlay, faceDisplay, xDelta, yDelta, zDelta, sprites, yBounds, zBounds, xBounds, matrix4f, normal);
        }

        matrix.popPose();
    }

    private static void renderSideZAxis(VertexConsumer buffer, int[] colors, int light, int overlay, FaceDisplay faceDisplay, int xDelta, int yDelta, int zDelta, TextureAtlasSprite[] sprites, float[] yBounds, float[] zBounds, float[] xBounds, Matrix4f matrix4f, NormalData normal) {

        TextureAtlasSprite northSprite = sprites[Direction.NORTH.ordinal()];
        TextureAtlasSprite southSprite = sprites[Direction.SOUTH.ordinal()];
        boolean hasNorth = northSprite != null;
        boolean hasSouth = southSprite != null;

        if (!hasNorth && !hasSouth) {
            return; //sanity check failed
        }

        int colorNorth = colors[Direction.NORTH.ordinal()];
        int colorSouth = colors[Direction.SOUTH.ordinal()];

        int redNorth = ARGB32.red(colorNorth);
        int greenNorth = ARGB32.green(colorNorth);
        int blueNorth = ARGB32.blue(colorNorth);
        int alphaNorth = ARGB32.alpha(colorNorth);
        int redSouth = ARGB32.red(colorSouth);
        int greenSouth = ARGB32.green(colorSouth);
        int blueSouth = ARGB32.blue(colorSouth);
        int alphaSouth = ARGB32.alpha(colorSouth);

        // render each side
        for (int y = 0; y <= yDelta; y += 1) {
            float y1 = yBounds[y];
            float y2 = yBounds[y + 1];
            float vBoundsMin = minBound(y1, y2);
            float vBoundsMax = maxBound(y1, y2);

            //Flip V - north
            float minVNorth;
            float maxVNorth;
            if (hasNorth) {
                minVNorth = northSprite.getV(1 - vBoundsMax);
                maxVNorth = northSprite.getV(1 - vBoundsMin);
            } else {
                minVNorth = 0F;
                maxVNorth = 0F;
            }

            //Flip V
            float minVSouth;
            float maxVSouth;
            if (hasSouth) {
                minVSouth = southSprite.getV(1 - vBoundsMax);
                maxVSouth = southSprite.getV(1 - vBoundsMin);
            } else {
                minVSouth = 0F;
                maxVSouth = 0F;
            }

            for (int x = 0; x <= xDelta; x += 1) {
                // start with texture coordinates
                float x1 = xBounds[x];
                float x2 = xBounds[x + 1];

                // choose UV based on opposite two axis
                float uBoundsMin = minBound(x2, x1);
                float uBoundsMax = maxBound(x2, x1);

                float minUNorth;
                float maxUNorth;
                if (hasNorth) {
                    minUNorth = northSprite.getU(uBoundsMin);
                    maxUNorth = northSprite.getU(uBoundsMax);
                } else {
                    minUNorth = 0F;
                    maxUNorth = 0F;
                }

                float minUSouth;
                float maxUSouth;
                if (hasSouth) {
                    minUSouth = southSprite.getU(uBoundsMin);
                    maxUSouth = southSprite.getU(uBoundsMax);
                } else {
                    minUSouth = 0F;
                    maxUSouth = 0F;
                }

                if (hasNorth) {
                    float z1 = zBounds[0];
                    // add quads

                    drawFace(buffer, matrix4f, minUNorth, maxUNorth, minVNorth, maxVNorth, light, overlay, faceDisplay, normal,
                          x1, y1, z1,
                          x1, y2, z1,
                          x2, y2, z1,
                          x2, y1, z1, redNorth, greenNorth, blueNorth, alphaNorth);
                }
                if (hasSouth) {
                    float z2 = zBounds[zDelta + 1];
                    // add quads
                    drawFace(buffer, matrix4f, minUSouth, maxUSouth, minVSouth, maxVSouth, light, overlay, faceDisplay, normal,
                          x2, y1, z2,
                          x2, y2, z2,
                          x1, y2, z2,
                          x1, y1, z2, redSouth, greenSouth, blueSouth, alphaSouth);
                }

            }
        }
    }

    private static void renderSideXAxis(VertexConsumer buffer, int[] colors, int light, int overlay, FaceDisplay faceDisplay, int xDelta, int yDelta, int zDelta, TextureAtlasSprite[] sprites, float[] yBounds, float[] zBounds, float[] xBounds, Matrix4f matrix4f, NormalData normal) {
        TextureAtlasSprite westSprite = sprites[Direction.WEST.ordinal()];
        TextureAtlasSprite eastSprite = sprites[Direction.EAST.ordinal()];
        boolean hasWest = westSprite != null;
        boolean hasEast = eastSprite != null;

        if (!hasWest && !hasEast) {
            return; //sanity check failed
        }

        int westColor = colors[Direction.WEST.ordinal()];
        int eastColor = colors[Direction.EAST.ordinal()];
        int redWest = ARGB32.red(westColor);
        int greenWest = ARGB32.green(westColor);
        int blueWest = ARGB32.blue(westColor);
        int alphaWest = ARGB32.alpha(westColor);
        int redEast = ARGB32.red(eastColor);
        int greenEast = ARGB32.green(eastColor);
        int blueEast = ARGB32.blue(eastColor);
        int alphaEast = ARGB32.alpha(eastColor);

        // render each side
        for (int y = 0; y <= yDelta; y += 1) {
            float y1 = yBounds[y], y2 = yBounds[y + 1];
            float vBoundsMin = minBound(y1, y2);
            float vBoundsMax = maxBound(y1, y2);

            //Flip V - West
            float minVWest;
            float maxVWest;
            if (hasWest) {
                minVWest = westSprite.getV(1 - vBoundsMax);
                maxVWest = westSprite.getV(1 - vBoundsMin);
            } else {
                minVWest = 0F;
                maxVWest = 0F;
            }

            //Flip V - East
            float minVEast;
            float maxVEast;
            if (hasEast) {
                minVEast = eastSprite.getV(1 - vBoundsMax);
                maxVEast = eastSprite.getV(1 - vBoundsMin);
            } else {
                minVEast = 0F;
                maxVEast = 0F;
            }

            for (int z = 0; z <= zDelta; z += 1) {
                float z1 = zBounds[z];
                float z2 = zBounds[z + 1];
                float uBoundsMin = minBound(z2, z1);
                float uBoundsMax = maxBound(z2, z1);

                float minUWest;
                float maxUWest;
                if (hasWest) {
                    minUWest = westSprite.getU(uBoundsMin);
                    maxUWest = westSprite.getU(uBoundsMax);
                } else {
                    minUWest = 0F;
                    maxUWest = 0F;
                }

                float minUEast;
                float maxUEast;
                if (hasEast) {
                    minUEast = eastSprite.getU(uBoundsMin);
                    maxUEast = eastSprite.getU(uBoundsMax);
                } else {
                    minUEast = 0F;
                    maxUEast = 0F;
                }

                if (hasWest) {
                    float x1 = xBounds[0];
                    // add quads
                    drawFace(buffer, matrix4f, minUWest, maxUWest, minVWest, maxVWest, light, overlay, faceDisplay, normal,
                          x1, y1, z2,
                          x1, y2, z2,
                          x1, y2, z1,
                          x1, y1, z1, redWest, greenWest, blueWest, alphaWest);
                }
                if (hasEast) {
                    float x2 = xBounds[xDelta + 1];
                    // add quads
                    drawFace(buffer, matrix4f, minUEast, maxUEast, minVEast, maxVEast, light, overlay, faceDisplay, normal,
                          x2, y1, z1,
                          x2, y2, z1,
                          x2, y2, z2,
                          x2, y1, z2, redEast, greenEast, blueEast, alphaEast);

                }
            }
        }
    }

    private static void renderSideYAxis(VertexConsumer buffer, int[] colors, int light, int overlay, FaceDisplay faceDisplay, int xDelta, int yDelta, int zDelta, TextureAtlasSprite[] sprites, float[] yBounds, float[] zBounds, float[] xBounds, Matrix4f matrix4f, NormalData normal) {
        TextureAtlasSprite upSprite = sprites[Direction.UP.ordinal()];
        TextureAtlasSprite downSprite = sprites[Direction.DOWN.ordinal()];
        boolean hasUp = upSprite != null;
        boolean hasDown = downSprite != null;

        if (!hasUp && !hasDown) {
            return; //sanity check failed
        }

        int downColor = colors[Direction.DOWN.ordinal()];
        int upColor = colors[Direction.UP.ordinal()];
        int redUp = ARGB32.red(upColor);
        int greenUp = ARGB32.green(upColor);
        int blueUp = ARGB32.blue(upColor);
        int alphaUp = ARGB32.alpha(upColor);
        int redDown = ARGB32.red(downColor);
        int greenDown = ARGB32.green(downColor);
        int blueDown = ARGB32.blue(downColor);
        int alphaDown = ARGB32.alpha(downColor);

        // render each side
        for (int z = 0; z <= zDelta; z += 1) {
            float z1 = zBounds[z];
            float z2 = zBounds[z + 1];
            float vBoundsMin = minBound(z2, z1);
            float vBoundsMax = maxBound(z2, z1);
            //Flip V - Up
            float minVUp;
            float maxVUp;
            if (hasUp) {
                minVUp = upSprite.getV(1 - vBoundsMax);
                maxVUp = upSprite.getV(1 - vBoundsMin);
            } else {
                minVUp = 0F;
                maxVUp = 0F;
            }
            //Flip V - Down
            float minV;
            float maxV;
            if (hasDown) {
                minV = downSprite.getV(1 - vBoundsMax);
                maxV = downSprite.getV(1 - vBoundsMin);
            } else {
                minV = 0F;
                maxV = 0F;
            }

            for (int x = 0; x <= xDelta; x += 1) {
                float x1 = xBounds[x];
                float x2 = xBounds[x + 1];

                float uBoundsMin = minBound(x1, x2);
                float uBoundsMax = maxBound(x1, x2);

                float minUUp;
                float maxUUp;
                if (hasUp) {
                    minUUp = upSprite.getU(uBoundsMin);
                    maxUUp = upSprite.getU(uBoundsMax);
                } else {
                    minUUp = 0F;
                    maxUUp = 0F;
                }

                float minU;
                float maxU;
                if (hasDown) {
                    minU = downSprite.getU(uBoundsMin);
                    maxU = downSprite.getU(uBoundsMax);
                } else {
                    minU = 0F;
                    maxU = 0F;
                }

                if (hasUp) {
                    float y2 = yBounds[yDelta + 1];
                    // add quads
                    drawFace(buffer, matrix4f, minUUp, maxUUp, minVUp, maxVUp, light, overlay, faceDisplay, normal,
                          x1, y2, z1,
                          x1, y2, z2,
                          x2, y2, z2,
                          x2, y2, z1, redUp, greenUp, blueUp, alphaUp);
                }
                if (hasDown) {
                    float y1 = yBounds[0];
                    // add quads
                    drawFace(buffer, matrix4f, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal,
                          x1, y1, z2,
                          x1, y1, z1,
                          x2, y1, z1,
                          x2, y1, z2, redDown, greenDown, blueDown, alphaDown);
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

    private static void drawFace(VertexConsumer buffer, Matrix4f matrix, float minU, float maxU, float minV, float maxV, int light, int overlay,
          FaceDisplay faceDisplay, NormalData normal,
          float x1, float y1, float z1,
          float x2, float y2, float z2,
          float x3, float y3, float z3,
          float x4, float y4, float z4, int red, int green, int blue, int alpha) {
        if (faceDisplay.front) {
            buffer.addVertex(matrix, x1, y1, z1)
                  .setColor(red, green, blue, alpha)
                  .setUv(minU, maxV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.front.x(), normal.front.y(), normal.front.z());
            buffer.addVertex(matrix, x2, y2, z2)
                  .setColor(red, green, blue, alpha)
                  .setUv(minU, minV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.front.x(), normal.front.y(), normal.front.z());
            buffer.addVertex(matrix, x3, y3, z3)
                  .setColor(red, green, blue, alpha)
                  .setUv(maxU, minV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.front.x(), normal.front.y(), normal.front.z());
            buffer.addVertex(matrix, x4, y4, z4)
                  .setColor(red, green, blue, alpha)
                  .setUv(maxU, maxV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.front.x(), normal.front.y(), normal.front.z());
        }
        if (faceDisplay.back) {
            buffer.addVertex(matrix, x4, y4, z4)
                  .setColor(red, green, blue, alpha)
                  .setUv(maxU, maxV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.back.x(), normal.back.y(), normal.back.z());
            buffer.addVertex(matrix, x3, y3, z3)
                  .setColor(red, green, blue, alpha)
                  .setUv(maxU, minV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.back.x(), normal.back.y(), normal.back.z());
            buffer.addVertex(matrix, x2, y2, z2)
                  .setColor(red, green, blue, alpha)
                  .setUv(minU, minV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.back.x(), normal.back.y(), normal.back.z());
            buffer.addVertex(matrix, x1, y1, z1)
                  .setColor(red, green, blue, alpha)
                  .setUv(minU, maxV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.back.x(), normal.back.y(), normal.back.z());
        }
    }

    private static float minBound(float min, float max) {
        // wrap UV to be between 0 and 1, assumes none of the positions lie outside the 0, 0, 0 to 1, 1, 1 range
        // however, one of them might be exactly on the 1.0 bound, that one should be set to 1 instead of left at 0
        boolean bigger = min > max;
        min = min % 1;
        if (bigger) {
            return min == 0 ? 1 : min;
        }
        return min;
    }

    private static float maxBound(float min, float max) {
        // wrap UV to be between 0 and 1, assumes none of the positions lie outside the 0, 0, 0 to 1, 1, 1 range
        // however, one of them might be exactly on the 1.0 bound, that one should be set to 1 instead of left at 0
        boolean bigger = min > max;
        max = max % 1;
        if (bigger) {
            return max;
        }
        return max == 0 ? 1 : max;
    }

    /**
     * Used to only have to calculate normals once rather than transforming based on the matrix for every vertex call. If a face shouldn't be displayed the normal vector
     * will be zero.
     */
    private record NormalData(Vector3f front, Vector3f back) {

        private NormalData(Matrix3f normalMatrix, Vector3f normal, FaceDisplay faceDisplay) {
            this(faceDisplay.front ? calculate(normalMatrix, normal.x(), normal.y(), normal.z()) : new Vector3f(),
                  faceDisplay.back ? calculate(normalMatrix, -normal.x(), -normal.y(), -normal.z()) : new Vector3f());
        }

        private static Vector3f calculate(Matrix3f normalMatrix, float x, float y, float z) {
            Vector3f matrixAdjustedNormal = new Vector3f(x, y, z);
            return matrixAdjustedNormal.mul(normalMatrix);
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