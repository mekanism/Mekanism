package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityPersonalChest;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

//TODO: InventoryTweaks
//@ChestContainer(isLargeChest = true)
public class PersonalChestTileContainer extends MekanismTileContainer<TileEntityPersonalChest> {

    public PersonalChestTileContainer(int id, PlayerInventory inv, TileEntityPersonalChest tile) {
        super(MekanismContainerTypes.PERSONAL_CHEST_BLOCK, id, inv, tile);
    }

    public PersonalChestTileContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityPersonalChest.class));
    }

    @Override
    protected int getInventoryYOffset() {
        return 148;
    }
}