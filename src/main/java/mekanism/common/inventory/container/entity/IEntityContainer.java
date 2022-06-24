package mekanism.common.inventory.container.entity;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface IEntityContainer<ENTITY extends Entity> {

    @NotNull
    ENTITY getEntity();
}