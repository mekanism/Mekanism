package mekanism.common.integration.ic2;

import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;

//TODO: Include link to commit that the commented out code was removed
public class IC2Integration {

    public static double toEU(double joules) {
        return joules * MekanismConfig.general.TO_IC2.get();
    }

    public static int toEUAsInt(double joules) {
        return MekanismUtils.clampToInt(toEU(joules));
    }

    public static double fromEU(double eu) {
        return eu * MekanismConfig.general.FROM_IC2.get();
    }
}