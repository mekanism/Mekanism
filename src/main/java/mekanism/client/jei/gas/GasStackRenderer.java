package mekanism.client.jei.gas;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.util.LangUtils;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;

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
        this(Fluid.BUCKET_VOLUME, TooltipMode.ITEM_LIST, TEX_WIDTH, TEX_HEIGHT, null);
    }

    public GasStackRenderer(int capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay) {
        this(capacityMb, showCapacity ? TooltipMode.SHOW_AMOUNT_AND_CAPACITY : TooltipMode.SHOW_AMOUNT, width, height,
              overlay);
    }

    public GasStackRenderer(int capacityMb, TooltipMode tooltipMode, int width, int height,
          @Nullable IDrawable overlay) {
        this.capacityMb = capacityMb;
        this.tooltipMode = tooltipMode;
        this.width = width;
        this.height = height;
        this.overlay = overlay;
    }

    private static TextureAtlasSprite getStillGasSprite(Minecraft minecraft, Gas gas) {
        TextureMap textureMapBlocks = minecraft.getTextureMapBlocks();
        ResourceLocation gasStill = gas.getIcon();
        TextureAtlasSprite gasStillSprite = null;
        if (gasStill != null) {
            gasStillSprite = textureMapBlocks.getTextureExtry(gasStill.toString());
        }
        if (gasStillSprite == null) {
            gasStillSprite = textureMapBlocks.getMissingSprite();
        }
        return gasStillSprite;
    }

    private static void setGLColorFromInt(int color) {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        GlStateManager.color(red, green, blue, 1.0F);
    }

    private static void drawTextureWithMasking(double xCoord, double yCoord, TextureAtlasSprite textureSprite,
          int maskTop, int maskRight, double zLevel) {
        double uMin = (double) textureSprite.getMinU();
        double uMax = (double) textureSprite.getMaxU();
        double vMin = (double) textureSprite.getMinV();
        double vMax = (double) textureSprite.getMaxV();
        uMax = uMax - (maskRight / 16.0 * (uMax - uMin));
        vMax = vMax - (maskTop / 16.0 * (vMax - vMin));

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexBuffer = tessellator.getBuffer();
        vertexBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexBuffer.pos(xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
        vertexBuffer.pos(xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
        vertexBuffer.pos(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
        vertexBuffer.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    @Override
    public void render(Minecraft minecraft, final int xPosition, final int yPosition, @Nullable GasStack gasStack) {
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        drawGas(minecraft, xPosition, yPosition, gasStack);
        GlStateManager.color(1, 1, 1, 1);
        if (overlay != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 200);
            overlay.draw(minecraft, xPosition, yPosition);
            GlStateManager.popMatrix();
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    private void drawGas(Minecraft minecraft, final int xPosition, final int yPosition, @Nullable GasStack gasStack) {
        Gas gas = gasStack == null ? null : gasStack.getGas();
        if (gas == null) {
            return;
        }
        int scaledAmount = (gasStack.amount * height) / capacityMb;
        if (gasStack.amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
            scaledAmount = MIN_FLUID_HEIGHT;
        }
        if (scaledAmount > height) {
            scaledAmount = height;
        }
        drawTiledSprite(minecraft, xPosition, yPosition, width, height, gas.getTint(), scaledAmount, getStillGasSprite(minecraft, gas));
    }

    private void drawTiledSprite(Minecraft minecraft, final int xPosition, final int yPosition, final int tiledWidth,
          final int tiledHeight, int color, int scaledAmount, TextureAtlasSprite sprite) {
        minecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        setGLColorFromInt(color);

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
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, GasStack gasStack, ITooltipFlag tooltipFlag) {
        List<String> tooltip = new ArrayList<>();
        Gas gasType = gasStack.getGas();
        if (gasType == null) {
            return tooltip;
        }
        String gasName = gasType.getLocalizedName();
        tooltip.add(gasName);
        if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
            String amount = LangUtils
                  .localizeWithFormat("jei.tooltip.liquid.amount.with.capacity", gasStack.amount, capacityMb);
            tooltip.add(TextFormatting.GRAY + amount);
        } else if (tooltipMode == TooltipMode.SHOW_AMOUNT) {
            String amount = LangUtils.localizeWithFormat("jei.tooltip.liquid.amount", gasStack.amount);
            tooltip.add(TextFormatting.GRAY + amount);
        }
        return tooltip;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, GasStack gasStack) {
        return minecraft.fontRenderer;
    }

    enum TooltipMode {
        SHOW_AMOUNT,
        SHOW_AMOUNT_AND_CAPACITY,
        ITEM_LIST
    }
}