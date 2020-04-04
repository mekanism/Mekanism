package mekanism.common.content.gear.mekasuit;

import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.Modules;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;

public abstract class ModuleMekaSuit extends Module {

    public static class ModuleElectrolyticBreathingUnit extends ModuleMekaSuit {
        @Override
        public void tickServer(PlayerEntity player) {
            FloatingLong usage = MekanismConfig.general.FROM_H2.get().multiply(2);
            int maxRate = Math.min(getMaxRate(), getContainerEnergy().divide(usage).intValue());
            int hydrogenUsed = 0;
            GasStack hydrogenStack = new GasStack(MekanismGases.HYDROGEN.get(), maxRate * 2);
            ItemStack chestStack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            Optional<IGasHandler> capability = MekanismUtils.toOptional(chestStack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY));
            if (Modules.load(chestStack, Modules.JETPACK_UNIT) != null && capability.isPresent()) {
                hydrogenUsed = maxRate * 2 - capability.get().insertGas(hydrogenStack, Action.EXECUTE).getAmount();
            }
            int oxygenUsed = Math.min(maxRate, player.getMaxAir() - player.getAir());
            int used = Math.max((int)Math.ceil(hydrogenUsed / 2D), oxygenUsed);
            useEnergy(usage.multiply(used));
            player.setAir(player.getAir() + oxygenUsed);
        }

        private int getMaxRate() {
            return (int)Math.pow(2, getInstalledCount());
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
