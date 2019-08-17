package mekanism.common.inventory.container.tile.advanced;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class OsmiumCompressorContainer extends AdvancedElectricMachineContainer<OsmiumCompressorRecipe, TileEntityOsmiumCompressor> {

    public OsmiumCompressorContainer(int id, PlayerInventory inv, TileEntityOsmiumCompressor tile) {
        super(MekanismContainerTypes.OSMIUM_COMPRESSOR, id, inv, tile);
    }

    public OsmiumCompressorContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityOsmiumCompressor.class));
    }
}