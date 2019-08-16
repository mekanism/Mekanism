package mekanism.common.inventory.container_old.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.WorkbenchContainer;

public class ContainerRobitCrafting extends WorkbenchContainer {

    public EntityRobit robit;

    public ContainerRobitCrafting(PlayerInventory inventory, EntityRobit entity) {
        super(id, inventory);
        robit = entity;
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity entityplayer) {
        return robit.isAlive();
    }
}