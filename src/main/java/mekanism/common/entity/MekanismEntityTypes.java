package mekanism.common.entity;

import javax.annotation.Nonnull;
import mekanism.api.providers.IEntityTypeProvider;
import mekanism.common.Mekanism;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

//TODO: Check the different settings. For example should robit be immune to fire
public enum MekanismEntityTypes implements IEntityTypeProvider {
    FLAME("flame", EntityType.Builder.<EntityFlame>create(EntityFlame::new, EntityClassification.MISC).size(0.5F, 0.5F)),
    ROBIT("robit", EntityType.Builder.<EntityRobit>create(EntityRobit::new, EntityClassification.MISC).size(0.5F, 0.5F));

    private final EntityType entityType;

    <T extends Entity> MekanismEntityTypes(String name, EntityType.Builder<T> builder) {
        builder.setCustomClientFactory((spawnEntity, world) -> (T) getEntityType().create(world));
        EntityType<T> type = builder.build(name);
        type.setRegistryName(new ResourceLocation(Mekanism.MODID, name));
        entityType = type;
    }

    @Override
    @Nonnull
    public EntityType getEntityType() {
        return entityType;
    }

    public static void registerEntities(IForgeRegistry<EntityType<?>> registry) {
        for (IEntityTypeProvider entityTypeProvider : values()) {
            registry.register(entityTypeProvider.getEntityType());
        }
    }
}