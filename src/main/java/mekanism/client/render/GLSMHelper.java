package mekanism.client.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.tier.BaseTier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GLSMHelper<HELPER extends GLSMHelper<HELPER>> {

    public final static GLSMHelper INSTANCE = new GLSMHelper();

    /**
     * @return this cast to HELPER as to reduce unchecked cast warnings
     */
    private HELPER get() {
        return (HELPER) this;
    }

    public HELPER scale(float scaleX, float scaleY, float scaleZ) {
        GlStateManager.scale(scaleX, scaleY, scaleZ);
        return get();
    }

    public HELPER scale(double scaleX, double scaleY, double scaleZ) {
        GlStateManager.scale(scaleX, scaleY, scaleZ);
        return get();
    }

    public HELPER scale(float scale) {
        return scale(scale, scale, scale);
    }

    public HELPER scale(double scale) {
        return scale(scale, scale, scale);
    }

    public HELPER translate(float x, float y, float z) {
        GlStateManager.translate(x, y, z);
        return get();
    }

    public HELPER translate(double x, double y, double z) {
        GlStateManager.translate(x, y, z);
        return get();
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
        return translate(x, 0, z);
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
        return get();
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
        return get();
    }

    //TODO: Better description saying that resetColor needs to be called
    //Color
    public HELPER resetColor() {
        return color(1, 1, 1, 1);
    }

    public HELPER color(float red, float green, float blue, float alpha) {
        GlStateManager.color(red, green, blue, alpha);
        return get();
    }

    public HELPER colorAlpha(float alpha) {
        return color(1, 1, 1, alpha);
    }

    public HELPER color3f(float red, float green, float blue) {
        return color(red, green, blue, 1);
    }

    public HELPER color3f(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        return color3f(red, green, blue);
    }

    public HELPER color(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float alpha = (color >> 24 & 0xFF) / 255f;
        return color(red, green, blue, alpha);
    }

    public HELPER color(@Nullable FluidStack fluid) {
        return fluid == null || fluid.getFluid() == null ? get() : color(fluid.getFluid().getColor(fluid));
    }

    public HELPER color(@Nullable Fluid fluid) {
        return fluid == null ? get() : color(fluid.getColor());
    }

    public HELPER color(@Nullable GasStack gasStack) {
        return gasStack == null ? get() : color(gasStack.getGas());
    }

    public HELPER color(@Nullable Gas gas) {
        return gas == null ? get() : color3f(gas.getTint());
    }

    public HELPER color(@Nonnull BaseTier tier) {
        return color(tier.getColor());
    }

    public HELPER color(@Nullable EnumColor color) {
        return color(color, 1.0F);
    }

    public HELPER color(@Nullable EnumColor color, float alpha) {
        return color(color, alpha, 1.0F);
    }

    public HELPER color(@Nullable EnumColor color, float alpha, float multiplier) {
        return color == null ? get() : color(color.getColor(0) * multiplier, color.getColor(1) * multiplier, color.getColor(2) * multiplier, alpha);
    }
}