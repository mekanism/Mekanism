package mekanism.generators.common.inventory.container;

import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class GasBurningGeneratorContainer extends MekanismTileContainer<TileEntityGasGenerator> {

    public GasBurningGeneratorContainer(int id, PlayerInventory inv, TileEntityGasGenerator tile) {
        super(GeneratorsContainerTypes.GAS_BURNING_GENERATOR, id, inv, tile);
    }

    public GasBurningGeneratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityGasGenerator.class));
    }
}