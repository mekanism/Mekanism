package mekanism.common.tile;

import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.inventory.personalstorage.AbstractPersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityPersonalStorage extends TileEntityMekanism {

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
            TileEntityPersonalStorage.this.onOpen(level, pos, state);
        }

        @Override
        protected void onClose(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
            TileEntityPersonalStorage.this.onClose(level, pos, state);
        }

        @Override
        protected void openerCountChanged(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, int oldCount, int openCount) {
            level.blockEvent(pos, state.getBlock(), ChestBlock.EVENT_SET_OPEN_COUNT, openCount);
        }

        @Override
        protected boolean isOwnContainer(@NotNull Player player) {
            return player.containerMenu instanceof MekanismTileContainer<?> container && container.getTileEntity() == TileEntityPersonalStorage.this;
        }
    };

    protected TileEntityPersonalStorage(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        //Note: We always allow manual interaction (even for insertion), as if a player has the GUI open we treat that as they are allowed to interact with it
        // and if the security mode changes we then boot any players who can't interact with it anymore out of the GUI
        //Note: We can just directly pass ourselves as a security object as we know we are present and that we aren't just an owner item
        //Note: we allow access to the slots from all sides as long as it is public, unlike in 1.12 where we always denied the bottom face
        // We did that to ensure that things like hoppers that could check IInventory did not bypass any restrictions
        BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInteract = (stack, automationType) ->
              automationType == AutomationType.MANUAL || ISecurityUtils.INSTANCE.getEffectiveSecurityMode(this, isRemote()) == SecurityMode.PUBLIC;
        PersonalStorageManager.createSlots(builder::addSlot, canInteract, listener);
        return builder.build();
    }

    @Override
    public void open(Player player) {
        super.open(player);
        if (!isRemoved() && !player.isSpectator() && level != null) {
            openersCounter.incrementOpeners(player, level, getBlockPos(), getBlockState());
        }
    }

    @Override
    public void close(Player player) {
        super.close(player);
        if (!isRemoved() && !player.isSpectator() && level != null) {
            openersCounter.decrementOpeners(player, level, getBlockPos(), getBlockState());
        }
    }

    public void recheckOpen() {
        if (!isRemoved() && level != null) {
            openersCounter.recheckOpeners(level, getBlockPos(), getBlockState());
        }
    }

    protected abstract void onOpen(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state);

    protected abstract void onClose(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state);

    protected abstract ResourceLocation getStat();

    @Override
    public InteractionResult openGui(Player player) {
        InteractionResult result = super.openGui(player);
        if (result.consumesAction() && !isRemote()) {
            player.awardStat(Stats.CUSTOM.get(getStat()));
            PiglinAi.angerNearbyPiglins(player, true);
        }
        return result;
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        if (!isRemote()) {
            UUID owner = input.get(MekanismDataComponents.OWNER);
            if (owner != null) {
                AbstractPersonalStorageItemInventory storageItemInventory = PersonalStorageManager.getInventoryForUnchecked(
                      input.get(MekanismDataComponents.PERSONAL_STORAGE_ID), owner).orElse(null);
                if (storageItemInventory != null) {
                    List<IInventorySlot> inventorySlots = storageItemInventory.getInventorySlots(null);
                    for (int i = 0; i < inventorySlots.size(); i++) {
                        setStackInSlot(i, inventorySlots.get(i).getStack().copy());
                    }
                }
            }
        }
    }

    @Override
    public boolean persistsToItem(ContainerType<?, ?, ?> type) {
        return type != ContainerType.ITEM && super.persistsToItem(type);
    }
}