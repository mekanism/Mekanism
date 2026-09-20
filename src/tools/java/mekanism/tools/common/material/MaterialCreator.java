package mekanism.tools.common.material;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.config.ToolsConfigTranslations.MaterialTranslations;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ModConfigSpec;

public class MaterialCreator implements BaseMekanismMaterial {

    private final BaseMekanismMaterial fallBack;
    private final ResourceKey<EquipmentAsset> equipmentAsset;

    private final CachedIntValue toolDurability;
    private final CachedFloatValue efficiency;
    private final CachedFloatValue attackDamage;
    private final CachedIntValue enchantability;

    private final CachedFloatValue swordDamage;
    private final CachedFloatValue swordAtkSpeed;

    private final CachedFloatValue shovelDamage;
    private final CachedFloatValue shovelAtkSpeed;

    private final CachedFloatValue axeDamage;
    private final CachedFloatValue axeAtkSpeed;

    private final CachedFloatValue pickaxeDamage;
    private final CachedFloatValue pickaxeAtkSpeed;

    private final CachedFloatValue hoeDamage;
    private final CachedFloatValue hoeAtkSpeed;

    private final CachedFloatValue paxelDamage;
    private final CachedFloatValue paxelAtkSpeed;
    private final CachedFloatValue paxelEfficiency;
    private final CachedIntValue paxelEnchantability;
    private final CachedIntValue paxelDurability;
    private final CachedFloatValue paxelDisableBlockingSeconds;

    private final CachedFloatValue spearAttackDuration;
    private final CachedFloatValue spearDamageMultiplier;
    private final CachedFloatValue spearDelay;
    private final CachedFloatValue spearDismountTime;
    private final CachedFloatValue spearDismountThreshold;
    private final CachedFloatValue spearKnockbackTime;
    private final CachedFloatValue spearKnockbackThreshold;
    private final CachedFloatValue spearDamageTime;
    private final CachedFloatValue spearDamageThreshold;

    private final CachedIntValue shieldDurability;
    private final CachedFloatValue shieldBlockDelay;
    private final CachedFloatValue shieldDisableCooldownScale;
    private final CachedFloatValue shieldHorizontalBlockingAngle;
    private final CachedFloatValue shieldDamageReductionBase;
    private final CachedFloatValue shieldDamageReductionFactor;
    private final CachedFloatValue shieldDamageThreshold;
    private final CachedFloatValue shieldItemDamageBase;
    private final CachedFloatValue shieldItemDamageFactor;

    private final CachedFloatValue toughness;
    private final CachedFloatValue knockbackResistance;
    private final CachedIntValue armorEnchantability;
    private final CachedIntValue bootDurability;
    private final CachedIntValue leggingDurability;
    private final CachedIntValue chestplateDurability;
    private final CachedIntValue helmetDurability;
    private final CachedIntValue bootArmor;
    private final CachedIntValue leggingArmor;
    private final CachedIntValue chestplateArmor;
    private final CachedIntValue helmetArmor;

    public MaterialCreator(IMekanismConfig config, ModConfigSpec.Builder builder, BaseMekanismMaterial materialDefaults) {
        fallBack = materialDefaults;
        String toolKey = registryPrefix();
        equipmentAsset = ResourceKey.create(EquipmentAssets.ROOT_ID, MekanismTools.rl(toolKey));
        MaterialTranslations translations = MaterialTranslations.create(toolKey);
        translations.topLevel().applyToBuilder(builder).push(toolKey);
        toolDurability = CachedIntValue.wrap(config, translations.toolDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "ToolDurability", materialDefaults.durability(), 1, Integer.MAX_VALUE));
        efficiency = CachedFloatValue.wrap(config, translations.efficiency().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "Efficiency", (double) materialDefaults.speed()));
        attackDamage = CachedFloatValue.wrap(config, translations.damage().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "AttackDamage", materialDefaults.attackDamageBonus(), 0, Float.MAX_VALUE));
        enchantability = CachedIntValue.wrap(config, translations.enchantability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "Enchantability", materialDefaults.enchantmentValue(), 0, Integer.MAX_VALUE));
        //Note: Damage predicate to allow for tools to go negative to the value of the base tier so that a tool
        // can effectively have zero damage for things like the hoe
        Predicate<Object> damageModifierPredicate = this::validateDamageModifier;
        swordDamage = CachedFloatValue.wrap(config, translations.swordDamage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SwordDamage", validateDefaultModifier(materialDefaults.swordDamage()), damageModifierPredicate));
        swordAtkSpeed = CachedFloatValue.wrap(config, translations.swordAtkSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SwordAtkSpeed", (double) materialDefaults.swordAtkSpeed()));
        shovelDamage = CachedFloatValue.wrap(config, translations.shovelDamage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "ShovelDamage", validateDefaultModifier(materialDefaults.shovelDamage()), damageModifierPredicate));
        shovelAtkSpeed = CachedFloatValue.wrap(config, translations.shovelAtkSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "ShovelAtkSpeed", (double) materialDefaults.shovelAtkSpeed()));
        axeDamage = CachedFloatValue.wrap(config, translations.axeDamage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "AxeDamage", validateDefaultModifier(materialDefaults.axeDamage()), damageModifierPredicate));
        axeAtkSpeed = CachedFloatValue.wrap(config, translations.axeAtkSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "AxeAtkSpeed", (double) materialDefaults.axeAtkSpeed()));
        pickaxeDamage = CachedFloatValue.wrap(config, translations.pickaxeDamage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PickaxeDamage", validateDefaultModifier(materialDefaults.pickaxeDamage()), damageModifierPredicate));
        pickaxeAtkSpeed = CachedFloatValue.wrap(config, translations.pickaxeAtkSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PickaxeAtkSpeed", (double) materialDefaults.pickaxeAtkSpeed()));
        hoeDamage = CachedFloatValue.wrap(config, translations.hoeDamage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "HoeDamage", validateDefaultModifier(materialDefaults.hoeDamage()), damageModifierPredicate));
        hoeAtkSpeed = CachedFloatValue.wrap(config, translations.hoeAtkSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "HoeAtkSpeed", (double) materialDefaults.hoeAtkSpeed()));
        paxelDamage = CachedFloatValue.wrap(config, translations.paxelDamage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PaxelDamage", validateDefaultModifier(materialDefaults.paxelDamage()), damageModifierPredicate));
        paxelAtkSpeed = CachedFloatValue.wrap(config, translations.paxelAtkSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PaxelAtkSpeed", (double) materialDefaults.paxelAtkSpeed()));
        paxelEfficiency = CachedFloatValue.wrap(config, translations.paxelEfficiency().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PaxelEfficiency", (double) materialDefaults.paxelEfficiency()));
        paxelEnchantability = CachedIntValue.wrap(config, translations.paxelEnchantability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "PaxelEnchantability", materialDefaults.paxelEnchantability(), 0, Integer.MAX_VALUE));
        paxelDurability = CachedIntValue.wrap(config, translations.paxelDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "PaxelDurability", materialDefaults.paxelDurability(), 1, Integer.MAX_VALUE));
        paxelDisableBlockingSeconds = CachedFloatValue.wrap(config, translations.paxelDisableBlockingSeconds().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PaxelDisableBlockingSeconds", (double) materialDefaults.paxelDisableBlockingSeconds(), MaterialCreator::nonNegativeFloat));

        spearAttackDuration = CachedFloatValue.wrap(config, translations.spearAttackDuration().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SpearAttackDuration", (double) materialDefaults.spearAttackDuration(),
                    value -> value instanceof Double && (int) (getActualValue((double) value) * SharedConstants.TICKS_PER_SECOND) > 0));
        spearDamageMultiplier = CachedFloatValue.wrap(config, translations.spearDamageMultiplier().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SpearDamageMultiplier", (double) materialDefaults.spearDamageMultiplier()));
        spearDelay = CachedFloatValue.wrap(config, translations.spearDelay().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SpearDelay", (double) materialDefaults.spearDelay(), MaterialCreator::nonNegativeFloat));
        spearDismountTime = CachedFloatValue.wrap(config, translations.spearDismountTime().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SpearDismountTime", (double) materialDefaults.spearDismountTime(), MaterialCreator::nonNegativeFloat));
        spearDismountThreshold = CachedFloatValue.wrap(config, translations.spearDismountThreshold().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SpearDismountThreshold", (double) materialDefaults.spearDismountThreshold()));
        spearKnockbackTime = CachedFloatValue.wrap(config, translations.spearKnockbackTime().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SpearKnockbackTime", (double) materialDefaults.spearKnockbackTime(), MaterialCreator::nonNegativeFloat));
        spearKnockbackThreshold = CachedFloatValue.wrap(config, translations.spearKnockbackThreshold().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SpearKnockbackThreshold", (double) materialDefaults.spearKnockbackThreshold()));
        spearDamageTime = CachedFloatValue.wrap(config, translations.spearDamageTime().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SpearDamageTime", (double) materialDefaults.spearDamageTime(), MaterialCreator::nonNegativeFloat));
        spearDamageThreshold = CachedFloatValue.wrap(config, translations.spearDamageThreshold().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SpearDamageThreshold", (double) materialDefaults.spearDamageThreshold()));

        shieldDurability = CachedIntValue.wrap(config, translations.shieldDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "ShieldDurability", materialDefaults.shieldDurability(), 0, Integer.MAX_VALUE));
        shieldBlockDelay = CachedFloatValue.wrap(config, translations.shieldBlockDelay().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "ShieldBlockDelay", (double) materialDefaults.shieldBlockDelay(), MaterialCreator::nonNegativeFloat));
        shieldDisableCooldownScale = CachedFloatValue.wrap(config, translations.shieldDisableCooldownScale().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "ShieldDisableCooldownScale", (double) materialDefaults.shieldDisableCooldownScale(), MaterialCreator::nonNegativeFloat));
        shieldHorizontalBlockingAngle = CachedFloatValue.wrap(config, translations.shieldHorizontalBlockingAngle().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "ShieldHorizontalBlockingAngle", (double) materialDefaults.shieldHorizontalBlockingAngle(), MaterialCreator::positiveFloat));
        shieldDamageReductionBase = CachedFloatValue.wrap(config, translations.shieldDamageReductionBase().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "ShieldDamageReductionBase", (double) materialDefaults.shieldDamageReductionBase()));
        shieldDamageReductionFactor = CachedFloatValue.wrap(config, translations.shieldDamageReductionFactor().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "ShieldDamageReductionFactor", (double) materialDefaults.shieldDamageReductionFactor()));
        shieldDamageThreshold = CachedFloatValue.wrap(config, translations.shieldDamageThreshold().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "ShieldDamageThreshold", (double) materialDefaults.shieldDamageThreshold(), MaterialCreator::nonNegativeFloat));
        shieldItemDamageBase = CachedFloatValue.wrap(config, translations.shieldItemDamageBase().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "ShieldItemDamageBase", (double) materialDefaults.shieldItemDamageBase()));
        shieldItemDamageFactor = CachedFloatValue.wrap(config, translations.shieldItemDamageFactor().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "ShieldItemDamageFactor", (double) materialDefaults.shieldItemDamageFactor()));

        toughness = CachedFloatValue.wrap(config, translations.toughness().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "Toughness", materialDefaults.toughness(), 0, Float.MAX_VALUE));
        knockbackResistance = CachedFloatValue.wrap(config, translations.knockbackResistance().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "KnockbackResistance", materialDefaults.knockbackResistance(), 0, Float.MAX_VALUE));
        armorEnchantability = CachedIntValue.wrap(config, translations.armorEnchantability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "ArmorEnchantability", materialDefaults.armorEnchantmentValue(), 0, Integer.MAX_VALUE));
        bootDurability = CachedIntValue.wrap(config, translations.bootDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "BootDurability", materialDefaults.durability(ArmorType.BOOTS), 1, Integer.MAX_VALUE));
        bootArmor = CachedIntValue.wrap(config, translations.bootArmor().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "BootArmor", materialDefaults.defense(ArmorType.BOOTS), 0, Integer.MAX_VALUE));
        leggingDurability = CachedIntValue.wrap(config, translations.leggingDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "LeggingDurability", materialDefaults.durability(ArmorType.LEGGINGS), 1, Integer.MAX_VALUE));
        leggingArmor = CachedIntValue.wrap(config, translations.leggingArmor().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "LeggingArmor", materialDefaults.defense(ArmorType.LEGGINGS), 0, Integer.MAX_VALUE));
        chestplateDurability = CachedIntValue.wrap(config, translations.chestplateDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "ChestplateDurability", materialDefaults.durability(ArmorType.CHESTPLATE), 1, Integer.MAX_VALUE));
        chestplateArmor = CachedIntValue.wrap(config, translations.chestplateArmor().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "ChestplateArmor", materialDefaults.defense(ArmorType.CHESTPLATE), 0, Integer.MAX_VALUE));
        helmetDurability = CachedIntValue.wrap(config, translations.helmetDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "HelmetDurability", materialDefaults.durability(ArmorType.HELMET), 1, Integer.MAX_VALUE));
        helmetArmor = CachedIntValue.wrap(config, translations.helmetArmor().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "HelmetArmor", materialDefaults.defense(ArmorType.HELMET), 0, Integer.MAX_VALUE));
        builder.pop();
    }

    private static float getActualValue(double val) {
        if (val > Float.MAX_VALUE) {
            return Float.MAX_VALUE;
        } else if (val < -Float.MAX_VALUE) {
            //Note: Float.MIN_VALUE is the smallest positive value a float can represent
            // the smallest value a float can represent overall is -Float.MAX_VALUE
            return -Float.MAX_VALUE;
        }
        return (float) val;
    }

    private boolean validateDamageModifier(Object value) {
        if (value instanceof Double) {
            float actualValue = getActualValue((double) value);
            float baseDamage = attackDamage.getOrDefault();
            return actualValue >= -baseDamage && actualValue <= Float.MAX_VALUE - baseDamage;
        }
        return false;
    }

    private Supplier<Double> validateDefaultModifier(double defaultModifier) {
        return () -> {
            if (validateDamageModifier(defaultModifier)) {
                return defaultModifier;
            }
            return (double) -attackDamage.getOrDefault();
        };
    }

    public static boolean nonNegativeFloat(Object value) {
        return value instanceof Double && getActualValue((double) value) >= 0;
    }

    public static boolean positiveFloat(Object value) {
        return value instanceof Double && getActualValue((double) value) > 0;
    }

    @Override
    public float swordDamage() {
        return swordDamage.get();
    }

    @Override
    public float swordAtkSpeed() {
        return swordAtkSpeed.get();
    }

    @Override
    public float shovelDamage() {
        return shovelDamage.get();
    }

    @Override
    public float shovelAtkSpeed() {
        return shovelAtkSpeed.get();
    }

    @Override
    public float axeDamage() {
        return axeDamage.get();
    }

    @Override
    public float axeAtkSpeed() {
        return axeAtkSpeed.get();
    }

    @Override
    public float pickaxeDamage() {
        return pickaxeDamage.get();
    }

    @Override
    public float pickaxeAtkSpeed() {
        return pickaxeAtkSpeed.get();
    }

    @Override
    public float hoeDamage() {
        return hoeDamage.get();
    }

    @Override
    public float hoeAtkSpeed() {
        return hoeAtkSpeed.get();
    }

    @Override
    public int paxelDurability() {
        return paxelDurability.get();
    }

    @Override
    public float paxelEfficiency() {
        return paxelEfficiency.get();
    }

    @Override
    public float paxelDamage() {
        return paxelDamage.get();
    }

    @Override
    public float paxelAtkSpeed() {
        return paxelAtkSpeed.get();
    }

    @Override
    public int paxelEnchantability() {
        return paxelEnchantability.get();
    }

    @Override
    public float paxelDisableBlockingSeconds() {
        return paxelDisableBlockingSeconds.get();
    }

    @Override
    public float spearAttackDuration() {
        return spearAttackDuration.get();
    }

    @Override
    public float spearDamageMultiplier() {
        return spearDamageMultiplier.get();
    }

    @Override
    public float spearDelay() {
        return spearDelay.get();
    }

    @Override
    public float spearDismountTime() {
        return spearDismountTime.get();
    }

    @Override
    public float spearDismountThreshold() {
        return spearDismountThreshold.get();
    }

    @Override
    public float spearKnockbackTime() {
        return spearKnockbackTime.get();
    }

    @Override
    public float spearKnockbackThreshold() {
        return spearKnockbackThreshold.get();
    }

    @Override
    public float spearDamageTime() {
        return spearDamageTime.get();
    }

    @Override
    public float spearDamageThreshold() {
        return spearDamageThreshold.get();
    }

    @Override
    public int shieldDurability() {
        return shieldDurability.get();
    }

    @Override
    public float shieldBlockDelay() {
        return shieldBlockDelay.get();
    }

    @Override
    public float shieldDisableCooldownScale() {
        return shieldDisableCooldownScale.get();
    }

    @Override
    public float shieldHorizontalBlockingAngle() {
        return shieldHorizontalBlockingAngle.get();
    }

    @Override
    public float shieldDamageReductionBase() {
        return shieldDamageReductionBase.get();
    }

    @Override
    public float shieldDamageReductionFactor() {
        return shieldDamageReductionFactor.get();
    }

    @Override
    public float shieldDamageThreshold() {
        return shieldDamageThreshold.get();
    }

    @Override
    public float shieldItemDamageBase() {
        return shieldItemDamageBase.get();
    }

    @Override
    public float shieldItemDamageFactor() {
        return shieldItemDamageFactor.get();
    }

    @Override
    public int durability() {
        return toolDurability.get();
    }

    @Override
    public float speed() {
        return efficiency.get();
    }

    @Override
    public float attackDamageBonus() {
        return attackDamage.get();
    }

    @Override
    public TagKey<Block> incorrectBlocksForDrops() {
        return fallBack.incorrectBlocksForDrops();
    }

    @Override
    public int durability(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> bootDurability.get();
            case LEGGINGS -> leggingDurability.get();
            case CHESTPLATE, BODY -> chestplateDurability.get();
            case HELMET -> helmetDurability.get();
        };
    }

    @Override
    public int defense(ArmorType armorType) {
        return switch (armorType) {
            case BOOTS -> bootArmor.get();
            case LEGGINGS -> leggingArmor.get();
            case CHESTPLATE -> chestplateArmor.get();
            case HELMET -> helmetArmor.get();
            default -> 0;
        };
    }

    @Override
    public int enchantmentValue() {
        return enchantability.get();
    }

    @Override
    public boolean burnsInFire() {
        return fallBack.burnsInFire();
    }

    @Override
    public float toughness() {
        return toughness.get();
    }

    @Override
    public int armorEnchantmentValue() {
        return armorEnchantability.get();
    }

    @Override
    public Holder<SoundEvent> equipSound() {
        return fallBack.equipSound();
    }

    @Override
    public TagKey<Item> repairItems() {
        return fallBack.repairItems();
    }

    @Override
    public String registryPrefix() {
        return fallBack.registryPrefix();
    }

    @Override
    public float knockbackResistance() {
        return knockbackResistance.get();
    }

    public ResourceKey<EquipmentAsset> equipmentAsset() {
        return equipmentAsset;
    }

    public ArmorMaterial toArmorMaterial(ArmorType type) {
        return new ArmorMaterial(
              //Best effort durability multiplier
              Mth.ceil(durability(type) / (float) type.getDurability(1)),
              Map.of(type, defense(type)),
              armorEnchantmentValue(),
              equipSound(),
              toughness(),
              knockbackResistance(),
              repairItems(),
              equipmentAsset()
        );
    }
}