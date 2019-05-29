package mekanism.client.render;

import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.tier.BaseTier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class MekanismRenderHelper {

    private Deque<Pair<KnownStates, Boolean>> changedStates = new ArrayDeque<>();
    private boolean hasMatrix;
    private boolean colorSet;
    private boolean colorMasked;
    private boolean glowEnabled;
    private float lightmapLastX;
    private float lightmapLastY;

    public MekanismRenderHelper() {
        this(false);
    }

    public MekanismRenderHelper(boolean hasMatrix) {
        this.hasMatrix = hasMatrix;
        if (hasMatrix) {
            GlStateManager.pushMatrix();
        }
    }

    //TODO: Invalidate this better/throw some kind of warning so that we can easier see if something attempts to use one we already cleaned up
    // That or add support for reusing it. Adding support for reusing it at least via a reset method would potentially be useful for various spots
    // that the helper is just used for color management
    public void cleanup() {
        if (glowEnabled) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapLastX, lightmapLastY);
            glowEnabled = false;
        }
        if (colorMasked) {
            GlStateManager.colorMask(true, true, true, true);
            colorMasked = false;
        }
        if (colorSet) {
            //Reset the color
            GlStateManager.color(1, 1, 1, 1);
            colorSet = false;
        }
        while (!changedStates.isEmpty()) {
            Pair<KnownStates, Boolean> stateInfo = changedStates.pop();
            stateInfo.getKey().cleanup(stateInfo.getValue());
        }
        if (hasMatrix) {
            GlStateManager.popMatrix();
            hasMatrix = false;
        }
    }

    private MekanismRenderHelper enable(KnownStates state) {
        changedStates.push(Pair.of(state, true));
        state.enable();
        return this;
    }

    private MekanismRenderHelper disable(KnownStates state) {
        changedStates.push(Pair.of(state, false));
        state.disable();
        return this;
    }

    //TODO: Better name so that it is clear it only happens if no matrix exists for use after cleanup is called
    public MekanismRenderHelper addMatrix() {
        if (!hasMatrix) {
            hasMatrix = true;
            GlStateManager.pushMatrix();
        }
        return this;
    }

    public MekanismRenderHelper enableBlendPreset() {
        //TODO: Verify we don't need more here to mirror the saving done by glPushAttrib
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        disableAlpha().enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        return this;
    }

    //Glow
    public MekanismRenderHelper enableGlow() {
        return enableGlow(15);
    }

    public MekanismRenderHelper enableGlow(int glow) {
        //TODO: Is glow even needed anymore it seems things work fine without it??
        if (!glowEnabled && !FMLClientHandler.instance().hasOptifine()) {
            //TODO: Verify we don't need more here to mirror the saving done by glPushAttrib
            lightmapLastX = OpenGlHelper.lastBrightnessX;
            lightmapLastY = OpenGlHelper.lastBrightnessY;

            float glowRatioX = Math.min((glow / 15F) * 240F + lightmapLastX, 240);
            float glowRatioY = Math.min((glow / 15F) * 240F + lightmapLastY, 240);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, glowRatioX, glowRatioY);
            glowEnabled = true;
        }
        return this;
    }

    public MekanismRenderHelper enableGlow(@Nullable FluidStack fluid) {
        return fluid == null || fluid.getFluid() == null ? this : enableGlow(fluid.getFluid().getLuminosity(fluid));
    }

    public MekanismRenderHelper enableGlow(@Nullable Fluid fluid) {
        return fluid == null ? this : enableGlow(fluid.getLuminosity());
    }

    //Helper wrappers
    public MekanismRenderHelper scale(float scaleX, float scaleY, float scaleZ) {
        GlStateManager.scale(scaleX, scaleY, scaleZ);
        return this;
    }

    public MekanismRenderHelper scale(double scaleX, double scaleY, double scaleZ) {
        GlStateManager.scale(scaleX, scaleY, scaleZ);
        return this;
    }

    public MekanismRenderHelper scale(float scale) {
        return scale(scale, scale, scale);
    }

    public MekanismRenderHelper scale(double scale) {
        return scale(scale, scale, scale);
    }

    //Color
    public MekanismRenderHelper colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        //TODO: If all true then don't set colorMasked
        colorMasked = true;
        GlStateManager.colorMask(red, green, blue, alpha);
        return this;
    }

    public MekanismRenderHelper colorMaskAlpha() {
        return colorMask(true, true, true, false);
    }

    public MekanismRenderHelper color(float red, float green, float blue, float alpha) {
        colorSet = true;
        GlStateManager.color(red, green, blue, alpha);
        return this;
    }

    public MekanismRenderHelper colorAlpha(float alpha) {
        return color(1, 1, 1, alpha);
    }

    public MekanismRenderHelper color3f(float red, float green, float blue) {
        return color(red, green, blue, 1.0F);
    }

    public MekanismRenderHelper color3f(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        return color3f(red, green, blue);
    }

    public MekanismRenderHelper color(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float alpha = (color >> 24 & 0xFF) / 255f;
        return color(red, green, blue, alpha);
    }

    public MekanismRenderHelper color(@Nullable FluidStack fluid) {
        return fluid == null || fluid.getFluid() == null ? this : color(fluid.getFluid().getColor(fluid));
    }

    public MekanismRenderHelper color(@Nullable Fluid fluid) {
        return fluid == null ? this : color(fluid.getColor());
    }

    public MekanismRenderHelper color(@Nullable GasStack gasStack) {
        return gasStack == null ? this : color(gasStack.getGas());
    }

    public MekanismRenderHelper color(@Nullable Gas gas) {
        return gas == null ? this : color3f(gas.getTint());
    }

    public MekanismRenderHelper color(@Nonnull BaseTier tier) {
        return color(tier.getColor());
    }

    public MekanismRenderHelper color(@Nullable EnumColor color) {
        return color(color, 1.0F);
    }

    public MekanismRenderHelper color(@Nullable EnumColor color, float alpha) {
        return color(color, alpha, 1.0F);
    }

    public MekanismRenderHelper color(@Nullable EnumColor color, float alpha, float multiplier) {
        return color == null ? this : color(color.getColor(0) * multiplier, color.getColor(1) * multiplier, color.getColor(2) * multiplier, alpha);
    }

    //Instead of RenderHelper

    public MekanismRenderHelper enableGUIStandardItemLighting() {
        return enable(KnownStates.GUI_STANDARD_ITEM_LIGHTING);
    }

    public MekanismRenderHelper enableStandardItemLighting() {
        return enable(KnownStates.STANDARD_ITEM_LIGHTING);
    }

    public MekanismRenderHelper disableStandardItemLighting() {
        return disable(KnownStates.STANDARD_ITEM_LIGHTING);
    }

    //Instead of GlStateManager

    public MekanismRenderHelper enableAlpha() {
        return enable(KnownStates.ALPHA);
    }

    public MekanismRenderHelper disableAlpha() {
        return disable(KnownStates.ALPHA);
    }

    public MekanismRenderHelper enableBlend() {
        return enable(KnownStates.BLEND);
    }

    public MekanismRenderHelper disableBlend() {
        return disable(KnownStates.BLEND);
    }

    public MekanismRenderHelper enableCull() {
        return enable(KnownStates.CULL);
    }

    public MekanismRenderHelper disableCull() {
        return disable(KnownStates.CULL);
    }

    public MekanismRenderHelper enableDepth() {
        return enable(KnownStates.DEPTH);
    }

    public MekanismRenderHelper disableDepth() {
        return disable(KnownStates.DEPTH);
    }

    public MekanismRenderHelper enableDepthMask() {
        return enable(KnownStates.DEPTH_MASK);
    }

    public MekanismRenderHelper disableDepthMask() {
        return disable(KnownStates.DEPTH_MASK);
    }

    public MekanismRenderHelper enableLighting() {
        return enable(KnownStates.LIGHTING);
    }

    public MekanismRenderHelper disableLighting() {
        return disable(KnownStates.LIGHTING);
    }

    public MekanismRenderHelper enablePolygonOffset() {
        return enable(KnownStates.POLYGON_OFFSET);
    }

    public MekanismRenderHelper disablePolygonOffset() {
        return disable(KnownStates.POLYGON_OFFSET);
    }

    public MekanismRenderHelper enableRescaleNormal() {
        return enable(KnownStates.RESCALE_NORMAL);
    }

    public MekanismRenderHelper disableRescaleNormal() {
        return disable(KnownStates.RESCALE_NORMAL);
    }

    public MekanismRenderHelper enableTexture2D() {
        return enable(KnownStates.TEXTURE_2D);
    }

    public MekanismRenderHelper disableTexture2D() {
        return disable(KnownStates.TEXTURE_2D);
    }

    //Toggleable states
    private enum KnownStates {
        //Render Helper
        STANDARD_ITEM_LIGHTING(RenderHelper::enableStandardItemLighting, RenderHelper::disableStandardItemLighting),
        GUI_STANDARD_ITEM_LIGHTING(RenderHelper::enableGUIStandardItemLighting, STANDARD_ITEM_LIGHTING.disableMethod),
        //GlStateManager
        ALPHA(GlStateManager::enableAlpha, GlStateManager::disableAlpha),
        BLEND(GlStateManager::enableBlend, GlStateManager::disableBlend),
        CULL(GlStateManager::enableCull, GlStateManager::disableCull),
        DEPTH(GlStateManager::enableDepth, GlStateManager::disableDepth),
        DEPTH_MASK(() -> GlStateManager.depthMask(true), () -> GlStateManager.depthMask(false)),
        LIGHTING(GlStateManager::enableLighting, GlStateManager::disableLighting),
        POLYGON_OFFSET(GlStateManager::enablePolygonOffset, GlStateManager::disablePolygonOffset),
        RESCALE_NORMAL(GlStateManager::enableRescaleNormal, GlStateManager::disableRescaleNormal),
        TEXTURE_2D(GlStateManager::enableTexture2D, GlStateManager::disableTexture2D);

        private final Runnable enableMethod;
        private final Runnable disableMethod;

        KnownStates(Runnable enable, Runnable disable) {
            this.enableMethod = enable;
            this.disableMethod = disable;
        }

        public void enable() {
            enableMethod.run();
        }

        public void disable() {
            disableMethod.run();
        }

        public void cleanup(boolean previous) {
            if (previous) {
                disable();
            } else {
                enable();
            }
        }
    }
}