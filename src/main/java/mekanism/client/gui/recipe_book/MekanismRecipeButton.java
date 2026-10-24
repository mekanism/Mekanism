package mekanism.client.gui.recipe_book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackContentsFactory;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerIngredientHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackContentsFactory;
import net.neoforged.neoforge.client.ClientTooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;

public class MekanismRecipeButton extends RecipeButton implements IRecipeViewerIngredientHelper {

    //Keep a reference to the slot select time to skip having to AT the reference
    private final SlotSelectTime slotSelectTime;

    private List<ResolvedEntry> selectedEntries = Collections.emptyList();

    public MekanismRecipeButton(SlotSelectTime slotSelectTime) {
        this.slotSelectTime = slotSelectTime;
        super(slotSelectTime);
    }

    @Override
    public void init(RecipeCollection collection, boolean isFiltering, RecipeBookPage page, ContextMap resolutionContext) {
        this.collection = collection;
        List<RecipeDisplayEntry> fittingRecipes = collection.getSelectedRecipes(isFiltering ? RecipeCollection.CraftableStatus.CRAFTABLE : RecipeCollection.CraftableStatus.ANY);
        this.selectedEntries = fittingRecipes.stream().map(entry -> new ResolvedEntry(entry.id(), Stream.concat(
              entry.display().result().resolve(resolutionContext, ItemStackContentsFactory.INSTANCE),
              Stream.<Object>concat(
                    entry.display().result().resolve(resolutionContext, FluidStackContentsFactory.INSTANCE),
                    entry.display().result().resolve(resolutionContext, ChemicalStackContentsFactory.INSTANCE)
              )
        ).toList())).toList();
        this.allRecipesHaveSameResultDisplay = allRecipesHaveSameResultDisplay(this.selectedEntries);
        List<RecipeDisplayId> newlyShownRecipes = fittingRecipes.stream().map(RecipeDisplayEntry::id).filter(page.getRecipeBook()::willHighlight).toList();
        if (!newlyShownRecipes.isEmpty()) {
            newlyShownRecipes.forEach(page::recipeShown);
            this.animationTime = ANIMATION_TIME;
        }
    }

    //TODO - 26.3: Only match if they have the same between multiple types?
    private static boolean allRecipesHaveSameResultDisplay(List<ResolvedEntry> entries) {
        if (entries.size() < 2) {
            return true;
        }
        ItemStack firstItem = ItemStack.EMPTY;
        FluidStack firstFluid = FluidStack.EMPTY;
        ChemicalStack firstChemical = ChemicalStack.EMPTY;
        for (Iterator<Object> iterator = entries.stream().flatMap(e -> e.display().stream()).iterator(); iterator.hasNext(); ) {
            Object entry = iterator.next();
            switch (entry) {
                case ItemStack nextItem -> {
                    if (firstItem.isEmpty()) {
                        firstItem = nextItem;
                    } else if (!ItemStack.isSameItemSameComponents(firstItem, nextItem)) {
                        return false;
                    }
                }
                case FluidStack nextFluid -> {
                    if (firstFluid.isEmpty()) {
                        firstFluid = nextFluid;
                    } else if (!FluidStack.isSameFluidSameComponents(firstFluid, nextFluid)) {
                        return false;
                    }
                }
                case ChemicalStack nextChemical -> {
                    if (firstChemical.isEmpty()) {
                        firstChemical = nextChemical;
                    } else if (!ChemicalStack.isSameChemical(firstChemical, nextChemical)) {
                        return false;
                    }
                }
                default -> {
                    //Something went wrong
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void extractDisplayRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, boolean displayMultiple) {
        switch (getGeneralDisplayStack()) {
            case ItemStack currentItemStack -> {
                int offset = 4;
                if (displayMultiple) {
                    graphics.item(currentItemStack, getX() + offset + 1, getY() + offset + 1, 0);
                    offset--;
                }
                graphics.fakeItem(currentItemStack, getX() + offset, getY() + offset);
            }
            case FluidStack currentFluidStack ->
                  extractSpriteStates(graphics, displayMultiple, MekanismRenderer.getFluidTexture(currentFluidStack, FluidTextureType.STILL),
                        MekanismRenderer.getColorARGB(currentFluidStack));
            case ChemicalStack currentChemicalStack -> extractSpriteStates(graphics, displayMultiple, MekanismRenderer.getChemicalTexture(currentChemicalStack),
                  MekanismRenderer.color(currentChemicalStack));
            default -> {
            }
        }
    }

    private void extractSpriteStates(GuiGraphicsExtractor graphics, boolean displayMultiple, TextureAtlasSprite sprite, int tint) {
        int offset = 4;
        if (displayMultiple) {
            GuiUtils.drawTiledSprite(graphics, getX() + offset + 1, getY() + offset + 1, 16, 16, sprite, TilingDirection.UP_RIGHT, tint);
            offset--;
        }
        GuiUtils.drawTiledSprite(graphics, getX() + offset, getY() + offset, 16, 16, sprite, TilingDirection.UP_RIGHT, tint);
    }

    @Override
    @Deprecated
    public ItemStack getDisplayStack() {
        return getGeneralDisplayStack() instanceof ItemStack stack ? stack : ItemStack.EMPTY;
    }

    public Object getGeneralDisplayStack() {
        int currentIndex = this.slotSelectTime.currentIndex();
        int entryCount = this.selectedEntries.size();
        int offsetIndex = currentIndex / entryCount;
        int entryIndex = currentIndex - entryCount * offsetIndex;
        return this.selectedEntries.get(entryIndex).select(offsetIndex);
    }

    @Override
    protected boolean hasMultipleRecipes() {
        return this.selectedEntries.size() > 1;
    }

    @Override
    public boolean isOnlyOption() {
        return this.selectedEntries.size() == 1;
    }

    @Override
    public RecipeDisplayId getCurrentRecipe() {
        int index = this.slotSelectTime.currentIndex() % this.selectedEntries.size();
        return this.selectedEntries.get(index).id;
    }

    public List<Component> getTooltipText(FluidStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        List<Component> texts = new ArrayList<>(stack.getTooltipLines(
              Item.TooltipContext.of(minecraft.level, minecraft.player),
              minecraft.player,
              ClientTooltipFlag.of(minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL)
        ));
        if (hasMultipleRecipes()) {
            texts.add(MORE_RECIPES_TOOLTIP);
        }
        return texts;
    }

    public List<Component> getTooltipText(ChemicalStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        List<Component> texts = new ArrayList<>(stack.getTooltipLines(
              Item.TooltipContext.of(minecraft.level, minecraft.player),
              minecraft.player,
              ClientTooltipFlag.of(minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL)
        ));
        if (hasMultipleRecipes()) {
            texts.add(MORE_RECIPES_TOOLTIP);
        }
        return texts;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        //Provide the proper name for the recipe narration
        output.add(NarratedElementType.TITLE, Component.translatable("narration.recipe", TextComponentUtil.build(getGeneralDisplayStack())));
        if (hasMultipleRecipes()) {
            output.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"), Component.translatable("narration.recipe.usage.more"));
        } else {
            output.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
        }
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        return Optional.of(getGeneralDisplayStack());
    }

    @Override
    public Rect2i getIngredientBounds(double mouseX, double mouseY) {
        return new Rect2i(getX(), getY(), getWidth(), getHeight());
    }

    private record ResolvedEntry(RecipeDisplayId id, List<Object> display) {

        public Object select(int index) {
            if (this.display.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return this.display.get(index % this.display.size());
        }
    }
}