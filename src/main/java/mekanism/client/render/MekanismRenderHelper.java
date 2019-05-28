package mekanism.client.render;

import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

public class MekanismRenderHelper {

    private Deque<Pair<KnownStates, Boolean>> changedStates = new ArrayDeque<>();
    private final boolean hasMatrix;
    private boolean colorSet;

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
    // That or add support for reusing it
    public void cleanup() {
        if (colorSet) {
            //Reset the color
            GlStateManager.color(1, 1, 1, 1);
        }
        while (!changedStates.isEmpty()) {
            Pair<KnownStates, Boolean> stateInfo = changedStates.pop();
            stateInfo.getKey().cleanup(stateInfo.getValue());
        }
        if (hasMatrix) {
            GlStateManager.popMatrix();
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

    //Color
    public MekanismRenderHelper color(float red, float green, float blue, float alpha) {
        colorSet = true;
        GlStateManager.color(red, green, blue, alpha);
        return this;
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
        return fluid == null ? this : color(fluid.getFluid().getColor(fluid));
    }

    public MekanismRenderHelper color(@Nullable GasStack gasStack) {
        return gasStack == null ? this : color(gasStack.getGas());
    }

    public MekanismRenderHelper color(@Nullable Gas gas) {
        return gas == null ? this : color3f(gas.getTint());
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