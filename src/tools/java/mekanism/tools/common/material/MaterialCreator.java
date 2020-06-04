package mekanism.tools.common.material;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.ForgeConfigSpec;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class MaterialCreator extends BaseMekanismMaterial {

    private final BaseMekanismMaterial fallBack;

    private final CachedIntValue shieldDurability;
    private final CachedIntValue swordDamage;
    private final CachedFloatValue swordAtkSpeed;
    private final CachedFloatValue shovelDamage;
    private final CachedFloatValue shovelAtkSpeed;
    private final CachedFloatValue axeDamage;
    private final CachedFloatValue axeAtkSpeed;
    private final CachedIntValue pickaxeDamage;
    private final CachedFloatValue pickaxeAtkSpeed;
    private final CachedFloatValue hoeAtkSpeed;
    private final CachedIntValue paxelHarvestLevel;
    private final CachedFloatValue paxelDamage;
    private final CachedFloatValue paxelAtkSpeed;
    private final CachedFloatValue paxelEfficiency;
    private final CachedIntValue paxelEnchantability;
    private final CachedIntValue paxelMaxUses;
    private final CachedIntValue toolMaxUses;
    private final CachedFloatValue efficiency;
    private final CachedFloatValue attackDamage;
    private final CachedIntValue harvestLevel;
    private final CachedIntValue enchantability;
    private final CachedFloatValue toughness;
    private final CachedIntValue bootDurability;
    private final CachedIntValue leggingDurability;
    private final CachedIntValue chestplateDurability;
    private final CachedIntValue helmetDurability;
    private final CachedIntValue bootArmor;
    private final CachedIntValue leggingArmor;
    private final CachedIntValue chestplateArmor;
    private final CachedIntValue helmetArmor;

    public MaterialCreator(IMekanismConfig config, ForgeConfigSpec.Builder builder, BaseMekanismMaterial materialDefaults) {
        fallBack = materialDefaults;
        String toolKey = materialDefaults.getRegistryPrefix();
        builder.comment(" Material Settings for " + toolKey).push(toolKey);
        shieldDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " shields.")
              .defineInRange(toolKey + "ShieldDurability", materialDefaults.getShieldDurability(), 0, Integer.MAX_VALUE));
        swordDamage = CachedIntValue.wrap(config, builder.comment("Attack damage modifier of " + toolKey + " swords.")
              .defineInRange(toolKey + "SwordDamage", materialDefaults.getSwordDamage(), 0, Integer.MAX_VALUE));
        swordAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + toolKey + " swords.")
              .define(toolKey + "SwordAtkSpeed", (double) materialDefaults.getSwordAtkSpeed()));
        shovelDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + toolKey + " shovels.")
              .defineInRange(toolKey + "ShovelDamage", materialDefaults.getShovelDamage(), 0, Float.MAX_VALUE));
        shovelAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + toolKey + " shovels.")
              .define(toolKey + "ShovelAtkSpeed", (double) materialDefaults.getShovelAtkSpeed()));
        axeDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + toolKey + " axes.")
              .defineInRange(toolKey + "AxeDamage", materialDefaults.getAxeDamage(), 0, Float.MAX_VALUE));
        axeAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + toolKey + " axes.")
              .define(toolKey + "AxeAtkSpeed", (double) materialDefaults.getAxeAtkSpeed()));
        pickaxeDamage = CachedIntValue.wrap(config, builder.comment("Attack damage modifier of " + toolKey + " pickaxes.")
              .defineInRange(toolKey + "PickaxeDamage", materialDefaults.getPickaxeDamage(), 0, Integer.MAX_VALUE));
        pickaxeAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + toolKey + " pickaxes.")
              .define(toolKey + "PickaxeAtkSpeed", (double) materialDefaults.getPickaxeAtkSpeed()));
        hoeAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + toolKey + " hoes.")
              .define(toolKey + "HoeAtkSpeed", (double) materialDefaults.getHoeAtkSpeed()));
        toolMaxUses = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " tools.")
              .defineInRange(toolKey + "ToolMaxUses", materialDefaults.getMaxUses(), 1, Integer.MAX_VALUE));
        efficiency = CachedFloatValue.wrap(config, builder.comment("Efficiency of " + toolKey + " tools.")
              .define(toolKey + "Efficiency", (double) materialDefaults.getEfficiency()));
        paxelHarvestLevel = CachedIntValue.wrap(config, builder.comment("Harvest level of " + toolKey + " paxels.")
              .defineInRange(toolKey + "PaxelHarvestLevel", materialDefaults.getPaxelHarvestLevel(), 0, Integer.MAX_VALUE));
        paxelDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + toolKey + " paxels.")
              .defineInRange(toolKey + "PaxelDamage", materialDefaults.getPaxelDamage(), 0, Float.MAX_VALUE));
        paxelAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + toolKey + " paxels.")
              .define(toolKey + "PaxelAtkSpeed", (double) materialDefaults.getPaxelAtkSpeed()));
        paxelEfficiency = CachedFloatValue.wrap(config, builder.comment("Efficiency of " + toolKey + " paxels.")
              .define(toolKey + "PaxelEfficiency", (double) materialDefaults.getPaxelEfficiency()));
        paxelEnchantability = CachedIntValue.wrap(config, builder.comment("Natural enchantability factor of " + toolKey + " paxels.")
              .defineInRange(toolKey + "PaxelEnchantability", materialDefaults.getPaxelEnchantability(), 0, Integer.MAX_VALUE));
        paxelMaxUses = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " paxels.")
              .defineInRange(toolKey + "PaxelMaxUses", materialDefaults.getPaxelMaxUses(), 1, Integer.MAX_VALUE));
        attackDamage = CachedFloatValue.wrap(config, builder.comment("Base attack damage of " + toolKey + " items.")
              .defineInRange(toolKey + "AttackDamage", materialDefaults.getAttackDamage(), 0, Float.MAX_VALUE));
        harvestLevel = CachedIntValue.wrap(config, builder.comment("Harvest level of " + toolKey + " tools.")
              .defineInRange(toolKey + "HarvestLevel", materialDefaults.getHarvestLevel(), 0, Integer.MAX_VALUE));
        enchantability = CachedIntValue.wrap(config, builder.comment("Natural enchantability factor of " + toolKey + " items.")
              .defineInRange(toolKey + "Enchantability", materialDefaults.getCommonEnchantability(), 0, Integer.MAX_VALUE));
        toughness = CachedFloatValue.wrap(config, builder.comment("Base armor toughness value of " + toolKey + " armor.")
              .defineInRange(toolKey + "Toughness", materialDefaults.getToughness(), 0, Float.MAX_VALUE));
        bootDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " boots.")
              .defineInRange(toolKey + "BootDurability", materialDefaults.getDurability(EquipmentSlotType.FEET), 1, Integer.MAX_VALUE));
        leggingDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " leggings.")
              .defineInRange(toolKey + "LeggingDurability", materialDefaults.getDurability(EquipmentSlotType.LEGS), 1, Integer.MAX_VALUE));
        chestplateDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " chestplates.")
              .defineInRange(toolKey + "ChestplateDurability", materialDefaults.getDurability(EquipmentSlotType.CHEST), 1, Integer.MAX_VALUE));
        helmetDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " helmets.")
              .defineInRange(toolKey + "HelmetDurability", materialDefaults.getDurability(EquipmentSlotType.HEAD), 1, Integer.MAX_VALUE));
        bootArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + toolKey + " boots.")
              .defineInRange(toolKey + "BootArmor", materialDefaults.getDamageReductionAmount(EquipmentSlotType.FEET), 0, Integer.MAX_VALUE));
        leggingArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + toolKey + " leggings.")
              .defineInRange(toolKey + "LeggingArmor", materialDefaults.getDamageReductionAmount(EquipmentSlotType.LEGS), 0, Integer.MAX_VALUE));
        chestplateArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + toolKey + " chestplates.")
              .defineInRange(toolKey + "ChestplateArmor", materialDefaults.getDamageReductionAmount(EquipmentSlotType.CHEST), 0, Integer.MAX_VALUE));
        helmetArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + toolKey + " helmets.")
              .defineInRange(toolKey + "HelmetArmor", materialDefaults.getDamageReductionAmount(EquipmentSlotType.HEAD), 0, Integer.MAX_VALUE));
        builder.pop();
    }

    @Override
    public int getShieldDurability() {
        return shieldDurability.get();
    }

    @Override
    public int getSwordDamage() {
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
    public int getPickaxeDamage() {
        return pickaxeDamage.get();
    }

    @Override
    public float getPickaxeAtkSpeed() {
        return pickaxeAtkSpeed.get();
    }

    @Override
    public float getHoeAtkSpeed() {
        return hoeAtkSpeed.get();
    }

    @Override
    public int getPaxelHarvestLevel() {
        return paxelHarvestLevel.get();
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
    public int getMaxUses() {
        return toolMaxUses.get();
    }

    @Override
    public float getEfficiency() {
        return efficiency.get();
    }

    @Override
    public float getAttackDamage() {
        return attackDamage.get();
    }

    @Override
    public int getHarvestLevel() {
        return harvestLevel.get();
    }

    @Override
    public int getDurability(EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return bootDurability.get();
            case LEGS:
                return leggingDurability.get();
            case CHEST:
                return chestplateDurability.get();
            case HEAD:
                return helmetDurability.get();
        }
        return fallBack.getDurability(slotType);
    }

    @Override
    public int getDamageReductionAmount(EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return bootArmor.get();
            case LEGS:
                return leggingArmor.get();
            case CHEST:
                return chestplateArmor.get();
            case HEAD:
                return helmetArmor.get();
        }
        return fallBack.getDamageReductionAmount(slotType);
    }

    @Override
    public int getCommonEnchantability() {
        return enchantability.get();
    }

    @Override
    public float getToughness() {
        return toughness.get();
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return fallBack.getSoundEvent();
    }

    @Nonnull
    @Override
    public Ingredient getCommonRepairMaterial() {
        return fallBack.getCommonRepairMaterial();
    }

    /**
     * Only used on the client in vanilla
     */
    @Nonnull
    @Override
    public String getName() {
        return fallBack.getName();
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return fallBack.getRegistryPrefix();
    }

    @Override
    public int getPaxelEnchantability() {
        return paxelEnchantability.get();
    }
}