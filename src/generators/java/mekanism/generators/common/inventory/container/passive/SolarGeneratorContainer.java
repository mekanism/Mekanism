package mekanism.generators.common.inventory.container.passive;

import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class SolarGeneratorContainer extends PassiveGeneratorContainer<TileEntitySolarGenerator> {

    public SolarGeneratorContainer(int id, PlayerInventory inv, TileEntitySolarGenerator tile) {
        super(GeneratorsContainerTypes.SOLAR_GENERATOR, id, inv, tile);
    }

    public SolarGeneratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntitySolarGenerator.class));
    }
}