package mekanism.common.inventory.container.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public abstract class MekanismEntityContainer<ENTITY extends Entity> extends MekanismContainer implements IEntityContainer<ENTITY> {

    @Nonnull
    protected final ENTITY entity;

    protected MekanismEntityContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, @Nonnull ENTITY entity) {
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
    public boolean stillValid(@Nonnull Player player) {
        return entity.isAlive();
    }

    @Nullable
    @Override
    public ICapabilityProvider getSecurityObject() {
        return entity;
    }
}