package mekanism.client.jei.chemical;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextComponentUtil;
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
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

//TODO: Fix the fact that the textures look stretched in JEI (see infuser)
public class ChemicalStackRenderer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IIngredientRenderer<STACK> {

    private static final NumberFormat nf = NumberFormat.getIntegerInstance();
    protected static final int TEX_WIDTH = 16;
    protected static final int TEX_HEIGHT = 16;
    private static final int MIN_FLUID_HEIGHT = 1; // ensure tiny amounts of gas are still visible

    private final int capacityMb;
    private final TooltipMode tooltipMode;
    private final int width;
    private final int height;
    @Nullable
    private final IDrawable overlay;

    protected ChemicalStackRenderer(int capacityMb, TooltipMode tooltipMode, int width, int height, @Nullable IDrawable overlay) {
        this.capacityMb = capacityMb;
        this.tooltipMode = tooltipMode;
        this.width = width;
        this.height = height;
        this.overlay = overlay;
    }

    private static void drawTextureWithMasking(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
        float uMin = textureSprite.getMinU();
        float uMax = textureSprite.getMaxU();
        float vMin = textureSprite.getMinV();
        float vMax = textureSprite.getMaxV();
        uMax = (float) (uMax - (maskRight / 16.0 * (uMax - uMin)));
        vMax = (float) (vMax - (maskTop / 16.0 * (vMax - vMin)));

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
    public void render(int xPosition, int yPosition, @Nullable STACK stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        drawChemical(xPosition, yPosition, stack);
        if (overlay != null) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0, 0, 200);
            overlay.draw(xPosition, yPosition);
            RenderSystem.popMatrix();
        }
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
    }

    private void drawChemical(int xPosition, int yPosition, @Nonnull STACK stack) {
        if (stack.isEmpty()) {
            return;
        }
        int scaledAmount = (stack.getAmount() * height) / capacityMb;
        if (scaledAmount < MIN_FLUID_HEIGHT) {
            scaledAmount = MIN_FLUID_HEIGHT;
        }
        if (scaledAmount > height) {
            scaledAmount = height;
        }
        CHEMICAL chemical = stack.getType();
        drawTiledSprite(xPosition, yPosition, width, height, chemical, scaledAmount, MekanismRenderer.getSprite(chemical.getIcon()));
    }

    private void drawTiledSprite(int xPosition, int yPosition, int tiledWidth, int tiledHeight, @Nonnull CHEMICAL chemical, int scaledAmount, TextureAtlasSprite sprite) {
        MekanismRenderer.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        MekanismRenderer.color(chemical);

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
    public List<String> getTooltip(@Nonnull STACK stack, ITooltipFlag tooltipFlag) {
        List<String> tooltip = new ArrayList<>();
        CHEMICAL chemical = stack.getType();
        if (chemical.isEmptyType()) {
            return tooltip;
        }
        tooltip.add(TextComponentUtil.build(chemical).getFormattedText());
        ITextComponent component = null;
        if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
            component = MekanismLang.JEI_AMOUNT_WITH_CAPACITY.translateColored(EnumColor.GRAY, nf.format(stack.getAmount()), nf.format(capacityMb));
        } else if (tooltipMode == TooltipMode.SHOW_AMOUNT) {
            component = MekanismLang.GENERIC_MB.translateColored(EnumColor.GRAY, nf.format(stack.getAmount()));
        } else if (tooltipMode == TooltipMode.SHOW_AMOUNT_NO_UNITS) {
            component = MekanismLang.GENERIC.translateColored(EnumColor.GRAY, nf.format(stack.getAmount()));
        }
        if (component != null) {
            tooltip.add(component.getFormattedText());
        }
        return tooltip;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, @Nonnull STACK stack) {
        return minecraft.fontRenderer;
    }

    enum TooltipMode {
        SHOW_AMOUNT,
        SHOW_AMOUNT_NO_UNITS,
        SHOW_AMOUNT_AND_CAPACITY,
        ITEM_LIST
    }
}