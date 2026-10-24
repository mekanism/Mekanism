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
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent.OverlayCraftingRecipeButton;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent.OverlayRecipeButton;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent.OverlaySmeltingRecipeButton;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;

//TODO - 26.4: Make this extend OverlayRecipeButton directly
public class MekanismOverlayRecipeButton {

    @SuppressWarnings("JoinDeclarationAndAssignmentJava")//Don't bother joining as they are still separate to make it easier in 26.4 to make this a proper subclass instead
    public static OverlayRecipeButton construct(OverlayRecipeComponent parent, int x, int y, RecipeDisplayId id, RecipeDisplay recipe, ContextMap context, boolean isCraftable,
          Supplier<Font> fontSupplier, List<? extends IResourceContainer<?>> inputs) {
        //TODO: These should become proper fields
        final Identifier enabledSprite;
        final Identifier highlightedEnabledSprite;
        final Identifier disabledSprite;
        final Identifier highlightedDisabledSprite;
        //TODO: Constructor logic
        List<OverlayRecipeButton.Pos> slots = new ArrayList<>();
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
            for (int i = 0; i < inputCount; i++) {
                SlotDisplay ingredient = ingredients.get(i);
                int xPos;
                int yPos = 1;
                switch (inputCount) {
                    case 1 -> xPos = 1;
                    case 2 -> xPos = i == 0 ? 0 : 2;
                    case 3 -> {
                        xPos = i;
                        yPos = i == 1 ? 0 : 2;
                    }
                    default -> {
                        xPos = i % 3;
                        yPos = i / 3;
                    }
                }
                switch (inputs.get(i)) {
                    case IInventorySlot _ -> {
                        List<ItemStack> items = ingredient.resolveForStacks(context);
                        if (!items.isEmpty()) {
                            slots.add(OverlayRecipeButton.createGridPos(xPos, yPos, items));
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
            }
        }
        if (inputCount == 1) {
            //Re-use vanilla's single input smelting sprites
            enabledSprite = OverlaySmeltingRecipeButton.ENABLED_SPRITE;
            highlightedEnabledSprite = OverlaySmeltingRecipeButton.ENABLED_SPRITE;
            disabledSprite = OverlaySmeltingRecipeButton.DISABLED_SPRITE;
            highlightedDisabledSprite = OverlaySmeltingRecipeButton.HIGHLIGHTED_DISABLED_SPRITE;
        } else if (inputCount == 2 && false) {
            //TODO - 26.3: Switch this out with custom ones
        } else if (inputCount == 3 && false) {
            //TODO - 26.3: Switch this out with custom ones
        } else {//Fallback to vanilla crafting
            enabledSprite = OverlayCraftingRecipeButton.ENABLED_SPRITE;
            highlightedEnabledSprite = OverlayCraftingRecipeButton.ENABLED_SPRITE;
            disabledSprite = OverlayCraftingRecipeButton.DISABLED_SPRITE;
            highlightedDisabledSprite = OverlayCraftingRecipeButton.HIGHLIGHTED_DISABLED_SPRITE;
        }
        //TODO: These three should be proper fields as well (which is why they aren't joined with their assignment)
        final List<OverlayRecipeButton.Pos> items;
        final List<ChemicalPos> chemicals;
        final List<FluidPos> fluids;
        items = List.copyOf(slots);
        chemicals = List.copyOf(chemicalTanks);
        fluids = List.copyOf(fluidTanks);
        //Pass no items so that super doesn't try to render them, and we can instead render them with a count if necessary
        return parent.new OverlayRecipeButton(x, y, id, isCraftable, Collections.emptyList()) {
            @Override
            public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
                float gridPosX = getX() + 2;
                float gridPosY = getY() + 2;
                Font font = fontSupplier.get();
                //Render here instead of in super so that we can support rendering stack size
                for (Pos pos : items) {
                    graphics.pose().pushMatrix();
                    graphics.pose().translate(gridPosX + pos.x(), gridPosY + pos.y());
                    graphics.pose().scale(OverlayRecipeComponent.ITEM_RENDER_SCALE, OverlayRecipeComponent.ITEM_RENDER_SCALE);
                    graphics.pose().translate(-8.0F, -8.0F);
                    ItemStack stack = pos.selectIngredient(parent.slotSelectTime.currentIndex());
                    graphics.item(stack, 0, 0);
                    graphics.itemCount(font, stack, 0, 0, null);
                    graphics.pose().popMatrix();
                }
                for (FluidPos pos : fluids) {
                    FluidStack fluidStack = pos.selectIngredient(parent.slotSelectTime.currentIndex());
                    extractSpriteState(graphics, font, pos.x, pos.y, gridPosX, gridPosY, MekanismRenderer.getFluidTexture(fluidStack, FluidTextureType.STILL),
                          MekanismRenderer.getColorARGB(fluidStack), fluidStack.amount());
                }
                for (ChemicalPos pos : chemicals) {
                    ChemicalStack chemicalStack = pos.selectIngredient(parent.slotSelectTime.currentIndex());
                    extractSpriteState(graphics, font, pos.x, pos.y, gridPosX, gridPosY, MekanismRenderer.getChemicalTexture(chemicalStack), MekanismRenderer.color(chemicalStack),
                          chemicalStack.amount());
                }
            }

            private void extractSpriteState(GuiGraphicsExtractor graphics, Font font, int posX, int posY, float gridPosX, float gridPosY, TextureAtlasSprite sprite,
                  int tint, int amount) {
                graphics.pose().pushMatrix();
                graphics.pose().translate(gridPosX + posX, gridPosY + posY);
                graphics.pose().scale(OverlayRecipeComponent.ITEM_RENDER_SCALE, OverlayRecipeComponent.ITEM_RENDER_SCALE);
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
        };
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