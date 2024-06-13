package mekanism.tools.common.material;

import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
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
    private final CachedIntValue paxelMaxUses;
    private final CachedIntValue toolMaxUses;
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
        String name = getConfigCommentName();
        builder.comment("Material Settings for " + name).push(toolKey);
        attackDamage = CachedFloatValue.wrap(config, builder.comment("Base attack damage of " + name + " items.")
              .defineInRange(toolKey + "AttackDamage", materialDefaults.getAttackDamageBonus(), 0, Float.MAX_VALUE));
        //Note: Damage predicate to allow for tools to go negative to the value of the base tier so that a tool
        // can effectively have zero damage for things like the hoe
        Predicate<Object> damageModifierPredicate = value -> {
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
                float baseDamage = attackDamage.get();
                return actualValue >= -baseDamage && actualValue <= Float.MAX_VALUE - baseDamage;
            }
            return false;
        };
        shieldDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + name + " shields.")
              .defineInRange(toolKey + "ShieldDurability", materialDefaults.getShieldDurability(), 0, Integer.MAX_VALUE));
        swordDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + name + " swords.")
              .define(toolKey + "SwordDamage", (double) materialDefaults.getSwordDamage(), damageModifierPredicate));
        swordAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + name + " swords.")
              .define(toolKey + "SwordAtkSpeed", (double) materialDefaults.getSwordAtkSpeed()));
        shovelDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + name + " shovels.")
              .define(toolKey + "ShovelDamage", (double) materialDefaults.getShovelDamage(), damageModifierPredicate));
        shovelAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + name + " shovels.")
              .define(toolKey + "ShovelAtkSpeed", (double) materialDefaults.getShovelAtkSpeed()));
        axeDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + name + " axes.")
              .define(toolKey + "AxeDamage", (double) materialDefaults.getAxeDamage(), damageModifierPredicate));
        axeAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + name + " axes.")
              .define(toolKey + "AxeAtkSpeed", (double) materialDefaults.getAxeAtkSpeed()));
        pickaxeDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + name + " pickaxes.")
              .define(toolKey + "PickaxeDamage", (double) materialDefaults.getPickaxeDamage(), damageModifierPredicate));
        pickaxeAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + name + " pickaxes.")
              .define(toolKey + "PickaxeAtkSpeed", (double) materialDefaults.getPickaxeAtkSpeed()));
        hoeDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + name + " hoes.")
              .define(toolKey + "HoeDamage", (double) materialDefaults.getHoeDamage(), damageModifierPredicate));
        hoeAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + name + " hoes.")
              .define(toolKey + "HoeAtkSpeed", (double) materialDefaults.getHoeAtkSpeed()));
        toolMaxUses = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + name + " tools.")
              .defineInRange(toolKey + "ToolMaxUses", materialDefaults.getUses(), 1, Integer.MAX_VALUE));
        efficiency = CachedFloatValue.wrap(config, builder.comment("Efficiency of " + name + " tools.")
              .define(toolKey + "Efficiency", (double) materialDefaults.getSpeed()));
        paxelDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + name + " paxels.")
              .define(toolKey + "PaxelDamage", (double) materialDefaults.getPaxelDamage(), damageModifierPredicate));
        paxelAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + name + " paxels.")
              .define(toolKey + "PaxelAtkSpeed", (double) materialDefaults.getPaxelAtkSpeed()));
        paxelEfficiency = CachedFloatValue.wrap(config, builder.comment("Efficiency of " + name + " paxels.")
              .define(toolKey + "PaxelEfficiency", (double) materialDefaults.getPaxelEfficiency()));
        paxelEnchantability = CachedIntValue.wrap(config, builder.comment("Natural enchantability factor of " + name + " paxels.")
              .defineInRange(toolKey + "PaxelEnchantability", materialDefaults.getPaxelEnchantability(), 0, Integer.MAX_VALUE));
        paxelMaxUses = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + name + " paxels.")
              .defineInRange(toolKey + "PaxelMaxUses", materialDefaults.getPaxelMaxUses(), 1, Integer.MAX_VALUE));
        enchantability = CachedIntValue.wrap(config, builder.comment("Natural enchantability factor of " + name + " items.")
              .defineInRange(toolKey + "Enchantability", materialDefaults.getEnchantmentValue(), 0, Integer.MAX_VALUE));
        toughness = CachedFloatValue.wrap(config, builder.comment("Base armor toughness value of " + name + " armor.")
              .defineInRange(toolKey + "Toughness", materialDefaults.toughness(), 0, Float.MAX_VALUE));
        knockbackResistance = CachedFloatValue.wrap(config, builder.comment("Base armor knockback resistance value of " + name + " armor.")
              .defineInRange(toolKey + "KnockbackResistance", materialDefaults.knockbackResistance(), 0, Float.MAX_VALUE));
        bootDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + name + " boots.")
              .defineInRange(toolKey + "BootDurability", materialDefaults.getDurabilityForType(ArmorItem.Type.BOOTS), 1, Integer.MAX_VALUE));
        leggingDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + name + " leggings.")
              .defineInRange(toolKey + "LeggingDurability", materialDefaults.getDurabilityForType(ArmorItem.Type.LEGGINGS), 1, Integer.MAX_VALUE));
        chestplateDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + name + " chestplates.")
              .defineInRange(toolKey + "ChestplateDurability", materialDefaults.getDurabilityForType(ArmorItem.Type.CHESTPLATE), 1, Integer.MAX_VALUE));
        helmetDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + name + " helmets.")
              .defineInRange(toolKey + "HelmetDurability", materialDefaults.getDurabilityForType(ArmorItem.Type.HELMET), 1, Integer.MAX_VALUE));
        bootArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + name + " boots.")
              .defineInRange(toolKey + "BootArmor", materialDefaults.getDefense(ArmorItem.Type.BOOTS), 0, Integer.MAX_VALUE));
        leggingArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + name + " leggings.")
              .defineInRange(toolKey + "LeggingArmor", materialDefaults.getDefense(ArmorItem.Type.LEGGINGS), 0, Integer.MAX_VALUE));
        chestplateArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + name + " chestplates.")
              .defineInRange(toolKey + "ChestplateArmor", materialDefaults.getDefense(ArmorItem.Type.CHESTPLATE), 0, Integer.MAX_VALUE));
        helmetArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + name + " helmets.")
              .defineInRange(toolKey + "HelmetArmor", materialDefaults.getDefense(ArmorItem.Type.HELMET), 0, Integer.MAX_VALUE));
        builder.pop();
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
    public int getPaxelMaxUses() {
        return paxelMaxUses.get();
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
        return toolMaxUses.get();
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
    public String getConfigCommentName() {
        return fallBack.getConfigCommentName();
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