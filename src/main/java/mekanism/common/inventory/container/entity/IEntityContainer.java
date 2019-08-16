package mekanism.common.inventory.container.entity;

import net.minecraft.entity.Entity;

public interface IEntityContainer<ENTITY extends Entity> {

    ENTITY getEntity();
}