package mekanism.client.jei;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.gauge.GuiGauge.Type;
import mekanism.client.jei.gas.GasStackRenderer;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class BaseRecipeCategory<RECIPE> implements IRecipeCategory<RECIPE>, IGuiWrapper {

    private static final GuiDummy gui = new GuiDummy();

    private IGuiHelper guiHelper;
    protected ResourceLocation guiLocation;
    @Nullable
    protected ProgressBar progressBar;
    protected ITickTimer timer;
    protected int xOffset;
    protected int yOffset;
    protected IDrawable fluidOverlayLarge;
    protected IDrawable fluidOverlaySmall;
    protected Set<GuiElement> guiElements = new HashSet<>();
    private ResourceLocation recipeUID;
    private String unlocalizedName;

    private final IDrawable background;

    protected BaseRecipeCategory(IGuiHelper helper, String guiTexture, MekanismBlock mekanismBlock, @Nullable ProgressBar progress, int xOffset, int yOffset, int width, int height) {
        this(helper, guiTexture, mekanismBlock.getJEICategory(), mekanismBlock.getTranslationKey(), progress, xOffset, yOffset, width, height);
    }

    protected BaseRecipeCategory(IGuiHelper helper, String guiTexture, ResourceLocation name, String unlocalized, @Nullable ProgressBar progress, int xOffset, int yOffset, int width, int height) {
        guiHelper = helper;
        guiLocation = new ResourceLocation(guiTexture);

        progressBar = progress;

        recipeUID = name;
        unlocalizedName = unlocalized;

        timer = helper.createTickTimer(20, 20, false);

        ResourceLocation resource = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, Type.STANDARD.textureLocation);
        fluidOverlayLarge = guiHelper.createDrawable(resource, 19, 1, 16, 59);
        fluidOverlaySmall = guiHelper.createDrawable(resource, 19, 1, 16, 29);

        addGuiElements();

        this.xOffset = xOffset;
        this.yOffset = yOffset;
        background = guiHelper.createDrawable(guiLocation, xOffset, yOffset, width, height);
    }

    @Override
    public ResourceLocation getUid() {
        return recipeUID;
    }

    @Override
    public String getTitle() {
        return TextComponentUtil.build(unlocalizedName).getFormattedText();
    }

    @Override
    public void draw(RECIPE recipe, double mouseX, double mouseY) {
        MekanismRenderer.bindTexture(guiLocation);
        guiElements.forEach(e -> e.renderBackground(0, 0, -xOffset, -yOffset));
    }

    @Override
    public void drawTexturedRect(int x, int y, int u, int v, int w, int h) {
        gui.blit(x, y, u, v, w, h);
    }

    @Override
    public void drawTexturedRectFromIcon(int x, int y, TextureAtlasSprite icon, int w, int h) {
        gui.drawTexturedModalRect(x, y, icon, w, h);
    }

    @Override
    public void displayTooltip(ITextComponent component, int xAxis, int yAxis) {
    }

    @Override
    public void displayTooltips(List<ITextComponent> components, int xAxis, int yAxis) {
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

    @Nonnull
    @Override
    public IDrawable getIcon() {
        //TODO
        return null;
    }

    public List<ITextComponent> getTooltipComponents(RECIPE recipe, double mouseX, double mouseY) {
        return Collections.emptyList();
    }

    @Override
    public final List<String> getTooltipStrings(RECIPE recipe, double mouseX, double mouseY) {
        return getTooltipComponents(recipe, mouseX, mouseY).stream().map(ITextComponent::getFormattedText).collect(Collectors.toList());
    }

    protected void initGas(IGuiIngredientGroup<GasStack> group, int slot, boolean input, int x, int y, int width, int height, @Nullable GasStack stack, boolean overlay) {
        if (stack == null) {
            return;
        }

        IDrawable fluidOverlay = height > 50 ? fluidOverlayLarge : fluidOverlaySmall;
        GasStackRenderer renderer = new GasStackRenderer(stack.amount, false, width, height, overlay ? fluidOverlay : null);
        group.init(slot, input, renderer, x, y, width, height, 0, 0);
        group.set(slot, stack);
    }

    public static class GuiDummy extends AbstractGui {
    }
}