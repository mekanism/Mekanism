package mekanism.client.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IBaseProvider;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.gauge.GaugeOverlay;
import mekanism.client.jei.chemical.ChemicalStackRenderer;
import mekanism.common.Mekanism;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public abstract class BaseRecipeCategory<RECIPE> implements IRecipeCategory<RECIPE>, IGuiWrapper {

    private final IGuiHelper guiHelper;
    protected ITickTimer timer;
    protected int xOffset;
    protected int yOffset;
    protected IDrawable fluidOverlayLarge;
    protected IDrawable fluidOverlaySmall;
    protected IDrawable fluidOverlaySmallMed;
    protected Set<GuiTexturedElement> guiElements = new ObjectOpenHashSet<>();
    private final IBaseProvider provider;

    private final IDrawable background;

    protected BaseRecipeCategory(IGuiHelper helper, IBaseProvider provider, int xOffset, int yOffset, int width, int height) {
        this.guiHelper = helper;
        this.provider = provider;
        //TODO: Only make a timer for ones we need it
        this.timer = helper.createTickTimer(20, 20, false);
        this.fluidOverlayLarge = createDrawable(guiHelper, GaugeOverlay.STANDARD);
        this.fluidOverlaySmall = createDrawable(guiHelper, GaugeOverlay.SMALL);
        this.fluidOverlaySmallMed = createDrawable(guiHelper, GaugeOverlay.SMALL_MED);
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
        //Note: This is allowed to be null even though annotations imply it isn't supposed to be
        return null;
    }

    protected <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void initChemical(IGuiIngredientGroup<STACK> group, int slot, boolean input,
          int x, int y, int width, int height, @Nonnull List<STACK> stacks) {
        initChemical(group, slot, input, x, y, width, height, stacks, max -> new ChemicalStackRenderer<>(max, width, height));
    }

    protected <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void initChemical(IGuiIngredientGroup<STACK> group, int slot, boolean input,
          int x, int y, int width, int height, @Nonnull List<STACK> stacks, boolean overlay) {
        initChemical(group, slot, input, x, y, width, height, stacks, max -> new ChemicalStackRenderer<>(max, false, width, height,
              overlay ? height > 50 ? fluidOverlayLarge : (height == 46 ? fluidOverlaySmallMed : fluidOverlaySmall) : null));
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void initChemical(IGuiIngredientGroup<@NonNull STACK> group, int slot,
          boolean input, int x, int y, int width, int height, @Nonnull List<STACK> stacks, Long2ObjectFunction<ChemicalStackRenderer<CHEMICAL, STACK>> rendererSupplier) {
        if (!stacks.isEmpty()) {
            long max = stacks.stream().mapToLong(STACK::getAmount).filter(stack -> stack >= 0).max().orElse(0);
            group.init(slot, input, rendererSupplier.apply(max), x, y, width, height, 0, 0);
            group.set(slot, stacks);
        }
    }
}