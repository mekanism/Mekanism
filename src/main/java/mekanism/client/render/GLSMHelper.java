package mekanism.client.render;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.tier.BaseTier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GLSMHelper<HELPER extends GLSMHelper<HELPER>> {

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

    public static final GlowInfo NO_GLOW = new GlowInfo(0, 0, false);

    @Nonnull
    public static GlowInfo enableGlow() {
        return enableGlow(15);
    }

    @Nonnull
    public static GlowInfo enableGlow(int glow) {
        //Glow is needed when underground or in the dark. Initial thoughts were wrong
        if (!FMLClientHandler.instance().hasOptifine() && glow > 0) {
            GlowInfo info = new GlowInfo(OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY, true);

            float glowRatioX = Math.min((glow / 15F) * 240F + info.lightmapLastX, 240);
            float glowRatioY = Math.min((glow / 15F) * 240F + info.lightmapLastY, 240);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, glowRatioX, glowRatioY);
            return info;
        }
        return NO_GLOW;
    }

    @Nonnull
    public static GlowInfo enableGlow(@Nullable FluidStack fluid) {
        return fluid == null || fluid.getFluid() == null ? NO_GLOW : enableGlow(fluid.getFluid().getLuminosity(fluid));
    }

    @Nonnull
    public static GlowInfo enableGlow(@Nullable Fluid fluid) {
        return fluid == null ? NO_GLOW : enableGlow(fluid.getLuminosity());
    }

    public static void disableGlow(@Nonnull GlowInfo info) {
        if (info.glowEnabled) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, info.lightmapLastX, info.lightmapLastY);
        }
    }

    public static class GlowInfo {

        private final boolean glowEnabled;
        private final float lightmapLastX;
        private final float lightmapLastY;

        public GlowInfo(float lightmapLastX, float lightmapLastY, boolean glowEnabled) {
            this.lightmapLastX = lightmapLastX;
            this.lightmapLastY = lightmapLastY;
            this.glowEnabled = glowEnabled;
        }
    }

    //TODO: Better description saying that resetColor needs to be called
    //Color
    public static void resetColor() {
        color(1, 1, 1, 1);
    }

    public static void color(float red, float green, float blue, float alpha) {
        GlStateManager.color(red, green, blue, alpha);
    }

    public static void colorAlpha(float alpha) {
        color(1, 1, 1, alpha);
    }

    public static void color3f(float red, float green, float blue) {
        color(red, green, blue, 1);
    }

    private static float getRed(int color) {
        return (color >> 16 & 0xFF) / 255.0F;
    }

    private static float getGreen(int color) {
        return (color >> 8 & 0xFF) / 255.0F;
    }

    private static float getBlue(int color) {
        return (color & 0xFF) / 255.0F;
    }

    public static void color3f(int color) {
        color3f(getRed(color), getGreen(color), getBlue(color));
    }

    public static void color(int color) {
        float alpha = (color >> 24 & 0xFF) / 255f;
        color(getRed(color), getGreen(color), getBlue(color), alpha);
    }

    public static void color(@Nullable FluidStack fluid, float fluidScale) {
        if (fluid == null || fluid.getFluid() == null) {
            return;
        }
        int color = fluid.getFluid().getColor(fluid);
        if (fluid.getFluid().isGaseous(fluid)) {
            color(getRed(color), getGreen(color), getBlue(color), Math.min(1, fluidScale + 0.2F));
        } else {
            color(color);
        }
    }

    public static void color(@Nullable FluidStack fluid) {
        if (fluid != null && fluid.getFluid() != null) {
            color(fluid.getFluid().getColor(fluid));
        }
    }

    public static void color(@Nullable Fluid fluid) {
        if (fluid != null) {
            color(fluid.getColor());
        }
    }

    public static void color(@Nullable GasStack gasStack) {
        if (gasStack != null) {
            color(gasStack.getGas());
        }
    }

    public static void color(@Nullable Gas gas) {
        if (gas != null) {
            color3f(gas.getTint());
        }
    }

    public static void color(@Nonnull BaseTier tier) {
        color(tier.getColor());
    }

    public static void color(@Nullable EnumColor color) {
        color(color, 1.0F);
    }

    public static void color(@Nullable EnumColor color, float alpha) {
        color(color, alpha, 1.0F);
    }

    public static void color(@Nullable EnumColor color, float alpha, float multiplier) {
        if (color != null) {
            color(color.getColor(0) * multiplier, color.getColor(1) * multiplier, color.getColor(2) * multiplier, alpha);
        }
    }
}