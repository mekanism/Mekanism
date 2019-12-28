package mekanism.generators.common.inventory.container;

import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class HeatGeneratorContainer extends MekanismTileContainer<TileEntityHeatGenerator> {

    public HeatGeneratorContainer(int id, PlayerInventory inv, TileEntityHeatGenerator tile) {
        super(GeneratorsContainerTypes.HEAT_GENERATOR, id, inv, tile);
    }

    public HeatGeneratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityHeatGenerator.class));
    }
}