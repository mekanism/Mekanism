package mekanism.common.inventory.container.tile.electric;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityCrusher;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class CrusherContainer extends ElectricMachineContainer<TileEntityCrusher> {

    public CrusherContainer(int id, PlayerInventory inv, TileEntityCrusher tile) {
        super(MekanismContainerTypes.CRUSHER, id, inv, tile);
    }

    public CrusherContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityCrusher.class));
    }
}