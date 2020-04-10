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

    //TODO: Limits
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
        //TODO: Do we want to remove the requires world restart given they actually seem to sync properly and fine without
        // a world restart. Though if other mods cache any values then they would not necessarily behave properly
        swordDamage = CachedIntValue.wrap(config, builder.comment("Attack damage modifier of " + toolKey + " swords.")
              .worldRestart()
              .define(toolKey + "SwordDamage", materialDefaults.getSwordDamage()));
        swordAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + toolKey + " swords.")
              .worldRestart()
              .define(toolKey + "SwordAtkSpeed", (double) materialDefaults.getSwordAtkSpeed()));
        shovelDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + toolKey + " shovels.")
              .worldRestart()
              .define(toolKey + "ShovelDamage", (double) materialDefaults.getShovelDamage()));
        shovelAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + toolKey + " shovels.")
              .worldRestart()
              .define(toolKey + "ShovelAtkSpeed", (double) materialDefaults.getShovelAtkSpeed()));
        axeDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + toolKey + " axes.")
              .worldRestart()
              .define(toolKey + "AxeDamage", (double) materialDefaults.getAxeDamage()));
        axeAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + toolKey + " axes.")
              .worldRestart()
              .define(toolKey + "AxeAtkSpeed", (double) materialDefaults.getAxeAtkSpeed()));
        pickaxeDamage = CachedIntValue.wrap(config, builder.comment("Attack damage modifier of " + toolKey + " pickaxes.")
              .worldRestart()
              .define(toolKey + "PickaxeDamage", materialDefaults.getPickaxeDamage()));
        pickaxeAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + toolKey + " pickaxes.")
              .worldRestart()
              .define(toolKey + "PickaxeAtkSpeed", (double) materialDefaults.getPickaxeAtkSpeed()));
        hoeAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + toolKey + " hoes.")
              .worldRestart()
              .define(toolKey + "HoeAtkSpeed", (double) materialDefaults.getHoeAtkSpeed()));
        toolMaxUses = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " tools.")
              .worldRestart()
              .define(toolKey + "ToolMaxUses", materialDefaults.getMaxUses()));
        efficiency = CachedFloatValue.wrap(config, builder.comment("Efficiency of " + toolKey + " tools.")
              .worldRestart()
              .define(toolKey + "Efficiency", (double) materialDefaults.getEfficiency()));
        paxelHarvestLevel = CachedIntValue.wrap(config, builder.comment("Harvest level of " + toolKey + " paxels.")
              .worldRestart()
              .define(toolKey + "PaxelHarvestLevel", materialDefaults.getPaxelHarvestLevel()));
        paxelDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + toolKey + " paxels.")
              .worldRestart()
              .define(toolKey + "PaxelDamage", (double) materialDefaults.getPaxelDamage()));
        paxelAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + toolKey + " paxels.")
              .worldRestart()
              .define(toolKey + "PaxelAtkSpeed", (double) materialDefaults.getPaxelAtkSpeed()));
        paxelEfficiency = CachedFloatValue.wrap(config, builder.comment("Efficiency of " + toolKey + " paxels.")
              .worldRestart()
              .define(toolKey + "PaxelEfficiency", (double) materialDefaults.getPaxelEfficiency()));
        paxelEnchantability = CachedIntValue.wrap(config, builder.comment("Natural enchantability factor of " + toolKey + " paxels.")
              .worldRestart()
              .define(toolKey + "PaxelEnchantability", materialDefaults.getPaxelEnchantability()));
        paxelMaxUses = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " paxels.")
              .worldRestart()
              .define(toolKey + "PaxelMaxUses", materialDefaults.getPaxelMaxUses()));
        attackDamage = CachedFloatValue.wrap(config, builder.comment("Base attack damage of " + toolKey + " items.")
              .worldRestart()
              .define(toolKey + "AttackDamage", (double) materialDefaults.getAttackDamage()));
        harvestLevel = CachedIntValue.wrap(config, builder.comment("Harvest level of " + toolKey + " tools.")
              .worldRestart()
              .define(toolKey + "HarvestLevel", materialDefaults.getHarvestLevel()));
        enchantability = CachedIntValue.wrap(config, builder.comment("Natural enchantability factor of " + toolKey + " items.")
              .worldRestart()
              .define(toolKey + "Enchantability", materialDefaults.getCommonEnchantability()));
        toughness = CachedFloatValue.wrap(config, builder.comment("Base armor toughness value of " + toolKey + " armor.")
              .worldRestart()
              .define(toolKey + "Toughness", (double) materialDefaults.getToughness()));
        bootDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " boots.")
              .worldRestart()
              .define(toolKey + "BootDurability", materialDefaults.getDurability(EquipmentSlotType.FEET)));
        leggingDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " leggings.")
              .worldRestart()
              .define(toolKey + "LeggingDurability", materialDefaults.getDurability(EquipmentSlotType.LEGS)));
        chestplateDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " chestplates.")
              .worldRestart()
              .define(toolKey + "ChestplateDurability", materialDefaults.getDurability(EquipmentSlotType.CHEST)));
        helmetDurability = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + toolKey + " helmets.")
              .worldRestart()
              .define(toolKey + "HelmetDurability", materialDefaults.getDurability(EquipmentSlotType.HEAD)));
        bootArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + toolKey + " boots.")
              .worldRestart()
              .define(toolKey + "BootArmor", materialDefaults.getDamageReductionAmount(EquipmentSlotType.FEET)));
        leggingArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + toolKey + " leggings.")
              .worldRestart()
              .define(toolKey + "LeggingArmor", materialDefaults.getDamageReductionAmount(EquipmentSlotType.LEGS)));
        chestplateArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + toolKey + " chestplates.")
              .worldRestart()
              .define(toolKey + "ChestplateArmor", materialDefaults.getDamageReductionAmount(EquipmentSlotType.CHEST)));
        helmetArmor = CachedIntValue.wrap(config, builder.comment("Protection value of " + toolKey + " helmets.")
              .worldRestart()
              .define(toolKey + "HelmetArmor", materialDefaults.getDamageReductionAmount(EquipmentSlotType.HEAD)));
        builder.pop();
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