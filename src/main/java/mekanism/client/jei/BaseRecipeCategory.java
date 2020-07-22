package mekanism.client.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IBaseProvider;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.gauge.GaugeOverlay;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class BaseRecipeCategory<RECIPE> implements IRecipeCategory<RECIPE>, IGuiWrapper {

    protected final ITickTimer timer;
    protected final int xOffset;
    protected final int yOffset;
    protected final IDrawable fluidOverlayLarge;
    protected final IDrawable fluidOverlaySmall;
    protected final IDrawable fluidOverlaySmallMed;
    protected final Set<GuiTexturedElement> guiElements = new ObjectOpenHashSet<>();
    private final ResourceLocation id;
    private final ITextComponent component;
    private final IDrawable background;
    @Nullable
    protected IDrawable icon;

    protected BaseRecipeCategory(IGuiHelper helper, IBaseProvider provider, int xOffset, int yOffset, int width, int height) {
        this(helper, provider.getRegistryName(), provider.getTextComponent(), xOffset, yOffset, width, height);
    }

    protected BaseRecipeCategory(IGuiHelper helper, ResourceLocation id, ITextComponent component, int xOffset, int yOffset, int width, int height) {
        this.id = id;
        this.component = component;
        //TODO: Only make a timer for ones we need it
        this.timer = helper.createTickTimer(20, 20, false);
        this.fluidOverlayLarge = createDrawable(helper, GaugeOverlay.STANDARD);
        this.fluidOverlaySmall = createDrawable(helper, GaugeOverlay.SMALL);
        this.fluidOverlaySmallMed = createDrawable(helper, GaugeOverlay.SMALL_MED);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.background = new NOOPDrawable(width, height);
        addGuiElements();
    }

    private IDrawable createDrawable(IGuiHelper helper, GaugeOverlay gaugeOverlay) {
        return helper.drawableBuilder(gaugeOverlay.getBarOverlay(), 0, 0, gaugeOverlay.getWidth(), gaugeOverlay.getHeight())
              .setTextureSize(gaugeOverlay.getWidth(), gaugeOverlay.getHeight())
              .build();
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
    public String getTitle() {
        return component.getString();
    }

    @Override
    public void draw(RECIPE recipe, MatrixStack matrix, double mouseX, double mouseY) {
        guiElements.forEach(e -> e.render(matrix, (int) mouseX, (int) mouseY, 0));
        guiElements.forEach(e -> e.drawBackground(matrix, (int) mouseX, (int) mouseY, 0));
    }

    @Override
    public ItemRenderer getItemRenderer() {
        return Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public FontRenderer getFont() {
        return Minecraft.getInstance().fontRenderer;
    }

    protected void addGuiElements() {
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        //Note: Even though we usually return null form here, this is allowed even though annotations imply it isn't supposed to be
        return icon;
    }

    protected <STACK extends ChemicalStack<?>> void initChemical(IGuiIngredientGroup<STACK> group, int slot, boolean input, int x, int y, int width, int height,
          @Nonnull List<STACK> stacks) {
        initChemical(group, slot, input, x, y, width, height, stacks, max -> new ChemicalStackRenderer<>(max, width, height));
    }

    protected <STACK extends ChemicalStack<?>> void initChemical(IGuiIngredientGroup<STACK> group, int slot, boolean input, int x, int y, int width, int height,
          @Nonnull List<STACK> stacks, boolean overlay) {
        initChemical(group, slot, input, x, y, width, height, stacks, max -> new ChemicalStackRenderer<>(max, false, width, height,
              overlay ? height > 50 ? fluidOverlayLarge : (height == 46 ? fluidOverlaySmallMed : fluidOverlaySmall) : null));
    }

    private <STACK extends ChemicalStack<?>> void initChemical(IGuiIngredientGroup<@NonNull STACK> group, int slot, boolean input, int x, int y, int width, int height,
          @Nonnull List<STACK> stacks, Long2ObjectFunction<ChemicalStackRenderer<STACK>> rendererSupplier) {
        if (!stacks.isEmpty()) {
            long max = stacks.stream().mapToLong(ChemicalStack::getAmount).filter(stack -> stack >= 0).max().orElse(0);
            group.init(slot, input, rendererSupplier.apply(max), x, y, width, height, 0, 0);
            group.set(slot, stacks);
        }
    }
}