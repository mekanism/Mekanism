package mekanism.common.tile.laser;

import java.util.List;
import mekanism.api.Action;
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
            List<IInventorySlot> inventorySlots = getInventorySlots(null);
            for (ItemStack drop : drops) {
                //Try inserting it first where it can stack and then into empty slots
                drop = InventoryUtils.insertItem(inventorySlots, drop, Action.EXECUTE, AutomationType.INTERNAL);
                if (!drop.isEmpty()) {
                    //If we have some drop left over that we couldn't fit, then spawn it into the world
                    // Note: We use an adjusted position and an opposite direction to provide the item with momentum towards the tractor beam
                    // so that even though we couldn't fit the items into our inventory we can still have them appear to be "pulled" to the tractor beam
                    if (dropPos == null) {
                        Direction direction = getDirection();
                        dropPos = worldPosition.relative(direction, 2);
                        opposite = direction.getOpposite();
                    }
                    Block.popResourceFromFace(level, dropPos, opposite, drop);
                }
            }
        }
    }

    @Override
    protected boolean handleHitItem(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        //Try inserting it first where it can stack and then into empty slots
        stack = InventoryUtils.insertItem(getInventorySlots(null), stack, Action.EXECUTE, AutomationType.INTERNAL);
        if (stack.isEmpty()) {
            //If we have finished grabbing it all then remove the entity
            entity.discard();
        }
        return true;
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    int getSlotCount() {
        return getSlots();
    }

    @ComputerMethod
    ItemStack getItemInSlot(int slot) throws ComputerException {
        int slots = getSlotCount();
        if (slot < 0 || slot >= slots) {
            throw new ComputerException("Slot: '%d' is out of bounds, as this laser amplifier only has '%d' slots (zero indexed).", slot, slots);
        }
        return getStackInSlot(slot);
    }
    //End methods IComputerTile
}