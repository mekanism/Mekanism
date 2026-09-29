package mekanism.additions.common.registries;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.config.MekanismAdditionsConfig;
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
import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.EntityTypeDeferredRegister;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.item.ItemStack;
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
        registerBaby(BabyType.CREEPER, map, () -> baby(EntityBabyCreeper::new, EntityTypes.CREEPER, 0.6F, 0.9F), Creeper::createAttributes);
        registerBaby(BabyType.ENDERMAN, map, () -> baby(EntityBabyEnderman::new, EntityTypes.ENDERMAN, 0.625F, 0.88F), Enderman::createAttributes);
        registerBaby(BabyType.PARCHED, map, () -> baby(EntityBabyParched::new, EntityTypes.PARCHED), Parched::createAttributes);
        registerBaby(BabyType.SKELETON, map, () -> baby(EntityBabySkeleton::new, EntityTypes.SKELETON), AbstractSkeleton::createAttributes);
        registerBaby(BabyType.STRAY, map, () -> baby(EntityBabyStray::new, EntityTypes.STRAY), AbstractSkeleton::createAttributes, Stray::checkStraySpawnRules);
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

    public static void depopulateDefaultEquipmentSlots(LivingEntity baby, BabyType babyType) {
        if (babyType.hasEquipment()) {
            if (MekanismAdditionsConfig.additions.getConfig(babyType).disableArmorSpawning.getAsBoolean()) {
                for (EquipmentSlot slot : EquipmentSlot.VALUES) {
                    if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                        baby.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
        } else {
            Mekanism.logger.warn("Attempted to depopulate equipment slots for a {}, but that baby type does not have equipment slots.", babyType.displayName());
        }
    }

    private static <ENTITY extends Entity> EntityType.Builder<ENTITY> baby(EntityType.EntityFactory<ENTITY> factory, EntityType<?> parent) {
        return baby(factory, parent, 0.5F, 0.815F);
    }

    private static <ENTITY extends Entity> EntityType.Builder<ENTITY> baby(EntityType.EntityFactory<ENTITY> factory, EntityType<?> parent, float heightScale, float eyeHeightScale) {
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
        if (!parent.trackDeltas()) {
            builder.dontTrackDeltas();
        }
        builder.requiredFeatures = parent.requiredFeatures();
        builder.immuneTo(parent.immuneTo)
              .spawnDimensionsScale(parent.spawnDimensionsScale)
              .setShouldReceiveVelocityUpdates(parent.hasUpdateInterval())
              .clientTrackingRange(parent.clientTrackingRange())
              .setTrackingRange(parent.clientTrackingRange())
              .updateInterval(parent.updateInterval())
              .setUpdateInterval(parent.updateInterval());
        float widthScale = 0.817F;
        EntityDimensions babyDimensions = parent.getDimensions().scale(widthScale, heightScale);
        builder.sized(babyDimensions.width(), babyDimensions.height());
        builder.eyeHeight(babyDimensions.height() * eyeHeightScale);
        EntityAttachments attachments = parent.getDimensions().attachments().scale(widthScale, 0.25F, widthScale);
        for (Map.Entry<EntityAttachment, List<Vec3>> entry : attachments.attachments.entrySet()) {
            EntityAttachment attachment = entry.getKey();
            for (Vec3 vec3 : entry.getValue()) {
                builder.attach(attachment, vec3);
            }
        }
        return builder;
    }
}