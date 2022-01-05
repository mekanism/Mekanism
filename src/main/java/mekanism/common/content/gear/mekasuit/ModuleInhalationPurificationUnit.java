package mekanism.common.content.gear.mekasuit;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

@ParametersAreNonnullByDefault
public class ModuleInhalationPurificationUnit implements ICustomModule<ModuleInhalationPurificationUnit> {

    private static final ModuleDamageAbsorbInfo INHALATION_ABSORB_INFO = new ModuleDamageAbsorbInfo(MekanismConfig.gear.mekaSuitMagicDamageRatio,
          MekanismConfig.gear.mekaSuitEnergyUsageMagicReduce);

    private IModuleConfigItem<Boolean> beneficialEffects;
    private IModuleConfigItem<Boolean> neutralEffects;
    private IModuleConfigItem<Boolean> harmfulEffects;

    @Override
    public void init(IModule<ModuleInhalationPurificationUnit> module, ModuleConfigItemCreator configItemCreator) {
        beneficialEffects = configItemCreator.createConfigItem("beneficial_effects", MekanismLang.MODULE_PURIFICATION_BENEFICIAL, new ModuleBooleanData(false));
        neutralEffects = configItemCreator.createConfigItem("neutral_effects", MekanismLang.MODULE_PURIFICATION_NEUTRAL, new ModuleBooleanData());
        harmfulEffects = configItemCreator.createConfigItem("harmful_effects", MekanismLang.MODULE_PURIFICATION_HARMFUL, new ModuleBooleanData());
    }

    @Override
    public void tickClient(IModule<ModuleInhalationPurificationUnit> module, Player player) {
        //Messy rough estimate version of tickServer so that the timer actually properly updates
        if (!player.isSpectator()) {
            FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsagePotionTick.get();
            boolean free = usage.isZero() || player.isCreative();
            FloatingLong energy = free ? FloatingLong.ZERO : module.getContainerEnergy().copy();
            if (free || energy.greaterOrEqual(usage)) {
                //Gather all the active effects that we can handle, so that we have them in their own list and
                // don't run into any issues related to CMEs
                List<MobEffectInstance> effects = player.getActiveEffects().stream().filter(effect -> canHandle(effect.getEffect().getCategory())).toList();
                for (MobEffectInstance effect : effects) {
                    if (free) {
                        speedupEffect(player, effect);
                    } else {
                        energy = energy.minusEqual(usage);
                        speedupEffect(player, effect);
                        if (energy.smallerThan(usage)) {
                            //If after using energy, our remaining energy is now smaller than how much we need to use, exit
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void tickServer(IModule<ModuleInhalationPurificationUnit> module, Player player) {
        FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsagePotionTick.get();
        boolean free = usage.isZero() || player.isCreative();
        IEnergyContainer energyContainer = free ? null : module.getEnergyContainer();
        if (free || (energyContainer != null && energyContainer.getEnergy().greaterOrEqual(usage))) {
            //Gather all the active effects that we can handle, so that we have them in their own list and
            // don't run into any issues related to CMEs
            List<MobEffectInstance> effects = player.getActiveEffects().stream()
                  .filter(effect -> canHandle(effect.getEffect().getCategory())).toList();
            for (MobEffectInstance effect : effects) {
                if (free) {
                    speedupEffect(player, effect);
                } else if (module.useEnergy(player, energyContainer, usage, true).isZero()) {
                    //If we can't actually extract energy, exit
                    break;
                } else {
                    speedupEffect(player, effect);
                    if (energyContainer.getEnergy().smallerThan(usage)) {
                        //If after using energy, our remaining energy is now smaller than how much we need to use, exit
                        break;
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public ModuleDamageAbsorbInfo getDamageAbsorbInfo(IModule<ModuleInhalationPurificationUnit> module, DamageSource damageSource) {
        return damageSource.isMagic() ? INHALATION_ABSORB_INFO : null;
    }

    private void speedupEffect(Player player, MobEffectInstance effect) {
        for (int i = 0; i < 9; i++) {
            MekanismUtils.speedUpEffectSafely(player, effect);
        }
    }

    private boolean canHandle(MobEffectCategory effectType) {
        return switch (effectType) {
            case BENEFICIAL -> beneficialEffects.get();
            case HARMFUL -> harmfulEffects.get();
            case NEUTRAL -> neutralEffects.get();
        };
    }
}