package mekanism.common.tile;

import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.inventory.personalstorage.AbstractPersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;

public abstract class TileEntityPersonalStorage extends TileEntityMekanism {

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            TileEntityPersonalStorage.this.onOpen(level, pos, state);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            TileEntityPersonalStorage.this.onClose(level, pos, state);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int openCount) {
            level.blockEvent(pos, state.getBlock(), ChestBlock.EVENT_SET_OPEN_COUNT, openCount);
        }

        @Override
        public boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof MekanismTileContainer<?> container && container.getTileEntity() == TileEntityPersonalStorage.this;
        }
    };

    protected TileEntityPersonalStorage(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        //Note: We always allow manual interaction (even for insertion), as if a player has the GUI open we treat that as they are allowed to interact with it
        // and if the security mode changes we then boot any players who can't interact with it anymore out of the GUI
        //Note: We can just directly pass ourselves as a security object as we know we are present and that we aren't just an owner item
        //Note: we allow access to the slots from all sides as long as it is public, unlike in 1.12 where we always denied the bottom face
        // We did that to ensure that things like hoppers that could check IInventory did not bypass any restrictions
        BiPredicate<ItemResource, AutomationType> canInteract = (_, automationType) ->
              automationType.isManual() || level != null && ISecurityUtils.INSTANCE.getEffectiveSecurityMode(this, level.isClientSide()) == SecurityMode.PUBLIC;
        PersonalStorageManager.createSlots(builder::addContainer, canInteract, listener);
        return builder.build();
    }

    @Override
    public void open(Player player) {
        super.open(player);
        if (!isRemoved() && !player.isSpectator() && level != null) {
            openersCounter.incrementOpeners(player, level, getBlockPos(), getBlockState(), player.getContainerInteractionRange());
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

    protected abstract void onOpen(Level level, BlockPos pos, BlockState state);

    protected abstract void onClose(Level level, BlockPos pos, BlockState state);

    protected abstract Identifier getStat();

    @Override
    public InteractionResult openGui(Level level, Player player) {
        InteractionResult result = super.openGui(level, player);
        if (result.consumesAction() && level instanceof ServerLevel serverLevel) {
            player.awardStat(Stats.CUSTOM.get(getStat()));
            PiglinAi.angerNearbyPiglins(serverLevel, player, true);
        }
        return result;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        if (level != null && !level.isClientSide()) {
            UUID owner = input.get(MekanismDataComponents.OWNER);
            if (owner != null) {
                AbstractPersonalStorageItemInventory storageItemInventory = PersonalStorageManager.getInventoryForUnchecked(input.get(MekanismDataComponents.PERSONAL_STORAGE_ID), owner);
                if (storageItemInventory != null) {
                    //TODO - 26.2: Re-evaluate how we interact with our tile's slots
                    List<IInventorySlot> inventorySlots = storageItemInventory.getContainers();
                    List<IInventorySlot> tileSlots = getInventorySlots();
                    if (inventorySlots.size() == tileSlots.size()) {//TODO - 26.2: If they don't match how should we handle it?
                        for (int i = 0, size = inventorySlots.size(); i < size; i++) {
                            tileSlots.get(i).copyContents(inventorySlots.get(i), null);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean persistsToItem(IContainerType<?, ?> type) {
        return type != ContainerType.ITEM && super.persistsToItem(type);
    }
}