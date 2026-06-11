package mekanism.common.inventory.container.entity;

import net.minecraft.world.entity.Entity;

public interface IEntityContainer<ENTITY extends Entity> {

    ENTITY getEntity();
}