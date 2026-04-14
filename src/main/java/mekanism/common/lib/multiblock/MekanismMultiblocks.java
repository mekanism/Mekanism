package mekanism.common.lib.multiblock;

import mekanism.common.Mekanism;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.content.boiler.BoilerValidator;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.content.evaporation.EvaporationValidator;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.content.matrix.MatrixValidator;
import mekanism.common.content.sps.SPSCache;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSValidator;
import mekanism.common.content.tank.TankCache;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.content.tank.TankValidator;
import net.neoforged.bus.api.IEventBus;

public class MekanismMultiblocks {

    private static final MekanismMultiblockRegistry REGISTRY = new MekanismMultiblockRegistry(Mekanism.MODID);

    public static final MultiblockType<TankMultiblockData> TANK = REGISTRY.registerMultiblock("dynamicTank", TankCache::new, TankValidator::new);
    public static final MultiblockType<MatrixMultiblockData> MATRIX = REGISTRY.registerMultiblock("inductionMatrix", MultiblockCache::new, MatrixValidator::new);
    public static final MultiblockType<BoilerMultiblockData> BOILER = REGISTRY.registerMultiblock("thermoelectricBoiler", MultiblockCache::new, BoilerValidator::new);
    public static final MultiblockType<EvaporationMultiblockData> EVAPORATION = REGISTRY.registerMultiblock("evaporation", MultiblockCache::new, EvaporationValidator::new);
    public static final MultiblockType<SPSMultiblockData> SPS = REGISTRY.registerMultiblock("sps", SPSCache::new, SPSValidator::new);

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }
}
