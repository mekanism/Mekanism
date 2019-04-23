package mekanism.common.inventory.container.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.util.math.BlockPos;

public class ContainerRobitCrafting extends ContainerWorkbench {

    public EntityRobit robit;

    public ContainerRobitCrafting(InventoryPlayer inventory, EntityRobit entity) {
        super(inventory, entity.world, BlockPos.ORIGIN);
        robit = entity;
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
        return !robit.isDead;
    }
}
