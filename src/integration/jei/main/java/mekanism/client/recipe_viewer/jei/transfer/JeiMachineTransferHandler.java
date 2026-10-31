package mekanism.client.recipe_viewer.jei.transfer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.AutomationType;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.client.recipe_viewer.RVTransferUtils;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.recipetransfer.RecipePlacementHelper;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketTransferMekanismRecipe;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.RecipeContainers;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferError.Type;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public abstract class JeiMachineTransferHandler<CONTAINER extends MekanismContainer, RECIPE extends MekanismRecipe<?, ?, ?>> implements IRecipeTransferHandler<CONTAINER, RecipeHolder<RECIPE>> {

    /// Helper marker
    private static final IRecipeTransferError SUCCESS = () -> Type.INTERNAL;
    /// Helper marker
    private static final IRecipeTransferError MISSING_SLOTS = () -> Type.INTERNAL;

    private final IRecipeType<RecipeHolder<RECIPE>> recipeType;
    protected final IRecipeTransferHandlerHelper transferHelper;
    private final Class<CONTAINER> containerClass;
    private final MenuType<CONTAINER> menuType;

    public JeiMachineTransferHandler(IRecipeTransferHandlerHelper transferHelper, Class<CONTAINER> containerClass, MenuType<CONTAINER> menuType, IRecipeViewerRecipeType<RECIPE> recipeTypeProvider) {
        this.menuType = menuType;
        this.containerClass = containerClass;
        this.transferHelper = transferHelper;
        this.recipeType = MekanismJEI.holderRecipeType(recipeTypeProvider);
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
    public IRecipeType<RecipeHolder<RECIPE>> getRecipeType() {
        return recipeType;
    }

    @Nullable
    protected abstract IRecipeTransferError transferRecipe(RecipeContainers recipeContainers, RECIPE recipe, JeiPlacementHelper helper, TransactionContext transaction,
          AutomationType automationType);

    @Nullable
    @Override
    @Deprecated(since = "JEI 30.30.0", forRemoval = true)
    public final IRecipeTransferError transferRecipe(CONTAINER container, RecipeHolder<RECIPE> recipeHolder, IRecipeSlotsView recipeSlots, Player player,
          boolean maxTransfer, boolean doTransfer) {
        RecipeContainers recipeContainers = container.getRecipeContainers();
        if (recipeContainers == null) {
            Mekanism.logger.error("Unable to find recipe input for container of type: {}", BuiltInRegistries.MENU.getKey(menuType));
            return transferHelper.createInternalError();
        }
        try (Transaction ignored = TransactionHelper.openTransactionSafe()) {
            List<IRecipeSlotView> inputSlots = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
            JeiPlacementHelper helper = new JeiPlacementHelper(player, maxTransfer, inputSlots.size(), recipeHolder.id());
            IRecipeTransferError error = transferRecipe(recipeContainers, recipeHolder.value(), helper, ignored, AutomationType.MANUAL);
            if (error == SUCCESS) {
                if (doTransfer) {
                    helper.sendTransferPacket(-1, null);
                }
                return null;
            } else if (error == MISSING_SLOTS) {
                if (helper.missingSlots.isEmpty()) {
                    Mekanism.logger.error("Recipe transfer for {} returned that there were missing inputs, but somehow didn't specify which inputs.", recipeHolder.id().identifier());
                    return transferHelper.createInternalError();
                }
                return transferHelper.createUserErrorForMissingSlots(MekanismLang.JEI_MISSING_ITEMS.translate(), helper.missingSlots.intStream().mapToObj(inputSlots::get).toList());
            }
            return error;
        }
    }

    @Nullable
    @Override
    public final IRecipeTransferError transferRecipe(IRecipeTransferContext<RecipeHolder<RECIPE>, CONTAINER> context, boolean doTransfer) {
        IRecipeTransferError error = tryTransferRecipe(context, doTransfer);
        if (error != null && doTransfer) {
            context.completeRecipeTransfer(RecipeTransferResult.REJECTED);
        }
        return error;
    }

    @Nullable
    private IRecipeTransferError tryTransferRecipe(IRecipeTransferContext<RecipeHolder<RECIPE>, CONTAINER> context, boolean doTransfer) {
        RecipeContainers recipeContainers = context.getContainer().getRecipeContainers();
        if (recipeContainers == null) {
            Mekanism.logger.error("Unable to find recipe input for container of type: {}", BuiltInRegistries.MENU.getKey(menuType));
            return transferHelper.createInternalError();
        }
        try (Transaction ignored = TransactionHelper.openTransactionSafe()) {
            List<IRecipeSlotView> inputSlots = context.getRecipeSlots().getSlotViews(RecipeIngredientRole.INPUT);
            JeiPlacementHelper helper = new JeiPlacementHelper(context.getPlayer(), context.isMaxTransfer(), inputSlots.size(), context.getRecipe().id());
            IRecipeTransferError error = transferRecipe(recipeContainers, context.getRecipe().value(), helper, ignored, AutomationType.MANUAL);
            if (error == SUCCESS) {
                if (doTransfer) {
                    helper.sendTransferPacket(context.getTransferId(),
                          successful -> context.completeRecipeTransfer(successful ? RecipeTransferResult.SUCCESS : RecipeTransferResult.REJECTED));
                }
                return null;
            } else if (error == MISSING_SLOTS) {
                if (helper.missingSlots.isEmpty()) {
                    Mekanism.logger.error("Recipe transfer for {} returned that there were missing inputs, but somehow didn't specify which inputs.", context.getRecipe().id().identifier());
                    return transferHelper.createInternalError();
                }
                return transferHelper.createUserErrorForMissingSlots(MekanismLang.JEI_MISSING_ITEMS.translate(), helper.missingSlots.intStream().mapToObj(inputSlots::get).toList());
            }
            if (error == null && doTransfer) {
                context.completeRecipeTransfer(RecipeTransferResult.SUCCESS);
            }
            return error;
        }
    }

    protected class JeiPlacementHelper implements RecipePlacementHelper<@Nullable IRecipeTransferError> {

        private final ResourceHandler<ItemResource> mainSlots;
        private final ResourceKey<Recipe<?>> recipeKey;
        private final boolean useMaxItems;
        private final int totalSlots;
        private Int2ObjectMap<ResourceStack<ItemResource>> itemsTransferred = Int2ObjectMaps.emptyMap();
        private IntList missingSlots = IntLists.EMPTY_LIST;

        private JeiPlacementHelper(Player player, boolean useMaxItems, int totalSlots, ResourceKey<Recipe<?>> recipeKey) {
            this.mainSlots = PlayerInventoryWrapper.of(player).getMainSlots();
            this.useMaxItems = useMaxItems;
            this.totalSlots = totalSlots;
            this.recipeKey = recipeKey;
        }

        @Override
        public ResourceHandler<ItemResource> itemHandler() {
            return mainSlots;
        }

        @Nullable
        @Override
        public IRecipeTransferError successNoAction() {
            return null;
        }

        @Override
        public IRecipeTransferError error() {
            if (!missingSlots.isEmpty()) {
                return MISSING_SLOTS;
            }
            return transferHelper.createInternalError();
        }

        @Override
        public IRecipeTransferError success() {
            return SUCCESS;
        }

        @Override
        public IRecipeTransferError missingItems(int slot) {
            if (slot >= 0 && slot < totalSlots) {
                if (missingSlots.isEmpty()) {
                    missingSlots = new IntArrayList();
                }
                missingSlots.add(slot);
            }
            return MISSING_SLOTS;
        }

        @Override
        public IRecipeTransferError notEnoughRoom() {
            return transferHelper.createUserErrorWithTooltip(MekanismLang.JEI_INVENTORY_FULL.translate());
        }

        @Override
        public IRecipeTransferError unableToClearInput(int slot) {
            Mekanism.logger.error("Unable to clear recipe slot: {} for jei transfer request of recipe: {}", slot, recipeKey.identifier());
            return transferHelper.createInternalError();
        }

        @Override
        public void markInputUsed(int slotUsedFor, ItemResource resource, int amount) {
            if (itemsTransferred.isEmpty()) {
                itemsTransferred = new Int2ObjectOpenHashMap<>();
            }
            itemsTransferred.put(slotUsedFor, new ResourceStack<>(resource, amount));
        }

        public void sendTransferPacket(int transferId, @Nullable Consumer<Boolean> completion) {
            if (!itemsTransferred.isEmpty()) {
                if (transferId != -1 && completion != null) {
                    RVTransferUtils.registerTransfer(transferId, completion);
                }
                PacketUtils.sendToServer(new PacketTransferMekanismRecipe(itemsTransferred, transferId));
            } else if (completion != null) {
                completion.accept(true);
            }
        }

        @Override
        public boolean useMaxItems() {
            return useMaxItems;
        }
    }
}