package mekanism.generators.common.content.gear.mekasuit;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;

@ParametersAreNotNullByDefault
public class ModuleSolarRechargingUnit implements ICustomModule<ModuleSolarRechargingUnit> {

    private static final FloatingLong RAIN_MULTIPLIER = FloatingLong.createConst(0.2);

    @Override
    public void tickServer(IModule<ModuleSolarRechargingUnit> module, Player player) {
        IEnergyContainer energyContainer = module.getEnergyContainer();
        if (energyContainer != null && !energyContainer.getNeeded().isZero()) {
            //Use the position that is roughly where the solar panel is
            BlockPos pos = new BlockPos(player.getX(), player.getEyeY() + 0.2, player.getZ());
            //Based on how TileEntitySolarGenerator and the rest of our solar things do energy calculations
            if (WorldUtils.canSeeSun(player.level, pos)) {
                Biome b = player.level.getBiomeManager().getBiome(pos).value();
                boolean needsRainCheck = b.getPrecipitation() != Precipitation.NONE;
                // Consider the best temperature to be 0.8; biomes that are higher than that
                // will suffer an efficiency loss (semiconductors don't like heat); biomes that are cooler
                // get a boost. We scale the efficiency to around 30% so that it doesn't totally dominate
                float tempEff = 0.3F * (0.8F - b.getTemperature(pos));

                // Treat rainfall as a proxy for humidity; any humidity works as a drag on overall efficiency.
                // As with temperature, we scale it so that it doesn't overwhelm production. Note the signedness
                // on the scaling factor. Also note that we only use rainfall as a proxy if it CAN rain; some dimensions
                // (like the End) have rainfall set, but can't actually support rain.
                float humidityEff = needsRainCheck ? -0.3F * b.getDownfall() : 0.0F;
                FloatingLong peakOutput = MekanismConfig.gear.mekaSuitSolarRechargingRate.get().multiply(1.0F + tempEff + humidityEff);

                //Get the brightness of the sun; note that there are some implementations that depend on the base
                // brightness function which doesn't take into account the fact that rain can't occur in some biomes.
                float brightness = WorldUtils.getSunBrightness(player.level, 1.0F);

                //Production is a function of the peak possible output in this biome and sun's current brightness
                FloatingLong production = peakOutput.multiply(brightness);
                //If the generator is in a biome where it can rain, and it's raining penalize production by 80%
                if (needsRainCheck && (player.level.isRaining() || player.level.isThundering())) {
                    production = production.timesEqual(RAIN_MULTIPLIER);
                }
                //Multiply actual production based on how many modules are installed
                energyContainer.insert(production.multiply(module.getInstalledCount()), Action.EXECUTE, AutomationType.MANUAL);
            }
        }
    }
}