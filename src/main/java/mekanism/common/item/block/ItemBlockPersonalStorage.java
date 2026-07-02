package mekanism.common.item.block;

import java.util.Collections;
import java.util.List;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockPersonalStorage;
import mekanism.common.inventory.container.item.PersonalStorageItemContainer;
import mekanism.common.item.interfaces.IDroppableContents;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.lib.inventory.personalstorage.AbstractPersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.lib.security.ItemSecurityUtils;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ItemBlockPersonalStorage<BLOCK extends BlockPersonalStorage<?, ?>> extends ItemBlockTooltip<BLOCK> implements IDroppableContents, IGuiItem {

    private final Identifier openStat;

    public ItemBlockPersonalStorage(BLOCK block, Item.Properties properties, Identifier openStat) {
        super(block, true, properties);
        this.openStat = openStat;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        return ItemSecurityUtils.get().claimOrOpenGui(world, player, hand, (p, h, itemAccess, transaction) -> {
            if (PersonalStorageManager.getInventoryFor(itemAccess, transaction) == null) {
                p.sendOverlayMessage(MekanismLang.STORAGE_ACCESS_FAIL.translateColored(EnumColor.RED));
            }
            getContainerType().tryOpenGui(p, h, itemAccess);
            p.awardStat(Stats.CUSTOM.get(openStat));
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        //TODO - 26.2: Theoretically a datapack could make it food by adding a component, so we may want to check if it is?
        //Like super.onItemUse, except we validate the player is not null, and pass the onItemRightClick regardless of if
        // we are consumable or not (as we know the personal chest is never food). This allows us to open the personal chest's
        // GUI if we didn't interact with a block that caused something to happen like opening a GUI.
        InteractionResult result = place(new BlockPlaceContext(context));
        Player player = context.getPlayer();
        return result.consumesAction() || player == null ? result : use(context.getLevel(), player, context.getHand());
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        Player player = context.getPlayer();
        //Only allow placing if there is no player, it is a fake player, or the player is sneaking
        return (player == null || player.isFakePlayer() || player.isShiftKeyDown()) && super.canPlace(context, state);
    }

    @Override
    public ContainerTypeRegistryObject<PersonalStorageItemContainer> getContainerType() {
        return MekanismContainerTypes.PERSONAL_STORAGE_ITEM;
    }

    @Override
    public void onDestroyed(ItemEntity item, DamageSource damageSource) {
        super.onDestroyed(item, damageSource);
        if (!item.level().isClientSide()) {
            ItemStack stack = item.getItem();
            ItemAccess itemAccess = ItemAccess.forStack(stack);
            //If the inventory was actually empty we can prune the data from the storage manager
            // (if it isn't empty we want to persist it so that server admins can recover their items)
            try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                AbstractPersonalStorageItemInventory inventory = PersonalStorageManager.getInventoryIfPresent(itemAccess, transaction);
                if (inventory != null && ResourceHandlerUtil.isEmpty(inventory)) {
                    PersonalStorageManager.deleteInventory(itemAccess, transaction);
                    transaction.commit();
                }
            }
        }
    }

    @Override
    public List<LargeResourceStack<ItemResource>> getDroppedSlots(ItemAccess itemAccess, TransactionContext transaction) {
        AbstractPersonalStorageItemInventory itemInventory = PersonalStorageManager.getInventoryIfPresent(itemAccess, transaction);
        if (itemInventory == null) {
            return Collections.emptyList();
        }
        return itemInventory.getNonEmptyContents();
    }

    @Override
    public int getScalar(ItemAccess itemAccess) {
        //If for some reason a personal storage block is destroyed that has an inventory and is stacked
        // we only want to drop one of the backing item
        return 1;
    }
}