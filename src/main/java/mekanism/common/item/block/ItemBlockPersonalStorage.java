package mekanism.common.item.block;

import java.util.Collections;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.block.BlockPersonalStorage;
import mekanism.common.inventory.container.item.PersonalStorageItemContainer;
import mekanism.common.item.interfaces.IDroppableContents;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.lib.inventory.personalstorage.AbstractPersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.lib.security.ItemSecurityUtils;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ItemBlockPersonalStorage<BLOCK extends BlockPersonalStorage<?, ?>> extends ItemBlockTooltip<BLOCK> implements IDroppableContents, IGuiItem {

    private final ResourceLocation openStat;

    public ItemBlockPersonalStorage(BLOCK block, Item.Properties properties, ResourceLocation openStat) {
        super(block, true, properties);
        this.openStat = openStat;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        return ItemSecurityUtils.get().claimOrOpenGui(world, player, hand, (p, h, s) -> {
            if (!world.isClientSide) {
                PersonalStorageManager.getInventoryFor(s);
            }
            getContainerType().tryOpenGui(p, h, s);
            p.awardStat(Stats.CUSTOM.get(openStat));
        });
    }

    @NotNull
    @Override
    public InteractionResult useOn(@NotNull UseOnContext context) {
        //Like super.onItemUse, except we validate the player is not null, and pass the onItemRightClick regardless of if
        // we are food or not (as we know the personal chest is never food). This allows us to open the personal chest's
        // GUI if we didn't interact with a block that caused something to happen like opening a GUI.
        InteractionResult result = place(new BlockPlaceContext(context));
        Player player = context.getPlayer();
        return result.consumesAction() || player == null ? result : use(context.getLevel(), player, context.getHand()).getResult();
    }

    @Override
    protected boolean canPlace(@NotNull BlockPlaceContext context, @NotNull BlockState state) {
        Player player = context.getPlayer();
        //Only allow placing if there is no player, it is a fake player, or the player is sneaking
        return (player == null || player.isFakePlayer() || player.isShiftKeyDown()) && super.canPlace(context, state);
    }

    @Override
    public ContainerTypeRegistryObject<PersonalStorageItemContainer> getContainerType() {
        return MekanismContainerTypes.PERSONAL_STORAGE_ITEM;
    }

    @Override
    public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
        super.onDestroyed(item, damageSource);
        if (!item.level().isClientSide) {
            ItemStack stack = item.getItem();
            AbstractPersonalStorageItemInventory inventory = PersonalStorageManager.getInventoryIfPresent(stack).orElse(null);
            if (inventory != null && inventory.isInventoryEmpty()) {
                //If the inventory was actually empty we can prune the data from the storage manager
                // (if it isn't empty we want to persist it so that server admins can recover their items)
                PersonalStorageManager.deleteInventory(stack);
            }
        }
    }

    @Override
    public List<IInventorySlot> getDroppedSlots(ItemStack stack) {
        return PersonalStorageManager.getInventoryIfPresent(stack)
              .map(inventory -> inventory.getInventorySlots(null))
              .orElse(Collections.emptyList());
    }

    @Override
    public int getScalar(ItemStack stack) {
        //If for some reason a personal storage block is destroyed that has an inventory and is stacked
        // we only want to drop one of the backing item
        return 1;
    }
}