package mekanism.common.config;

import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class MekanismStartupConfig extends BaseMekanismConfig {

    private static final String ARMORED_SUBCATEGORY = "armored";

    private final ModConfigSpec configSpec;

    //Armored Free Runner
    public final CachedIntValue armoredFreeRunnerArmor;
    public final CachedFloatValue armoredFreeRunnerToughness;
    public final CachedFloatValue armoredFreeRunnerKnockbackResistance;
    //Armored Jetpack
    public final CachedIntValue armoredJetpackArmor;
    public final CachedFloatValue armoredJetpackToughness;
    public final CachedFloatValue armoredJetpackKnockbackResistance;
    //MekaSuit
    public final CachedIntValue mekaSuitHelmetArmor;
    public final CachedIntValue mekaSuitBodyArmorArmor;
    public final CachedIntValue mekaSuitPantsArmor;
    public final CachedIntValue mekaSuitBootsArmor;
    public final CachedFloatValue mekaSuitToughness;
    public final CachedFloatValue mekaSuitKnockbackResistance;

    MekanismStartupConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        MekanismConfigTranslations.STARTUP_GEAR.applyToBuilder(builder).push("gear");

        MekanismConfigTranslations.STARTUP_FREE_RUNNERS_ARMORED.applyToBuilder(builder).push(ARMORED_SUBCATEGORY + "_" + GearConfig.FREE_RUNNER_CATEGORY);
        armoredFreeRunnerArmor = CachedIntValue.wrap(this, MekanismConfigTranslations.STARTUP_FREE_RUNNERS_ARMOR.applyToBuilder(builder)
              .gameRestart()
              .defineInRange("armor", 3, 0, Integer.MAX_VALUE));
        armoredFreeRunnerToughness = CachedFloatValue.wrap(this, MekanismConfigTranslations.STARTUP_FREE_RUNNERS_TOUGHNESS.applyToBuilder(builder)
              .gameRestart()
              .defineInRange("toughness", 2.0, 0, Float.MAX_VALUE));
        armoredFreeRunnerKnockbackResistance = CachedFloatValue.wrap(this, MekanismConfigTranslations.STARTUP_FREE_RUNNERS_KNOCKBACK_RESISTANCE.applyToBuilder(builder)
              .gameRestart()
              .defineInRange("knockbackResistance", 0.0, 0, Float.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.STARTUP_JETPACK_ARMORED.applyToBuilder(builder).push(ARMORED_SUBCATEGORY + "_" + GearConfig.JETPACK_CATEGORY);
        armoredJetpackArmor = CachedIntValue.wrap(this, MekanismConfigTranslations.STARTUP_JETPACK_ARMOR.applyToBuilder(builder)
              .gameRestart()
              .defineInRange("armor", 8, 0, Integer.MAX_VALUE));
        armoredJetpackToughness = CachedFloatValue.wrap(this, MekanismConfigTranslations.STARTUP_JETPACK_TOUGHNESS.applyToBuilder(builder)
              .gameRestart()
              .defineInRange("toughness", 2.0, 0, Float.MAX_VALUE));
        armoredJetpackKnockbackResistance = CachedFloatValue.wrap(this, MekanismConfigTranslations.STARTUP_JETPACK_KNOCKBACK_RESISTANCE.applyToBuilder(builder)
              .gameRestart()
              .defineInRange("knockbackResistance", 0.0, 0, Float.MAX_VALUE));
        builder.pop();

        MekanismConfigTranslations.GEAR_MEKA_SUIT.applyToBuilder(builder).push(GearConfig.MEKASUIT_CATEGORY);
        mekaSuitHelmetArmor = CachedIntValue.wrap(this, MekanismConfigTranslations.STARTUP_MEKA_SUIT_ARMOR_HELMET.applyToBuilder(builder)
              .gameRestart()
              .defineInRange("helmetArmor", ArmorMaterials.NETHERITE.value().defense().get(ArmorItem.Type.HELMET), 0, Integer.MAX_VALUE));
        mekaSuitBodyArmorArmor = CachedIntValue.wrap(this, MekanismConfigTranslations.STARTUP_MEKA_SUIT_ARMOR_CHESTPLATE.applyToBuilder(builder)
              .gameRestart()
              .defineInRange("bodyArmorArmor", ArmorMaterials.NETHERITE.value().defense().get(ArmorItem.Type.CHESTPLATE), 0, Integer.MAX_VALUE));
        mekaSuitPantsArmor = CachedIntValue.wrap(this, MekanismConfigTranslations.STARTUP_MEKA_SUIT_ARMOR_LEGGINGS.applyToBuilder(builder)
              .gameRestart()
              .defineInRange("pantsArmor", ArmorMaterials.NETHERITE.value().defense().get(ArmorItem.Type.LEGGINGS), 0, Integer.MAX_VALUE));
        mekaSuitBootsArmor = CachedIntValue.wrap(this, MekanismConfigTranslations.STARTUP_MEKA_SUIT_ARMOR_BOOTS.applyToBuilder(builder)
              .gameRestart()
              .defineInRange("bootsArmor", ArmorMaterials.NETHERITE.value().defense().get(ArmorItem.Type.BOOTS), 0, Integer.MAX_VALUE));
        mekaSuitToughness = CachedFloatValue.wrap(this, MekanismConfigTranslations.STARTUP_MEKA_SUIT_TOUGHNESS.applyToBuilder(builder)
              .gameRestart()
              .defineInRange("toughness", ArmorMaterials.NETHERITE.value().toughness(), 0, Float.MAX_VALUE));
        mekaSuitKnockbackResistance = CachedFloatValue.wrap(this, MekanismConfigTranslations.STARTUP_MEKA_SUIT_KNOCKBACK_RESISTANCE.applyToBuilder(builder)
              .gameRestart()
              .defineInRange("knockbackResistance", ArmorMaterials.NETHERITE.value().knockbackResistance(), 0, Float.MAX_VALUE));
        builder.pop();//End mekasuit

        builder.pop();//End gear
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "startup";
    }

    @Override
    public String getTranslation() {
        return "Startup Config";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.STARTUP;
    }
}