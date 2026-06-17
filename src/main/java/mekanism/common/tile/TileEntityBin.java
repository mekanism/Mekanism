package mekanism.common.tile;

import java.util.Objects;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.component.LockData;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.lib.inventory.HandlerTransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.upgrade.BinUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class TileEntityBin extends TileEntityMekanism implements IConfigurable {

    @Nullable
    private BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction> targetInventory;
    public int addTicks = 0;
    public int removeTicks = 0;
    private int delayTicks;
    private boolean needsSync;
    private final BinTier tier;

    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getStored", docPlaceholder = "bin")
    BinInventorySlot binSlot;

    public TileEntityBin(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        tier = Objects.requireNonNull(Attribute.getTierNN(blockProvider, BinTier.class));
        super(blockProvider, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(binSlot = BinInventorySlot.create(listener, tier));
        return builder.build();
    }

    public BinTier getTier() {
        return tier;
    }

    public BinInventorySlot getBinSlot() {
        return binSlot;
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        addTicks = Math.max(0, addTicks - 1);
        removeTicks = Math.max(0, removeTicks - 1);
        delayTicks = Math.max(0, delayTicks - 1);
        if (delayTicks == 0) {
            if (getActive() && !binSlot.isEmpty()) {
                //Note: We can't just pass "this" and have to instead look up the capability to make sure we respect any sidedness
                // we short circuit looking it up from the world though, and just query the provider we add to the tile directly
                ResourceHandler<ItemResource> capability = ITEM_HANDLER_PROVIDER.getCapability(this, Direction.DOWN);
                HandlerTransitRequest request = new HandlerTransitRequest(capability);
                //Note: Instead of getting the bin item type, we just get the stored resource as we only do things if it isn't empty anyway
                ItemResource storedType = binSlot.resource();
                //Limit how much we allow sending at once to a single stack of the stored item
                request.addItem(storedType, Math.min(binSlot.amountAsInt(), storedType.getMaxStackSize()), 0);
                if (targetInventory == null) {
                    targetInventory = Capabilities.ITEM.createCache(level, getBlockPos().below(), Direction.UP);
                }
                try (Transaction transaction = Transaction.openRoot()) {
                    TransitResponse response = request.eject(this, level, targetInventory.getCapability(), 1, null, transaction);
                    if (response.useAll(transaction)) {
                        transaction.commit();
                    }
                }
                delayTicks = MekanismUtils.TICKS_PER_HALF_SECOND;
            }
        } else {
            delayTicks--;
        }
        if (needsSync) {
            sendUpdatePacket = true;
            needsSync = false;
        }
        return sendUpdatePacket;
    }

    @Override
    public InteractionResult onSneakRightClick(Level level, Player player) {
        setActive(!getActive());
        level.playSound(null, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.3F, 1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onRightClick(Level level, Player player) {
        return InteractionResult.PASS;
    }

    public boolean toggleLock() {
        return setLocked(!binSlot.isLocked());
    }

    public boolean setLocked(boolean isLocked) {
        if (binSlot.setLocked(isLocked)) {
            if (level != null && !level.isClientSide()) {
                needsSync = true;
                markForSave();
                level.playSound(null, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.3F, 1);
            }
            return true;
        }
        return false;
    }

    @Override
    public void parseUpgradeData(IUpgradeData upgradeData, Provider provider, TransactionContext transaction) {
        if (upgradeData instanceof BinUpgradeData(boolean redstoneData, BinInventorySlot slot)) {
            redstone = redstoneData;
            binSlot.copyContents(slot, transaction);
        } else {
            super.parseUpgradeData(upgradeData, provider, transaction);
        }
    }

    @Override
    public BinUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new BinUpgradeData(redstone, getBinSlot());
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (level != null && !level.isClientSide()) {
            needsSync = true;
        }
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        output.putChild(SerializationConstants.ITEM, binSlot);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);
        input.readChild(SerializationConstants.ITEM, binSlot);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        //Note: In theory doing this before super doesn't matter, but we want to make sure that the lock is set before
        // setting the data on the item just for good measure
        builder.set(MekanismDataComponents.LOCK, LockData.create(binSlot.getLockType()));
        super.collectImplicitComponents(builder);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
        //Apply the lock before processing the stored data
        binSlot.setLockType(input.getOrDefault(MekanismDataComponents.LOCK, LockData.EMPTY).lock(), null);
        super.applyImplicitComponents(input);
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = "Get the maximum number of items the bin can contain.")
    long getCapacity() {
        return binSlot.capacityAsLong(binSlot.resource());
    }

    @ComputerMethod(methodDescription = "If true, the Bin is locked to a particular item type.")
    boolean isLocked() {
        return binSlot.isLocked();
    }

    @ComputerMethod(methodDescription = "Get the type of item the Bin is locked to (or Air if not locked)")
    ItemResource getLock() {
        return binSlot.getLockType();
    }

    @ComputerMethod(methodDescription = "Lock the Bin to the currently stored item type. The Bin must not be creative, empty, or already locked")
    void lock() throws ComputerException {
        if (getTier() == BinTier.CREATIVE) {
            throw new ComputerException("Creative bins cannot be locked!");
        } else if (binSlot.isEmpty()) {
            throw new ComputerException("Empty bins cannot be locked!");
        } else if (!setLocked(true)) {
            throw new ComputerException("This bin is already locked!");
        }
    }

    @ComputerMethod(methodDescription = "Unlock the Bin's fixed item type. The Bin must not be creative, or already unlocked")
    void unlock() throws ComputerException {
        if (getTier() == BinTier.CREATIVE) {
            throw new ComputerException("Creative bins cannot be unlocked!");
        } else if (!setLocked(true)) {
            throw new ComputerException("This bin is not locked!");
        }
    }
    //End methods IComputerTile
}
