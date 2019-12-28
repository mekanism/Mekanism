package mekanism.generators.common.inventory.container;

import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.generators.common.registries.GeneratorsContainerTypes;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class BioGeneratorContainer extends MekanismTileContainer<TileEntityBioGenerator> {

    public BioGeneratorContainer(int id, PlayerInventory inv, TileEntityBioGenerator tile) {
        super(GeneratorsContainerTypes.BIO_GENERATOR, id, inv, tile);
    }

    public BioGeneratorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityBioGenerator.class));
    }
}