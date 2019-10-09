package mekanism.generators.common.inventory.container.fuel;

import mekanism.common.inventory.container.slot.SlotEnergy.SlotCharge;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class HeatGeneratorContainer extends FuelGeneratorContainer<TileEntityHeatGenerator> {

    public HeatGeneratorContainer(int id, PlayerInventory inv, TileEntityHeatGenerator tile) {
        super(GeneratorsContainerTypes.HEAT_GENERATOR, id, inv, tile);
    }

    public HeatGeneratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityHeatGenerator.class));
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tile, 0, 17, 35));
        addSlot(new SlotCharge(tile, 1, 143, 35));
    }

    @Override
    protected boolean tryFuel(ItemStack slotStack) {
        return tile.getFuel(slotStack) > 0;
    }
}