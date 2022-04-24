package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.api.providers.IEntityTypeProvider;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.RegistryObject;

public class EntityTypeRegistryObject<ENTITY extends Entity> extends WrappedRegistryObject<EntityType<ENTITY>> implements IEntityTypeProvider {

    public EntityTypeRegistryObject(RegistryObject<EntityType<ENTITY>> registryObject) {
        super(registryObject);
    }

    @Nonnull
    @Override
    public EntityType<ENTITY> getEntityType() {
        return get();
    }
}