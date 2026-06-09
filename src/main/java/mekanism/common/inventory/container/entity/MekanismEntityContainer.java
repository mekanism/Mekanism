package mekanism.common.inventory.container.entity;

import mekanism.api.security.IEntitySecurityUtils;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public abstract class MekanismEntityContainer<ENTITY extends Entity> extends MekanismContainer implements IEntityContainer<ENTITY> {

    protected final ENTITY entity;

    protected MekanismEntityContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, ENTITY entity) {
        super(type, id, inv);
        this.entity = entity;
        addSlotsAndOpen();
    }

    @Override
    public ENTITY getEntity() {
        return entity;
    }

    @Override
    public boolean stillValid(Player player) {
        return entity.isAlive();
    }

    @Override
    public boolean canPlayerAccess(Player player) {
        return IEntitySecurityUtils.INSTANCE.canAccess(player, entity);
    }
}