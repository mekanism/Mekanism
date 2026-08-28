package mekanism.client.recipe_viewer.jei;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToIntFunction;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackContentsFactory;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.gauge.GaugeOverlay;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.IProgressInfoHandler;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.TilingDirection;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

//TODO: Re-evaluate this extending AbstractContainerEventHandler
public abstract class BaseRecipeCategory<RECIPE> extends AbstractContainerEventHandler implements IRecipeCategory<RECIPE>, IGuiWrapper {

    protected static IDrawable createIcon(IGuiHelper helper, IRecipeViewerRecipeType<?> recipeType) {
        ItemStack stack = recipeType.iconStack();
        if (stack.isEmpty()) {
            Identifier icon = recipeType.icon();
            if (icon == null) {
                throw new IllegalStateException("Expected recipe type to have either an icon stack or an icon location");
            }
            return helper.createDrawableSprite(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI), icon, 16, 16);
        }
        return helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, stack);
    }

    private final List<GuiElement> guiElements = new ArrayList<>();
    private final Component component;
    private final IGuiHelper guiHelper;
    private final IRecipeType<RECIPE> recipeType;
    private final IDrawable icon;
    private final int xOffset;
    private final int yOffset;
    private final int width;
    private final int height;
    @Nullable
    private Map<GaugeOverlay, IDrawable> overlayLookup;
    @Nullable
    private ITickTimer timer;

    protected BaseRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<RECIPE> recipeType) {
        this(helper, MekanismJEI.recipeType(recipeType), recipeType.getTextComponent(), createIcon(helper, recipeType), recipeType.xOffset(), recipeType.yOffset(), recipeType.width(), recipeType.height());
    }

    protected BaseRecipeCategory(IGuiHelper helper, IRecipeType<RECIPE> recipeType, Component component, IDrawable icon, int xOffset, int yOffset, int width, int height) {
        this.recipeType = recipeType;
        this.component = component;
        this.guiHelper = helper;
        this.icon = icon;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
    }

    protected <ELEMENT extends GuiElement> ELEMENT addElement(ELEMENT element) {
        guiElements.add(element);
        return element;
    }

    @Override
    public List<GuiElement> children() {
        return guiElements;
    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(getLeftPos(), getTopPos(), this.getImageWidth(), getImageHeight());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RECIPE recipe, IFocusGroup focuses) {
        for (GuiElement guiElement : guiElements) {
            //TODO: I believe we can use this method and adding via builder.addWidget to reduce some of our draw override hacks
            //TODO: Only add this for ones that we actually have interaction behavior for?
            builder.addGuiEventListener(new MekJeiWidget(guiElement));
        }
    }

    /// @apiNote x and y are based on the values set in the tile, as the GUI then shifts the slots by one to account for the border. This method is mostly meant as a
    /// helper to make keeping track of the positioning numbers easier.
    protected GuiSlot addSlot(SlotType type, int x, int y) {
        return addSlot(type, x, y, SlotType.SLOT_SIZE, SlotType.SLOT_SIZE);
    }

    protected GuiSlot addSlot(SlotType type, int x, int y, int width, int height) {
        return addElement(new GuiSlot(type, this, x - 1, y - 1, width, height));
    }

    protected GuiProgress addSimpleProgress(ProgressType type, int x, int y) {
        return addElement(new GuiProgress(getSimpleProgressTimer(), type, this, x, y));
    }

    protected GuiProgress addConstantProgress(ProgressType type, int x, int y) {
        return addElement(new GuiProgress(RecipeViewerUtils.CONSTANT_PROGRESS, type, this, x, y));
    }

    @Override
    public int getLeftPos() {
        return xOffset;
    }

    @Override
    public int getTopPos() {
        return yOffset;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getImageWidth() {
        return getWidth();
    }

    @Override
    public int getImageHeight() {
        return getHeight();
    }

    @Override
    public IRecipeType<RECIPE> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return component;
    }

    @Override
    public void draw(RECIPE recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        //Translate back by our offset so that we are effectively rendering the foreground starting at 0, 0
        // This is needed to make sure that we render things like crystallizer text in the correct spot
        // If this ends up causing issues elsewhere we will need to look into it further
        Matrix3x2fStack matrix = guiGraphics.pose();
        matrix.pushMatrix();
        matrix.translate(getLeftPos(), getTopPos());
        renderElements(recipe, recipeSlotsView, guiGraphics, (int) mouseX, (int) mouseY);
        matrix.popMatrix();
    }

    protected void renderElements(RECIPE recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, int x, int y) {
        Matrix3x2fStack matrix = guiGraphics.pose();
        for (GuiElement guiElement : guiElements) {
            guiElement.renderShifted(guiGraphics, x, y, 0);
        }
        for (GuiElement e : guiElements) {
            e.onDrawBackground(guiGraphics, x, y, 0);
        }
        //Note: We don't care that onRenderForeground updates the maxZOffset in the mekanism gui as that is just used for rendering windows
        // and as our categories don't support windows we don't need to worry about that
        //TODO: Re-evaluate this zOffset. We use 200 in GuiMekanism, but at least in JEI everything seems to render fine using zero.
        // When using 200 the crystallizer screen's ore type slot ends up rendering in front of JEI's item rendering, so for now we are just setting this to zero
        int zOffset = 0;//200;
        for (GuiElement element : guiElements) {
            matrix.pushMatrix();
            element.onRenderForeground(guiGraphics, x, y);
            matrix.popMatrix();
        }
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Nullable
    @Override
    public abstract Identifier getIdentifier(RECIPE recipe);

    @Override
    public abstract Codec<RECIPE> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager);

    protected IProgressInfoHandler getSimpleProgressTimer() {
        return () -> {
            if (timer == null) {
                timer = guiHelper.createTickTimer(SharedConstants.TICKS_PER_SECOND, SharedConstants.TICKS_PER_SECOND, false);
            }
            return timer.getValue() / (float) SharedConstants.TICKS_PER_SECOND;
        };
    }

    protected IBarInfoHandler getBarProgressTimer() {
        return new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismLang.PROGRESS.translate(TextUtils.getPercent(getLevel()));
            }

            @Override
            public double getLevel() {
                if (timer == null) {
                    timer = guiHelper.createTickTimer(SharedConstants.TICKS_PER_SECOND, SharedConstants.TICKS_PER_SECOND, false);
                }
                return timer.getValue() / (double) SharedConstants.TICKS_PER_SECOND;
            }
        };
    }

    private IDrawable getOverlay(GuiGauge<?> gauge) {
        if (overlayLookup == null) {
            overlayLookup = new EnumMap<>(GaugeOverlay.class);
        }
        GaugeOverlay overlay = gauge.getGaugeOverlay();
        IDrawable drawable = overlayLookup.get(overlay);
        if (drawable == null) {
            drawable = guiHelper.createDrawableSprite(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI), overlay.getBarOverlay(), overlay.getWidth(), overlay.getHeight());
            overlayLookup.put(overlay, drawable);
        }
        return drawable;
    }

    protected <STACK> STACK getDisplayedStack(IRecipeSlotsView recipeSlotsView, String slotName, IIngredientType<STACK> type, STACK empty) {
        Optional<IRecipeSlotView> slotByName = recipeSlotsView.findSlotByName(slotName);
        //noinspection OptionalIsPresent - Capturing lambda
        if (slotByName.isPresent()) {
            return slotByName.get().getDisplayedIngredient(type).orElse(empty);
        }
        return empty;
    }

    protected IRecipeSlotBuilder initItem(IRecipeLayoutBuilder builder, RecipeIngredientRole role, GuiSlot slot, SlotDisplay display) {
        return initItem(builder, role, slot.getX(), slot.getY(), display);
    }

    protected IRecipeSlotBuilder initItem(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, SlotDisplay display) {
        return builder.addSlot(role, x + 1, y + 1).add(VanillaTypes.ITEM_STACK, display);
    }

    protected IRecipeSlotBuilder initFluid(IRecipeLayoutBuilder builder, RecipeIngredientRole role, GuiGauge<?> gauge, SlotDisplay display) {
        RecipeTankBuilder tankBuilder = init(builder, NeoForgeTypes.FLUID_STACK, role, gauge, display, FluidStackContentsFactory.INSTANCE, FluidStack::amount);
        return tankBuilder.slotBuilder().setFluidRenderer(tankBuilder.max(), false, tankBuilder.width(), tankBuilder.height(), TilingDirection.UP_RIGHT);
    }

    protected IRecipeSlotBuilder initChemical(IRecipeLayoutBuilder builder, RecipeIngredientRole role, GuiElement element, SlotDisplay display) {
        RecipeTankBuilder tankBuilder = init(builder, MekanismJEI.TYPE_CHEMICAL, role, element, display, ChemicalStackContentsFactory.INSTANCE, ChemicalStack::amount);
        return tankBuilder.slotBuilder().setCustomRenderer(MekanismJEI.TYPE_CHEMICAL, new ChemicalStackRenderer(tankBuilder.max(), tankBuilder.width(), tankBuilder.height()));
    }

    private <STACK> RecipeTankBuilder init(IRecipeLayoutBuilder builder, IIngredientType<STACK> type, RecipeIngredientRole role, GuiElement element,
          SlotDisplay display, DisplayContentsFactory<STACK> factory, ToIntFunction<STACK> sizeExtractor) {
        int width = element.getWidth() - 2;
        int height = element.getHeight() - 2;
        int x = element.getX() + 1;
        int y = element.getY() + 1;
        IRecipeSlotBuilder slotBuilder = builder.addSlot(role, x, y);
        if (element instanceof GuiGauge<?> gauge) {
            slotBuilder.setOverlay(getOverlay(gauge), 0, 0);
        }
        //If we have no max (no fluids or just an empty fluid) we want to ensure the fluid renderer doesn't throw errors,
        // so we just return a capacity for the render of a bucket
        int max = display.resolve(slotBuilder.getContextMap(), factory)
              .mapToInt(sizeExtractor)
              .filter(stackSize -> stackSize > 0)
              .max().orElse(FluidType.BUCKET_VOLUME);
        return new RecipeTankBuilder(slotBuilder.add(type, display), max, width, height);
    }

    private record RecipeTankBuilder(IRecipeSlotBuilder slotBuilder, int max, int width, int height) {
    }
}