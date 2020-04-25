package mekanism.api.chemical.gas.attribute;

import java.util.List;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.providers.IGasProvider;
import mekanism.api.text.APILang;
import net.minecraft.util.text.ITextComponent;

public class GasAttributes {

    /**
     * This defines the radioactivity of a certain chemical. This attribute <i>requires validation</i>,
     * meaning chemical containers won't be able to accept chemicals with this attribute by default.
     * Radioactivity is measured in Sv/h.
     *
     * @author aidancbrady
     *
     */
    public static class Radiation extends ChemicalAttribute {

        private double radioactivity;

        public Radiation(double radioactivity) {
            this.radioactivity = radioactivity;
        }

        /**
         * Gets the radioactivity of this gas in Sv/h. Each mB of this chemical released into the
         * environment will cause a radiation source of the given radioactivity.
         *
         * @return the radioactivity of this chemical
         */
        public double getRadioactivity() {
            return radioactivity;
        }

        @Override
        public boolean needsValidation() {
            return true;
        }

        @Override
        public List<ITextComponent> addTooltipText(List<ITextComponent> list) {
            super.addTooltipText(list);
            list.add(APILang.CHEMICAL_ATTRIBUTE_RADIATION.translate(radioactivity));
            return list;
        }
    }

    public static abstract class Coolant extends ChemicalAttribute {

        private double thermalEnthalpy;
        private double conductivity;

        public Coolant(double thermalEnthalpy, double conductivity) {
            this.thermalEnthalpy = thermalEnthalpy;
            this.conductivity = conductivity;
        }

        public double getThermalEnthalpy() {
            return thermalEnthalpy;
        }

        public double getConductivity() {
            return conductivity;
        }

        @Override
        public List<ITextComponent> addTooltipText(List<ITextComponent> list) {
            super.addTooltipText(list);
            list.add(APILang.CHEMICAL_ATTRIBUTE_COOLANT_EFFICIENCY.translate(Math.round(conductivity * 100)));
            list.add(APILang.CHEMICAL_ATTRIBUTE_COOLANT_ENTHALPY.translate(thermalEnthalpy));
            return list;
        }
    }

    public static class CooledCoolant extends Coolant {

        private IGasProvider heatedGas;

        public CooledCoolant(IGasProvider heatedGas, double thermalEnthalpy, double conductivity) {
            super(thermalEnthalpy, conductivity);
            this.heatedGas = heatedGas;
        }

        public IGasProvider getHeatedGas() {
            return heatedGas.getGas();
        }
    }

    public static class HeatedCoolant extends Coolant {

        private IGasProvider cooledGas;

        public HeatedCoolant(IGasProvider cooledGas, double thermalEnthalpy, double conductivity) {
            super(thermalEnthalpy, conductivity);
            this.cooledGas = cooledGas;
        }

        public Gas getCooledGas() {
            return cooledGas.getGas();
        }

        @Override
        public List<ITextComponent> addTooltipText(List<ITextComponent> list) {
            super.addTooltipText(list);
            list.add(APILang.CHEMICAL_ATTRIBUTE_SUPERHEATED.translate());
            return list;
        }
    }
}
