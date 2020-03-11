package mekanism.common.tile.laser;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.LaserManager;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class TileEntityLaserTractorBeam extends TileEntityLaserReceptor {

    public TileEntityLaserTractorBeam() {
        super(MekanismBlocks.LASER_TRACTOR_BEAM);
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
    protected void handleBreakBlock(Coord4D coord) {
        List<ItemStack> drops = LaserManager.breakBlock(coord, false, world, pos);
        if (drops != null) {
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
                    Block.spawnAsEntity(getWorld(), pos, drop);
                }
            }
        }
    }
}