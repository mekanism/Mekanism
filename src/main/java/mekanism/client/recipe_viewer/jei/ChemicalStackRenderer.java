package mekanism.client.recipe_viewer.jei;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public class ChemicalStackRenderer<STACK extends ChemicalStack<?>> implements IIngredientRenderer<STACK> {

    private static final int TEXTURE_SIZE = 16;
    private static final int MIN_CHEMICAL_HEIGHT = 1; // ensure tiny amounts of chemical are still visible

    private final long capacityMb;
    private final TooltipMode tooltipMode;
    private final int width;
    private final int height;

    public ChemicalStackRenderer() {
        this(FluidType.BUCKET_VOLUME, TooltipMode.ITEM_LIST, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    public ChemicalStackRenderer(long capacityMb, int width, int height) {
        this(capacityMb, TooltipMode.SHOW_AMOUNT, width, height);
    }

    private ChemicalStackRenderer(long capacityMb, TooltipMode tooltipMode, int width, int height) {
        Preconditions.checkArgument(capacityMb > 0, "capacity must be > 0");
        this.capacityMb = capacityMb;
        this.tooltipMode = tooltipMode;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull STACK stack) {
        if (!stack.isEmpty()) {
            int desiredHeight = MathUtils.clampToInt(height * (double) stack.getAmount() / capacityMb);
            if (desiredHeight < MIN_CHEMICAL_HEIGHT) {
                desiredHeight = MIN_CHEMICAL_HEIGHT;
            }
            if (desiredHeight > height) {
                desiredHeight = height;
            }
            Chemical<?> chemical = stack.getChemical();
            MekanismRenderer.color(guiGraphics, chemical);
            //Tile upwards and to the right as the majority of things we render are gauges which look better when tiling upwards
            GuiUtils.drawTiledSprite(guiGraphics, 0, 0, height, width, desiredHeight, MekanismRenderer.getSprite(chemical.getIcon()),
                  TEXTURE_SIZE, TEXTURE_SIZE, 100, TilingDirection.UP_RIGHT);
            MekanismRenderer.resetColor(guiGraphics);
        }
    }

    @Override
    public List<Component> getTooltip(@NotNull STACK stack, TooltipFlag tooltipFlag) {
        Chemical<?> chemical = stack.getChemical();
        if (chemical.isEmptyType()) {
            return Collections.emptyList();
        }
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(TextComponentUtil.build(chemical));
        if (tooltipMode == TooltipMode.SHOW_AMOUNT_AND_CAPACITY) {
            tooltips.add(MekanismLang.JEI_AMOUNT_WITH_CAPACITY.translateColored(EnumColor.GRAY, TextUtils.format(stack.getAmount()), TextUtils.format(capacityMb)));
        } else if (tooltipMode == TooltipMode.SHOW_AMOUNT) {
            tooltips.add(MekanismLang.GENERIC_MB.translateColored(EnumColor.GRAY, TextUtils.format(stack.getAmount())));
        }
        ChemicalUtil.addChemicalDataToTooltip(tooltips, stack.getChemical(), tooltipFlag.isAdvanced());
        return tooltips;
    }

    @Override
    public Font getFontRenderer(Minecraft minecraft, @NotNull STACK stack) {
        return minecraft.font;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    enum TooltipMode {
        SHOW_AMOUNT,
        SHOW_AMOUNT_AND_CAPACITY,
        ITEM_LIST
    }
}