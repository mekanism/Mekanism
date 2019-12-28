package mekanism.common.inventory.container.entity.robit;

import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class MainRobitContainer extends RobitContainer {

    public MainRobitContainer(int id, PlayerInventory inv, EntityRobit robit) {
        super(MekanismContainerTypes.MAIN_ROBIT, id, inv, robit);
    }

    public MainRobitContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getEntityFromBuf(buf, EntityRobit.class));
    }
}