package mekanism.common.registration.impl;

import mekanism.api.providers.IEntityTypeProvider;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class EntityTypeRegistryObject<ENTITY extends Entity> extends MekanismDeferredHolder<EntityType<?>, EntityType<ENTITY>> implements IEntityTypeProvider {

    public EntityTypeRegistryObject(ResourceKey<EntityType<?>> key) {
        super(key);
    }

    @NotNull
    @Override
    public EntityType<ENTITY> getEntityType() {
        return value();
    }
}