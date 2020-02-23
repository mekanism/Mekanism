package mekanism.client.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.providers.IBaseProvider;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.gauge.GaugeOverlay;
import mekanism.client.jei.chemical.ChemicalStackRenderer;
import mekanism.client.jei.chemical.GasStackRenderer;
import mekanism.client.jei.chemical.InfusionStackRenderer;
import mekanism.common.Mekanism;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public abstract class BaseRecipeCategory<RECIPE> implements IRecipeCategory<RECIPE>, IGuiWrapper {

    private IGuiHelper guiHelper;
    protected ITickTimer timer;
    protected int xOffset;
    protected int yOffset;
    protected IDrawable fluidOverlayLarge;
    protected IDrawable fluidOverlaySmall;
    protected Set<GuiTexturedElement> guiElements = new ObjectOpenHashSet<>();
    private IBaseProvider provider;

    private final IDrawable background;

    protected BaseRecipeCategory(IGuiHelper helper, IBaseProvider provider, int xOffset, int yOffset, int width, int height) {
        this.guiHelper = helper;
        this.provider = provider;
        //TODO: Only make a timer for ones we need it
        this.timer = helper.createTickTimer(20, 20, false);
        this.fluidOverlayLarge = createDrawable(guiHelper, GaugeOverlay.STANDARD);
        this.fluidOverlaySmall = createDrawable(guiHelper, GaugeOverlay.SMALL);
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
        return provider.getRegistryName();
    }

    @Override
    public String getTitle() {
        return provider.getTextComponent().getFormattedText();
    }

    @Override
    public void draw(RECIPE recipe, double mouseX, double mouseY) {
        guiElements.forEach(e -> e.render((int) mouseX, (int) mouseY, 0));
    }

    @Override
    public void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis, float scale) {
        if (!stack.isEmpty()) {
            try {
                RenderSystem.pushMatrix();
                RenderSystem.enableDepthTest();
                RenderHelper.enableStandardItemLighting();
                if (scale != 1) {
                    RenderSystem.scalef(scale, scale, scale);
                }
                Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(stack, xAxis, yAxis);
                RenderHelper.disableStandardItemLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.popMatrix();
            } catch (Exception e) {
                Mekanism.logger.error("Failed to render stack into gui: " + stack, e);
            }
        }
    }

    @Override
    public void displayTooltip(ITextComponent component, int x, int y) {
        this.displayTooltips(Collections.singletonList(component), x, y);
    }

    @Override
    public void displayTooltips(List<ITextComponent> components, int xAxis, int yAxis) {
        List<String> toolTips = components.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList());
        GuiUtils.drawHoveringText(toolTips, xAxis, yAxis, getWidth(), getHeight(), -1, getFont());
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
        //Note: This is allowed to be null even though annotations imply it isn't supposed to be
        return null;
    }

    protected void initInfusion(IGuiIngredientGroup<@NonNull InfusionStack> group, int slot, boolean input, int x, int y, int width, int height, @Nonnull List<InfusionStack> stacks) {
        initChemical(group, slot, input, x, y, width, height, stacks, max -> new InfusionStackRenderer(max, width, height));
    }

    protected void initGas(IGuiIngredientGroup<@NonNull GasStack> group, int slot, boolean input, int x, int y, int width, int height, @Nonnull List<GasStack> stacks, boolean overlay) {
        initChemical(group, slot, input, x, y, width, height, stacks, max -> new GasStackRenderer(max, false, width, height,
              overlay ? height > 50 ? fluidOverlayLarge : fluidOverlaySmall : null));
    }

    protected <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void initChemical(IGuiIngredientGroup<@NonNull STACK> group, int slot,
          boolean input, int x, int y, int width, int height, @Nonnull List<STACK> stacks, Int2ObjectFunction<ChemicalStackRenderer<CHEMICAL, STACK>> rendererSupplier) {
        if (stacks.isEmpty()) {
            return;
        }
        int max = stacks.stream().mapToInt(STACK::getAmount).filter(stack -> stack >= 0).max().orElse(0);
        group.init(slot, input, rendererSupplier.apply(max), x, y, width, height, 0, 0);
        group.set(slot, stacks);
    }
}