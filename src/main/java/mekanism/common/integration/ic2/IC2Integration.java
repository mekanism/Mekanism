package mekanism.common.integration.ic2;

import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;

//Commented out IC2 integration was removed in this commit: https://github.com/mekanism/Mekanism/commit/06acb9f65ea1174caa515a3cc93a140e7ae0ac56
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