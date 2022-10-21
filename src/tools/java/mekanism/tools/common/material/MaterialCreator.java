package mekanism.tools.common.material;

import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class MaterialCreator extends BaseMekanismMaterial {

    private final BaseMekanismMaterial fallBack;

    private final CachedIntValue shieldDurability;
    public final CachedFloatValue swordDamage;
    public final CachedFloatValue swordAtkSpeed;
    public final CachedFloatValue shovelDamage;
    public final CachedFloatValue shovelAtkSpeed;
    public final CachedFloatValue axeDamage;
    public final CachedFloatValue axeAtkSpeed;
    public final CachedFloatValue pickaxeDamage;
    public final CachedFloatValue pickaxeAtkSpeed;
    public final CachedFloatValue hoeDamage;
    public final CachedFloatValue hoeAtkSpeed;
    public final CachedFloatValue paxelDamage;
    public final CachedFloatValue paxelAtkSpeed;
    private final CachedFloatValue paxelEfficiency;
    private final CachedIntValue paxelEnchantability;
    private final CachedIntValue paxelMaxUses;
    private final CachedIntValue toolMaxUses;
    private final CachedFloatValue efficiency;
    public final CachedFloatValue attackDamage;
    private final CachedIntValue enchantability;
    public final CachedFloatValue toughness;
    public final CachedFloatValue knockbackResistance;
    private final CachedIntValue bootDurability;
    private final CachedIntValue leggingDurability;
    private final CachedIntValue chestplateDurability;
    private final CachedIntValue helmetDurability;
    public final CachedIntValue bootArmor;
    public final CachedIntValue leggingArmor;
    public final CachedIntValue chestplateArmor;
    public final CachedIntValue helmetArmor;

    public MaterialCreator(IMekanismConfig config, ForgeConfigSpec.Builder builder, BaseMekanismMaterial materialDefaults) {
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
              .defineInRange(toolKey + "Enchantability", materialDefaults.getCommonEnchantability(), 0, Integer.MAX_VALUE));
        toughness = CachedFloatValue.wrap(config, builder.comment("Base armor toughness value of " + name + " armor.")
              .defineInRange(toolKey + "Toughness", materialDefaults.getToughness(), 0, Float.MAX_VALUE));
        knockbackResistance = CachedFloatValue.wrap(config, builder.comment("Base armor knockback resistance value of " + name + " armor.")
              .defineInRange(toolKey + "KnockbackResistance", materialDefaults.getKnockbackResistance(), 0, Float.MAX_VALUE));
        bootDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + name + " boots.")
              .defineInRange(toolKey + "BootDurability", materialDefaults.getDurabilityForSlot(EquipmentSlot.FEET), 1, Integer.MAX_VALUE));
        leggingDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + name + " leggings.")
              .defineInRange(toolKey + "LeggingDurability", materialDefaults.getDurabilityForSlot(EquipmentSlot.LEGS), 1, Integer.MAX_VALUE));
        chestplateDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + name + " chestplates.")
              .defineInRange(toolKey + "ChestplateDurability", materialDefaults.getDurabilityForSlot(EquipmentSlot.CHEST), 1, Integer.MAX_VALUE));
        helmetDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + name + " helmets.")
              .defineInRange(toolKey + "HelmetDurability", materialDefaults.getDurabilityForSlot(EquipmentSlot.HEAD), 1, Integer.MAX_VALUE));
        bootArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + name + " boots.")
              .defineInRange(toolKey + "BootArmor", materialDefaults.getDefenseForSlot(EquipmentSlot.FEET), 0, Integer.MAX_VALUE));
        leggingArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + name + " leggings.")
              .defineInRange(toolKey + "LeggingArmor", materialDefaults.getDefenseForSlot(EquipmentSlot.LEGS), 0, Integer.MAX_VALUE));
        chestplateArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + name + " chestplates.")
              .defineInRange(toolKey + "ChestplateArmor", materialDefaults.getDefenseForSlot(EquipmentSlot.CHEST), 0, Integer.MAX_VALUE));
        helmetArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + name + " helmets.")
              .defineInRange(toolKey + "HelmetArmor", materialDefaults.getDefenseForSlot(EquipmentSlot.HEAD), 0, Integer.MAX_VALUE));
        builder.pop();
    }

    @Override
    public int getShieldDurability() {
        return shieldDurability.getOrDefault();
    }

    @Override
    public float getSwordDamage() {
        return swordDamage.getOrDefault();
    }

    @Override
    public float getSwordAtkSpeed() {
        return swordAtkSpeed.getOrDefault();
    }

    @Override
    public float getShovelDamage() {
        return shovelDamage.getOrDefault();
    }

    @Override
    public float getShovelAtkSpeed() {
        return shovelAtkSpeed.getOrDefault();
    }

    @Override
    public float getAxeDamage() {
        return axeDamage.getOrDefault();
    }

    @Override
    public float getAxeAtkSpeed() {
        return axeAtkSpeed.getOrDefault();
    }

    @Override
    public float getPickaxeDamage() {
        return pickaxeDamage.getOrDefault();
    }

    @Override
    public float getPickaxeAtkSpeed() {
        return pickaxeAtkSpeed.getOrDefault();
    }

    @Override
    public float getHoeDamage() {
        return hoeDamage.getOrDefault();
    }

    @Override
    public float getHoeAtkSpeed() {
        return hoeAtkSpeed.getOrDefault();
    }

    @Override
    public int getPaxelMaxUses() {
        return paxelMaxUses.getOrDefault();
    }

    @Override
    public float getPaxelEfficiency() {
        return paxelEfficiency.get();
    }

    @Override
    public float getPaxelDamage() {
        return paxelDamage.getOrDefault();
    }

    @Override
    public float getPaxelAtkSpeed() {
        return paxelAtkSpeed.getOrDefault();
    }

    @Override
    public int getUses() {
        return toolMaxUses.getOrDefault();
    }

    @Override
    public float getSpeed() {
        return efficiency.getOrDefault();
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamage.getOrDefault();
    }

    @Override
    @Deprecated
    public int getLevel() {
        return fallBack.getLevel();
    }

    @Nullable
    @Override
    public TagKey<Block> getTag() {
        return fallBack.getTag();
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlot slotType) {
        return switch (slotType) {
            case FEET -> bootDurability.getOrDefault();
            case LEGS -> leggingDurability.getOrDefault();
            case CHEST -> chestplateDurability.getOrDefault();
            case HEAD -> helmetDurability.getOrDefault();
            default -> fallBack.getDurabilityForSlot(slotType);
        };
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slotType) {
        return switch (slotType) {
            case FEET -> bootArmor.getOrDefault();
            case LEGS -> leggingArmor.getOrDefault();
            case CHEST -> chestplateArmor.getOrDefault();
            case HEAD -> helmetArmor.getOrDefault();
            default -> fallBack.getDefenseForSlot(slotType);
        };
    }

    @Override
    public int getCommonEnchantability() {
        return enchantability.get();
    }

    @Override
    public boolean burnsInFire() {
        return fallBack.burnsInFire();
    }

    @Override
    public float getToughness() {
        return toughness.getOrDefault();
    }

    @Override
    public SoundEvent getEquipSound() {
        return fallBack.getEquipSound();
    }

    @Override
    public Ingredient getCommonRepairMaterial() {
        return fallBack.getCommonRepairMaterial();
    }

    @Override
    public String getConfigCommentName() {
        return fallBack.getConfigCommentName();
    }

    /**
     * Only used on the client in vanilla
     */
    @Override
    public String getName() {
        return fallBack.getName();
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
    public float getKnockbackResistance() {
        return knockbackResistance.getOrDefault();
    }
}