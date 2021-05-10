package mekanism.common.content.gear.mekasuit;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class ModuleSolarRechargingUnit implements ICustomModule<ModuleSolarRechargingUnit> {

    @Override
    public void tickServer(IModule<ModuleSolarRechargingUnit> module, PlayerEntity player) {
        IEnergyContainer energyContainer = module.getEnergyContainer();
        if (energyContainer != null && !energyContainer.getNeeded().isZero() && player.level.isDay() &&
            player.level.canSeeSky(new BlockPos(player.position())) && !player.level.isRaining() && player.level.dimensionType().hasSkyLight()) {
            FloatingLong rate = MekanismConfig.gear.mekaSuitSolarRechargingRate.get().multiply(module.getInstalledCount());
            energyContainer.insert(rate, Action.EXECUTE, AutomationType.MANUAL);
        }
    }
}