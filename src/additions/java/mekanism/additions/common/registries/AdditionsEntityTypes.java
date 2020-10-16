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

public class AdditionsEntityTypes {

    private AdditionsEntityTypes() {
    }

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(MekanismAdditions.MODID);

    public static final EntityTypeRegistryObject<EntityBabyCreeper> BABY_CREEPER = ENTITY_TYPES.register("baby_creeper", EntityType.Builder.create(EntityBabyCreeper::new, EntityClassification.MONSTER).size(0.6F, 1.7F));
    public static final EntityTypeRegistryObject<EntityBabyEnderman> BABY_ENDERMAN = ENTITY_TYPES.register("baby_enderman", EntityType.Builder.create(EntityBabyEnderman::new, EntityClassification.MONSTER).size(0.6F, 2.9F));
    public static final EntityTypeRegistryObject<EntityBabySkeleton> BABY_SKELETON = ENTITY_TYPES.register("baby_skeleton", EntityType.Builder.create(EntityBabySkeleton::new, EntityClassification.MONSTER).size(0.6F, 1.99F));
    public static final EntityTypeRegistryObject<EntityBabyStray> BABY_STRAY = ENTITY_TYPES.register("baby_stray", EntityType.Builder.create(EntityBabyStray::new, EntityClassification.MONSTER).size(0.6F, 1.99F));
    public static final EntityTypeRegistryObject<EntityBabyWitherSkeleton> BABY_WITHER_SKELETON = ENTITY_TYPES.register("baby_wither_skeleton", EntityType.Builder.create(EntityBabyWitherSkeleton::new, EntityClassification.MONSTER).immuneToFire().size(0.7F, 2.4F));
    public static final EntityTypeRegistryObject<EntityBalloon> BALLOON = ENTITY_TYPES.register("balloon", EntityType.Builder.<EntityBalloon>create(EntityBalloon::new, EntityClassification.MISC).size(0.4F, 0.45F));
    public static final EntityTypeRegistryObject<EntityObsidianTNT> OBSIDIAN_TNT = ENTITY_TYPES.register("obsidian_tnt", EntityType.Builder.<EntityObsidianTNT>create(EntityObsidianTNT::new, EntityClassification.MISC).immuneToFire().size(0.98F, 0.98F));
}