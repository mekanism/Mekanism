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
public class GLSMHelper {

    public static void rotate(EnumFacing facing, float north, float south, float west, float east) {
        switch (facing) {
            case NORTH:
                GlStateManager.rotate(north, 0, 1, 0);
                break;
            case SOUTH:
                GlStateManager.rotate(south, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.rotate(west, 0, 1, 0);
                break;
            case EAST:
                GlStateManager.rotate(east, 0, 1, 0);
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
        if (!FMLClientHandler.instance().hasOptifine() && glow > 0) {
            GlowInfo info = new GlowInfo(OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY, true);
            float glowStrength = (glow / 15F) * 240F;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, Math.min(glowStrength + info.lightmapLastX, 240), Math.min(glowStrength + info.lightmapLastY, 240));
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

    //Color
    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
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

    public static void color(int color) {
        GlStateManager.color(getRed(color), getGreen(color), getBlue(color), (color >> 24 & 0xFF) / 255f);
    }

    public static void color(@Nullable FluidStack fluid, float fluidScale) {
        if (fluid == null || fluid.getFluid() == null) {
            return;
        }
        int color = fluid.getFluid().getColor(fluid);
        if (fluid.getFluid().isGaseous(fluid)) {
            GlStateManager.color(getRed(color), getGreen(color), getBlue(color), Math.min(1, fluidScale + 0.2F));
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
            int color = gas.getTint();
            GlStateManager.color(getRed(color), getGreen(color), getBlue(color));
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
            GlStateManager.color(color.getColor(0) * multiplier, color.getColor(1) * multiplier, color.getColor(2) * multiplier, alpha);
        }
    }
}