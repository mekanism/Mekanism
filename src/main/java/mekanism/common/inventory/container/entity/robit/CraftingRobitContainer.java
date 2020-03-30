package mekanism.common.inventory.container.entity.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.inventory.container.entity.MekanismEntityContainer;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;

public class CraftingRobitContainer extends WorkbenchContainer implements IEntityContainer<EntityRobit> {

    private EntityRobit entity;

    public CraftingRobitContainer(int id, PlayerInventory inv, EntityRobit robit) {
        super(id, inv, IWorldPosCallable.of(robit.getEntityWorld(), null));
        this.entity = robit;
    }

    public CraftingRobitContainer(int id, PlayerInventory inv, PacketBuffer buf) {
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
        return MekanismContainerTypes.CRAFTING_ROBIT.getContainerType();
    }
}