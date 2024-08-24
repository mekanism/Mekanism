package mekanism.client.recipe_viewer.jei;

import java.util.List;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.client.recipe_viewer.QIOCraftingTransferHandler;
import mekanism.client.recipe_viewer.QIOCraftingTransferHandler.RVRecipeInfo;
import mekanism.client.recipe_viewer.QIOCraftingTransferHandler.RVRecipeSlot;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public class JeiQIOCraftingTransferHandler<CONTAINER extends QIOItemViewerContainer> implements IRecipeTransferHandler<CONTAINER, RecipeHolder<CraftingRecipe>> {

    private final IRecipeTransferHandlerHelper handlerHelper;
    private final Class<CONTAINER> containerClass;
    private final MenuType<CONTAINER> menuType;
    private final IStackHelper stackHelper;

    public JeiQIOCraftingTransferHandler(IRecipeTransferHandlerHelper handlerHelper, IStackHelper stackHelper, MenuType<CONTAINER> menuType, Class<CONTAINER> containerClass) {
        this.handlerHelper = handlerHelper;
        this.stackHelper = stackHelper;
        this.menuType = menuType;
        this.containerClass = containerClass;
    }

    @Override
    public Class<CONTAINER> getContainerClass() {
        return containerClass;
    }

    @Override
    public Optional<MenuType<CONTAINER>> getMenuType() {
        return Optional.of(menuType);
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(CONTAINER container, RecipeHolder<CraftingRecipe> recipeHolder, IRecipeSlotsView recipeSlots, Player player,
          boolean maxTransfer, boolean doTransfer) {
        return QIOCraftingTransferHandler.transferRecipe(new JeiRecipeInfo(container, recipeHolder, recipeSlots, player, maxTransfer ? Integer.MAX_VALUE : 1, handlerHelper, stackHelper),
              Action.get(doTransfer));
    }

    private record JeiRecipeInfo(
          QIOItemViewerContainer container, RecipeHolder<CraftingRecipe> recipeHolder, IRecipeSlotsView recipeSlots, Player player, int transferAmount,
          IRecipeTransferHandlerHelper handlerHelper, IStackHelper stackHelper
    ) implements RVRecipeInfo<IRecipeTransferError, JeiRecipeSlot, Object> {

        @Override
        public IRecipeTransferError createInternalError() {
            return handlerHelper.createInternalError();
        }

        @Override
        public IRecipeTransferError createNoRoomError() {
            return handlerHelper.createUserErrorWithTooltip(MekanismLang.JEI_INVENTORY_FULL.translate());
        }

        @Override
        public IRecipeTransferError createMissingSlotsError(List<JeiRecipeSlot> missing) {
            return handlerHelper.createUserErrorForMissingSlots(MekanismLang.JEI_MISSING_ITEMS.translate(), missing.stream().map(JeiRecipeSlot::slotView).toList());
        }

        @Override
        public Object itemUUID(HashedItem hashed) {
            return stackHelper.getUidForStack(hashed.getInternalStack(), UidContext.Recipe);
        }

        @Override
        public List<JeiRecipeSlot> inputs() {
            return recipeSlots.getSlotViews(RecipeIngredientRole.INPUT).stream().map(JeiRecipeSlot::new).toList();
        }
    }

    private record JeiRecipeSlot(IRecipeSlotView slotView) implements RVRecipeSlot {

        @Override
        public List<ItemStack> itemStacks() {
            return slotView.getItemStacks().toList();
        }

        @Override
        public ItemStack displayedIngredient() {
            return slotView.getDisplayedIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY);
        }
    }
}