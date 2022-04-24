package mekanism.generators.common.content.gear.mekasuit;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.FluidInDetails;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;

@ParametersAreNonnullByDefault
public class ModuleGeothermalGeneratorUnit implements ICustomModule<ModuleGeothermalGeneratorUnit> {

    @Override
    public void tickServer(IModule<ModuleGeothermalGeneratorUnit> module, Player player) {
        IEnergyContainer energyContainer = module.getEnergyContainer();
        if (energyContainer != null && !energyContainer.getNeeded().isZero()) {
            double highestScaledDegrees = 0;
            double legHeight = player.isCrouching() ? 0.6 : 0.7;
            Map<Fluid, FluidInDetails> fluidsIn = MekanismUtils.getFluidsIn(player, bb -> new AABB(bb.minX, bb.minY, bb.minZ, bb.maxX,
                  Math.min(bb.minY + legHeight, bb.maxY), bb.maxZ));
            for (Map.Entry<Fluid, FluidInDetails> entry : fluidsIn.entrySet()) {
                FluidInDetails details = entry.getValue();
                double height = details.getMaxHeight();
                if (height < 0.25) {
                    //Skip fluids that we are barely submerged in
                    continue;
                }
                double temperature = 0;
                List<BlockPos> positions = details.getPositions();
                for (BlockPos position : positions) {
                    temperature += entry.getKey().getAttributes().getTemperature(player.level, position);
                }
                //Divide the temperature by how many positions there are in case there is a difference due to the position in the world
                // Strictly speaking we should take the height of the position into account for calculating the average as a "weighted"
                // average, but we don't worry about that as it is highly unlikely the positions will actually have different temperatures,
                // and it would add a bunch of complexity to the calculations to account for it
                temperature /= positions.size();
                if (temperature > HeatAPI.AMBIENT_TEMP) {
                    //If the temperature is above the ambient temperature, calculate how many degrees above
                    // and factor in how much of the legs are submerged
                    double scaledDegrees = (temperature - HeatAPI.AMBIENT_TEMP) * height / legHeight;
                    if (scaledDegrees > highestScaledDegrees) {
                        highestScaledDegrees = scaledDegrees;
                    }
                }
            }
            if (highestScaledDegrees > 0 || player.isOnFire()) {
                //Note: We compare this against zero as we adjust against ambient before scaling
                if (highestScaledDegrees < 200 && player.isOnFire()) {
                    //Treat fire as having a temperature of ~500K, this is on the cooler side of what fire tends to
                    // be but should be good enough for factoring in how much a heat adapter would be able to transfer
                    highestScaledDegrees = 200;
                }
                //Insert energy
                FloatingLong rate = MekanismGeneratorsConfig.gear.mekaSuitGeothermalChargingRate.get().multiply(module.getInstalledCount()).multiply(highestScaledDegrees);
                energyContainer.insert(rate, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
    }

    @Nullable
    @Override
    public ModuleDamageAbsorbInfo getDamageAbsorbInfo(IModule<ModuleGeothermalGeneratorUnit> module, DamageSource damageSource) {
        if (damageSource.isFire()) {
            //Scale the amount absorbed by how many modules are installed out of the possible number installed
            float ratio = MekanismGeneratorsConfig.gear.mekaSuitHeatDamageReductionRatio.get() * (module.getInstalledCount() / (float) module.getData().getMaxStackSize());
            return new ModuleDamageAbsorbInfo(() -> ratio, () -> FloatingLong.ZERO);
        }
        return null;
    }
}