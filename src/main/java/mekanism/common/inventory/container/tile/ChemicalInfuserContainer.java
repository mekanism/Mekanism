package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityChemicalInfuser;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class ChemicalInfuserContainer extends MekanismTileContainer<TileEntityChemicalInfuser> {

    public ChemicalInfuserContainer(int id, PlayerInventory inv, TileEntityChemicalInfuser tile) {
        super(MekanismContainerTypes.CHEMICAL_INFUSER, id, inv, tile);
    }

    public ChemicalInfuserContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityChemicalInfuser.class));
    }
}