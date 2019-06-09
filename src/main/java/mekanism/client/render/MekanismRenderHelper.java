package mekanism.client.render;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
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
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class MekanismRenderHelper extends GLSMHelper<MekanismRenderHelper> {

    private Map<KnownStates, Boolean> changedStates = new EnumMap<>(KnownStates.class);
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
            resetColor();
        }
        for (Entry<KnownStates, Boolean> entry : changedStates.entrySet()) {
            entry.getKey().cleanup(entry.getValue());
        }
        if (hasMatrix) {
            GlStateManager.popMatrix();
            hasMatrix = false;
        }
    }

    private MekanismRenderHelper enable(KnownStates state) {
        Boolean previous = changedStates.get(state);
        if (previous != null && previous) {
            return this;
        }
        changedStates.put(state, true);
        state.enable();
        return this;
    }

    private MekanismRenderHelper disable(KnownStates state) {
        Boolean previous = changedStates.get(state);
        if (previous != null && !previous) {
            return this;
        }
        changedStates.put(state, false);
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
        //Glow is needed when underground or in the dark. Initial thoughts were wrong
        if (!glowEnabled && !FMLClientHandler.instance().hasOptifine()) {
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

    //Color
    public MekanismRenderHelper colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        //If they are all being set to true, then we don't need the colorMasked boolean to be true
        colorMasked = !red || !green || !blue || !alpha;
        GlStateManager.colorMask(red, green, blue, alpha);
        return this;
    }

    public MekanismRenderHelper colorMaskAlpha() {
        return colorMask(true, true, true, false);
    }

    @Override
    public MekanismRenderHelper color(float red, float green, float blue, float alpha) {
        //If the color is our default color then we do not need to have colorSet be called
        colorSet = red != 1 || green != 1 || blue != 1 || alpha != 1;
        return super.color(red, green, blue, alpha);
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