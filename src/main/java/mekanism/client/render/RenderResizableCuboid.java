package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import mekanism.client.render.data.ValveRenderData;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
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

    private static final Vector3f NORMAL = new Vector3f(1, 1, 1).normalize();
    private static final int X_AXIS_MASK = 1 << Axis.X.ordinal();
    private static final int Y_AXIS_MASK = 1 << Axis.Y.ordinal();
    private static final int Z_AXIS_MASK = 1 << Axis.Z.ordinal();

    private RenderResizableCuboid() {
    }

    public static void renderCube(MekanismRenderer.TMP_SideRenderCheck sidesToRender, float cubeMinX, float cubeMinY, float cubeMinZ, float cubeMaxX, float cubeMaxY, float cubeMaxZ, PoseStack matrix, RenderType renderType, OrderedSubmitNodeCollector nodeCollector, int argb, int light, int overlay, FaceDisplay faceDisplay, Vec3 camPos,
          @Nullable Vec3 renderPos, TexturePicker spriteFromDirection) {
        renderCube(sidesToRender, cubeMinX, cubeMinY, cubeMinZ, cubeMaxX, cubeMaxY, cubeMaxZ, matrix, renderType, nodeCollector, light, overlay, faceDisplay, camPos, renderPos, argb, argb, argb, argb, argb, argb, spriteFromDirection);
    }

    //TODO - 26.1: Try use some kind of ColorGetter instead of unrolling arrays?

    /// Based off of Tinker's in the distant past and thoroughly disassembled and reassembled over the years.
    ///
    /// @implNote if ever different colours are used for axis side, this won't handle that like it does sprites. (e.g. currently EAST+WEST colours are the same)
    ///
    /// This has a buttload of params, but boxing them causes either a bunch of allocations, or caching to avoid said allocations as it is a fairly hot path,
    /// historically.
    ///
    /// In many cases, the boxing is for static values, so there is really no point aside from 'code tidyness'.
    ///
    /// We can revisit this if value types become a thing.
    public static void renderCube(MekanismRenderer.TMP_SideRenderCheck sidesToRender, float cubeMinX, float cubeMinY, float cubeMinZ, float cubeMaxX, float cubeMaxY, float cubeMaxZ, PoseStack matrix, RenderType renderType, OrderedSubmitNodeCollector nodeCollector, int light, int overlay, FaceDisplay faceDisplay, Vec3 camPos,
          @Nullable Vec3 renderPos, int westColor, int eastColor, int downColor, int upColor, int northColor, int southColor, TexturePicker spriteFromDirection) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[6];
        int axisToRender = 0;
        //TODO: Eventually try not rendering faces that are covered by things? At the very least for things like multiblocks
        // when one face is entirely casing and not glass
        if (renderPos != null && faceDisplay != FaceDisplay.BOTH) {
            //If we know the position this model is based around in the world, and we aren't displaying both faces
            // then calculate to see if we can skip rendering any faces due to the camera not facing them
            Vec3 minPos = renderPos.add(cubeMinX, cubeMinY, cubeMinZ);
            Vec3 maxPos = renderPos.add(cubeMaxX, cubeMaxY, cubeMaxZ);
            for (Direction direction : EnumUtils.DIRECTIONS) {
                if (!sidesToRender.shouldRenderSide(direction)) {
                    continue;
                }
                TextureAtlasSprite sprite = spriteFromDirection.apply(direction);
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
                if (!sidesToRender.shouldRenderSide(direction)) {
                    continue;
                }
                TextureAtlasSprite sprite = spriteFromDirection.apply(direction);
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
        int xShift = Mth.floor(cubeMinX);
        int yShift = Mth.floor(cubeMinY);
        int zShift = Mth.floor(cubeMinZ);
        float minX = cubeMinX - xShift;
        float minY = cubeMinY - yShift;
        float minZ = cubeMinZ - zShift;
        float maxX = cubeMaxX - xShift;
        float maxY = cubeMaxY - yShift;
        float maxZ = cubeMaxZ - zShift;
        int xDelta = calculateDelta(minX, maxX);
        int yDelta = calculateDelta(minY, maxY);
        int zDelta = calculateDelta(minZ, maxZ);
        float[] xBounds = getBlockBounds(xDelta, minX, maxX);
        float[] yBounds = getBlockBounds(yDelta, minY, maxY);
        float[] zBounds = getBlockBounds(zDelta, minZ, maxZ);

        matrix.pushPose();
        matrix.translate(xShift, yShift, zShift);

        int finalAxisToRender = axisToRender;
        nodeCollector.submitCustomGeometry(matrix, renderType, ((pose, buffer) -> {
            NormalData normalData = new NormalData(pose.normal(), NORMAL, faceDisplay);
            if ((finalAxisToRender & X_AXIS_MASK) != 0) {
                renderSideXAxis(buffer, light, overlay, faceDisplay, xDelta, yDelta, zDelta, sprites, yBounds, zBounds, xBounds, pose.pose(), normalData, westColor, eastColor);
            }
            if ((finalAxisToRender & Y_AXIS_MASK) != 0) {
                renderSideYAxis(buffer, light, overlay, faceDisplay, xDelta, yDelta, zDelta, sprites, yBounds, zBounds, xBounds, pose.pose(), normalData, downColor, upColor);
            }
            if ((finalAxisToRender & Z_AXIS_MASK) != 0) {
                renderSideZAxis(buffer, light, overlay, faceDisplay, xDelta, yDelta, zDelta, sprites, yBounds, zBounds, xBounds, pose.pose(), normalData, northColor, southColor);
            }
        }));

        matrix.popPose();
    }

    private static void renderSideZAxis(VertexConsumer buffer, int light, int overlay, FaceDisplay faceDisplay, int xDelta, int yDelta, int zDelta, TextureAtlasSprite[] sprites, float[] yBounds, float[] zBounds, float[] xBounds, Matrix4f matrix4f, NormalData normal, int northColor, int southColor) {

        TextureAtlasSprite northSprite = sprites[Direction.NORTH.ordinal()];
        TextureAtlasSprite southSprite = sprites[Direction.SOUTH.ordinal()];
        boolean hasNorth = northSprite != null;
        boolean hasSouth = southSprite != null;

        if (!hasNorth && !hasSouth) {
            return; //sanity check failed
        }

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

                    drawFace(buffer, matrix4f, minUNorth, maxUNorth, minVNorth, maxVNorth, light, overlay, faceDisplay, normal, northColor,
                          x1, y1, z1,
                          x1, y2, z1,
                          x2, y2, z1,
                          x2, y1, z1);
                }
                if (hasSouth) {
                    float z2 = zBounds[zDelta + 1];
                    // add quads
                    drawFace(buffer, matrix4f, minUSouth, maxUSouth, minVSouth, maxVSouth, light, overlay, faceDisplay, normal, southColor,
                          x2, y1, z2,
                          x2, y2, z2,
                          x1, y2, z2,
                          x1, y1, z2);
                }

            }
        }
    }

    private static void renderSideXAxis(VertexConsumer buffer, int light, int overlay, FaceDisplay faceDisplay, int xDelta, int yDelta, int zDelta,
          TextureAtlasSprite[] sprites, float[] yBounds, float[] zBounds, float[] xBounds, Matrix4f matrix4f, NormalData normal, int westColor, int eastColor) {
        TextureAtlasSprite westSprite = sprites[Direction.WEST.ordinal()];
        TextureAtlasSprite eastSprite = sprites[Direction.EAST.ordinal()];
        boolean hasWest = westSprite != null;
        boolean hasEast = eastSprite != null;

        if (!hasWest && !hasEast) {
            return; //sanity check failed
        }

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
                    drawFace(buffer, matrix4f, minUWest, maxUWest, minVWest, maxVWest, light, overlay, faceDisplay, normal, westColor,
                          x1, y1, z2,
                          x1, y2, z2,
                          x1, y2, z1,
                          x1, y1, z1);
                }
                if (hasEast) {
                    float x2 = xBounds[xDelta + 1];
                    // add quads
                    drawFace(buffer, matrix4f, minUEast, maxUEast, minVEast, maxVEast, light, overlay, faceDisplay, normal, eastColor,
                          x2, y1, z1,
                          x2, y2, z1,
                          x2, y2, z2,
                          x2, y1, z2);

                }
            }
        }
    }

    private static void renderSideYAxis(VertexConsumer buffer, int light, int overlay, FaceDisplay faceDisplay, int xDelta, int yDelta, int zDelta, TextureAtlasSprite[] sprites, float[] yBounds, float[] zBounds, float[] xBounds, Matrix4f matrix4f, NormalData normal, int downColor, int upColor) {
        TextureAtlasSprite upSprite = sprites[Direction.UP.ordinal()];
        TextureAtlasSprite downSprite = sprites[Direction.DOWN.ordinal()];
        boolean hasUp = upSprite != null;
        boolean hasDown = downSprite != null;

        if (!hasUp && !hasDown) {
            return; //sanity check failed
        }

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
                    drawFace(buffer, matrix4f, minUUp, maxUUp, minVUp, maxVUp, light, overlay, faceDisplay, normal, upColor,
                          x1, y2, z1,
                          x1, y2, z2,
                          x2, y2, z2,
                          x2, y2, z1);
                }
                if (hasDown) {
                    float y1 = yBounds[0];
                    // add quads
                    drawFace(buffer, matrix4f, minU, maxU, minV, maxV, light, overlay, faceDisplay, normal, downColor,
                          x1, y1, z2,
                          x1, y1, z1,
                          x2, y1, z1,
                          x2, y1, z2);
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
          FaceDisplay faceDisplay, NormalData normal, int color,
          float x1, float y1, float z1,
          float x2, float y2, float z2,
          float x3, float y3, float z3,
          float x4, float y4, float z4) {
        if (faceDisplay.front) {
            buffer.addVertex(matrix, x1, y1, z1)
                  .setColor(color)
                  .setUv(minU, maxV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.front.x(), normal.front.y(), normal.front.z());
            buffer.addVertex(matrix, x2, y2, z2)
                  .setColor(color)
                  .setUv(minU, minV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.front.x(), normal.front.y(), normal.front.z());
            buffer.addVertex(matrix, x3, y3, z3)
                  .setColor(color)
                  .setUv(maxU, minV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.front.x(), normal.front.y(), normal.front.z());
            buffer.addVertex(matrix, x4, y4, z4)
                  .setColor(color)
                  .setUv(maxU, maxV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.front.x(), normal.front.y(), normal.front.z());
        }
        if (faceDisplay.back) {
            buffer.addVertex(matrix, x4, y4, z4)
                  .setColor(color)
                  .setUv(maxU, maxV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.back.x(), normal.back.y(), normal.back.z());
            buffer.addVertex(matrix, x3, y3, z3)
                  .setColor(color)
                  .setUv(maxU, minV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.back.x(), normal.back.y(), normal.back.z());
            buffer.addVertex(matrix, x2, y2, z2)
                  .setColor(color)
                  .setUv(minU, minV)
                  .setOverlay(overlay)
                  .setLight(light)
                  .setNormal(normal.back.x(), normal.back.y(), normal.back.z());
            buffer.addVertex(matrix, x1, y1, z1)
                  .setColor(color)
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

    public static boolean isInsideBounds(Vec3 camera, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return minX <= camera.x && camera.x <= maxX &&
               minY <= camera.y && camera.y <= maxY &&
               minZ <= camera.z && camera.z <= maxZ;
    }

    protected static FaceDisplay getFaceDisplay(Vec3 camPos, BlockPos renderStart, int length, int width, float height) {
        return isInsideBounds(camPos, renderStart.getX(), renderStart.getY(), renderStart.getZ(),
              renderStart.getX() + length, renderStart.getY() + height, renderStart.getZ() + width)
               ? FaceDisplay.BACK : FaceDisplay.FRONT;
    }

    public static void renderValves(Vec3 camPos, PoseStack matrix, RenderType renderType, SubmitNodeCollector nodeCollector, List<ValveRenderData> valves, int overlay, TexturePicker valveTexture, BlockPos terPos, BlockPos renderStartPos, int physicalLength, int physicalWidth, int physicalHeight, int fluidColor, int glowLight, float mainFluidHeight) {
        if (!valves.isEmpty()) {
            //Use the full multiblock's render data unlike getFaceDisplay which gets the current height for calculating if it is inside
            //If we are in the multiblock, render both faces of the valves as we may be "inside" of them or inside and outside them
            // if we aren't in the multiblock though we can just get away with only rendering the front faces
            FaceDisplay faceDisplay = isInsideBounds(camPos, renderStartPos.getX(), renderStartPos.getY(), renderStartPos.getZ(), renderStartPos.getX() + physicalLength,
                  renderStartPos.getY() + physicalHeight, renderStartPos.getZ() + physicalWidth) ? FaceDisplay.BOTH : FaceDisplay.FRONT;
            for (ValveRenderData valveRenderData : valves) {
                renderValve(camPos, matrix, renderType, nodeCollector, terPos, overlay, valveRenderData, glowLight, faceDisplay, valveTexture, fluidColor, mainFluidHeight, physicalHeight);
            }
        }
    }

    private static void renderValve(Vec3 camPos, PoseStack matrix, RenderType renderType, SubmitNodeCollector nodeCollector, BlockPos rendererPos, int overlay, ValveRenderData valveRenderData, int glow, FaceDisplay faceDisplay, TexturePicker valveTexture, int fluidColor, float mainFluidHeight, int physicalHeight) {
        Direction side = valveRenderData.getSide();
        if (ModelRenderer.shouldSkipValveRender(mainFluidHeight, side, physicalHeight, valveRenderData.getValveFluidHeight())) {
            return;
        }
        matrix.pushPose();
        matrix.translate(valveRenderData.getValveLocation().getX() - rendererPos.getX(), valveRenderData.getValveLocation().getY() - rendererPos.getY(), valveRenderData.getValveLocation().getZ() - rendererPos.getZ());
        renderCube(valveRenderData.renderCheck, valveRenderData.minX, valveRenderData.minY, valveRenderData.minZ, valveRenderData.maxX, valveRenderData.maxY, valveRenderData.maxZ, matrix, renderType, nodeCollector, fluidColor, glow, overlay, faceDisplay, camPos, Vec3.atLowerCornerOf(valveRenderData.getValveLocation()), valveTexture);
        matrix.popPose();

    }

    //TODO - 26.1: Should we no-op all the cases of scale == 0
    public static void renderObject(Vec3 camPos, PoseStack matrix, RenderType renderType, SubmitNodeCollector nodeCollector, MekanismRenderer.TMP_SideRenderCheck sideRenderCheck, float cubeMinX, float cubeMinY, float cubeMinZ, float cubeMaxX, float cubeMaxY, float cubeMaxZ, TexturePicker spriteFromDirection, int overlay, int glowLight, int scaledColor, BlockPos terPos, BlockPos renderStartPos, int physicalLength, int physicalWidth, float physicalHeight) {
        matrix.pushPose();
        matrix.translate(renderStartPos.getX() - terPos.getX(), renderStartPos.getY() - terPos.getY(), renderStartPos.getZ() - terPos.getZ());
        FaceDisplay faceDisplay = getFaceDisplay(camPos, renderStartPos, physicalLength, physicalWidth, physicalHeight);
        renderCube(sideRenderCheck, cubeMinX, cubeMinY, cubeMinZ, cubeMaxX, cubeMaxY, cubeMaxZ, matrix, renderType, nodeCollector, scaledColor, glowLight, overlay, faceDisplay, camPos, Vec3.atLowerCornerOf(renderStartPos), spriteFromDirection);
        matrix.popPose();
    }

    /// avoid allocating a new one just to be non-null
    private static final Vector3f UNUSED = new Vector3f();
    /**
     * Used to only have to calculate normals once rather than transforming based on the matrix for every vertex call. If a face shouldn't be displayed the normal vector
     * will be zero.
     */
    private record NormalData(Vector3f front, Vector3f back) {

        private NormalData(Matrix3f normalMatrix, Vector3f normal, FaceDisplay faceDisplay) {
            this(faceDisplay.front ? calculate(normalMatrix, normal.x(), normal.y(), normal.z()) : UNUSED,
                  faceDisplay.back ? calculate(normalMatrix, -normal.x(), -normal.y(), -normal.z()) : UNUSED);
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

    public sealed interface TexturePicker permits MekanismRenderer.SingleTexturePicker, MekanismRenderer.ValveTextureGetter {

        TextureAtlasSprite apply(Direction direction);
    }
}