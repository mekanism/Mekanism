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
        builder.comment("Mekanism Startup Config. This config is loaded on early, and requires a game restart to take effect, and is not synced automatically between "
                        + "client and server. It is highly recommended to ensure you are using the same values for this config on the server and client.").push("startup");

        builder.comment("Startup gear configs").push("gear");

        builder.comment("Free Runner Settings").push(GearConfig.FREE_RUNNER_CATEGORY);
        builder.comment("Armored Free Runner Settings").push(ARMORED_SUBCATEGORY);
        armoredFreeRunnerArmor = CachedIntValue.wrap(this, builder.comment("Armor value of the Armored Free Runners")
              .defineInRange("armor", 3, 0, Integer.MAX_VALUE));
        armoredFreeRunnerToughness = CachedFloatValue.wrap(this, builder.comment("Toughness value of the Armored Free Runners.")
              .defineInRange("toughness", 2.0, 0, Float.MAX_VALUE));
        armoredFreeRunnerKnockbackResistance = CachedFloatValue.wrap(this, builder.comment("Knockback resistance value of the Armored Free Runners.")
              .defineInRange("knockbackResistance", 0.0, 0, Float.MAX_VALUE));
        builder.pop(2);//End free runner and armored

        builder.comment("Jetpack Settings").push(GearConfig.JETPACK_CATEGORY);
        builder.comment("Armored Jetpack Settings").push(ARMORED_SUBCATEGORY);
        armoredJetpackArmor = CachedIntValue.wrap(this, builder.comment("Armor value of the Armored Jetpack.")
              .defineInRange("armor", 8, 0, Integer.MAX_VALUE));
        armoredJetpackToughness = CachedFloatValue.wrap(this, builder.comment("Toughness value of the Armored Jetpack.")
              .defineInRange("toughness", 2.0, 0, Float.MAX_VALUE));
        armoredJetpackKnockbackResistance = CachedFloatValue.wrap(this, builder.comment("Knockback resistance value of the Armored Jetpack.")
              .defineInRange("knockbackResistance", 0.0, 0, Float.MAX_VALUE));
        builder.pop(2);//End jetpack and armored

        builder.comment("MekaSuit Settings").push(GearConfig.MEKASUIT_CATEGORY);
        mekaSuitHelmetArmor = CachedIntValue.wrap(this, builder.comment("Armor value of MekaSuit Helmets.")
              .defineInRange("helmetArmor", ArmorMaterials.NETHERITE.value().defense().get(ArmorItem.Type.HELMET), 0, Integer.MAX_VALUE));
        mekaSuitBodyArmorArmor = CachedIntValue.wrap(this, builder.comment("Armor value of MekaSuit BodyArmor.")
              .defineInRange("bodyArmorArmor", ArmorMaterials.NETHERITE.value().defense().get(ArmorItem.Type.CHESTPLATE), 0, Integer.MAX_VALUE));
        mekaSuitPantsArmor = CachedIntValue.wrap(this, builder.comment("Armor value of MekaSuit Pants.")
              .defineInRange("pantsArmor", ArmorMaterials.NETHERITE.value().defense().get(ArmorItem.Type.LEGGINGS), 0, Integer.MAX_VALUE));
        mekaSuitBootsArmor = CachedIntValue.wrap(this, builder.comment("Armor value of MekaSuit Boots.")
              .defineInRange("bootsArmor", ArmorMaterials.NETHERITE.value().defense().get(ArmorItem.Type.BOOTS), 0, Integer.MAX_VALUE));
        mekaSuitToughness = CachedFloatValue.wrap(this, builder.comment("Toughness value of the MekaSuit.")
              .defineInRange("toughness", ArmorMaterials.NETHERITE.value().toughness(), 0, Float.MAX_VALUE));
        mekaSuitKnockbackResistance = CachedFloatValue.wrap(this, builder.comment("Knockback resistance value of the MekaSuit.")
              .defineInRange("knockbackResistance", ArmorMaterials.NETHERITE.value().knockbackResistance(), 0, Float.MAX_VALUE));
        builder.pop();//End mekasuit

        builder.pop();//End gear

        builder.pop();
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