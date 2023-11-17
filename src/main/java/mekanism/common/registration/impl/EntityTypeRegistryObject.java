package mekanism.common.registration.impl;

import mekanism.api.providers.IEntityTypeProvider;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class EntityTypeRegistryObject<ENTITY extends Entity> extends WrappedRegistryObject<EntityType<?>, EntityType<ENTITY>> implements IEntityTypeProvider {

    public EntityTypeRegistryObject(DeferredHolder<EntityType<?>, EntityType<ENTITY>> registryObject) {
        super(registryObject);
    }

    @NotNull
    @Override
    public EntityType<ENTITY> getEntityType() {
        return get();
    }
}