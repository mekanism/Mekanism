package mekanism.generators.common.content.gear.mekasuit;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.common.config.listener.ConfigBasedCachedFloatSupplier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.FluidInDetails;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class ModuleGeothermalGeneratorUnit implements ICustomModule<ModuleGeothermalGeneratorUnit> {

    private static final Int2ObjectMap<ModuleDamageAbsorbInfo> DAMAGE_ABSORB_VALUES = Util.make(() -> {
        int maxSize = 8;
        //Based on the max size of the module
        Int2ObjectMap<ModuleDamageAbsorbInfo> map = new Int2ObjectArrayMap<>(maxSize);
        for (int count = 1; count <= maxSize; count++) {
            //Scale the amount absorbed by how many modules are installed out of the possible number installed
            float ratio = count / (float) maxSize;
            map.put(count, new ModuleDamageAbsorbInfo(new ConfigBasedCachedFloatSupplier(() -> MekanismGeneratorsConfig.gear.mekaSuitHeatDamageReductionRatio.get() * ratio,
                  MekanismGeneratorsConfig.gear.mekaSuitHeatDamageReductionRatio), ConstantPredicates.ZERO_LONG));
        }
        return map;
    });

    @Override
    public void tickServer(IModule<ModuleGeothermalGeneratorUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        IEnergyContainer energyContainer = module.getEnergyContainer(stack);
        if (energyContainer != null && energyContainer.getNeeded() > 0L) {
            double highestScaledDegrees = 0;
            double legHeight = player.isCrouching() ? 0.6 : 0.7;
            Map<FluidType, FluidInDetails> fluidsIn = MekanismUtils.getFluidsIn(player, legHeight, (bb, data) -> new AABB(bb.minX, bb.minY, bb.minZ, bb.maxX,
                  Math.min(bb.minY + data, bb.maxY), bb.maxZ));
            for (Map.Entry<FluidType, FluidInDetails> entry : fluidsIn.entrySet()) {
                FluidInDetails details = entry.getValue();
                double height = details.getMaxHeight();
                if (height < 0.25) {
                    //Skip fluids that we are barely submerged in
                    continue;
                }
                double temperature = 0;
                Map<BlockPos, FluidState> positions = details.getPositions();
                for (Map.Entry<BlockPos, FluidState> positionEntry : positions.entrySet()) {
                    temperature += entry.getKey().getTemperature(positionEntry.getValue(), player.level(), positionEntry.getKey());
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
                long rate = MathUtils.clampToLong(module.getInstalledCount() * MekanismGeneratorsConfig.gear.mekaSuitGeothermalChargingRate.get() * highestScaledDegrees);
                energyContainer.insert(rate, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
    }

    @Nullable
    @Override
    public ModuleDamageAbsorbInfo getDamageAbsorbInfo(IModule<ModuleGeothermalGeneratorUnit> module, DamageSource damageSource) {
        return damageSource.is(DamageTypeTags.IS_FIRE) ? DAMAGE_ABSORB_VALUES.get(module.getInstalledCount()) : null;
    }
}