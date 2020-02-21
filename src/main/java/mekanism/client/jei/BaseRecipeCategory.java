package mekanism.client.jei;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.providers.IBaseProvider;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.gauge.GuiGauge.Type;
import mekanism.client.jei.chemical.ChemicalStackRenderer;
import mekanism.client.jei.chemical.GasStackRenderer;
import mekanism.client.jei.chemical.InfusionStackRenderer;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class BaseRecipeCategory<RECIPE> implements IRecipeCategory<RECIPE>, IGuiWrapper {

    private static final AbstractGui gui = new AbstractGui() {
    };

    private IGuiHelper guiHelper;
    @Nullable
    protected ResourceLocation guiLocation;
    protected ITickTimer timer;
    protected int xOffset;
    protected int yOffset;
    protected IDrawable fluidOverlayLarge;
    protected IDrawable fluidOverlaySmall;
    protected Set<GuiTexturedElement> guiElements = new ObjectOpenHashSet<>();
    private IBaseProvider provider;

    private final IDrawable background;

    protected BaseRecipeCategory(IGuiHelper helper, String guiTexture, IBaseProvider provider, int xOffset, int yOffset, int width, int height) {
        this(helper, new ResourceLocation(guiTexture), provider, xOffset, yOffset, width, height);
    }

    protected BaseRecipeCategory(IGuiHelper helper, IBaseProvider provider, int xOffset, int yOffset, int width, int height) {
        this(helper, (ResourceLocation) null, provider, xOffset, yOffset, width, height);
    }

    protected BaseRecipeCategory(IGuiHelper helper, @Nullable ResourceLocation guiLocation, IBaseProvider provider, int xOffset, int yOffset, int width, int height) {
        guiHelper = helper;
        this.guiLocation = guiLocation;

        this.provider = provider;

        timer = helper.createTickTimer(20, 20, false);

        ResourceLocation resource = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, Type.STANDARD.textureLocation);
        fluidOverlayLarge = guiHelper.createDrawable(resource, 19, 1, 16, 59);
        fluidOverlaySmall = guiHelper.createDrawable(resource, 19, 1, 16, 29);

        this.xOffset = xOffset;
        this.yOffset = yOffset;
        if (guiLocation == null) {
            background = new NOOPDrawable(width, height);
        } else {
            background = guiHelper.createDrawable(guiLocation, xOffset, yOffset, width, height);
        }
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
        return provider.getRegistryName();
    }

    @Override
    public String getTitle() {
        return provider.getTextComponent().getFormattedText();
    }

    @Override
    public void draw(RECIPE recipe, double mouseX, double mouseY) {
        if (guiLocation != null) {
            MekanismRenderer.bindTexture(guiLocation);
        }
        guiElements.forEach(e -> e.render((int) mouseX, (int) mouseY, 0));
    }

    @Override
    public void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis, float scale) {
        //NO-OP at least for now
    }

    @Override
    public void drawTexturedRect(int x, int y, int textureX, int textureY, int width, int height) {
        gui.blit(x, y, textureX, textureY, width, height);
    }

    @Override
    public void drawTexturedRectFromIcon(int x, int y, TextureAtlasSprite icon, int width, int height) {
        AbstractGui.blit(x, y, gui.getBlitOffset(), width, height, icon);
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

    protected void initInfusion(IGuiIngredientGroup<@NonNull InfusionStack> group, int slot, boolean input, int x, int y, int width, int height, @Nonnull List<InfusionStack> stacks) {
        initChemical(group, slot, input, x, y, width, height, stacks, max -> new InfusionStackRenderer(max, width, height,null));
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
        //TODO: Make sure it renders properly once we have multiple different types
    }
}