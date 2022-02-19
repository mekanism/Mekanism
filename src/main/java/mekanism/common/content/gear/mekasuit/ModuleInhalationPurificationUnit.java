package mekanism.common.content.gear.mekasuit;

import java.util.List;
import java.util.stream.Collectors;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;

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
    public void tickClient(IModule<ModuleInhalationPurificationUnit> module, PlayerEntity player) {
        //Messy rough estimate version of tickServer so that the timer actually properly updates
        if (!player.isSpectator()) {
            FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsagePotionTick.get();
            boolean free = usage.isZero() || player.isCreative();
            FloatingLong energy = free ? FloatingLong.ZERO : module.getContainerEnergy().copy();
            if (free || energy.greaterOrEqual(usage)) {
                //Gather all the active effects that we can handle, so that we have them in their own list and
                // don't run into any issues related to CMEs
                List<EffectInstance> effects = player.getActiveEffects().stream().filter(effect -> canHandle(effect.getEffect().getCategory()))
                      .collect(Collectors.toList());
                for (EffectInstance effect : effects) {
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
    public void tickServer(IModule<ModuleInhalationPurificationUnit> module, PlayerEntity player) {
        FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsagePotionTick.get();
        boolean free = usage.isZero() || player.isCreative();
        IEnergyContainer energyContainer = free ? null : module.getEnergyContainer();
        if (free || (energyContainer != null && energyContainer.getEnergy().greaterOrEqual(usage))) {
            //Gather all the active effects that we can handle, so that we have them in their own list and
            // don't run into any issues related to CMEs
            List<EffectInstance> effects = player.getActiveEffects().stream()
                  .filter(effect -> canHandle(effect.getEffect().getCategory()))
                  .collect(Collectors.toList());
            for (EffectInstance effect : effects) {
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

    private void speedupEffect(PlayerEntity player, EffectInstance effect) {
        for (int i = 0; i < 9; i++) {
            MekanismUtils.speedUpEffectSafely(player, effect);
        }
    }

    private boolean canHandle(EffectType effectType) {
        switch (effectType) {
            case BENEFICIAL:
                return beneficialEffects.get();
            case HARMFUL:
                return harmfulEffects.get();
            case NEUTRAL:
                return neutralEffects.get();
        }
        return false;
    }
}