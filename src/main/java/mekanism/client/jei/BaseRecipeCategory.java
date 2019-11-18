package mekanism.client.jei;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.gauge.GuiGauge.Type;
import mekanism.client.jei.gas.GasStackRenderer;
import mekanism.client.render.MekanismRenderer;
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
    protected Set<GuiTexturedElement> guiElements = new HashSet<>();
    private ResourceLocation recipeUID;
    private String unlocalizedName;

    private final IDrawable background;

    protected BaseRecipeCategory(IGuiHelper helper, String guiTexture, IBlockProvider mekanismBlock, @Nullable ProgressBar progress, int xOffset, int yOffset, int width, int height) {
        this(helper, guiTexture, mekanismBlock.getRegistryName(), mekanismBlock.getTranslationKey(), progress, xOffset, yOffset, width, height);
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

        this.xOffset = xOffset;
        this.yOffset = yOffset;
        background = guiHelper.createDrawable(guiLocation, xOffset, yOffset, width, height);

        addGuiElements();
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
    public ResourceLocation getUid() {
        return recipeUID;
    }

    @Override
    public String getTitle() {
        return TextComponentUtil.translate(unlocalizedName).getFormattedText();
    }

    @Override
    public void draw(RECIPE recipe, double mouseX, double mouseY) {
        MekanismRenderer.bindTexture(guiLocation);
        guiElements.forEach(e -> e.render((int) mouseX, (int) mouseY, 0));
    }

    @Override
    public void drawTexturedRect(int x, int y, int textureX, int textureY, int width, int height) {
        gui.blit(x, y, textureX, textureY, width, height);
    }

    @Override
    public void drawTexturedRectFromIcon(int x, int y, TextureAtlasSprite icon, int width, int height) {
        gui.blit(x, y, icon, width, height);
    }

    @Override
    public void drawModalRectWithCustomSizedTexture(int x, int y, int textureX, int textureY, int width, int height, int textureWidth, int textureHeight) {
        AbstractGui.blit(x, y, textureX, textureY, width, height, textureWidth, textureHeight);
    }

    @Override
    public void drawModalRectWithCustomSizedTexture(int x, int y, int desiredWidth, int desiredHeight, int textureX, int textureY, int width, int height, int textureWidth, int textureHeight) {
        AbstractGui.blit(x, y, desiredWidth, desiredHeight, textureX, textureY, width, height, textureWidth, textureHeight);
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
        //TODO: Query the gui elements, except they will ave to return a list instead of rendering it
        return Collections.emptyList();
    }

    @Override
    public final List<String> getTooltipStrings(RECIPE recipe, double mouseX, double mouseY) {
        return getTooltipComponents(recipe, mouseX, mouseY).stream().map(ITextComponent::getFormattedText).collect(Collectors.toList());
    }

    protected void initGas(IGuiIngredientGroup<@NonNull GasStack> group, int slot, boolean input, int x, int y, int width, int height, @Nonnull List<GasStack> stacks, boolean overlay) {
        if (stacks.isEmpty()) {
            return;
        }
        IDrawable fluidOverlay = height > 50 ? fluidOverlayLarge : fluidOverlaySmall;
        int max = stacks.stream().mapToInt(GasStack::getAmount).filter(stack -> stack >= 0).max().orElse(0);
        GasStackRenderer renderer = new GasStackRenderer(max, false, width, height, overlay ? fluidOverlay : null);
        group.init(slot, input, renderer, x, y, width, height, 0, 0);
        group.set(slot, stacks);
        //TODO: Make sure it renders properly once we have multiple different types (might not have to deal with it until 1.14)
    }

    @Deprecated
    protected void initGas(IGuiIngredientGroup<@NonNull GasStack> group, int slot, boolean input, int x, int y, int width, int height, @Nonnull GasStack stack, boolean overlay) {
        if (stack.isEmpty()) {
            return;
        }
        IDrawable fluidOverlay = height > 50 ? fluidOverlayLarge : fluidOverlaySmall;
        GasStackRenderer renderer = new GasStackRenderer(stack.getAmount(), false, width, height, overlay ? fluidOverlay : null);
        group.init(slot, input, renderer, x, y, width, height, 0, 0);
        group.set(slot, stack);
    }

    public static class GuiDummy extends AbstractGui {

        public void blit(int x, int y, TextureAtlasSprite sprite, int width, int height) {
            //Have this helper method as blitOffset is protected
            blit(x, y, blitOffset, width, height, sprite);
        }
    }
}