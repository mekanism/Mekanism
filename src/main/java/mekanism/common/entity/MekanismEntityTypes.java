package mekanism.common.entity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

//TODO: Check the different settings. For example should robit be immune to fire
public class MekanismEntityTypes {

    //TODO: Ensure baby skeleton has a spawn egg
    public static final EntityType<EntityBabySkeleton> BABY_SKELETON = EntityType.Builder.create(EntityBabySkeleton::new, EntityClassification.MONSTER).build("baby_skeleton");
    public static final EntityType<EntityRobit> ROBIT = EntityType.Builder.<EntityRobit>create(EntityRobit::new, EntityClassification.MISC).size(0.5F, 0.5F).build("robit");
    public static final EntityType<EntityObsidianTNT> OBSIDIAN_TNT = EntityType.Builder.<EntityObsidianTNT>create(EntityObsidianTNT::new, EntityClassification.MISC).immuneToFire()
          .size(0.98F, 0.98F).build("obsidian_tnt");
    public static final EntityType<EntityBalloon> BALLOON = EntityType.Builder.<EntityBalloon>create(EntityBalloon::new, EntityClassification.MISC)
          .size(0.25F, 0.25F).build("balloon");
    public static final EntityType<EntityFlame> FLAME = EntityType.Builder.<EntityFlame>create(EntityFlame::new, EntityClassification.MISC)
          .size(0.5F, 0.5F).build("flame");
}