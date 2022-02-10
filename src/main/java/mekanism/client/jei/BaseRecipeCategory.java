package mekanism.client.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IItemProvider;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.gauge.GaugeOverlay;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.IProgressInfoHandler;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

public abstract class BaseRecipeCategory<RECIPE> implements IRecipeCategory<RECIPE>, IGuiWrapper {

    private static final IProgressInfoHandler CONSTANT_PROGRESS = () -> 1;
    protected static final IBarInfoHandler FULL_BAR = () -> 1;

    protected static IDrawable createIcon(IGuiHelper helper, ResourceLocation iconRL) {
        return helper.drawableBuilder(iconRL, 0, 0, 18, 18).setTextureSize(18, 18).build();
    }

    protected static IDrawable createIcon(IGuiHelper helper, IItemProvider provider) {
        return helper.createDrawableIngredient(provider.getItemStack());
    }

    private final List<GuiTexturedElement> guiElements = new ArrayList<>();
    private final ITextComponent component;
    private final IGuiHelper guiHelper;
    private final IDrawable background;
    private final ResourceLocation id;
    private final IDrawable icon;
    private final int xOffset;
    private final int yOffset;
    @Nullable
    private Map<GaugeOverlay, IDrawable> overlayLookup;
    @Nullable
    private ITickTimer timer;

    protected BaseRecipeCategory(IGuiHelper helper, IItemProvider provider, int xOffset, int yOffset, int width, int height) {
        this(helper, provider.getRegistryName(), provider.getTextComponent(), createIcon(helper, provider), xOffset, yOffset, width, height);
    }

    protected BaseRecipeCategory(IGuiHelper helper, ResourceLocation id, ITextComponent component, IDrawable icon, int xOffset, int yOffset, int width, int height) {
        this.id = id;
        this.component = component;
        this.guiHelper = helper;
        this.icon = icon;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.background = new NOOPDrawable(width, height);
    }

    protected <ELEMENT extends GuiTexturedElement> ELEMENT addElement(ELEMENT element) {
        guiElements.add(element);
        return element;
    }

    /**
     * @apiNote x and y are based on the values set in the tile, as the GUI then shifts the slots by one to account for the border. This method is mostly meant as a
     * helper to make keeping track of the positioning numbers easier.
     */
    protected GuiSlot addSlot(SlotType type, int x, int y) {
        return addElement(new GuiSlot(type, this, x - 1, y - 1));
    }

    protected GuiProgress addSimpleProgress(ProgressType type, int x, int y) {
        return addElement(new GuiProgress(getSimpleProgressTimer(), type, this, x, y));
    }

    protected GuiProgress addConstantProgress(ProgressType type, int x, int y) {
        return addElement(new GuiProgress(CONSTANT_PROGRESS, type, this, x, y));
    }

    @Override
    public int getLeft() {
        return -xOffset;
    }

    @Override
    public int getTop() {
        return -yOffset;
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public ResourceLocation getUid() {
        return id;
    }

    @Override
    @Deprecated
    public String getTitle() {
        return getTitleAsTextComponent().getString();
    }

    @Override
    public ITextComponent getTitleAsTextComponent() {
        return component;
    }

    @Override
    public void draw(RECIPE recipe, MatrixStack matrix, double mouseX, double mouseY) {
        int x = (int) mouseX;
        int y = (int) mouseY;
        guiElements.forEach(e -> e.render(matrix, x, y, 0));
        guiElements.forEach(e -> e.onDrawBackground(matrix, x, y, 0));
        int zOffset = 200;
        //Translate back by our offset so that we are effectively rendering the foreground starting at 0, 0
        // This is needed to make sure that we render things like crystallizer text in the correct spot
        // If this ends up causing issues elsewhere we will need to look into it further
        matrix.pushPose();
        matrix.translate(-xOffset, -yOffset, 0);
        for (GuiTexturedElement element : guiElements) {
            matrix.pushPose();
            element.onRenderForeground(matrix, x, y, zOffset, zOffset);
            matrix.popPose();
        }
        matrix.popPose();
    }

    @Override
    public ItemRenderer getItemRenderer() {
        return Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public FontRenderer getFont() {
        return Minecraft.getInstance().font;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    protected IProgressInfoHandler getSimpleProgressTimer() {
        if (timer == null) {
            timer = guiHelper.createTickTimer(20, 20, false);
        }
        return () -> timer.getValue() / 20D;
    }

    protected IBarInfoHandler getBarProgressTimer() {
        if (timer == null) {
            timer = guiHelper.createTickTimer(20, 20, false);
        }
        return new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.PROGRESS.translate(TextUtils.getPercent(getLevel()));
            }

            @Override
            public double getLevel() {
                return timer.getValue() / 20D;
            }
        };
    }

    private IDrawable getOverlay(GuiGauge<?> gauge) {
        if (overlayLookup == null) {
            overlayLookup = new EnumMap<>(GaugeOverlay.class);
        }
        return overlayLookup.computeIfAbsent(gauge.getGaugeOverlay(), overlay -> createDrawable(guiHelper, overlay));
    }

    private IDrawable createDrawable(IGuiHelper helper, GaugeOverlay gaugeOverlay) {
        return helper.drawableBuilder(gaugeOverlay.getBarOverlay(), 0, 0, gaugeOverlay.getWidth(), gaugeOverlay.getHeight())
              .setTextureSize(gaugeOverlay.getWidth(), gaugeOverlay.getHeight())
              .build();
    }

    protected void initItem(IGuiIngredientGroup<ItemStack> group, int slotIndex, boolean input, GuiSlot slot, List<ItemStack> stacks) {
        initItem(group, slotIndex, input, slot.getRelativeX(), slot.getRelativeY(), stacks);
    }

    protected void initItem(IGuiIngredientGroup<ItemStack> group, int slotIndex, boolean input, int relativeX, int relativeY, List<ItemStack> stacks) {
        group.init(slotIndex, input, relativeX - xOffset, relativeY - yOffset);
        group.set(slotIndex, stacks);
    }

    protected void initFluid(IGuiFluidStackGroup group, int tankIndex, boolean input, GuiGauge<?> gauge, List<FluidStack> stacks) {
        //Gauges have a 1 pixel border
        int x = gauge.getRelativeX() + 1 - xOffset;
        int y = gauge.getRelativeY() + 1 - yOffset;
        int width = gauge.getWidth() - 2;
        int height = gauge.getHeight() - 2;
        int max = stacks.stream().mapToInt(FluidStack::getAmount).filter(stackSize -> stackSize >= 0).max().orElse(0);
        group.init(tankIndex, input, x, y, width, height, max, false, getOverlay(gauge));
        group.set(tankIndex, stacks);
    }

    protected <STACK extends ChemicalStack<?>> void initChemical(IGuiIngredientGroup<STACK> group, int tankIndex, boolean input, GuiBar<?> bar, List<STACK> stacks) {
        initChemical(group, tankIndex, input, bar, stacks, ChemicalStackRenderer::new);
    }

    protected <STACK extends ChemicalStack<?>> void initChemical(IGuiIngredientGroup<STACK> group, int tankIndex, boolean input, GuiGauge<?> gauge, List<STACK> stacks) {
        initChemical(group, tankIndex, input, gauge, stacks, (max, width, height) -> new ChemicalStackRenderer<>(max, width, height, getOverlay(gauge)));
    }

    private <STACK extends ChemicalStack<?>> void initChemical(IGuiIngredientGroup<@NonNull STACK> group, int tankIndex, boolean input, GuiElement element,
          List<STACK> stacks, ChemicalStackRendererCreator<STACK> rendererCreator) {
        int x = element.getRelativeX() + 1 - xOffset;
        int y = element.getRelativeY() + 1 - yOffset;
        int width = element.getWidth() - 2;
        int height = element.getHeight() - 2;
        long max = stacks.stream().mapToLong(ChemicalStack::getAmount).filter(stackSize -> stackSize >= 0).max().orElse(0);
        group.init(tankIndex, input, rendererCreator.create(max, width, height), x, y, width, height, 0, 0);
        group.set(tankIndex, stacks);
    }

    @FunctionalInterface
    private interface ChemicalStackRendererCreator<STACK extends ChemicalStack<?>> {

        ChemicalStackRenderer<STACK> create(long max, int width, int height);
    }
}