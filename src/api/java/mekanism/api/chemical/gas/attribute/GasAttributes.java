package mekanism.api.chemical.gas.attribute;

import java.util.List;
import java.util.function.IntSupplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.providers.IGasProvider;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import net.minecraft.util.text.ITextComponent;

public class GasAttributes {

    private GasAttributes() {
    }

    /**
     * This defines the radioactivity of a certain chemical. This attribute <i>requires validation</i>, meaning chemical containers won't be able to accept chemicals with
     * this attribute by default. Radioactivity is measured in Sv/h.
     *
     * @author aidancbrady
     */
    public static class Radiation extends ChemicalAttribute {

        private final double radioactivity;

        public Radiation(double radioactivity) {
            this.radioactivity = radioactivity;
        }

        /**
         * Gets the radioactivity of this gas in Sv/h. Each mB of this chemical released into the environment will cause a radiation source of the given radioactivity.
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
            list.add(APILang.CHEMICAL_ATTRIBUTE_RADIATION.translateColored(EnumColor.GRAY, EnumColor.INDIGO, radioactivity));
            return list;
        }
    }

    /**
     * Defines the root data of a coolant, for use in Fission Reactors. Coolants have two primary properties: 'thermal enthalpy' and 'conductivity'. Thermal Enthalpy
     * defines how much energy one mB of the chemical can store; as such, lower values will cause reactors to require more of the chemical to stay cool. 'Conductivity'
     * defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated variant.
     *
     * @author aidancbrady
     */
    public abstract static class Coolant extends ChemicalAttribute {

        private final double thermalEnthalpy;
        private final double conductivity;

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
            list.add(APILang.CHEMICAL_ATTRIBUTE_COOLANT_EFFICIENCY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, Math.round(conductivity * 100) + "%"));
            list.add(APILang.CHEMICAL_ATTRIBUTE_COOLANT_ENTHALPY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, thermalEnthalpy));
            return list;
        }
    }

    /**
     * Defines the 'cooled' variant of a coolant- the heated variant must be supplied in this class.
     *
     * @author aidancbrady
     */
    public static class CooledCoolant extends Coolant {

        private final IGasProvider heatedGas;

        public CooledCoolant(IGasProvider heatedGas, double thermalEnthalpy, double conductivity) {
            super(thermalEnthalpy, conductivity);
            this.heatedGas = heatedGas;
        }

        public Gas getHeatedGas() {
            return heatedGas.getChemical();
        }
    }

    /**
     * Defines the 'heated' variant of a coolant- the cooled variant must be supplied in this class.
     *
     * @author aidancbrady
     */
    public static class HeatedCoolant extends Coolant {

        private final IGasProvider cooledGas;

        public HeatedCoolant(IGasProvider cooledGas, double thermalEnthalpy, double conductivity) {
            super(thermalEnthalpy, conductivity);
            this.cooledGas = cooledGas;
        }

        public Gas getCooledGas() {
            return cooledGas.getChemical();
        }
    }

    /**
     * Defines a fuel which can be processed by a Gas-Burning Generator to produce energy. Fuels have two primary values: 'burn ticks', defining how many ticks one mB of
     * fuel can be burned for before being depleted, and 'energyDensity', defining how much energy is stored in one mB of fuel.
     *
     * @author aidancbrady
     */
    public static class Fuel extends ChemicalAttribute {

        private final IntSupplier burnTicks;
        private final FloatingLongSupplier energyDensity;

        public Fuel(IntSupplier burnTicks, FloatingLongSupplier energyDensity) {
            this.burnTicks = burnTicks;
            this.energyDensity = energyDensity;
        }

        public int getBurnTicks() {
            return burnTicks.getAsInt();
        }

        public FloatingLong getEnergyPerTick() {
            int ticks = getBurnTicks();
            //If we have less than 2 ticks, the density is the
            if (ticks < 1) {
                MekanismAPI.logger.warn("Invalid tick count ({}) for Fuel attribute, this number should be at least 1.", ticks);
                return FloatingLong.ZERO;
            } else if (ticks == 1) {
                //Single tick, no division necessary
                return energyDensity.get();
            }
            return energyDensity.get().divide(ticks);
        }
    }
}
