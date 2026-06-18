package mekanism.additions.common.registries;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.additions.common.entity.EntityObsidianTNT;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.entity.baby.EntityBabyBogged;
import mekanism.additions.common.entity.baby.EntityBabyCreeper;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import mekanism.additions.common.entity.baby.EntityBabyParched;
import mekanism.additions.common.entity.baby.EntityBabySkeleton;
import mekanism.additions.common.entity.baby.EntityBabyStray;
import mekanism.additions.common.entity.baby.EntityBabyWitherSkeleton;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.EntityTypeDeferredRegister;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.phys.Vec3;

public class AdditionsEntityTypes {

    private AdditionsEntityTypes() {
    }

    //Opposite of Zombie SPEED_MODIFIER_BABY_ID and SPEED_MODIFIER_BABY
    private static final AttributeModifier BABY_SPEED_NERF_MODIFIER = new AttributeModifier(Identifier.withDefaultNamespace("baby"), -0.5D, Operation.ADD_MULTIPLIED_BASE);
    private static final AttributeModifier BABY_HEALTH_NERF_MODIFIER = new AttributeModifier(Identifier.withDefaultNamespace("baby_health"), -0.5D, Operation.ADD_MULTIPLIED_TOTAL);
    private static final AttributeModifier BABY_ATTACK_NERF_MODIFIER = new AttributeModifier(Identifier.withDefaultNamespace("baby_attack"), -0.75D, Operation.ADD_MULTIPLIED_TOTAL);

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(MekanismAdditions.MODID);

    public static final Map<BabyType, MekanismDeferredHolder<EntityType<?>, ? extends EntityType<? extends Monster>>> BABIES = Collections.unmodifiableMap(Util.make(new EnumMap<>(BabyType.class), map -> {
        registerBaby(BabyType.BOGGED, map, () -> baby(EntityBabyBogged::new, EntityTypes.BOGGED), Bogged::createAttributes);
        registerBaby(BabyType.CREEPER, map, () -> baby(EntityBabyCreeper::new, EntityTypes.CREEPER, 0.625F), Creeper::createAttributes);
        registerBaby(BabyType.ENDERMAN, map, () -> baby(EntityBabyEnderman::new, EntityTypes.ENDERMAN, 0.525F), EnderMan::createAttributes);
        registerBaby(BabyType.PARCHED, map, () -> baby(EntityBabyParched::new, EntityTypes.PARCHED), Parched::createAttributes);
        registerBaby(BabyType.SKELETON, map, () -> baby(EntityBabySkeleton::new, EntityTypes.SKELETON), AbstractSkeleton::createAttributes);
        registerBaby(BabyType.STRAY, map, () -> baby(EntityBabyStray::new, EntityTypes.STRAY), AbstractSkeleton::createAttributes, EntityBabyStray::spawnRestrictions);
        registerBaby(BabyType.WITHER_SKELETON, map, () -> baby(EntityBabyWitherSkeleton::new, EntityTypes.WITHER_SKELETON), AbstractSkeleton::createAttributes);
    }));

    @SuppressWarnings("unchecked")
    public static <ENTITY extends Monster> MekanismDeferredHolder<EntityType<?>, EntityType<ENTITY>> getBaby(BabyType babyType) {
        return (MekanismDeferredHolder<EntityType<?>, EntityType<ENTITY>>) Objects.requireNonNull(BABIES.get(babyType));
    }

    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabyBogged>> BABY_BOGGED = getBaby(BabyType.BOGGED);
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabyCreeper>> BABY_CREEPER = getBaby(BabyType.CREEPER);
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabyEnderman>> BABY_ENDERMAN = getBaby(BabyType.ENDERMAN);
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabyParched>> BABY_PARCHED = getBaby(BabyType.PARCHED);
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabySkeleton>> BABY_SKELETON = getBaby(BabyType.SKELETON);
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabyStray>> BABY_STRAY = getBaby(BabyType.STRAY);
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBabyWitherSkeleton>> BABY_WITHER_SKELETON = getBaby(BabyType.WITHER_SKELETON);

    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityBalloon>> BALLOON = ENTITY_TYPES.registerBuilder("balloon", () -> EntityType.Builder.of(EntityBalloon::new, MobCategory.MISC)
          .sized(0.4F, 0.45F)
          .eyeHeight(0.45F - EntityBalloon.OFFSET)
    );
    public static final MekanismDeferredHolder<EntityType<?>, EntityType<EntityObsidianTNT>> OBSIDIAN_TNT = ENTITY_TYPES.registerBuilder("obsidian_tnt", () -> EntityType.Builder.<EntityObsidianTNT>of(EntityObsidianTNT::new, MobCategory.MISC)
          //Copied from EntityTypes.TNT
          .noLootTable()
          .fireImmune()
          .sized(0.98F, 0.98F)
          .eyeHeight(0.15F)
          .clientTrackingRange(10)
          .updateInterval(SharedConstants.TICKS_PER_SECOND / 2)
    );

    private static <ENTITY extends Monster> void registerBaby(BabyType babyType, Map<BabyType, MekanismDeferredHolder<EntityType<?>, ? extends EntityType<? extends Monster>>> map,
          Supplier<Builder<ENTITY>> builder, Supplier<AttributeSupplier.Builder> attributes) {
        registerBaby(babyType, map, builder, attributes, Monster::checkMonsterSpawnRules);
    }

    private static <ENTITY extends Monster> void registerBaby(BabyType babyType, Map<BabyType, MekanismDeferredHolder<EntityType<?>, ? extends EntityType<? extends Monster>>> map,
          Supplier<EntityType.Builder<ENTITY>> builder, Supplier<AttributeSupplier.Builder> attributes, SpawnPlacements.SpawnPredicate<ENTITY> placementPredicate) {
        map.put(babyType, ENTITY_TYPES.registerBasicPlacement(babyType.getSerializedName(), builder, attributes, placementPredicate));
    }

    public static void setupBabyModifiers(LivingEntity entity) {
        if (!entity.level().isClientSide()) {
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
        if (parent.onlyOpCanSetNbt()) {
            builder.setOnlyOpCanSetNbt(true);
        }
        if (!parent.isAllowedInPeaceful()) {
            builder.notInPeaceful();
        }
        if (parent.getDefaultLootTable().isEmpty()) {
            builder.noLootTable();
        }
        builder.requiredFeatures = parent.requiredFeatures();
        builder.immuneTo(parent.immuneTo)
              .spawnDimensionsScale(parent.spawnDimensionsScale)
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