package mekanism.api.chemical.gas.attribute;

import mekanism.api.chemical.attribute.ChemicalAttribute;

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
    }
}
