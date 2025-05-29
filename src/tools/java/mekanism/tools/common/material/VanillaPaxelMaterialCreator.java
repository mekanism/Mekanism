package mekanism.tools.common.material;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.config.ToolsConfigTranslations.VanillaPaxelMaterialTranslations;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Tool;
import net.neoforged.neoforge.common.ModConfigSpec;

@NothingNullByDefault
public class VanillaPaxelMaterialCreator implements IPaxelMaterial {

    private final VanillaPaxelMaterial fallback;

    private final CachedFloatValue paxelDamage;
    private final CachedFloatValue paxelAtkSpeed;
    private final CachedFloatValue paxelEfficiency;
    private final CachedIntValue paxelEnchantability;
    private final CachedIntValue paxelDurability;

    public VanillaPaxelMaterialCreator(IMekanismConfig config, ModConfigSpec.Builder builder, VanillaPaxelMaterial materialDefaults) {
        this.fallback = materialDefaults;
        String toolKey = getRegistryPrefix();
        VanillaPaxelMaterialTranslations translations = VanillaPaxelMaterialTranslations.create(toolKey);
        translations.topLevel().applyToBuilder(builder).push(toolKey);
        //Note: Damage predicate to allow for tools to go negative to the value of the base tier so that a tool
        // can effectively have zero damage for things like the hoe
        paxelDamage = CachedFloatValue.wrap(config, translations.damage().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PaxelDamage", validateDefaultModifier(materialDefaults.getPaxelDamage()), this::validateDamageModifier));
        paxelAtkSpeed = CachedFloatValue.wrap(config, translations.attackSpeed().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PaxelAtkSpeed", (double) materialDefaults.getPaxelAtkSpeed()));
        paxelEfficiency = CachedFloatValue.wrap(config, translations.efficiency().applyToBuilder(builder)
              .gameRestart()
              .define(toolKey + "PaxelEfficiency", (double) materialDefaults.getPaxelEfficiency()));
        paxelEnchantability = CachedIntValue.wrap(config, translations.enchantability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "PaxelEnchantability", materialDefaults.getPaxelEnchantability(), 0, Integer.MAX_VALUE));
        paxelDurability = CachedIntValue.wrap(config, translations.durability().applyToBuilder(builder)
              .gameRestart()
              .defineInRange(toolKey + "PaxelDurability", materialDefaults.getPaxelDurability(), 1, Integer.MAX_VALUE));
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
            float baseDamage = getVanillaTier().getAttackDamageBonus();
            return actualValue >= -baseDamage && actualValue <= Float.MAX_VALUE - baseDamage;
        }
        return false;
    }

    private Supplier<Double> validateDefaultModifier(double defaultModifier) {
        return () -> {
            if (validateDamageModifier(defaultModifier)) {
                return defaultModifier;
            }
            return (double) -getVanillaTier().getAttackDamageBonus();
        };
    }

    public Tiers getVanillaTier() {
        return fallback.getVanillaTier();
    }

    public String getRegistryPrefix() {
        return fallback.getRegistryPrefix();
    }

    @Override
    public int getPaxelDurability() {
        return paxelDurability.getOrDefault();
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
    public int getPaxelEnchantability() {
        return paxelEnchantability.get();
    }

    public Tool createToolProperties() {
        return new Tool(List.of(Tool.Rule.deniesDrops(getVanillaTier().getIncorrectBlocksForDrops()), Tool.Rule.minesAndDrops(ToolsTags.Blocks.MINEABLE_WITH_PAXEL, getPaxelEfficiency())), 1.0F, 1);
    }
}