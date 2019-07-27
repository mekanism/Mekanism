package mekanism.common.integration.redstoneflux;

import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;

public class RFIntegration {

    public static double fromRF(int rf) {
        return rf * MekanismConfig.current().general.FROM_RF.val();
    }

    public static double fromRF(double rf) {
        return rf * MekanismConfig.current().general.FROM_RF.val();
    }

    public static int toRF(double joules) {
        return MekanismUtils.clampToInt(joules * MekanismConfig.current().general.TO_RF.val());
    }

    public static long toRFAsLong(double joules) {
        return Math.round(joules * MekanismConfig.current().general.TO_RF.val());
    }

    public static double toRFAsDouble(double joules) {
        return joules * MekanismConfig.current().general.TO_RF.val();
    }
}