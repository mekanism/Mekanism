package mekanism.common.tile.laser;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class TileEntityLaserTractorBeam extends TileEntityLaserReceptor {

    public TileEntityLaserTractorBeam() {
        super(MekanismBlocks.LASER_TRACTOR_BEAM);
    }

    @Override
    protected void addInitialEnergyContainers(EnergyContainerHelper builder) {
        builder.addContainer(energyContainer = LaserEnergyContainer.create(BasicEnergyContainer.notExternal, BasicEnergyContainer.internalOnly, this));
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        for (int slotX = 0; slotX < 9; slotX++) {
            for (int slotY = 0; slotY < 3; slotY++) {
                OutputInventorySlot slot = OutputInventorySlot.at(this, 8 + slotX * 18, 16 + slotY * 18);
                builder.addSlot(slot);
                slot.setSlotType(ContainerSlotType.NORMAL);
            }
        }
        return builder.build();
    }

    @Override
    protected void handleBreakBlock(BlockState state, BlockPos hitPos) {
        List<ItemStack> drops = Block.getDrops(state, (ServerWorld) world, hitPos, WorldUtils.getTileEntity(world, hitPos));
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
                    Block.spawnAsEntity(world, pos, drop);
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
            entity.remove();
        }
        return true;
    }
}