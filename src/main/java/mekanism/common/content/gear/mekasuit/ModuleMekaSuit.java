package mekanism.common.content.gear.mekasuit;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.Module;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;

public abstract class ModuleMekaSuit extends Module {

    public static class ModuleElectrolyticBreathingUnit extends ModuleMekaSuit {

        private static final int MAX_RATE = 10;

        @Override
        public void tickServer(PlayerEntity player) {
            int toUse = Math.min(MAX_RATE, player.getMaxAir() - player.getAir());
            toUse = Math.min(toUse, getContainerEnergy().divide(MekanismConfig.general.FROM_H2.get()).intValue());
            useEnergy(MekanismConfig.general.FROM_H2.get().multiply(toUse));
            player.setAir(player.getAir() + toUse);
        }
    }

    public static class ModuleInhalationPurificationUnit extends ModuleMekaSuit {

        private static final FloatingLong ENERGY_USAGE_PER_POTION_TICK = FloatingLong.createConst(1000);

        @Override
        public void tickServer(PlayerEntity player) {
            for (EffectInstance effect : player.getActivePotionEffects()) {
                if (getContainerEnergy().smallerThan(ENERGY_USAGE_PER_POTION_TICK)) {
                    break;
                }
                useEnergy(ENERGY_USAGE_PER_POTION_TICK);
                for (int i = 0; i < 9; i++) {
                    effect.tick(player, () -> MekanismUtils.onChangedPotionEffect(player, effect, true));
                }
            }
        }
    }

    public static class ModuleRadiationShieldingUnit extends ModuleMekaSuit {}
}
