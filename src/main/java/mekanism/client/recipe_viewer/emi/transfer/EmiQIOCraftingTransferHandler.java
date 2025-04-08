package mekanism.client.recipe_viewer.emi.transfer;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.recipe_viewer.QIOCraftingTransferHandler;
import mekanism.client.recipe_viewer.QIOCraftingTransferHandler.RVRecipeInfo;
import mekanism.client.recipe_viewer.QIOCraftingTransferHandler.RVRecipeSlot;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EmiQIOCraftingTransferHandler<CONTAINER extends QIOItemViewerContainer> implements EmiRecipeHandler<CONTAINER> {

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<CONTAINER> screen) {
        //TODO - 1.20.4: ?? Do we need to somehow implement this in order to support the craftables view in Emi?
        return new EmiPlayerInventory(Collections.emptyList());
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        if (recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree()) {
            RecipeHolder<?> backingRecipe = recipe.getBackingRecipe();
            //TODO - 1.20.4: Can we expand upon this to support others as long as their category is crafting like we return in supports recipe??
            // For example for special recipes like shulker box coloring https://github.com/emilyploszaj/emi/issues/487
            return backingRecipe != null && backingRecipe.value() instanceof CraftingRecipe;
        }
        return false;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<CONTAINER> context) {
        EmiRecipeInfo recipeInfo = EmiRecipeInfo.create(recipe, context);
        return recipeInfo != null && QIOCraftingTransferHandler.transferRecipe(recipeInfo, Action.SIMULATE) == null;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<CONTAINER> context) {
        EmiRecipeInfo recipeInfo = EmiRecipeInfo.create(recipe, context);
        if (recipeInfo != null && QIOCraftingTransferHandler.transferRecipe(recipeInfo, Action.EXECUTE) == null) {
            //Note: We are expected to handle switching back to the backing screen
            Minecraft.getInstance().setScreen(context.getScreen());
            return true;
        }
        return false;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<CONTAINER> context) {
        EmiRecipeInfo recipeInfo = EmiRecipeInfo.create(recipe, context);
        if (recipeInfo != null) {
            TransferResult transferResult = QIOCraftingTransferHandler.transferRecipe(recipeInfo, Action.SIMULATE);
            if (transferResult != null && transferResult.tooltip() != null) {
                return List.of(EmiTooltipComponents.of(transferResult.tooltip()));
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<CONTAINER> context, List<Widget> widgets, GuiGraphics graphics) {
        //Based on StandardRecipeHandler#renderMissing, except with our own logic for determining what ingredients are missing
        EmiRecipeInfo recipeInfo = EmiRecipeInfo.create(recipe, context);
        if (recipeInfo != null) {
            TransferResult transferResult = QIOCraftingTransferHandler.transferRecipe(recipeInfo, Action.SIMULATE);
            if (transferResult != null && transferResult.missingSlots() != null) {
                RenderSystem.enableDepthTest();
                Object2IntMap<EmiIngredient> missingIngredients = new Object2IntOpenHashMap<>(transferResult.missingSlots().size());
                for (EmiRecipeSlot missingSlot : transferResult.missingSlots()) {
                    missingIngredients.mergeInt(missingSlot.ingredient(), 1, Integer::sum);
                }
                for (Widget w : widgets) {
                    if (w instanceof SlotWidget sw && sw.getRecipe() == null) {
                        EmiIngredient stack = sw.getStack();
                        if (!stack.isEmpty()) {
                            int numberMissing = missingIngredients.getOrDefault(stack, 0);
                            if (numberMissing > 0) {
                                Bounds bounds = sw.getBounds();
                                graphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), 0x44FF0000);
                                if (numberMissing == 1) {
                                    missingIngredients.removeInt(stack);
                                } else {
                                    missingIngredients.put(stack, numberMissing - 1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private record EmiRecipeInfo(
          EmiCraftContext<? extends QIOItemViewerContainer> context, RecipeHolder<CraftingRecipe> recipeHolder, List<EmiRecipeSlot> inputs
    ) implements RVRecipeInfo<TransferResult, EmiRecipeSlot, EmiStack> {

        @Nullable
        @SuppressWarnings("unchecked")
        private static EmiRecipeInfo create(EmiRecipe recipe, EmiCraftContext<? extends QIOItemViewerContainer> context) {
            //TODO - 1.20.4: Support the context's destination thing?
            RecipeHolder<?> backingRecipe = recipe.getBackingRecipe();
            if (backingRecipe == null || !(backingRecipe.value() instanceof CraftingRecipe)) {
                //Note: Theoretically this is always true as we checked it in supportsRecipe, but validate it here just in case
                return null;
            }
            return new EmiRecipeInfo(context, (RecipeHolder<CraftingRecipe>) backingRecipe, recipe.getInputs().stream().map(EmiRecipeSlot::new).toList());
        }

        @Override
        public QIOItemViewerContainer container() {
            return context.getScreenHandler();
        }

        @Override
        public int transferAmount() {
            return context.getAmount();
        }

        @Override
        public TransferResult createInternalError() {
            return TransferResult.INTERNAL_ERROR;
        }

        @Override
        public TransferResult createNoRoomError() {
            return new TransferResult(MekanismLang.RECIPE_VIEWER_INVENTORY_FULL.translate(), null);
        }

        @Override
        public TransferResult createMissingSlotsError(List<EmiRecipeSlot> missing) {
            return new TransferResult(NOT_ENOUGH_INGREDIENTS, missing);
        }

        @Override
        public Player player() {
            return Minecraft.getInstance().player;
        }

        @Override
        public EmiStack itemUUID(HashedItem hashed) {
            //TODO - 1.20.4: Evaluate this
            //Note: ItemEmiStack copies the passed in stack before doing anything to it, so we can safely just pass the internal stack
            // and let it get copied
            return EmiStack.of(hashed.getInternalStack(), 1);
        }
    }

    private record EmiRecipeSlot(EmiIngredient ingredient, List<ItemStack> itemStacks) implements RVRecipeSlot {

        private EmiRecipeSlot(EmiIngredient ingredient) {
            this(ingredient, ingredient.getEmiStacks().stream()
                  .map(EmiStack::getItemStack)
                  .filter(stack -> !stack.isEmpty())
                  .toList());
        }

        @Override
        public ItemStack displayedIngredient() {
            if (itemStacks.isEmpty()) {
                return ItemStack.EMPTY;
            }
            //TODO - 1.20.4: ?? Improve/fix implementation of this?? such as for a ListIngredient to properly cycle?
            return itemStacks.getFirst();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return ingredient.equals(((EmiRecipeSlot) o).ingredient);
        }

        @Override
        public int hashCode() {
            return ingredient.hashCode();
        }
    }

    private record TransferResult(@Nullable Component tooltip, @Nullable List<EmiRecipeSlot> missingSlots) {

        private static final TransferResult INTERNAL_ERROR = new TransferResult(null, null);
    }
}