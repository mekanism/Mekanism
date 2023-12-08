package mekanism.common.inventory.container.entity;

import mekanism.api.security.IEntitySecurityUtils;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public abstract class MekanismEntityContainer<ENTITY extends Entity> extends MekanismContainer implements IEntityContainer<ENTITY> {

    @NotNull
    protected final ENTITY entity;

    protected MekanismEntityContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, @NotNull ENTITY entity) {
        super(type, id, inv);
        this.entity = entity;
        addSlotsAndOpen();
    }

    @NotNull
    @Override
    public ENTITY getEntity() {
        return entity;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return entity.isAlive();
    }

    @Override
    public boolean canPlayerAccess(@NotNull Player player) {
        return IEntitySecurityUtils.INSTANCE.canAccess(player, entity);
    }
}