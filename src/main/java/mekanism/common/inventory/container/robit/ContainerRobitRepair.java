package mekanism.common.inventory.container.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.util.math.BlockPos;

public class ContainerRobitRepair extends RepairContainer {

    public EntityRobit robit;

    public ContainerRobitRepair(PlayerInventory inventory, EntityRobit entity) {
        super(inventory, entity.world, BlockPos.ZERO, inventory.player);
        robit = entity;
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity entityplayer) {
        return robit.isAlive();
    }
}