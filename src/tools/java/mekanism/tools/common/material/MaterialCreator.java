package mekanism.tools.common.material;

import javax.annotation.Nonnull;
import mekanism.common.config.FloatValue;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class MaterialCreator implements IMekanismMaterial {

    @Nonnull
    private final IMekanismMaterial fallBack;

    //TODO: Limits
    private final ConfigValue<Integer> swordDamage;
    private final FloatValue swordAtkSpeed;
    private final FloatValue shovelDamage;
    private final FloatValue shovelAtkSpeed;
    private final FloatValue axeDamage;
    private final FloatValue axeAtkSpeed;
    private final ConfigValue<Integer> pickaxeDamage;
    private final FloatValue pickaxeAtkSpeed;
    private final FloatValue hoeAtkSpeed;
    private final ConfigValue<Integer> paxelHarvestLevel;
    private final FloatValue paxelDamage;
    private final FloatValue paxelAtkSpeed;
    private final ConfigValue<Integer> toolMaxUses;
    private final FloatValue efficiency;
    private final FloatValue attackDamage;
    private final ConfigValue<Integer> harvestLevel;
    private final ConfigValue<Integer> enchantability;
    private final FloatValue toughness;
    private final ConfigValue<Integer> bootDurability;
    private final ConfigValue<Integer> leggingDurability;
    private final ConfigValue<Integer> chestplateDurability;
    private final ConfigValue<Integer> helmetDurability;
    private final ConfigValue<Integer> bootArmor;
    private final ConfigValue<Integer> leggingArmor;
    private final ConfigValue<Integer> chestplateArmor;
    private final ConfigValue<Integer> helmetArmor;

    public MaterialCreator(@Nonnull ForgeConfigSpec.Builder builder, @Nonnull IMekanismMaterial materialDefaults) {
        fallBack = materialDefaults;
        String toolKey = materialDefaults.getRegistryPrefix();
        builder.comment(" Material Settings for " + toolKey).push(toolKey);

        swordDamage = builder.comment("Attack damage modifier of " + toolKey + " swords.").worldRestart()
              .define(toolKey + "SwordDamage", materialDefaults.getSwordDamage());
        swordAtkSpeed = FloatValue.of(builder.comment("Attack speed of " + toolKey + " swords.").worldRestart()
              .define(toolKey + "SwordAtkSpeed", (double) materialDefaults.getSwordAtkSpeed()));
        shovelDamage = FloatValue.of(builder.comment("Attack damage modifier of " + toolKey + " shovels.").worldRestart()
              .define(toolKey + "ShovelDamage", (double) materialDefaults.getShovelDamage()));
        shovelAtkSpeed = FloatValue.of(builder.comment("Attack speed of " + toolKey + " shovels.").worldRestart()
              .define(toolKey + "ShovelAtkSpeed", (double) materialDefaults.getShovelAtkSpeed()));
        axeDamage = FloatValue.of(builder.comment("Attack damage modifier of " + toolKey + " axes.").worldRestart()
              .define(toolKey + "AxeDamage", (double) materialDefaults.getAxeDamage()));
        axeAtkSpeed = FloatValue.of(builder.comment("Attack speed of " + toolKey + " axes.").worldRestart()
              .define(toolKey + "AxeAtkSpeed", (double) materialDefaults.getAxeAtkSpeed()));
        pickaxeDamage = builder.comment("Attack damage modifier of " + toolKey + " pickaxes.").worldRestart()
              .define(toolKey + "PickaxeDamage", materialDefaults.getPickaxeDamage());
        pickaxeAtkSpeed = FloatValue.of(builder.comment("Attack speed of " + toolKey + " pickaxes.").worldRestart()
              .define(toolKey + "PickaxeAtkSpeed", (double) materialDefaults.getPickaxeAtkSpeed()));
        hoeAtkSpeed = FloatValue.of(builder.comment("Attack speed of " + toolKey + " hoes.").worldRestart()
              .define(toolKey + "HoeAtkSpeed", (double) materialDefaults.getHoeAtkSpeed()));
        paxelHarvestLevel = builder.comment("Harvest level of " + toolKey + " paxels.").worldRestart()
              .define(toolKey + "PaxelHarvestLevel", materialDefaults.getPaxelHarvestLevel());
        paxelDamage = FloatValue.of(builder.comment("Attack damage modifier of " + toolKey + " paxels.").worldRestart()
              .define(toolKey + "PaxelDamage", (double) materialDefaults.getPaxelDamage()));
        paxelAtkSpeed = FloatValue.of(builder.comment("Attack speed of " + toolKey + " paxels.").worldRestart()
              .define(toolKey + "PaxelAtkSpeed", (double) materialDefaults.getPaxelAtkSpeed()));
        toolMaxUses = builder.comment("Maximum durability of " + toolKey + " tools.").worldRestart()
              .define(toolKey + "ToolMaxUses", materialDefaults.getMaxUses());
        efficiency = FloatValue.of(builder.comment("Efficiency of " + toolKey + " tools.").worldRestart()
              .define(toolKey + "Efficiency", (double) materialDefaults.getEfficiency()));
        attackDamage = FloatValue.of(builder.comment("Base attack damage of " + toolKey + " items.").worldRestart()
              .define(toolKey + "AttackDamage", (double) materialDefaults.getAttackDamage()));
        harvestLevel = builder.comment("Harvest level of " + toolKey + " tools.").worldRestart()
              .define(toolKey + "HarvestLevel", materialDefaults.getHarvestLevel());
        enchantability = builder.comment("Natural enchantability factor of " + toolKey + " items.").worldRestart()
              .define(toolKey + "Enchantability", materialDefaults.getEnchantability());
        toughness = FloatValue.of(builder.comment("Base armor toughness value of " + toolKey + " armor.").worldRestart()
              .define(toolKey + "Toughness", (double) materialDefaults.getToughness()));
        bootDurability = builder.comment("Maximum durability of " + toolKey + " boots.").worldRestart()
              .define(toolKey + "BootDurability", materialDefaults.getDurability(EquipmentSlotType.FEET));
        leggingDurability = builder.comment("Maximum durability of " + toolKey + " leggings.").worldRestart()
              .define(toolKey + "LeggingDurability", materialDefaults.getDurability(EquipmentSlotType.LEGS));
        chestplateDurability = builder.comment("Maximum durability of " + toolKey + " chestplates.").worldRestart()
              .define(toolKey + "ChestplateDurability", materialDefaults.getDurability(EquipmentSlotType.CHEST));
        helmetDurability = builder.comment("Maximum durability of " + toolKey + " helmets.").worldRestart()
              .define(toolKey + "HelmetDurability", materialDefaults.getDurability(EquipmentSlotType.HEAD));
        bootArmor = builder.comment("Protection value of " + toolKey + " boots.").worldRestart()
              .define(toolKey + "BootArmor", materialDefaults.getDamageReductionAmount(EquipmentSlotType.FEET));
        leggingArmor = builder.comment("Protection value of " + toolKey + " leggings.").worldRestart()
              .define(toolKey + "LeggingArmor", materialDefaults.getDamageReductionAmount(EquipmentSlotType.LEGS));
        chestplateArmor = builder.comment("Protection value of " + toolKey + " chestplates.").worldRestart()
              .define(toolKey + "ChestplateArmor", materialDefaults.getDamageReductionAmount(EquipmentSlotType.CHEST));
        helmetArmor = builder.comment("Protection value of " + toolKey + " helmets.").worldRestart()
              .define(toolKey + "HelmetArmor", materialDefaults.getDamageReductionAmount(EquipmentSlotType.HEAD));

        builder.pop();
    }

    @Override
    public int getSwordDamage() {
        return swordDamage == null ? fallBack.getSwordDamage() : swordDamage.get();
    }

    @Override
    public float getSwordAtkSpeed() {
        return swordAtkSpeed == null ? fallBack.getSwordAtkSpeed() : swordAtkSpeed.get();
    }

    @Override
    public float getShovelDamage() {
        return shovelDamage == null ? fallBack.getShovelDamage() : shovelDamage.get();
    }

    @Override
    public float getShovelAtkSpeed() {
        return shovelAtkSpeed == null ? fallBack.getShovelAtkSpeed() : shovelAtkSpeed.get();
    }

    @Override
    public float getAxeDamage() {
        return axeDamage == null ? fallBack.getAxeDamage() : axeDamage.get();
    }

    @Override
    public float getAxeAtkSpeed() {
        return axeAtkSpeed == null ? fallBack.getAxeAtkSpeed() : axeAtkSpeed.get();
    }

    @Override
    public int getPickaxeDamage() {
        return pickaxeDamage == null ? fallBack.getPickaxeDamage() : pickaxeDamage.get();
    }

    @Override
    public float getPickaxeAtkSpeed() {
        return pickaxeAtkSpeed == null ? fallBack.getPickaxeAtkSpeed() : pickaxeAtkSpeed.get();
    }

    @Override
    public float getHoeAtkSpeed() {
        return hoeAtkSpeed == null ? fallBack.getHoeAtkSpeed() : hoeAtkSpeed.get();
    }

    @Override
    public int getPaxelHarvestLevel() {
        return paxelHarvestLevel == null ? fallBack.getPaxelHarvestLevel() : paxelHarvestLevel.get();
    }

    @Override
    public float getPaxelDamage() {
        return paxelDamage == null ? fallBack.getPaxelDamage() : paxelDamage.get();
    }

    @Override
    public float getPaxelAtkSpeed() {
        return paxelAtkSpeed == null ? fallBack.getPaxelAtkSpeed() : paxelAtkSpeed.get();
    }

    @Override
    public int getMaxUses() {
        return toolMaxUses == null ? fallBack.getMaxUses() : toolMaxUses.get();
    }

    @Override
    public float getEfficiency() {
        return efficiency == null ? fallBack.getEfficiency() : efficiency.get();
    }

    @Override
    public float getAttackDamage() {
        return attackDamage == null ? fallBack.getAttackDamage() : attackDamage.get();
    }

    @Override
    public int getHarvestLevel() {
        return harvestLevel == null ? fallBack.getHarvestLevel() : harvestLevel.get();
    }

    @Override
    public int getDurability(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return bootDurability == null ? fallBack.getDurability(slotType) : bootDurability.get();
            case LEGS:
                return leggingDurability == null ? fallBack.getDurability(slotType) : leggingDurability.get();
            case CHEST:
                return chestplateDurability == null ? fallBack.getDurability(slotType) : chestplateDurability.get();
            case HEAD:
                return helmetDurability == null ? fallBack.getDurability(slotType) : helmetDurability.get();
        }
        return fallBack.getDurability(slotType);
    }

    @Override
    public int getDamageReductionAmount(@Nonnull EquipmentSlotType slotType) {
        switch (slotType) {
            case FEET:
                return bootArmor == null ? fallBack.getDamageReductionAmount(slotType) : bootArmor.get();
            case LEGS:
                return leggingArmor == null ? fallBack.getDamageReductionAmount(slotType) : leggingArmor.get();
            case CHEST:
                return chestplateArmor == null ? fallBack.getDamageReductionAmount(slotType) : chestplateArmor.get();
            case HEAD:
                return helmetArmor == null ? fallBack.getDamageReductionAmount(slotType) : helmetArmor.get();
        }
        return fallBack.getDamageReductionAmount(slotType);
    }

    @Override
    public int getEnchantability() {
        return enchantability == null ? fallBack.getEnchantability() : enchantability.get();
    }

    @Override
    public float getToughness() {
        return toughness == null ? fallBack.getToughness() : toughness.get();
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return fallBack.getSoundEvent();
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return fallBack.getRepairMaterial();
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public String getName() {
        return fallBack.getName();
    }

    @Nonnull
    @Override
    public String getRegistryPrefix() {
        return fallBack.getRegistryPrefix();
    }
}