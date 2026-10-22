package mekanism.client.gui.recipe_book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.display.recipe.MekanismRecipeDisplay;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackContentsFactory;
import mekanism.api.resource.IResourceContainer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;

public class MekanismOverlayRecipeComponent extends OverlayRecipeComponent {

    private final List<? extends IResourceContainer<?>> inputs;
    private final Supplier<Font> fontSupplier;

    public MekanismOverlayRecipeComponent(OverlayRecipeComponent vanilla, Supplier<Font> fontSupplier, List<? extends IResourceContainer<?>> inputs) {
        this.inputs = inputs;
        this.fontSupplier = fontSupplier;
        super(vanilla.slotSelectTime, false);
    }

    @Override
    public void init(RecipeCollection collection, ContextMap context, boolean isFiltering, int buttonX, int buttonY, int centerX, int centerY, float buttonWidth) {
        this.collection = collection;
        List<RecipeDisplayEntry> craftable = collection.getSelectedRecipes(RecipeCollection.CraftableStatus.CRAFTABLE);
        List<RecipeDisplayEntry> unCraftable = isFiltering ? Collections.emptyList() : collection.getSelectedRecipes(RecipeCollection.CraftableStatus.NOT_CRAFTABLE);
        int craftables = craftable.size();
        int total = craftables + unCraftable.size();
        int maxRow = total <= 16 ? MAX_ROW : MAX_ROW_LARGE;
        int rows = (int) Math.ceil((float) total / maxRow);
        this.x = buttonX;
        this.y = buttonY;
        float rightPos = this.x + Math.min(total, maxRow) * BUTTON_SIZE;
        float maxLeftPos = centerX + 50;
        if (rightPos > maxLeftPos) {
            this.x = (int) (this.x - buttonWidth * (int) ((rightPos - maxLeftPos) / buttonWidth));
        }

        float bottomPos = this.y + rows * BUTTON_SIZE;
        float maxBottomPos = centerY + 50;
        if (bottomPos > maxBottomPos) {
            this.y = (int) (this.y - buttonWidth * Mth.ceil((bottomPos - maxBottomPos) / buttonWidth));
        }

        float topPos = this.y;
        float maxTopPos = centerY - 100;
        if (topPos < maxTopPos) {
            this.y = (int) (this.y - buttonWidth * Mth.ceil((topPos - maxTopPos) / buttonWidth));
        }

        setVisible(true);
        this.recipeButtons.clear();

        for (int i = 0; i < total; i++) {
            boolean canCraft = i < craftables;
            RecipeDisplayEntry recipe = canCraft ? craftable.get(i) : unCraftable.get(i - craftables);
            int x = this.x + MAX_ROW + BUTTON_SIZE * (i % maxRow);
            int y = this.y + MAX_ROW_LARGE + BUTTON_SIZE * (i / maxRow);
            //TODO - 26.3: Neo PR to allow creating custom buttons instead of having to copy the entire above logic
            this.recipeButtons.add(new MekanismOverlayRecipeButton(x, y, recipe.id(), recipe.display(), context, canCraft));
        }

        this.lastRecipeClicked = null;
    }

    private class MekanismOverlayRecipeButton extends OverlayRecipeButton {

        private final Identifier enabledSprite;
        private final Identifier highlightedEnabledSprite;
        private final Identifier disabledSprite;
        private final Identifier highlightedDisabledSprite;
        private final List<OverlayRecipeComponent.OverlayRecipeButton.Pos> items;
        private final List<ChemicalPos> chemicals;
        private final List<FluidPos> fluids;

        public MekanismOverlayRecipeButton(int x, int y, RecipeDisplayId id, RecipeDisplay recipe, ContextMap context, boolean isCraftable) {
            List<OverlayRecipeComponent.OverlayRecipeButton.Pos> slots = new ArrayList<>();
            List<ChemicalPos> chemicalTanks = new ArrayList<>();
            List<FluidPos> fluidTanks = new ArrayList<>();
            List<SlotDisplay> ingredients;
            if (recipe instanceof FurnaceRecipeDisplay furnaceRecipe) {
                ingredients = List.of(furnaceRecipe.ingredient());
            } else if (recipe instanceof MekanismRecipeDisplay mekanismRecipe) {
                ingredients = mekanismRecipe.ingredients();
            } else {
                ingredients = Collections.emptyList();
            }
            int inputCount = inputs.size();
            if (ingredients.size() == inputCount) {
                int xPos = 1;
                int yPos = 1;
                for (int i = 0; i < inputCount; i++) {
                    SlotDisplay ingredient = ingredients.get(i);
                    switch (inputs.get(i)) {
                        case IInventorySlot _ -> {
                            List<ItemStack> items = ingredient.resolveForStacks(context);
                            if (!items.isEmpty()) {
                                slots.add(createGridPos(xPos, yPos, items));
                            }
                        }
                        case IFluidTank _ -> {
                            List<FluidStack> fluids = ingredient.resolve(context, FluidStackContentsFactory.INSTANCE).toList();
                            if (!fluids.isEmpty()) {
                                fluidTanks.add(createFluidGridPos(xPos, yPos, fluids));
                            }
                        }
                        case IChemicalTank _ -> {
                            List<ChemicalStack> chemicals = ingredient.resolve(context, ChemicalStackContentsFactory.INSTANCE).toList();
                            if (!chemicals.isEmpty()) {
                                chemicalTanks.add(createChemicalGridPos(xPos, yPos, chemicals));
                            }
                        }
                        default -> {
                        }
                    }
                    if (i % 2 == 0) {
                        xPos++;
                    } else {
                        yPos++;
                    }
                }
            }
            if (inputCount == 1) {
                this.enabledSprite = OverlaySmeltingRecipeButton.ENABLED_SPRITE;
                this.highlightedEnabledSprite = OverlaySmeltingRecipeButton.ENABLED_SPRITE;
                this.disabledSprite = OverlaySmeltingRecipeButton.DISABLED_SPRITE;
                this.highlightedDisabledSprite = OverlaySmeltingRecipeButton.HIGHLIGHTED_DISABLED_SPRITE;
            } else if (inputCount == 2 && false) {
                //TODO - 26.3: Switch this out with custom ones
            } else if (inputCount == 3 && false) {
                //TODO - 26.3: Switch this out with custom ones
            } else {//Fallback to vanilla crafting
                this.enabledSprite = OverlayCraftingRecipeButton.ENABLED_SPRITE;
                this.highlightedEnabledSprite = OverlayCraftingRecipeButton.ENABLED_SPRITE;
                this.disabledSprite = OverlayCraftingRecipeButton.DISABLED_SPRITE;
                this.highlightedDisabledSprite = OverlayCraftingRecipeButton.HIGHLIGHTED_DISABLED_SPRITE;
            }
            this.items = List.copyOf(slots);
            this.chemicals = List.copyOf(chemicalTanks);
            this.fluids = List.copyOf(fluidTanks);
            //Pass no items so that super doesn't try to render them, and we can instead render them with a count if necessary
            super(x, y, id, isCraftable, Collections.emptyList());
        }

        @Override
        public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
            float gridPosX = getX() + 2;
            float gridPosY = getY() + 2;
            Font font = fontSupplier.get();
            //Render here instead of in super so that we can support rendering stack size
            for (OverlayRecipeComponent.OverlayRecipeButton.Pos pos : this.items) {
                graphics.pose().pushMatrix();
                graphics.pose().translate(gridPosX + pos.x(), gridPosY + pos.y());
                graphics.pose().scale(ITEM_RENDER_SCALE, ITEM_RENDER_SCALE);
                graphics.pose().translate(-8.0F, -8.0F);
                ItemStack stack = pos.selectIngredient(MekanismOverlayRecipeComponent.this.slotSelectTime.currentIndex());
                graphics.item(stack, 0, 0);
                graphics.itemCount(font, stack, 0, 0, null);
                graphics.pose().popMatrix();
            }
            for (FluidPos pos : this.fluids) {
                FluidStack fluidStack = pos.selectIngredient(MekanismOverlayRecipeComponent.this.slotSelectTime.currentIndex());
                extractSpriteState(graphics, font, pos.x, pos.y, gridPosX, gridPosY, MekanismRenderer.getFluidTexture(fluidStack, FluidTextureType.STILL),
                      MekanismRenderer.getColorARGB(fluidStack), fluidStack.amount());
            }
            for (ChemicalPos pos : this.chemicals) {
                ChemicalStack chemicalStack = pos.selectIngredient(MekanismOverlayRecipeComponent.this.slotSelectTime.currentIndex());
                extractSpriteState(graphics, font, pos.x, pos.y, gridPosX, gridPosY, MekanismRenderer.getChemicalTexture(chemicalStack), MekanismRenderer.color(chemicalStack),
                      chemicalStack.amount());
            }
        }

        private void extractSpriteState(GuiGraphicsExtractor graphics, Font font, int posX, int posY, float gridPosX, float gridPosY, TextureAtlasSprite sprite,
              int tint, int amount) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(gridPosX + posX, gridPosY + posY);
            graphics.pose().scale(ITEM_RENDER_SCALE, ITEM_RENDER_SCALE);
            graphics.pose().translate(-8.0F, -8.0F);
            GuiUtils.drawTiledSprite(graphics, 0, 0, 16, 16, sprite, TilingDirection.UP_RIGHT, tint);
            if (amount != 1) {
                String amountText = String.valueOf(amount);
                graphics.text(font, amountText, 17 - font.width(amountText), 9, CommonColors.WHITE, true);
            }
            graphics.pose().popMatrix();
        }

        @Override
        protected Identifier getSprite(boolean isCraftable) {
            if (isCraftable) {
                return this.isHoveredOrFocused() ? highlightedEnabledSprite : enabledSprite;
            }
            return this.isHoveredOrFocused() ? highlightedDisabledSprite : disabledSprite;
        }

        protected static FluidPos createFluidGridPos(int gridXPos, int gridYPos, List<FluidStack> fluidStacks) {
            return new FluidPos(3 + gridXPos * 7, 3 + gridYPos * 7, fluidStacks);
        }

        protected static ChemicalPos createChemicalGridPos(int gridXPos, int gridYPos, List<ChemicalStack> chemicalStacks) {
            return new ChemicalPos(3 + gridXPos * 7, 3 + gridYPos * 7, chemicalStacks);
        }

        protected record FluidPos(int x, int y, List<FluidStack> fluidIngredients) {

            protected FluidPos {
                if (fluidIngredients.isEmpty()) {
                    throw new IllegalArgumentException("Ingredient list must be non-empty");
                }
            }

            public FluidStack selectIngredient(int currentIndex) {
                return this.fluidIngredients.get(currentIndex % this.fluidIngredients.size());
            }
        }

        protected record ChemicalPos(int x, int y, List<ChemicalStack> chemicalIngredients) {

            protected ChemicalPos {
                if (chemicalIngredients.isEmpty()) {
                    throw new IllegalArgumentException("Ingredient list must be non-empty");
                }
            }

            public ChemicalStack selectIngredient(int currentIndex) {
                return this.chemicalIngredients.get(currentIndex % this.chemicalIngredients.size());
            }
        }
    }
}