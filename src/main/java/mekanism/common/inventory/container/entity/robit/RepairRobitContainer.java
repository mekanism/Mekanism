package mekanism.common.inventory.container.entity.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.inventory.container.entity.MekanismEntityContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.network.PacketBuffer;

//TODO: Fix this, as it gets very confused if it extends RepairContainer
public class RepairRobitContainer extends RepairContainer implements IEntityContainer<EntityRobit> {

    private EntityRobit entity;

    public RepairRobitContainer(int id, PlayerInventory inv, EntityRobit robit) {
        super(id, inv);
        this.entity = robit;
    }

    public RepairRobitContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, MekanismEntityContainer.getEntityFromBuf(buf, EntityRobit.class));
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        return entity.isAlive();
    }

    @Override
    public EntityRobit getEntity() {
        return entity;
    }
}