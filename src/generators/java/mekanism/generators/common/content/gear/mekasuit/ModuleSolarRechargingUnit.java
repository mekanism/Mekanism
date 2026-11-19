package mekanism.generators.common.content.gear.mekasuit;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.MathUtils;
import mekanism.common.util.LightLevelUtils;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
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
            if (LightLevelUtils.canSeeSun(player.level(), pos)) {
                double peakOutput = MekanismGeneratorsConfig.gear.mekaSuitSolarRechargingRate.get() * WorldUtils.calculateSolarProduction(player.level(), pos);

                //Get the brightness of the sun; this includes rain penalty from Vanilla
                float brightness = LightLevelUtils.getSunBrightness(player.level(), player.blockPosition());

                //Production is a function of the peak possible output in this biome and sun's current brightness
                double production = peakOutput * brightness;

                //Validate we are producing energy as for extremely hot biomes solar panels might not work https://github.com/mekanism/Mekanism/issues/8563
                if (production > 0) {
                    //Multiply actual production based on how many modules are installed
                    energyHandler.insert(MathUtils.clampToInt(production * module.getInstalledCount()), transaction);
                }
            }
        }
    }
}