package mekanism.generators.common.content.gear.mekasuit;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.MathUtils;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ModuleSolarRechargingUnit implements ICustomModule<ModuleSolarRechargingUnit> {

    @Override
    public void tickServer(IModule<ModuleSolarRechargingUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        EnergyHandler energyHandler = module.getEnergyHandler(itemAccess, true);
        if (energyHandler != null && !EnergyHandlerUtil.isFull(energyHandler)) {
            //Use the position that is roughly where the solar panel is
            BlockPos pos = BlockPos.containing(player.getX(), player.getEyeY() + 0.2, player.getZ());
            //Based on how TileEntitySolarGenerator and the rest of our solar things do energy calculations
            if (WorldUtils.canSeeSun(player.level(), pos)) {
                Biome b = player.level().getBiomeManager().getBiome(pos).value();
                int seaLevel = player.level().getSeaLevel();
                boolean needsRainCheck = b.getPrecipitationAt(pos, seaLevel) != Precipitation.NONE;
                // Consider the best temperature to be 0.8; biomes that are higher than that
                // will suffer an efficiency loss (semiconductors don't like heat); biomes that are cooler
                // get a boost. We scale the efficiency to around 30% so that it doesn't totally dominate
                float tempEff = 0.3F * (0.8F - b.getTemperature(pos, seaLevel));

                // Treat rainfall as a proxy for humidity; any humidity works as a drag on overall efficiency.
                // As with temperature, we scale it so that it doesn't overwhelm production. Note the signedness
                // on the scaling factor. Also note that we only use rainfall as a proxy if it CAN rain; some dimensions
                // (like the End) have rainfall set, but can't actually support rain.
                float humidityEff = needsRainCheck ? -0.3F * b.getModifiedClimateSettings().downfall() : 0.0F;
                double peakOutput = MekanismConfig.gear.mekaSuitSolarRechargingRate.get() * (1.0D + tempEff + humidityEff);

                //Get the brightness of the sun; this includes rain penalty from Vanilla
                float brightness = WorldUtils.getSunBrightness(player.level(), player.blockPosition());

                //Production is a function of the peak possible output in this biome and sun's current brightness
                double production = peakOutput * brightness;

                //Multiply actual production based on how many modules are installed
                energyHandler.insert(MathUtils.clampToInt(production * module.getInstalledCount()), transaction);
            }
        }
    }
}