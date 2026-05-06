package mekanism.common.tile.laser;

import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.InventoryUtils;
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
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class TileEntityLaserTractorBeam extends TileEntityLaserReceptor {

    public TileEntityLaserTractorBeam(BlockPos pos, BlockState state) {
        super(MekanismBlocks.LASER_TRACTOR_BEAM, pos, state);
    }

    @Override
    protected void addInitialEnergyContainers(EnergyContainerHelper builder, IContentsListener listener) {
        builder.addContainer(energyContainer = LaserEnergyContainer.create(BasicEnergyContainer.notExternal, BasicEnergyContainer.internalOnly, this, listener));
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        for (int slotX = 0; slotX < 9; slotX++) {
            for (int slotY = 0; slotY < 3; slotY++) {
                OutputInventorySlot slot = OutputInventorySlot.at(listener, 8 + slotX * 18, 16 + slotY * 18);
                builder.addSlot(slot);
                slot.setSlotType(ContainerSlotType.NORMAL);
            }
        }
        return builder.build();
    }

    @Override
    protected void handleBreakBlock(BlockState state, ServerLevel level, BlockPos hitPos, Player player, ItemStack tool) {
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
            List<IInventorySlot> inventorySlots = getContainers();
            try (Transaction transaction = Transaction.openRoot()) {
                for (ItemStack drop : drops) {
                    if (drop.isEmpty()) {//Not sure if this can ever be the case, but handle it just in case
                        continue;
                    }
                    int toInsert = drop.count();
                    //Try inserting it first where it can stack and then into empty slots
                    toInsert -= InventoryUtils.insertItem(inventorySlots, ItemResource.of(drop), toInsert, transaction, AutomationType.INTERNAL);
                    if (toInsert > 0) {
                        //If we have some drop left over that we couldn't fit, then spawn it into the world
                        // Note: We use an adjusted position and an opposite direction to provide the item with momentum towards the tractor beam
                        // so that even though we couldn't fit the items into our inventory we can still have them appear to be "pulled" to the tractor beam
                        if (dropPos == null) {
                            Direction direction = getDirection();
                            dropPos = worldPosition.relative(direction, 2);
                            opposite = direction.getOpposite();
                        }
                        Block.popResourceFromFace(level, dropPos, opposite, drop.copyWithCount(toInsert));
                    }
                }
                transaction.commit();
            }
        }
    }

    @Override
    protected boolean handleHitItem(ItemEntity entity) {
        try (Transaction transaction = Transaction.openRoot()) {
            ItemStack stack = entity.getItem();
            //Try inserting it first where it can stack and then into empty slots
            int inserted = InventoryUtils.insertItem(getContainers(), ItemResource.of(stack), stack.count(), transaction, AutomationType.INTERNAL);
            if (inserted == stack.count()) {
                //If we have finished grabbing it all then remove the entity
                entity.discard();
            } else {
                //TODO - 26.1: Validate this, it didn't used to be part of this method but I think it is needed?
                //If we couldn't fit it all, shrink how much of the item the entity is representing
                stack.shrink(inserted);
            }
            transaction.commit();
            return true;
        }
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    int getSlotCount() {
        //TODO - 26.1: This used to just call getSlots, which effectively now would be size, but that seems like an unclear name
        // and we likely will move away from directly implementing IMekanismInventory. Evaluate if this handling will be fine, or if we need to change things
        return getContainers().size();
    }

    //TODO - 26.1: Re-evaluate how we want to handle exposing this to computer integration
    //@WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getItemInSlot", docPlaceholder = "amplifier slot")
    IInventorySlot getSlot(int slot) throws ComputerException {
        int slots = getSlotCount();
        if (slot < 0 || slot >= slots) {
            throw new ComputerException("Slot: '%d' is out of bounds, as this laser amplifier only has '%d' slots (zero indexed).", slot, slots);
        }
        return getContainer(slot);
    }
    //End methods IComputerTile
}