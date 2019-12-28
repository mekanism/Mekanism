package mekanism.generators.common.inventory.container;

import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class WindGeneratorContainer extends MekanismTileContainer<TileEntityWindGenerator> {

    public WindGeneratorContainer(int id, PlayerInventory inv, TileEntityWindGenerator tile) {
        super(GeneratorsContainerTypes.WIND_GENERATOR, id, inv, tile);
    }

    public WindGeneratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityWindGenerator.class));
    }
}