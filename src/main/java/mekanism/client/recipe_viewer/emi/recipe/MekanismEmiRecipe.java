package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.ChemicalEmiStack;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.client.recipe_viewer.emi.widget.MekanismEmiWidget;
import mekanism.client.recipe_viewer.emi.widget.MekanismTankEmiWidget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class MekanismEmiRecipe<RECIPE> extends AbstractContainerEventHandler implements EmiRecipe, IGuiWrapper {

    private final List<EmiIngredient> inputs = new ArrayList<>();
    private final List<EmiIngredient> catalysts = new ArrayList<>();
    private final List<EmiStack> outputs = new ArrayList<>();
    private final List<EmiIngredient> renderOutputs = new ArrayList<>();
    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    protected final RECIPE recipe;
    private final int xOffset;
    private final int yOffset;
    private final int width;
    private final int height;

    public MekanismEmiRecipe(MekanismEmiRecipeCategory category, ResourceLocation id, RECIPE recipe) {
        this(category, id, recipe, category.xOffset(), category.yOffset(), category.width(), category.height());
    }

    public MekanismEmiRecipe(EmiRecipeCategory category, ResourceLocation id, RECIPE recipe, int xOffset, int yOffset, int width, int height) {
        this.category = category;
        this.recipe = recipe;
        this.id = id;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
    }

    protected EmiIngredient input(int index) {
        return inputs.get(index);
    }

    protected EmiIngredient catalyst(int index) {
        return catalysts.get(index);
    }

    protected EmiIngredient output(int index) {
        return renderOutputs.get(index);
    }

    protected void addInputDefinition(ItemStackIngredient ingredient) {
        inputs.add(ingredient(ingredient));
    }

    protected void addInputDefinition(FluidStackIngredient ingredient) {
        inputs.add(fluidIngredient(ingredient));
    }

    protected void addInputDefinition(ChemicalStackIngredient ingredient) {
        inputs.add(chemicalIngredient(ingredient));
    }

    protected void addInputDefinition(ChemicalStackIngredient ingredient, int scalar) {
        inputs.add(chemicalIngredient(ingredient, scalar));
    }

    protected void addEmptyInput() {
        inputs.add(EmiStack.EMPTY);
    }

    protected void addCatalsyst(ChemicalStackIngredient ingredient) {
        catalysts.add(ingredient(RecipeViewerUtils.getStacksFor(ingredient, true)));
    }

    protected void addItemOutputDefinition(List<ItemStack> definition) {
        addOutputDefinition(definition.stream().map(EmiStack::of).toList());
    }

    protected void addFluidOutputDefinition(List<FluidStack> definition) {
        addOutputDefinition(definition.stream().map(NeoForgeEmiStack::of).toList());
    }

    protected void addChemicalOutputDefinition(List<ChemicalStack> definition) {
        addOutputDefinition(definition.stream().<EmiStack>map(ChemicalEmiStack::new).toList());
    }

    protected void addOutputDefinition(List<EmiStack> stacks) {
        if (stacks.isEmpty()) {
            outputs.add(EmiStack.EMPTY);
            renderOutputs.add(EmiStack.EMPTY);
        } else {
            outputs.addAll(stacks);
            renderOutputs.add(EmiIngredient.of(stacks));
        }
    }

    @Override
    public int getGuiLeft() {
        return xOffset;
    }

    @Override
    public int getGuiTop() {
        return yOffset;
    }

    @Override
    public int getXSize() {
        return width;
    }

    @Override
    public int getYSize() {
        return height;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Nullable
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return this.catalysts;
    }

    @Override
    public int getDisplayWidth() {
        return getXSize();
    }

    @Override
    public int getDisplayHeight() {
        return getYSize();
    }

    @Nullable
    @Override
    public RecipeHolder<?> getBackingRecipe() {
        //Don't bother looking up the recipe. We will override this where we actually have holders
        return null;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        //TODO: I believe returning empty for this is fine for now, though we could potentially gather the children and then
        // return the same instance for the element each call to addWidgets, and just wrap it into a new widget?
        return Collections.emptyList();
    }

    protected static <STACK> Supplier<STACK> getSupplier(List<STACK> stacks, STACK empty) {
        if (stacks.isEmpty()) {
            return () -> empty;
        } else if (stacks.size() == 1) {
            STACK stack = stacks.getFirst();
            return () -> stack;
        }
        return () -> RecipeViewerUtils.getCurrent(stacks);
    }

    /**
     * @apiNote x and y are based on the values set in the tile, as the GUI then shifts the slots by one to account for the border. This method is mostly meant as a
     * helper to make keeping track of the positioning numbers easier.
     */
    protected SlotWidget addSlot(WidgetHolder widgetHolder, SlotType type, int x, int y, EmiIngredient ingredient) {
        GuiSlot slot = addSlot(widgetHolder, type, x, y);
        return initItem(widgetHolder, slot.getX(), slot.getY(), ingredient);
    }

    protected GuiSlot addSlot(WidgetHolder widgetHolder, SlotType type, int x, int y) {
        return addElement(widgetHolder, new GuiSlot(type, this, x - 1, y - 1));
    }

    protected GuiProgress addConstantProgress(WidgetHolder widgetHolder, ProgressType type, int x, int y) {
        return addElement(widgetHolder, new GuiProgress(RecipeViewerUtils.CONSTANT_PROGRESS, type, this, x, y));
    }

    protected GuiProgress addSimpleProgress(WidgetHolder widgetHolder, ProgressType type, int x, int y, int processTime) {
        return addElement(widgetHolder, new GuiProgress(RecipeViewerUtils.progressHandler(processTime), type, this, x, y));
    }

    protected <ELEMENT extends GuiElement> ELEMENT addElement(WidgetHolder widgetHolder, ELEMENT element) {
        return addElement(widgetHolder, element, false);
    }

    protected <ELEMENT extends GuiElement> ELEMENT addElement(WidgetHolder widgetHolder, ELEMENT element, boolean forwardClicks) {
        widgetHolder.add(new MekanismEmiWidget(element, forwardClicks));
        return element;
    }

    protected EmiIngredient ingredient(ItemStackIngredient ingredient) {
        return ingredient(ingredient.getRepresentations());
    }

    protected EmiIngredient ingredient(List<ItemStack> representations) {
        return EmiIngredient.of(representations.stream().map(EmiStack::of).toList());
    }

    protected EmiIngredient fluidIngredient(FluidStackIngredient ingredient) {
        return EmiIngredient.of(ingredient.getRepresentations().stream().map(NeoForgeEmiStack::of).toList());
    }

    protected EmiIngredient chemicalIngredient(ChemicalStackIngredient ingredient) {
        return EmiIngredient.of(ingredient.getRepresentations().stream().map(ChemicalEmiStack::new).toList());
    }

    protected EmiIngredient chemicalIngredient(ChemicalStackIngredient ingredient, int scalar) {
        List<? extends ChemicalStack> representations = ingredient.getRepresentations();
        if (representations.isEmpty()) {
            return EmiStack.EMPTY;
        }
        List<ChemicalEmiStack> list = new ArrayList<>(representations.size());
        for (ChemicalStack stack : representations) {
            list.add(new ChemicalEmiStack(stack.getChemicalHolder(), stack.getAmount() * scalar));
        }
        return EmiIngredient.of(list);
    }

    protected SlotWidget initItem(WidgetHolder widgetHolder, GuiSlot slot, EmiIngredient ingredient) {
        addElement(widgetHolder, slot);
        return initItem(widgetHolder, slot.getX(), slot.getY(), ingredient);
    }

    protected SlotWidget initItem(WidgetHolder widgetHolder, int x, int y, EmiIngredient ingredient) {
        return widgetHolder.addSlot(ingredient, x, y)
              .drawBack(false);
    }

    protected SlotWidget initTank(WidgetHolder widgetHolder, GuiElement element, EmiIngredient ingredient) {
        addElement(widgetHolder, element);
        return widgetHolder.add(new MekanismTankEmiWidget(ingredient, element, ingredient.getAmount()))
              .drawBack(false);
    }
}