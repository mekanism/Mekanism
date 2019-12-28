package mekanism.common.inventory.container.entity.robit;

import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class InventoryRobitContainer extends RobitContainer {

    public InventoryRobitContainer(int id, PlayerInventory inv, EntityRobit robit) {
        super(MekanismContainerTypes.INVENTORY_ROBIT, id, inv, robit);
    }

    public InventoryRobitContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getEntityFromBuf(buf, EntityRobit.class));
    }
}