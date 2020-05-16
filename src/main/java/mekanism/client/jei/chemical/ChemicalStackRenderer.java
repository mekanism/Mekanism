package mekanism.client.jei.chemical;

import com.mojang.blaze3d.systems.RenderSystem;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.MathUtils;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

public class ChemicalStackRenderer<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> implements IIngredientRenderer<STACK> {

    private static final NumberFormat nf = NumberFormat.getIntegerInstance();
    protected static final int TEX_WIDTH = 16;
    protected static final int TEX_HEIGHT = 16;
    private static final int MIN_CHEMICAL_HEIGHT = 1; // ensure tiny amounts of chemical are still visible

    private final long capacityMb;
    private final TooltipMode tooltipMode;
    private final int width;
    private final int height;
    @Nullable
    private final IDrawable overlay;

    protected ChemicalStackRenderer(long capacityMb, TooltipMode tooltipMode, int width, int height, @Nullable IDrawable overlay) {
        this.capacityMb = capacityMb;
        this.tooltipMode = tooltipMode;
        this.width = width;
        this.height = height;
        this.overlay = overlay;
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
        int desiredHeight = MathUtils.clampToInt(height * (double) stack.getAmount() / capacityMb);
        if (desiredHeight < MIN_CHEMICAL_HEIGHT) {
            desiredHeight = MIN_CHEMICAL_HEIGHT;
        }
        if (desiredHeight > height) {
            desiredHeight = height;
        }
        CHEMICAL chemical = stack.getType();
        drawTiledSprite(xPosition, yPosition, width, desiredHeight, height, chemical);
    }

    private void drawTiledSprite(int xPosition, int yPosition, int desiredWidth, int desiredHeight, int yOffset, @Nonnull CHEMICAL chemical) {
        if (desiredWidth == 0 || desiredHeight == 0) {
            return;
        }
        MekanismRenderer.color(chemical);
        TextureAtlasSprite sprite = MekanismRenderer.getSprite(chemical.getIcon());
        MekanismRenderer.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        int xTileCount = desiredWidth / TEX_WIDTH;
        int xRemainder = desiredWidth - (xTileCount * TEX_WIDTH);
        int yTileCount = desiredHeight / TEX_HEIGHT;
        int yRemainder = desiredHeight - (yTileCount * TEX_HEIGHT);
        int yStart = yPosition + yOffset;
        int zLevel = 100;
        float uMin = sprite.getMinU();
        float uMax = sprite.getMaxU();
        float vMin = sprite.getMinV();
        float vMax = sprite.getMaxV();
        float uDif = uMax - uMin;
        float vDif = vMax - vMin;
        BufferBuilder vertexBuffer = Tessellator.getInstance().getBuffer();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            int width = (xTile == xTileCount) ? xRemainder : TEX_WIDTH;
            if (width == 0) {
                break;
            }
            int x = xPosition + (xTile * TEX_WIDTH);
            int maskRight = TEX_WIDTH - width;
            int shiftedX = x + TEX_WIDTH - maskRight;
            float uMaxLocal = uMax - (uDif * maskRight / TEX_WIDTH);
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int height = (yTile == yTileCount) ? yRemainder : TEX_HEIGHT;
                if (height == 0) {
                    //Note: We don't want to fully break out because our height will be zero if we are looking to
                    // draw the remainder, but there is no remainder as it divided evenly
                    break;
                }
                int y = yStart - ((yTile + 1) * TEX_HEIGHT);
                int maskTop = TEX_HEIGHT - height;
                float vMaxLocal = vMax - (vDif * maskTop / TEX_HEIGHT);
                vertexBuffer.pos(x, y + TEX_HEIGHT, zLevel).tex(uMin, vMaxLocal).endVertex();
                vertexBuffer.pos(shiftedX, y + TEX_HEIGHT, zLevel).tex(uMaxLocal, vMaxLocal).endVertex();
                vertexBuffer.pos(shiftedX, y + maskTop, zLevel).tex(uMaxLocal, vMin).endVertex();
                vertexBuffer.pos(x, y + maskTop, zLevel).tex(uMin, vMin).endVertex();
            }
        }
        vertexBuffer.finishDrawing();
        WorldVertexBufferUploader.draw(vertexBuffer);
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
            component = APILang.GENERIC.translateColored(EnumColor.GRAY, nf.format(stack.getAmount()));
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