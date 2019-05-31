package mekanism.client.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GLSMHelper<HELPER extends GLSMHelper<HELPER>> {

    public static GLSMHelper INSTANCE = new GLSMHelper();

    public HELPER scale(float scaleX, float scaleY, float scaleZ) {
        GlStateManager.scale(scaleX, scaleY, scaleZ);
        return (HELPER) this;
    }

    public HELPER scale(double scaleX, double scaleY, double scaleZ) {
        GlStateManager.scale(scaleX, scaleY, scaleZ);
        return (HELPER) this;
    }

    public HELPER scale(float scale) {
        return scale(scale, scale, scale);
    }

    public HELPER scale(double scale) {
        return scale(scale, scale, scale);
    }

    public HELPER translate(float x, float y, float z) {
        GlStateManager.translate(x, y, z);
        return (HELPER) this;
    }

    public HELPER translate(double x, double y, double z) {
        GlStateManager.translate(x, y, z);
        return (HELPER) this;
    }

    public HELPER translateXY(float x, float y) {
        return translate(x, y, 0);
    }

    public HELPER translateXY(double x, double y) {
        return translate(x, y, 0);
    }

    public HELPER translateXZ(float x, float z) {
        return translate(x, 0, z);
    }

    public HELPER translateXZ(double x, double z) {
        GlStateManager.translate(x, 0, z);
        return (HELPER) this;
    }

    public HELPER translateYZ(float y, float z) {
        return translate(0, y, z);
    }

    public HELPER translateYZ(double y, double z) {
        return translate(0, y, z);
    }

    public HELPER translateAll(float t) {
        return translate(t, t, t);
    }

    public HELPER translateAll(double t) {
        return translate(t, t, t);
    }

    public HELPER translateX(float x) {
        return translate(x, 0, 0);
    }

    public HELPER translateX(double x) {
        return translate(x, 0, 0);
    }

    public HELPER translateY(float y) {
        return translate(0, y, 0);
    }

    public HELPER translateY(double y) {
        return translate(0, y, 0);
    }

    public HELPER translateZ(float z) {
        return translate(0, 0, z);
    }

    public HELPER translateZ(double z) {
        return translate(0, 0, z);
    }

    public HELPER rotate(float angle, float x, float y, float z) {
        GlStateManager.rotate(angle, x, y, z);
        return (HELPER) this;
    }

    public HELPER rotateXY(float angle, float x, float y) {
        return rotate(angle, x, y, 0);
    }

    public HELPER rotateXZ(float angle, float x, float z) {
        return rotate(angle, x, 0, z);
    }

    public HELPER rotateYZ(float angle, float y, float z) {
        return rotate(angle, 0, y, z);
    }

    public HELPER rotateX(float angle, float x) {
        return rotate(angle, x, 0, 0);
    }

    public HELPER rotateY(float angle, float y) {
        return rotate(angle, 0, y, 0);
    }

    public HELPER rotateZ(float angle, float z) {
        return rotate(angle, 0, 0, z);
    }

    //TODO: Figure out why a few things don't use this and if they should be instead
    public HELPER rotate(EnumFacing facing) {
        switch (facing) /*TODO: switch the enum*/ {
            case NORTH:
                return rotateY(0, 1);
            case SOUTH:
                return rotateY(180, 1);
            case WEST:
                return rotateY(90, 1);
            case EAST:
                return rotateY(270, 1);
        }
        return (HELPER) this;
    }
}