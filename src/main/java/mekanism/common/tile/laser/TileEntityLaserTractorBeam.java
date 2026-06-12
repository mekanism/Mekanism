package mekanism.common.tile.laser;

import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class TileEntityLaserTractorBeam extends TileEntityLaserReceptor {

    public TileEntityLaserTractorBeam(BlockPos pos, BlockState state) {
        super(MekanismBlocks.LASER_TRACTOR_BEAM, pos, state);
    }

    @Override
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = LaserEnergyContainer.create(BasicEnergyContainer.notExternal, BasicEnergyContainer.internalOnly, this, listener);
        return _ -> energyContainer;
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        for (int slotX = 0; slotX < 9; slotX++) {
            for (int slotY = 0; slotY < 3; slotY++) {
                OutputInventorySlot slot = OutputInventorySlot.at(listener, 8 + slotX * 18, 16 + slotY * 18);
                builder.addContainer(slot);
                slot.setSlotType(ContainerSlotType.NORMAL);
            }
        }
        return builder.build();
    }

    @Override
    protected void handleBreakBlock(BlockState state, ServerLevel level, BlockPos hitPos, Player player, ItemStack tool, TransactionContext transaction) {
        List<ItemStack> drops = WorldUtils.getDrops(state, level, hitPos, WorldUtils.getTileEntity(level, hitPos), player, tool);
        //Collect any extra drops that might have happened due to say breaking the top part of a door or flower and try to add them
        //Note: Technically we should just always return true rather than relying on the return result of the add method,
        // but as array lists always will return true as they are modified we don't have to worry about that
        CommonWorldTickHandler.fallbackItemCollector = drops::add;
        breakBlock(state, level, hitPos, tool);
        CommonWorldTickHandler.fallbackItemCollector = null;
        if (!drops.isEmpty()) {
            BlockPos dropPos = null;
            Direction opposite = null;
            IMekanismResourceHandler<ItemResource, IInventorySlot> inventoryHandler = this::getInventorySlots;
            for (ItemStack drop : drops) {
                if (drop.isEmpty()) {//Not sure if this can ever be the case, but handle it just in case
                    continue;
                }
                int toInsert = drop.count();
                //Try inserting it first where it can stack and then into empty slots
                int inserted = inventoryHandler.insert(ItemResource.of(drop), toInsert, transaction, AutomationType.INTERNAL);
                if (inserted < toInsert) {
                    //If we have some drop left over that we couldn't fit, then spawn it into the world
                    // Note: We use an adjusted position and an opposite direction to provide the item with momentum towards the tractor beam
                    // so that even though we couldn't fit the items into our inventory we can still have them appear to be "pulled" to the tractor beam
                    if (dropPos == null) {
                        Direction direction = getDirection();
                        dropPos = worldPosition.relative(direction, 2);
                        opposite = direction.getOpposite();
                    }
                    Block.popResourceFromFace(level, dropPos, opposite, drop.copyWithCount(toInsert - inserted));
                }
            }
        }
    }

    @Override
    protected boolean handleHitItem(ItemEntity entity, TransactionContext transaction) {
        ItemStack stack = entity.getItem();
        //Try inserting it first where it can stack and then into empty slots
        IMekanismResourceHandler<ItemResource, IInventorySlot> inventoryHandler = this::getInventorySlots;
        int inserted = inventoryHandler.insert(ItemResource.of(stack), stack.count(), transaction, AutomationType.INTERNAL);
        if (inserted == stack.count()) {
            //If we have finished grabbing it all then remove the entity
            entity.discard();
            return true;
        }
        //If we couldn't fit it all, shrink how much of the item the entity is representing and let it continue processing
        stack.shrink(inserted);
        return super.handleHitItem(entity, transaction);
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    int getSlotCount() {
        return getInventorySlots().size();
    }

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getItemInSlot", docPlaceholder = "amplifier slot")
    IInventorySlot getSlot(int slot) throws ComputerException {
        List<IInventorySlot> slots = getInventorySlots();
        if (slot < 0 || slot >= slots.size()) {
            throw new ComputerException("Slot: '%d' is out of bounds, as this laser amplifier only has '%d' slots (zero indexed).", slot, slots.size());
        }
        return slots.get(slot);
    }
    //End methods IComputerTile
}