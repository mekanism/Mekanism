package mekanism.generators.common.inventory.container.passive;

import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class WindGeneratorContainer extends PassiveGeneratorContainer<TileEntityWindGenerator> {

    public WindGeneratorContainer(int id, PlayerInventory inv, TileEntityWindGenerator tile) {
        super(GeneratorsContainerTypes.WIND_GENERATOR, id, inv, tile);
    }

    public WindGeneratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityWindGenerator.class));
    }

    @Override
    protected void addSlots() {
        addSlot(new SlotCharge(tile, 0, 143, 35));
    }
}