package mekanism.additions.common.registries;

import java.util.List;
import java.util.Map;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.additions.common.entity.EntityObsidianTNT;
import mekanism.additions.common.entity.baby.EntityBabyBogged;
import mekanism.additions.common.entity.baby.EntityBabyCreeper;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import mekanism.additions.common.entity.baby.EntityBabySkeleton;
import mekanism.additions.common.entity.baby.EntityBabyStray;
import mekanism.additions.common.entity.baby.EntityBabyWitherSkeleton;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.EntityTypeDeferredRegister;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Bogged;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class AdditionsEntityTypes {

    private AdditionsEntityTypes() {
    }

    //Opposite of Zombie SPEED_MODIFIER_BABY_ID and SPEED_MODIFIER_BABY
    private static final AttributeModifier BABY_SPEED_NERF_MODIFIER = new AttributeModifier(ResourceLocation.withDefaultNamespace("baby"), -0.5D, Operation.ADD_MULTIPLIED_BASE);
    private static final AttributeModifier BABY_HEALTH_NERF_MODIFIER = new AttributeModifier(ResourceLocation.withDefaultNamespace("baby_health"), -0.5D, Operation.ADD_MULTIPLIED_TOTAL);
    private static final AttributeModifier BABY_ATTACK_NERF_MODIFIER = new AttributeModifier(ResourceLocation.withDefaultNamespace("baby_attack"), -0.75D, Operation.ADD_MULTIPLIED_TOTAL);

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(MekanismAdditions.MODID);

    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabyBogged>> BABY_BOGGED = ENTITY_TYPES.registerBasicMonster("baby_bogged", () -> baby(EntityBabyBogged::new, EntityType.BOGGED), Bogged::createAttributes);
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabyCreeper>> BABY_CREEPER = ENTITY_TYPES.registerBasicMonster("baby_creeper", () -> baby(EntityBabyCreeper::new, EntityType.CREEPER, 0.625F), Creeper::createAttributes);
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabyEnderman>> BABY_ENDERMAN = ENTITY_TYPES.registerBasicMonster("baby_enderman", () -> baby(EntityBabyEnderman::new, EntityType.ENDERMAN, 0.525F), EnderMan::createAttributes);
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabySkeleton>> BABY_SKELETON = ENTITY_TYPES.registerBasicMonster("baby_skeleton", () -> baby(EntityBabySkeleton::new, EntityType.SKELETON), AbstractSkeleton::createAttributes);
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabyStray>> BABY_STRAY = ENTITY_TYPES.registerBasicPlacement("baby_stray", () -> baby(EntityBabyStray::new, EntityType.STRAY), AbstractSkeleton::createAttributes, EntityBabyStray::spawnRestrictions);
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabyWitherSkeleton>> BABY_WITHER_SKELETON = ENTITY_TYPES.registerBasicMonster("baby_wither_skeleton", () -> baby(EntityBabyWitherSkeleton::new, EntityType.WITHER_SKELETON), AbstractSkeleton::createAttributes);
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBalloon>> BALLOON = ENTITY_TYPES.registerBuilder("balloon", () -> EntityType.Builder.of(EntityBalloon::new, MobCategory.MISC)
          .sized(0.4F, 0.45F)
          .eyeHeight(0.45F - EntityBalloon.OFFSET)
    );
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityObsidianTNT>> OBSIDIAN_TNT = ENTITY_TYPES.registerBuilder("obsidian_tnt", () -> EntityType.Builder.of(EntityObsidianTNT::new, MobCategory.MISC)
          //Copied from EntityType.TNT
          .fireImmune()
          .sized(0.98F, 0.98F)
          .eyeHeight(0.15F)
          .clientTrackingRange(10)
          .updateInterval(SharedConstants.TICKS_PER_SECOND / 2)
    );

    public static void setupBabyModifiers(LivingEntity entity) {
        if (!entity.level().isClientSide) {
            AttributeInstance attributeInstance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attributeInstance != null) {
                attributeInstance.addPermanentModifier(BABY_SPEED_NERF_MODIFIER);
            }
            attributeInstance = entity.getAttribute(Attributes.MAX_HEALTH);
            if (attributeInstance != null) {
                attributeInstance.addPermanentModifier(BABY_HEALTH_NERF_MODIFIER);
            }
            attributeInstance = entity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attributeInstance != null) {
                attributeInstance.addPermanentModifier(BABY_ATTACK_NERF_MODIFIER);
            }
        }
    }

    private static <ENTITY extends Entity> EntityType.Builder<ENTITY> baby(EntityType.EntityFactory<ENTITY> factory, EntityType<?> parent) {
        //Vanilla's 0.5 scaling for baby mobs is too small compared to the visual of the mob
        return baby(factory, parent, 0.5625F);
    }

    private static <ENTITY extends Entity> EntityType.Builder<ENTITY> baby(EntityType.EntityFactory<ENTITY> factory, EntityType<?> parent, float scale) {
        EntityType.Builder<ENTITY> builder = Builder.of(factory, parent.getCategory());
        if (!parent.canSerialize()) {
            builder.noSave();
        }
        if (!parent.canSummon()) {
            builder.noSummon();
        }
        if (parent.fireImmune()) {
            builder.fireImmune();
        }
        if (parent.canSpawnFarFromPlayer()) {
            builder.canSpawnFarFromPlayer();
        }
        builder.immuneTo(parent.immuneTo.toArray(Block[]::new))
              .setShouldReceiveVelocityUpdates(parent.trackDeltas())
              .clientTrackingRange(parent.clientTrackingRange())
              .setTrackingRange(parent.clientTrackingRange())
              .updateInterval(parent.updateInterval())
              .setUpdateInterval(parent.updateInterval());
        EntityDimensions babyDimensions = parent.getDimensions().scale(scale);
        builder.sized(babyDimensions.width(), babyDimensions.height());
        //Note: We use a custom value rather than the 0.85 multiplier default as babies have larger heads than normal
        builder.eyeHeight(babyDimensions.height() * 0.83F);
        for (Map.Entry<EntityAttachment, List<Vec3>> entry : babyDimensions.attachments().attachments.entrySet()) {
            EntityAttachment attachment = entry.getKey();
            for (Vec3 vec3 : entry.getValue()) {
                builder.attach(attachment, vec3);
            }
        }
        return builder;
    }
}