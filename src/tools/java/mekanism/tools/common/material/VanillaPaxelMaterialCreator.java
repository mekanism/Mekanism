package mekanism.tools.common.material;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.common.config.IMekanismConfig;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeConfigSpec;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class VanillaPaxelMaterialCreator implements IPaxelMaterial {

    private final VanillaPaxelMaterial fallback;

    public final CachedFloatValue paxelDamage;
    public final CachedFloatValue paxelAtkSpeed;
    private final CachedFloatValue paxelEfficiency;
    private final CachedIntValue paxelEnchantability;
    private final CachedIntValue paxelMaxUses;

    public VanillaPaxelMaterialCreator(IMekanismConfig config, ForgeConfigSpec.Builder builder, VanillaPaxelMaterial materialDefaults) {
        this.fallback = materialDefaults;
        String toolKey = getRegistryPrefix();
        String name = getConfigCommentName();
        builder.comment("Vanilla Material Paxel Settings for " + name).push(toolKey);
        //Note: Damage predicate to allow for tools to go negative to the value of the base tier so that a tool
        // can effectively have zero damage for things like the hoe
        paxelDamage = CachedFloatValue.wrap(config, builder.comment("Attack damage modifier of " + name + " paxels.")
              .define(toolKey + "PaxelDamage", (double) materialDefaults.getPaxelDamage(), value -> {
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
              }));
        paxelAtkSpeed = CachedFloatValue.wrap(config, builder.comment("Attack speed of " + name + " paxels.")
              .define(toolKey + "PaxelAtkSpeed", (double) materialDefaults.getPaxelAtkSpeed()));
        paxelEfficiency = CachedFloatValue.wrap(config, builder.comment("Efficiency of " + name + " paxels.")
              .define(toolKey + "PaxelEfficiency", (double) materialDefaults.getPaxelEfficiency()));
        paxelEnchantability = CachedIntValue.wrap(config, builder.comment("Natural enchantability factor of " + name + " paxels.")
              .defineInRange(toolKey + "PaxelEnchantability", materialDefaults.getPaxelEnchantability(), 0, Integer.MAX_VALUE));
        paxelMaxUses = CachedIntValue.wrap(config, builder.comment("Maximum durability of " + name + " paxels.")
              .defineInRange(toolKey + "PaxelMaxUses", materialDefaults.getPaxelMaxUses(), 1, Integer.MAX_VALUE));
        builder.pop();
    }

    @Nonnull
    public Tiers getVanillaTier() {
        return fallback.getVanillaTier();
    }

    @Nonnull
    public String getRegistryPrefix() {
        return fallback.getRegistryPrefix();
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
    public int getPaxelEnchantability() {
        return paxelEnchantability.get();
    }

    @Override
    public String getConfigCommentName() {
        return fallback.getConfigCommentName();
    }
}