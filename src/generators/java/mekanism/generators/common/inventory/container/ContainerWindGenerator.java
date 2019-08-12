package mekanism.generators.common.inventory.container;

import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.entity.player.PlayerInventory;

public class ContainerWindGenerator extends ContainerPassiveGenerator<TileEntityWindGenerator> {

    public ContainerWindGenerator(PlayerInventory inventory, TileEntityWindGenerator generator) {
        super(inventory, generator);
    }

    @Override
    protected void addSlots() {
        addSlot(new SlotCharge(tileEntity, 0, 143, 35));
    }
}