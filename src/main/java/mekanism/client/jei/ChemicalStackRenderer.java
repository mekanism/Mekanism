package mekanism.client.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
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
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidAttributes;

public class ChemicalStackRenderer<STACK extends ChemicalStack<?>> implements IIngredientRenderer<STACK> {

    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;
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
        MekanismRenderer.color(chemical);
        //Tile upwards and to the right as the majority of things we render are gauges which look better when tiling upwards
        GuiUtils.drawTiledSprite(matrix, xPosition, yPosition, height, width, desiredHeight, MekanismRenderer.getSprite(chemical.getIcon()),
              TEX_WIDTH, TEX_HEIGHT, 100, TilingDirection.UP_RIGHT, false);
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
        ITextComponent component = null;
        if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
            component = MekanismLang.JEI_AMOUNT_WITH_CAPACITY.translateColored(EnumColor.GRAY, TextUtils.format(stack.getAmount()), TextUtils.format(capacityMb));
        } else if (tooltipMode == TooltipMode.SHOW_AMOUNT) {
            component = MekanismLang.GENERIC_MB.translateColored(EnumColor.GRAY, TextUtils.format(stack.getAmount()));
        }
        if (component != null) {
            tooltip.add(component);
        }
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