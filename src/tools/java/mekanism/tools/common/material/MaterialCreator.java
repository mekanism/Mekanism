package mekanism.tools.common.material;

import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.tools.common.config.ToolsConfigTranslations.MaterialTranslations;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ModConfigSpec;

@NothingNullByDefault
public class MaterialCreator implements BaseMekanismMaterial {

    private final BaseMekanismMaterial fallBack;

    private final CachedIntValue shieldDurability;
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
    private final CachedIntValue toolDurability;
    private final CachedFloatValue efficiency;
    private final CachedFloatValue attackDamage;
    private final CachedIntValue enchantability;
    private final CachedFloatValue toughness;
    private final CachedFloatValue knockbackResistance;
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
        String toolKey = getRegistryPrefix();
        MaterialTranslations translations = MaterialTranslations.create(toolKey);
        translations.topLevel().applyToBuilder(builder).push(toolKey);
        toolDurability = CachedIntValue.wrap(config, translations.toolDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "ToolDurability", materialDefaults.getUses(), 1, Integer.MAX_VALUE));
        efficiency = CachedFloatValue.wrap(config, translations.efficiency().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "Efficiency", (double) materialDefaults.getSpeed()));
        attackDamage = CachedFloatValue.wrap(config, translations.damage().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "AttackDamage", materialDefaults.getAttackDamageBonus(), 0, Float.MAX_VALUE));
        enchantability = CachedIntValue.wrap(config, translations.enchantability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "Enchantability", materialDefaults.getEnchantmentValue(), 0, Integer.MAX_VALUE));
        //Note: Damage predicate to allow for tools to go negative to the value of the base tier so that a tool
        // can effectively have zero damage for things like the hoe
        Predicate<Object> damageModifierPredicate = this::validateDamageModifier;
        shieldDurability = CachedIntValue.wrap(config, translations.shieldDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "ShieldDurability", materialDefaults.getShieldDurability(), 0, Integer.MAX_VALUE));
        swordDamage = CachedFloatValue.wrap(config, translations.swordDamage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SwordDamage", validateDefaultModifier(materialDefaults.getSwordDamage()), damageModifierPredicate));
        swordAtkSpeed = CachedFloatValue.wrap(config, translations.swordAtkSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "SwordAtkSpeed", (double) materialDefaults.getSwordAtkSpeed()));
        shovelDamage = CachedFloatValue.wrap(config, translations.shovelDamage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "ShovelDamage", validateDefaultModifier(materialDefaults.getShovelDamage()), damageModifierPredicate));
        shovelAtkSpeed = CachedFloatValue.wrap(config, translations.shovelAtkSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "ShovelAtkSpeed", (double) materialDefaults.getShovelAtkSpeed()));
        axeDamage = CachedFloatValue.wrap(config, translations.axeDamage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "AxeDamage", validateDefaultModifier(materialDefaults.getAxeDamage()), damageModifierPredicate));
        axeAtkSpeed = CachedFloatValue.wrap(config, translations.axeAtkSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "AxeAtkSpeed", (double) materialDefaults.getAxeAtkSpeed()));
        pickaxeDamage = CachedFloatValue.wrap(config, translations.pickaxeDamage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PickaxeDamage", validateDefaultModifier(materialDefaults.getPickaxeDamage()), damageModifierPredicate));
        pickaxeAtkSpeed = CachedFloatValue.wrap(config, translations.pickaxeAtkSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PickaxeAtkSpeed", (double) materialDefaults.getPickaxeAtkSpeed()));
        hoeDamage = CachedFloatValue.wrap(config, translations.hoeDamage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "HoeDamage", validateDefaultModifier(materialDefaults.getHoeDamage()), damageModifierPredicate));
        hoeAtkSpeed = CachedFloatValue.wrap(config, translations.hoeAtkSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "HoeAtkSpeed", (double) materialDefaults.getHoeAtkSpeed()));
        paxelDamage = CachedFloatValue.wrap(config, translations.paxelDamage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PaxelDamage", validateDefaultModifier(materialDefaults.getPaxelDamage()), damageModifierPredicate));
        paxelAtkSpeed = CachedFloatValue.wrap(config, translations.paxelAtkSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PaxelAtkSpeed", (double) materialDefaults.getPaxelAtkSpeed()));
        paxelEfficiency = CachedFloatValue.wrap(config, translations.paxelEfficiency().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PaxelEfficiency", (double) materialDefaults.getPaxelEfficiency()));
        paxelEnchantability = CachedIntValue.wrap(config, translations.paxelEnchantability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "PaxelEnchantability", materialDefaults.getPaxelEnchantability(), 0, Integer.MAX_VALUE));
        paxelDurability = CachedIntValue.wrap(config, translations.paxelDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "PaxelDurability", materialDefaults.getPaxelDurability(), 1, Integer.MAX_VALUE));
        toughness = CachedFloatValue.wrap(config, translations.toughness().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "Toughness", materialDefaults.toughness(), 0, Float.MAX_VALUE));
        knockbackResistance = CachedFloatValue.wrap(config, translations.knockbackResistance().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "KnockbackResistance", materialDefaults.knockbackResistance(), 0, Float.MAX_VALUE));
        bootDurability = CachedIntValue.wrap(config, translations.bootDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "BootDurability", materialDefaults.getDurabilityForType(ArmorItem.Type.BOOTS), 1, Integer.MAX_VALUE));
        bootArmor = CachedIntValue.wrap(config, translations.bootArmor().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "BootArmor", materialDefaults.getDefense(ArmorItem.Type.BOOTS), 0, Integer.MAX_VALUE));
        leggingDurability = CachedIntValue.wrap(config, translations.leggingDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "LeggingDurability", materialDefaults.getDurabilityForType(ArmorItem.Type.LEGGINGS), 1, Integer.MAX_VALUE));
        leggingArmor = CachedIntValue.wrap(config, translations.leggingArmor().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "LeggingArmor", materialDefaults.getDefense(ArmorItem.Type.LEGGINGS), 0, Integer.MAX_VALUE));
        chestplateDurability = CachedIntValue.wrap(config, translations.chestplateDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "ChestplateDurability", materialDefaults.getDurabilityForType(ArmorItem.Type.CHESTPLATE), 1, Integer.MAX_VALUE));
        chestplateArmor = CachedIntValue.wrap(config, translations.chestplateArmor().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "ChestplateArmor", materialDefaults.getDefense(ArmorItem.Type.CHESTPLATE), 0, Integer.MAX_VALUE));
        helmetDurability = CachedIntValue.wrap(config, translations.helmetDurability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "HelmetDurability", materialDefaults.getDurabilityForType(ArmorItem.Type.HELMET), 1, Integer.MAX_VALUE));
        helmetArmor = CachedIntValue.wrap(config, translations.helmetArmor().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "HelmetArmor", materialDefaults.getDefense(ArmorItem.Type.HELMET), 0, Integer.MAX_VALUE));
        builder.pop();
    }

    private boolean validateDamageModifier(Object value) {
        if (value instanceof Double) {
            double val = (double) value;
            float actualValue;
            if (val > Float.MAX_VALUE) {
                actualValue = Float.MAX_VALUE;
            } else if (val < -Float.MAX_VALUE) {
                //Note: Float.MIN_VALUE is the smallest positive value a float can represent
                // the smallest value a float can represent overall is -Float.MAX_VALUE
                actualValue = -Float.MAX_VALUE;
            } else {
                actualValue = (float) val;
            }
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

    @Override
    public int getShieldDurability() {
        return shieldDurability.get();
    }

    @Override
    public float getSwordDamage() {
        return swordDamage.get();
    }

    @Override
    public float getSwordAtkSpeed() {
        return swordAtkSpeed.get();
    }

    @Override
    public float getShovelDamage() {
        return shovelDamage.get();
    }

    @Override
    public float getShovelAtkSpeed() {
        return shovelAtkSpeed.get();
    }

    @Override
    public float getAxeDamage() {
        return axeDamage.get();
    }

    @Override
    public float getAxeAtkSpeed() {
        return axeAtkSpeed.get();
    }

    @Override
    public float getPickaxeDamage() {
        return pickaxeDamage.get();
    }

    @Override
    public float getPickaxeAtkSpeed() {
        return pickaxeAtkSpeed.get();
    }

    @Override
    public float getHoeDamage() {
        return hoeDamage.get();
    }

    @Override
    public float getHoeAtkSpeed() {
        return hoeAtkSpeed.get();
    }

    @Override
    public int getPaxelDurability() {
        return paxelDurability.get();
    }

    @Override
    public float getPaxelEfficiency() {
        return paxelEfficiency.get();
    }

    @Override
    public float getPaxelDamage() {
        return paxelDamage.get();
    }

    @Override
    public float getPaxelAtkSpeed() {
        return paxelAtkSpeed.get();
    }

    @Override
    public int getUses() {
        return toolDurability.get();
    }

    @Override
    public float getSpeed() {
        return efficiency.get();
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamage.get();
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return fallBack.getIncorrectBlocksForDrops();
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type armorType) {
        return switch (armorType) {
            case BOOTS -> bootDurability.get();
            case LEGGINGS -> leggingDurability.get();
            case CHESTPLATE, BODY -> chestplateDurability.get();
            case HELMET -> helmetDurability.get();
        };
    }

    @Override
    public int getDefense(ArmorItem.Type armorType) {
        return switch (armorType) {
            case BOOTS -> bootArmor.get();
            case LEGGINGS -> leggingArmor.get();
            case CHESTPLATE -> chestplateArmor.get();
            case HELMET -> helmetArmor.get();
            default -> 0;
        };
    }

    @Override
    public int getEnchantmentValue() {
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
    public Holder<SoundEvent> equipSound() {
        return fallBack.equipSound();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return fallBack.getRepairIngredient();
    }

    @Override
    public String getRegistryPrefix() {
        return fallBack.getRegistryPrefix();
    }

    @Override
    public int getPaxelEnchantability() {
        return paxelEnchantability.get();
    }

    @Override
    public float knockbackResistance() {
        return knockbackResistance.get();
    }
}