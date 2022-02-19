package mekanism.additions.common.registries;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.additions.common.entity.EntityObsidianTNT;
import mekanism.additions.common.entity.baby.EntityBabyCreeper;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import mekanism.additions.common.entity.baby.EntityBabySkeleton;
import mekanism.additions.common.entity.baby.EntityBabyStray;
import mekanism.additions.common.entity.baby.EntityBabyWitherSkeleton;
import mekanism.common.registration.impl.EntityTypeDeferredRegister;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;

public class AdditionsEntityTypes {

    private AdditionsEntityTypes() {
    }

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(MekanismAdditions.MODID);

    public static final EntityTypeRegistryObject<EntityBabyCreeper> BABY_CREEPER = ENTITY_TYPES.register("baby_creeper", EntityType.Builder.of(EntityBabyCreeper::new, EntityClassification.MONSTER).sized(0.6F, 1.7F), CreeperEntity::createAttributes);
    public static final EntityTypeRegistryObject<EntityBabyEnderman> BABY_ENDERMAN = ENTITY_TYPES.register("baby_enderman", EntityType.Builder.of(EntityBabyEnderman::new, EntityClassification.MONSTER).sized(0.6F, 2.9F), EndermanEntity::createAttributes);
    public static final EntityTypeRegistryObject<EntityBabySkeleton> BABY_SKELETON = ENTITY_TYPES.register("baby_skeleton", EntityType.Builder.of(EntityBabySkeleton::new, EntityClassification.MONSTER).sized(0.6F, 1.99F), AbstractSkeletonEntity::createAttributes);
    public static final EntityTypeRegistryObject<EntityBabyStray> BABY_STRAY = ENTITY_TYPES.register("baby_stray", EntityType.Builder.of(EntityBabyStray::new, EntityClassification.MONSTER).sized(0.6F, 1.99F), AbstractSkeletonEntity::createAttributes);
    public static final EntityTypeRegistryObject<EntityBabyWitherSkeleton> BABY_WITHER_SKELETON = ENTITY_TYPES.register("baby_wither_skeleton", EntityType.Builder.of(EntityBabyWitherSkeleton::new, EntityClassification.MONSTER).fireImmune().sized(0.7F, 2.4F), AbstractSkeletonEntity::createAttributes);
    public static final EntityTypeRegistryObject<EntityBalloon> BALLOON = ENTITY_TYPES.register("balloon", EntityType.Builder.of(EntityBalloon::new, EntityClassification.MISC).sized(0.4F, 0.45F));
    public static final EntityTypeRegistryObject<EntityObsidianTNT> OBSIDIAN_TNT = ENTITY_TYPES.register("obsidian_tnt", EntityType.Builder.of(EntityObsidianTNT::new, EntityClassification.MISC).fireImmune().sized(0.98F, 0.98F));
}