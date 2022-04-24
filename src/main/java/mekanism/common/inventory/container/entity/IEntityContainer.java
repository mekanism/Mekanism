package mekanism.common.inventory.container.entity;

import javax.annotation.Nonnull;
import net.minecraft.world.entity.Entity;

public interface IEntityContainer<ENTITY extends Entity> {

    @Nonnull
    ENTITY getEntity();
}