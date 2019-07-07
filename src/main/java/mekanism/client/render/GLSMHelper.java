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
    private static float GAS_RENDER_BASE = 0.2F;

    /**
     * @return this cast to HELPER as to reduce unchecked cast warnings
     */
    private HELPER get() {
        return (HELPER) this;
    }

    //TODO: Figure out why a few things don't use this and if they should be instead
    public static void rotate(EnumFacing facing) {
        switch (facing) /*TODO: switch the enum*/ {
            case NORTH:
                GlStateManager.rotate(0, 0, 1, 0);
                break;
            case SOUTH:
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case EAST:
                GlStateManager.rotate(270, 0, 1, 0);
                break;
        }
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

    private float getRed(int color) {
        return (color >> 16 & 0xFF) / 255.0F;
    }

    private float getGreen(int color) {
        return (color >> 8 & 0xFF) / 255.0F;
    }

    private float getBlue(int color) {
        return (color & 0xFF) / 255.0F;
    }

    public HELPER color3f(int color) {
        return color3f(getRed(color), getGreen(color), getBlue(color));
    }

    public HELPER color(int color) {
        float alpha = (color >> 24 & 0xFF) / 255f;
        return color(getRed(color), getGreen(color), getBlue(color), alpha);
    }

    public HELPER color(@Nullable FluidStack fluid, float fluidScale) {
        if (fluid == null || fluid.getFluid() == null) {
            return get();
        }
        int color = fluid.getFluid().getColor(fluid);
        if (fluid.getFluid().isGaseous(fluid)) {
            return color(getRed(color), getGreen(color), getBlue(color), Math.min(1, fluidScale + GAS_RENDER_BASE));
        }
        return color(color);
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