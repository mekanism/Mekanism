package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityPressurizedReactionChamber;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class PressurizedReactionChamberContainer extends MekanismTileContainer<TileEntityPressurizedReactionChamber> {

    public PressurizedReactionChamberContainer(int id, PlayerInventory inv, TileEntityPressurizedReactionChamber tile) {
        super(MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER, id, inv, tile);
    }

    public PressurizedReactionChamberContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityPressurizedReactionChamber.class));
    }
}