package mekanism.client.jei.gas;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidAttributes;
import org.lwjgl.opengl.GL11;

public class GasStackRenderer implements IIngredientRenderer<GasStack> {

    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;
    private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of gas are still visible

    private final int capacityMb;
    private final TooltipMode tooltipMode;
    private final int width;
    private final int height;
    @Nullable
    private final IDrawable overlay;

    public GasStackRenderer() {
        this(FluidAttributes.BUCKET_VOLUME, TooltipMode.ITEM_LIST, TEX_WIDTH, TEX_HEIGHT, null);
    }

    public GasStackRenderer(int capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay) {
        this(capacityMb, showCapacity ? TooltipMode.SHOW_AMOUNT_AND_CAPACITY : TooltipMode.SHOW_AMOUNT, width, height, overlay);
    }

    public GasStackRenderer(int capacityMb, TooltipMode tooltipMode, int width, int height, @Nullable IDrawable overlay) {
        this.capacityMb = capacityMb;
        this.tooltipMode = tooltipMode;
        this.width = width;
        this.height = height;
        this.overlay = overlay;
    }

    private static TextureAtlasSprite getStillGasSprite(@Nonnull Gas gas) {
        AtlasTexture textureMapBlocks = Minecraft.getInstance().getTextureMap();
        ResourceLocation gasStill = gas.getIcon();
        TextureAtlasSprite gasStillSprite = null;
        if (gasStill != null) {
            gasStillSprite = textureMapBlocks.getSprite(gasStill);
        }
        if (gasStillSprite == null) {
            gasStillSprite = MekanismRenderer.missingIcon;
        }
        return gasStillSprite;
    }

    private static void drawTextureWithMasking(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
        double uMin = textureSprite.getMinU();
        double uMax = textureSprite.getMaxU();
        double vMin = textureSprite.getMinV();
        double vMax = textureSprite.getMaxV();
        uMax = uMax - (maskRight / 16.0 * (uMax - uMin));
        vMax = vMax - (maskTop / 16.0 * (vMax - vMin));

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuffer();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vertexBuffer.pos(xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
        vertexBuffer.pos(xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
        vertexBuffer.pos(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
        vertexBuffer.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    @Override
    public void render(int xPosition, int yPosition, @Nullable GasStack gasStack) {
        if (gasStack == null) {
            return;
        }
        GlStateManager.enableBlend();
        GlStateManager.enableAlphaTest();
        drawGas(xPosition, yPosition, gasStack);
        if (overlay != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0, 0, 200);
            overlay.draw(xPosition, yPosition);
            GlStateManager.popMatrix();
        }
        GlStateManager.disableAlphaTest();
        GlStateManager.disableBlend();
    }

    private void drawGas(int xPosition, int yPosition, @Nonnull GasStack gasStack) {
        if (gasStack.isEmpty()) {
            return;
        }
        int scaledAmount = (gasStack.getAmount() * height) / capacityMb;
        if (scaledAmount < MIN_FLUID_HEIGHT) {
            scaledAmount = MIN_FLUID_HEIGHT;
        }
        if (scaledAmount > height) {
            scaledAmount = height;
        }
        Gas gas = gasStack.getGas();
        drawTiledSprite(xPosition, yPosition, width, height, gas, scaledAmount, getStillGasSprite(gas));
    }

    private void drawTiledSprite(int xPosition, int yPosition, int tiledWidth, int tiledHeight, Gas gas, int scaledAmount, TextureAtlasSprite sprite) {
        MekanismRenderer.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        MekanismRenderer.color(gas);

        final int xTileCount = tiledWidth / TEX_WIDTH;
        final int xRemainder = tiledWidth - (xTileCount * TEX_WIDTH);
        final int yTileCount = scaledAmount / TEX_HEIGHT;
        final int yRemainder = scaledAmount - (yTileCount * TEX_HEIGHT);
        final int yStart = yPosition + tiledHeight;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            int width = (xTile == xTileCount) ? xRemainder : TEX_WIDTH;
            if (width > 0) {
                int x = xPosition + (xTile * TEX_WIDTH);
                int maskRight = TEX_WIDTH - width;
                for (int yTile = 0; yTile <= yTileCount; yTile++) {
                    int height = (yTile == yTileCount) ? yRemainder : TEX_HEIGHT;
                    if (height > 0) {
                        int y = yStart - ((yTile + 1) * TEX_HEIGHT);
                        int maskTop = TEX_HEIGHT - height;
                        drawTextureWithMasking(x, y, sprite, maskTop, maskRight, 100);
                    }
                }
            }
        }
        MekanismRenderer.resetColor();
    }

    @Override
    public List<String> getTooltip(@Nonnull GasStack gasStack, ITooltipFlag tooltipFlag) {
        List<String> tooltip = new ArrayList<>();
        Gas gasType = gasStack.getGas();
        if (gasType == MekanismAPI.EMPTY_GAS) {
            return tooltip;
        }
        tooltip.add(TextComponentUtil.build(gasType).getFormattedText());
        ITextComponent component = null;
        if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
            component = TextComponentUtil.build(EnumColor.GRAY, Translation.of("jei.tooltip.liquid.amount.with.capacity", gasStack.getAmount(), capacityMb));
        } else if (tooltipMode == TooltipMode.SHOW_AMOUNT) {
            component = TextComponentUtil.build(EnumColor.GRAY, Translation.of("jei.tooltip.liquid.amount", gasStack.getAmount()));
        }
        if (component != null) {
            tooltip.add(component.getFormattedText());
        }
        return tooltip;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, @Nonnull GasStack gasStack) {
        return minecraft.fontRenderer;
    }

    enum TooltipMode {
        SHOW_AMOUNT,
        SHOW_AMOUNT_AND_CAPACITY,
        ITEM_LIST
    }
}