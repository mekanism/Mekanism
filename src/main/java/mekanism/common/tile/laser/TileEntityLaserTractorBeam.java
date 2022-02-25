package mekanism.common.tile.laser;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
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
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityLaserTractorBeam extends TileEntityLaserReceptor {

    public TileEntityLaserTractorBeam(BlockPos pos, BlockState state) {
        super(MekanismBlocks.LASER_TRACTOR_BEAM, pos, state);
    }

    @Override
    protected void addInitialEnergyContainers(EnergyContainerHelper builder, IContentsListener listener) {
        builder.addContainer(energyContainer = LaserEnergyContainer.create(BasicEnergyContainer.notExternal, BasicEnergyContainer.internalOnly, this, listener));
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
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
    protected void handleBreakBlock(BlockState state, BlockPos hitPos) {
        List<ItemStack> drops = Block.getDrops(state, (ServerLevel) level, hitPos, WorldUtils.getTileEntity(level, hitPos));
        if (!drops.isEmpty()) {
            List<IInventorySlot> inventorySlots = getInventorySlots(null);
            for (ItemStack drop : drops) {
                for (IInventorySlot slot : inventorySlots) {
                    drop = slot.insertItem(drop, Action.EXECUTE, AutomationType.INTERNAL);
                    if (drop.isEmpty()) {
                        //If we inserted it all, then break otherwise try to insert the remainder into another slot
                        break;
                    }
                }
                if (!drop.isEmpty()) {
                    //If we have some drop left over that we couldn't fit, then spawn it into the world
                    Block.popResource(level, worldPosition, drop);
                }
            }
        }
    }

    @Override
    protected boolean handleHitItem(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        for (IInventorySlot slot : getInventorySlots(null)) {
            stack = slot.insertItem(stack, Action.EXECUTE, AutomationType.INTERNAL);
            if (stack.isEmpty()) {
                //If we inserted it all, then break otherwise try to insert the remainder into another slot
                break;
            }
        }
        if (stack.isEmpty()) {
            //If we have finished grabbing it all then remove the entity
            entity.discard();
        }
        return true;
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private int getSlotCount() {
        return getSlots();
    }

    @ComputerMethod
    private ItemStack getItemInSlot(int slot) throws ComputerException {
        int slots = getSlotCount();
        if (slot < 0 || slot >= slots) {
            throw new ComputerException("Slot: '%d' is out of bounds, as this laser amplifier only has '%d' slots (zero indexed).", slot, slots);
        }
        return getStackInSlot(slot);
    }
    //End methods IComputerTile
}