package mekanism.additions.common.entity;

import javax.annotation.Nonnull;
import mekanism.additions.common.MekanismAdditions;
import mekanism.api.providers.IEntityTypeProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public enum AdditionsEntityType implements IEntityTypeProvider {
    BABY_SKELETON("baby_skeleton", EntityType.Builder.create(EntityBabySkeleton::new, EntityClassification.MONSTER)),
    BALLOON("balloon", EntityType.Builder.<EntityBalloon>create(EntityBalloon::new, EntityClassification.MISC).size(0.25F, 0.25F)),
    OBSIDIAN_TNT("obsidian_tnt", EntityType.Builder.<EntityObsidianTNT>create(EntityObsidianTNT::new, EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F));

    private final EntityType entityType;

    <T extends Entity> AdditionsEntityType(String name, EntityType.Builder<T> builder) {
        builder.setCustomClientFactory((spawnEntity, world) -> (T) getEntityType().create(world));
        EntityType<T> type = builder.build(name);
        type.setRegistryName(new ResourceLocation(MekanismAdditions.MODID, name));
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