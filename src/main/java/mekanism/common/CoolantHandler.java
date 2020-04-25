package mekanism.common;

import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.api.chemical.gas.Gas;

public class CoolantHandler {

    private static final Map<Gas, Coolant> coolRegistryMap = new Object2ObjectOpenHashMap<>();
    private static final Map<Gas, Coolant> hotRegistryMap = new Object2ObjectOpenHashMap<>();

    public static void addCoolant(Gas coolGas, Gas hotGas, double energyEfficiency, double thermalEnthalpy) {
        Coolant coolant = new Coolant(coolGas, hotGas, energyEfficiency, thermalEnthalpy);
        coolRegistryMap.put(coolGas, coolant);
        hotRegistryMap.put(hotGas, coolant);
    }

    public static boolean isCoolant(Gas gas) {
        Coolant coolant = coolRegistryMap.get(gas);
        return coolant != null && gas == coolant.getCoolGas();
    }

    public static boolean isHeatedCoolant(Gas gas) {
        Coolant coolant = hotRegistryMap.get(gas);
        return coolant != null && gas == coolant.getHotGas();
    }

    public static Coolant getForCool(Gas gas) {
        return coolRegistryMap.get(gas);
    }

    public static Coolant getForHot(Gas gas) {
        return hotRegistryMap.get(gas);
    }

    public static class Coolant {

        private Gas coolGas;
        private Gas hotGas;

        private double energyEfficiency;
        private double thermalEnthalpy;

        public Coolant(Gas coolGas, Gas hotGas, double energyEfficiency, double thermalEnthalpy) {
            this.coolGas = coolGas;
            this.hotGas = hotGas;
            this.energyEfficiency = energyEfficiency;
            this.thermalEnthalpy = thermalEnthalpy;
        }

        public Gas getCoolGas() {
            return coolGas;
        }

        public Gas getHotGas() {
            return hotGas;
        }

        public double getEnergyEfficiency() {
            return energyEfficiency;
        }

        public double getThermalEnthalpy() {
            return thermalEnthalpy;
        }
    }
}
