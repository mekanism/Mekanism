package mekanism.common.inventory.container.entity;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

public abstract class MekanismEntityContainer<ENTITY extends Entity> extends MekanismContainer implements IEntityContainer<ENTITY> {

    @Nonnull
    protected final ENTITY entity;

    protected MekanismEntityContainer(ContainerTypeRegistryObject<?> type, int id, PlayerInventory inv, @Nonnull ENTITY entity) {
        super(type, id, inv);
        this.entity = entity;
        addSlotsAndOpen();
    }

    @Nonnull
    @Override
    public ENTITY getEntity() {
        return entity;
    }

    @Override
    public boolean stillValid(@Nonnull PlayerEntity player) {
        return entity.isAlive();
    }

    @Override
    public ISecurityObject getSecurityObject() {
        return entity instanceof ISecurityObject ? (ISecurityObject) entity : ISecurityObject.NO_SECURITY;
    }
}