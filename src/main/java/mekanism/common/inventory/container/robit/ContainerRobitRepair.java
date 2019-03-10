package mekanism.common.inventory.container.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.util.math.BlockPos;

public class ContainerRobitRepair extends ContainerRepair {

    public EntityRobit robit;

    public ContainerRobitRepair(InventoryPlayer inventory, EntityRobit entity) {
        super(inventory, entity.world, BlockPos.ORIGIN, inventory.player);

        robit = entity;
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
        return !robit.isDead;
    }
}
