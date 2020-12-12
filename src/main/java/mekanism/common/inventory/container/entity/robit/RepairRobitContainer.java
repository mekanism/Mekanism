package mekanism.common.inventory.container.entity.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.ISecurityContainer;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.inventory.container.entity.MekanismEntityContainer;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.network.PacketBuffer;

public class RepairRobitContainer extends RepairContainer implements IEntityContainer<EntityRobit>, ISecurityContainer {

    private final EntityRobit entity;

    public RepairRobitContainer(int id, PlayerInventory inv, EntityRobit robit) {
        super(id, inv, robit.getWorldPosCallable());
        this.entity = robit;
        entity.open(inv.player);
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

    @Nonnull
    @Override
    public ContainerType<?> getType() {
        return MekanismContainerTypes.REPAIR_ROBIT.getContainerType();
    }

    @Override
    public void onContainerClosed(@Nonnull PlayerEntity player) {
        super.onContainerClosed(player);
        entity.close(player);
        //TODO - 10.1: Re-evaluate this, and other onContainerClosed overrides we have as they
        // get called when opening JEI, and then we are sometimes not in the fully correct state
        // after exiting JEI (for example here with the robit's repair container)
    }

    @Override
    public ISecurityObject getSecurityObject() {
        return entity;
    }
}