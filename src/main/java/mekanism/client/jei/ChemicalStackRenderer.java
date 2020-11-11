package mekanism.client.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.ChemicalUtil;
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
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidAttributes;
import org.lwjgl.opengl.GL11;

public class ChemicalStackRenderer<STACK extends ChemicalStack<?>> implements IIngredientRenderer<STACK> {

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

    public ChemicalStackRenderer() {
        this(FluidAttributes.BUCKET_VOLUME, TooltipMode.ITEM_LIST, TEX_WIDTH, TEX_HEIGHT, null);
    }

    public ChemicalStackRenderer(long capacityMb, int width, int height) {
        this(capacityMb, TooltipMode.SHOW_AMOUNT, width, height, null);
    }

    public ChemicalStackRenderer(long capacityMb, boolean showCapacity, int width, int height, @Nullable IDrawable overlay) {
        this(capacityMb, showCapacity ? TooltipMode.SHOW_AMOUNT_AND_CAPACITY : TooltipMode.SHOW_AMOUNT, width, height, overlay);
    }

    private ChemicalStackRenderer(long capacityMb, TooltipMode tooltipMode, int width, int height, @Nullable IDrawable overlay) {
        this.capacityMb = capacityMb;
        this.tooltipMode = tooltipMode;
        this.width = width;
        this.height = height;
        this.overlay = overlay;
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, int xPosition, int yPosition, @Nullable STACK stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        drawChemical(matrix, xPosition, yPosition, stack);
        if (overlay != null) {
            matrix.push();
            matrix.translate(0, 0, 200);
            overlay.draw(matrix, xPosition, yPosition);
            matrix.pop();
        }
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
    }

    private void drawChemical(MatrixStack matrix, int xPosition, int yPosition, @Nonnull STACK stack) {
        int desiredHeight = MathUtils.clampToInt(height * (double) stack.getAmount() / capacityMb);
        if (desiredHeight < MIN_CHEMICAL_HEIGHT) {
            desiredHeight = MIN_CHEMICAL_HEIGHT;
        }
        if (desiredHeight > height) {
            desiredHeight = height;
        }
        Chemical<?> chemical = stack.getType();
        drawTiledSprite(matrix, xPosition, yPosition, width, desiredHeight, height, chemical);
    }

    private void drawTiledSprite(MatrixStack matrix, int xPosition, int yPosition, int desiredWidth, int desiredHeight, int yOffset, @Nonnull Chemical<?> chemical) {
        if (desiredWidth == 0 || desiredHeight == 0) {
            return;
        }
        Matrix4f matrix4f = matrix.getLast().getMatrix();
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
                vertexBuffer.pos(matrix4f, x, y + TEX_HEIGHT, zLevel).tex(uMin, vMaxLocal).endVertex();
                vertexBuffer.pos(matrix4f, shiftedX, y + TEX_HEIGHT, zLevel).tex(uMaxLocal, vMaxLocal).endVertex();
                vertexBuffer.pos(matrix4f, shiftedX, y + maskTop, zLevel).tex(uMaxLocal, vMin).endVertex();
                vertexBuffer.pos(matrix4f, x, y + maskTop, zLevel).tex(uMin, vMin).endVertex();
            }
        }
        vertexBuffer.finishDrawing();
        WorldVertexBufferUploader.draw(vertexBuffer);
        MekanismRenderer.resetColor();
    }

    @Override
    public List<ITextComponent> getTooltip(@Nonnull STACK stack, ITooltipFlag tooltipFlag) {
        Chemical<?> chemical = stack.getType();
        if (chemical.isEmptyType()) {
            return Collections.emptyList();
        }
        List<ITextComponent> tooltip = new ArrayList<>();
        tooltip.add(TextComponentUtil.build(chemical));
        if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
            tooltip.add(MekanismLang.JEI_AMOUNT_WITH_CAPACITY.translateColored(EnumColor.GRAY, nf.format(stack.getAmount()), nf.format(capacityMb)));
        } else if (tooltipMode == TooltipMode.SHOW_AMOUNT) {
            tooltip.add(MekanismLang.GENERIC_MB.translateColored(EnumColor.GRAY, nf.format(stack.getAmount())));
        }
        tooltip.addAll(ChemicalUtil.getAttributeTooltips(stack.getType()));
        return tooltip;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, @Nonnull STACK stack) {
        return minecraft.fontRenderer;
    }

    enum TooltipMode {
        SHOW_AMOUNT,
        SHOW_AMOUNT_AND_CAPACITY,
        ITEM_LIST
    }
}