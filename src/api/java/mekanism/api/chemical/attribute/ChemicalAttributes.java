package mekanism.api.chemical.attribute;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.datamaps.chemical.attribute.ChemicalRadioactivity;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ITooltipHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

/**
 * @since 10.7.0 Previously was GasAttributes
 */
@SuppressWarnings("removal")
@Deprecated(forRemoval = true, since = "10.7.11")
public class ChemicalAttributes {

    private ChemicalAttributes() {
    }

    /**
     * This defines the radioactivity of a certain chemical. This attribute <i>requires validation</i>, meaning chemical containers won't be able to accept chemicals with
     * this attribute by default. Radioactivity is measured in Sv/h.
     *
     * @author aidancbrady
     * @deprecated Add as {@link mekanism.api.datamaps.chemical.attribute.ChemicalRadioactivity} to
     * {@link mekanism.api.datamaps.IMekanismDataMapTypes#chemicalRadioactivity()}.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public static class Radiation extends ChemicalAttribute {

        @Nullable
        private final ChemicalRadioactivity modernRepresentation;
        private final double radioactivity;

        /**
         * @param radioactivity Radioactivity of the chemical measured in Sv/h, must be greater than zero.
         */
        public Radiation(double radioactivity) {
            if (radioactivity <= 0) {
                throw new IllegalArgumentException("Radiation attribute should only be used when there actually is radiation! Radioactivity: " + radioactivity);
            }
            this.radioactivity = radioactivity;
            if (radioactivity <= IRadiationManager.INSTANCE.baselineRadiation()) {
                modernRepresentation = null;
            } else {
                modernRepresentation = new ChemicalRadioactivity(radioactivity);
            }
        }

        /**
         * @since 10.7.11
         */
        public Radiation(ChemicalRadioactivity modern) {
            this.modernRepresentation = Objects.requireNonNull(modern);
            this.radioactivity = modern.radioactivity();
        }

        @Internal
        @Nullable
        @Override
        public ChemicalRadioactivity asModern() {
            return modernRepresentation;
        }

        /**
         * Gets the radioactivity of this chemical in Sv/h. Each mB of this chemical released into the environment will cause a radiation source of the given
         * radioactivity.
         *
         * @return the radioactivity of this chemical
         */
        public double getRadioactivity() {
            return radioactivity;
        }

        @Override
        public boolean needsValidation() {
            //This attribute only actually needs validation if radiation is enabled
            return IRadiationManager.INSTANCE.isRadiationEnabled();
        }

        @Override
        @Deprecated(forRemoval = true, since = "10.7.4")
        public List<Component> addTooltipText(List<Component> list) {
            collectTooltips(list::add);
            return list;
        }

        @Override
        public void collectTooltips(Consumer<Component> adder) {
            if (needsValidation()) {
                //Only show the radioactive tooltip information if radiation is actually enabled
                ITooltipHelper tooltipHelper = ITooltipHelper.INSTANCE;
                adder.accept(APILang.CHEMICAL_ATTRIBUTE_RADIATION.translateColored(EnumColor.GRAY, EnumColor.INDIGO, tooltipHelper.getRadioactivityDisplayShort(getRadioactivity())));
            }
        }
    }

    /**
     * Defines the root data of a coolant, for use in Fission Reactors. Coolants have two primary properties: 'thermal enthalpy' and 'conductivity'. Thermal Enthalpy
     * defines how much energy one mB of the chemical can store; as such, lower values will cause reactors to require more of the chemical to stay cool. 'Conductivity'
     * defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated variant.
     *
     * @author aidancbrady
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public abstract static sealed class Coolant extends ChemicalAttribute permits CooledCoolant, HeatedCoolant {

        private final double thermalEnthalpy;
        private final double conductivity;

        /**
         * @param thermalEnthalpy Defines how much energy one mB of the chemical can store; lower values will cause reactors to require more of the chemical to stay cool.
         *                        Must be greater than zero.
         * @param conductivity    Defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated
         *                        variant. This value should be greater than zero, and at most one.
         */
        private Coolant(double thermalEnthalpy, double conductivity) {
            if (thermalEnthalpy <= 0) {
                throw new IllegalArgumentException("Coolant attributes must have a thermal enthalpy greater than zero! Thermal Enthalpy: " + thermalEnthalpy);
            } else if (conductivity <= 0 || conductivity > 1) {
                throw new IllegalArgumentException("Coolant attributes must have a conductivity greater than zero and at most one! Conductivity: " + conductivity);
            }
            this.thermalEnthalpy = thermalEnthalpy;
            this.conductivity = conductivity;
        }

        /**
         * Gets the thermal enthalpy of this coolant. Thermal Enthalpy defines how much energy one mB of the chemical can store.
         */
        public double getThermalEnthalpy() {
            return thermalEnthalpy;
        }

        /**
         * Gets the conductivity of this coolant. 'Conductivity' defines the proportion of a reactor's available heat that can be used at an instant to convert this
         * coolant's cool variant to its heated variant.
         */
        public double getConductivity() {
            return conductivity;
        }

        @Override
        @Deprecated(forRemoval = true, since = "10.7.4")
        public List<Component> addTooltipText(List<Component> list) {
            collectTooltips(list::add);
            return list;
        }

        @Override
        public void collectTooltips(Consumer<Component> adder) {
            ITooltipHelper tooltipHelper = ITooltipHelper.INSTANCE;
            adder.accept(APILang.CHEMICAL_ATTRIBUTE_COOLANT_EFFICIENCY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, tooltipHelper.getPercent(conductivity)));
            adder.accept(APILang.CHEMICAL_ATTRIBUTE_COOLANT_ENTHALPY.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                  tooltipHelper.getEnergyPerMBDisplayShort(MathUtils.clampToLong(thermalEnthalpy))));
        }
    }

    /**
     * Defines the 'cooled' variant of a coolant - the heated variant must be supplied in this class.
     *
     * @author aidancbrady
     * @deprecated Add as {@link mekanism.api.datamaps.chemical.attribute.CooledCoolant} to {@link mekanism.api.datamaps.IMekanismDataMapTypes#cooledChemicalCoolant()}.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public static non-sealed class CooledCoolant extends Coolant {

        @Nullable
        private final mekanism.api.datamaps.chemical.attribute.CooledCoolant modernRepresentation;
        @Deprecated(forRemoval = true, since = "10.7.11")
        private final IChemicalProvider heatedChemical;

        /**
         * @param heatedChemical  Chemical provider for the heated variant of this chemical.
         * @param thermalEnthalpy Defines how much energy one mB of the chemical can store; lower values will cause reactors to require more of the chemical to stay cool.
         *                        Must be greater than zero.
         * @param conductivity    Defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated
         *                        variant. This value should be greater than zero, and at most one.
         */
        @Deprecated(forRemoval = true, since = "10.7.11")
        public CooledCoolant(IChemicalProvider heatedChemical, double thermalEnthalpy, double conductivity) {
            super(thermalEnthalpy, conductivity);
            this.heatedChemical = heatedChemical;
            this.modernRepresentation = null;
        }

        /**
         * @since 10.7.11
         */
        public CooledCoolant(Holder<Chemical> heatedChemical, double thermalEnthalpy, double conductivity) {
            this(new mekanism.api.datamaps.chemical.attribute.CooledCoolant(heatedChemical, thermalEnthalpy, conductivity));
        }

        /**
         * @since 10.7.11
         */
        public CooledCoolant(mekanism.api.datamaps.chemical.attribute.CooledCoolant coolant) {
            super(coolant.thermalEnthalpy(), coolant.conductivity());
            this.modernRepresentation = coolant;
            this.heatedChemical = () -> this.modernRepresentation.otherVariant().value();
        }

        /**
         * Gets the heated version of this coolant.
         */
        public Chemical getHeatedChemical() {
            return heatedChemical.getChemical();
        }

        /**
         * Attempts to convert this fuel attribute into a modern fuel attribute
         *
         * @since 10.7.11
         */
        @Internal
        @Nullable
        @Override
        public mekanism.api.datamaps.chemical.attribute.CooledCoolant asModern() {
            if (modernRepresentation != null) {
                //CrT and JsonThings fuels will use this
                return modernRepresentation;
            }
            //Any modded ones that use a chemical provider
            Chemical chemical = heatedChemical.getChemical();
            if (chemical.isEmptyType()) {
                return null;
            }
            try {
                return new mekanism.api.datamaps.chemical.attribute.CooledCoolant(chemical.getAsHolder(), getThermalEnthalpy(), getConductivity());
            } catch (IllegalArgumentException ignored) {
            }
            return null;
        }
    }

    /**
     * Defines the 'heated' variant of a coolant - the cooled variant must be supplied in this class.
     *
     * @author aidancbrady
     * @deprecated Add as {@link mekanism.api.datamaps.chemical.attribute.HeatedCoolant} to {@link mekanism.api.datamaps.IMekanismDataMapTypes#heatedChemicalCoolant()}.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public static non-sealed class HeatedCoolant extends Coolant {

        @Nullable
        private final mekanism.api.datamaps.chemical.attribute.HeatedCoolant modernRepresentation;
        @Deprecated(forRemoval = true, since = "10.7.11")
        private final IChemicalProvider cooledChemical;

        /**
         * @param cooledChemical  Chemical provider for the cooled variant of this chemical.
         * @param thermalEnthalpy Defines how much energy one mB of the chemical can store; lower values will cause reactors to require more of the chemical to stay cool.
         *                        Must be greater than zero.
         * @param conductivity    Defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated
         *                        variant. This value should be greater than zero, and at most one.
         */
        @Deprecated(forRemoval = true, since = "10.7.11")
        public HeatedCoolant(IChemicalProvider cooledChemical, double thermalEnthalpy, double conductivity) {
            super(thermalEnthalpy, conductivity);
            this.cooledChemical = cooledChemical;
            this.modernRepresentation = null;
        }

        /**
         * @since 10.7.11
         */
        public HeatedCoolant(Holder<Chemical> cooledChemical, double thermalEnthalpy) {
            //Note: The value for conductivity used to have a different meaning/none so we ignore it here
            this(new mekanism.api.datamaps.chemical.attribute.HeatedCoolant(cooledChemical, thermalEnthalpy));
        }

        /**
         * @since 10.7.11
         */
        public HeatedCoolant(mekanism.api.datamaps.chemical.attribute.HeatedCoolant coolant) {
            //Note: The value for conductivity used to have a different meaning/none so we ignore it here
            super(coolant.thermalEnthalpy(), 1);
            this.modernRepresentation = coolant;
            this.cooledChemical = () -> this.modernRepresentation.otherVariant().value();
        }

        /**
         * Gets the cooled version of this coolant.
         */
        public Chemical getCooledChemical() {
            return cooledChemical.getChemical();
        }

        /**
         * Attempts to convert this fuel attribute into a modern fuel attribute
         *
         * @since 10.7.11
         */
        @Internal
        @Nullable
        @Override
        public mekanism.api.datamaps.chemical.attribute.HeatedCoolant asModern() {
            if (modernRepresentation != null) {
                //CrT and JsonThings fuels will use this
                return modernRepresentation;
            }
            //Any modded ones that use a chemical provider
            Chemical chemical = cooledChemical.getChemical();
            if (chemical.isEmptyType()) {
                return null;
            }
            try {
                return new mekanism.api.datamaps.chemical.attribute.HeatedCoolant(chemical.getAsHolder(), getThermalEnthalpy());
            } catch (IllegalArgumentException ignored) {
            }
            return null;
        }
    }

    /**
     * Defines a fuel which can be processed by a Gas-Burning Generator to produce energy. Fuels have two primary values: 'burn ticks', defining how many ticks one mB of
     * fuel can be burned for before being depleted, and 'energyDensity', defining how much energy is stored in one mB of fuel.
     *
     * @author aidancbrady
     * @deprecated Add as {@link mekanism.api.datamaps.chemical.attribute.ChemicalFuel} to {@link mekanism.api.datamaps.IMekanismDataMapTypes#chemicalFuel()}.
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public static class Fuel extends ChemicalAttribute {

        @Nullable
        private final ChemicalFuel modernRepresentation;
        private final IntSupplier burnTicks;
        private final LongSupplier energyDensity;

        /**
         * @param burnTicks     The number of ticks one mB of fuel can be burned for before being depleted; must be greater than zero.
         * @param energyDensity The energy density in one mB of fuel; must be greater than zero.
         *
         * @since 10.4.0
         */
        public Fuel(int burnTicks, long energyDensity) {
            long energyPerTick = energyDensity / burnTicks;
            if (energyPerTick < 0) {
                this.modernRepresentation = null;
                this.burnTicks = () -> burnTicks;
                this.energyDensity = () -> energyDensity;
            } else {
                this.modernRepresentation = new ChemicalFuel(burnTicks, energyPerTick);
                this.burnTicks = modernRepresentation::burnTicks;
                this.energyDensity = modernRepresentation::energyDensity;
            }
        }

        /**
         * @since 10.7.11
         */
        public Fuel(ChemicalFuel modernRepresentation) {
            this.modernRepresentation = modernRepresentation;
            this.burnTicks = modernRepresentation::burnTicks;
            this.energyDensity = modernRepresentation::energyDensity;
        }

        /**
         * @param burnTicks     Supplier for the number of ticks one mB of fuel can be burned for before being depleted. The supplier should return values greater than
         *                      zero.
         * @param energyDensity Supplier for the energy density of one mB of fuel. The supplier should return values be greater than zero.
         */
        public Fuel(IntSupplier burnTicks, LongSupplier energyDensity) {
            //Calculate it as necessary as we can't just make it at the start
            this.modernRepresentation = null;
            this.burnTicks = burnTicks;
            this.energyDensity = energyDensity;
        }

        /**
         * Gets the number of ticks this fuel burns for.
         */
        public int getBurnTicks() {
            return burnTicks.getAsInt();
        }

        /**
         * Gets the amount of energy produced per tick of this fuel.
         */
        public long getEnergyPerTick() {
            int ticks = getBurnTicks();
            //If we have less than one tick, the density is invalid
            if (ticks < 1) {
                MekanismAPI.logger.warn("Invalid tick count ({}) for Fuel attribute, this number should be at least 1.", ticks);
                return 0;
            } else if (ticks == 1) {
                //Single tick, no division necessary
                return energyDensity.getAsLong();
            }
            return energyDensity.getAsLong() / ticks;
        }

        /**
         * Attempts to convert this fuel attribute into a modern fuel attribute
         *
         * @since 10.7.11
         */
        @Internal
        @Nullable
        @Override
        public ChemicalFuel asModern() {
            if (modernRepresentation != null) {
                //CrT and JsonThings fuels will use this
                return modernRepresentation;
            }
            //Any modded ones that have to make use of suppliers
            int ticks = getBurnTicks();
            //If we have less than one tick, the density is invalid
            if (ticks < 1) {
                MekanismAPI.logger.warn("Invalid tick count ({}) for Fuel attribute, this number should be at least 1.", ticks);
                return null;
            }
            long density = energyDensity.getAsLong();
            long energyPerTick = density / ticks;
            if (energyPerTick == 0) {
                MekanismAPI.logger.warn("Invalid energy density ({}) for Fuel attribute, this number when divided by ticks should be at least 1.", density);
                return null;
            }
            try {
                return new ChemicalFuel(ticks, energyPerTick);
            } catch (IllegalArgumentException ignored) {
            }
            return null;
        }

        @Override
        @Deprecated(forRemoval = true, since = "10.7.4")
        public List<Component> addTooltipText(List<Component> list) {
            collectTooltips(list::add);
            return list;
        }

        @Override
        public void collectTooltips(Consumer<Component> adder) {
            ITooltipHelper tooltipHelper = ITooltipHelper.INSTANCE;
            adder.accept(APILang.CHEMICAL_ATTRIBUTE_FUEL_BURN_TICKS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, tooltipHelper.getFormattedNumber(getBurnTicks())));
            adder.accept(APILang.CHEMICAL_ATTRIBUTE_FUEL_ENERGY_DENSITY.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                  tooltipHelper.getEnergyPerMBDisplayShort(energyDensity.getAsLong())));
        }
    }
}
