package mekanism.common.util;

import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.api.IDisableableEnum;
import mekanism.api.IIncrementalEnum;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.network.chat.Component;

/**
 * Code taken from UE and modified to fit Mekanism.
 */
public class UnitDisplayUtils {
    //TODO: Maybe at some point improve on the ITextComponents the two getDisplay methods build, and have them have better translation keys with formats
    // That would improve how well this handles en_ud as currently the order of the number and the unit is not reversed and the unit is not upside down

    /**
     * Displays the unit as text. Does not handle negative numbers, as {@link FloatingLong} does not have a concept of negatives
     */
    public static Component getDisplay(FloatingLong value, EnergyUnit unit, int decimalPlaces, boolean isShort) {
        ILangEntry label = unit.pluralLangEntry;
        if (isShort) {
            label = unit.shortLangEntry;
        } else if (value.equals(FloatingLong.ONE)) {
            label = unit.singularLangEntry;
        }
        if (value.isZero()) {
            return TextComponentUtil.build(value + " ", label);
        }
        for (int i = 0; i < EnumUtils.FLOATING_LONG_MEASUREMENT_UNITS.length; i++) {
            FloatingLongMeasurementUnit lowerMeasure = EnumUtils.FLOATING_LONG_MEASUREMENT_UNITS[i];
            if (i == 0 && lowerMeasure.below(value)) {
                return TextComponentUtil.build(lowerMeasure.process(value).toString(decimalPlaces) + " " + lowerMeasure.getName(isShort), label);
            }
            if (i + 1 >= EnumUtils.FLOATING_LONG_MEASUREMENT_UNITS.length) {
                return TextComponentUtil.build(lowerMeasure.process(value).toString(decimalPlaces) + " " + lowerMeasure.getName(isShort), label);
            } else {
                FloatingLongMeasurementUnit upperMeasure = EnumUtils.FLOATING_LONG_MEASUREMENT_UNITS[i + 1];
                if ((lowerMeasure.above(value) && upperMeasure.below(value)) || lowerMeasure.value.equals(value)) {
                    return TextComponentUtil.build(lowerMeasure.process(value).toString(decimalPlaces) + " " + lowerMeasure.getName(isShort), label);
                }
            }
        }
        return TextComponentUtil.build(value.toString(decimalPlaces), label);
    }

    public static Component getDisplayShort(FloatingLong value, EnergyUnit unit) {
        return getDisplay(value, unit, 2, true);
    }

    public static Component getDisplay(double temp, TemperatureUnit unit, int decimalPlaces, boolean shift, boolean isShort) {
        return getDisplayBase(unit.convertFromK(temp, shift), unit, decimalPlaces, isShort, false);
    }

    public static Component getDisplayBase(double value, Unit unit, int decimalPlaces, boolean isShort, boolean spaceBetweenSymbol) {
        ILangEntry label = unit.getLabel();
        String spaceStr = spaceBetweenSymbol ? " " : "";
        if (value == 0) {
            return isShort ? TextComponentUtil.getString(value + spaceStr + unit.getSymbol()) : TextComponentUtil.build(value, label);
        }
        boolean negative = value < 0;
        if (negative) {
            value = Math.abs(value);
        }
        for (int i = 0; i < EnumUtils.MEASUREMENT_UNITS.length; i++) {
            MeasurementUnit lowerMeasure = EnumUtils.MEASUREMENT_UNITS[i];
            String symbolStr = spaceStr + lowerMeasure.symbol;
            if (lowerMeasure.below(value) && lowerMeasure.ordinal() == 0) {
                if (isShort) {
                    return TextComponentUtil.getString(roundDecimals(negative, lowerMeasure.process(value), decimalPlaces) + symbolStr + unit.getSymbol());
                }
                return TextComponentUtil.build(roundDecimals(negative, lowerMeasure.process(value), decimalPlaces) + " " + lowerMeasure.name, label);
            }
            if (lowerMeasure.ordinal() + 1 >= EnumUtils.MEASUREMENT_UNITS.length) {
                if (isShort) {
                    return TextComponentUtil.getString(roundDecimals(negative, lowerMeasure.process(value), decimalPlaces) + symbolStr + unit.getSymbol());
                }
                return TextComponentUtil.build(roundDecimals(negative, lowerMeasure.process(value), decimalPlaces) + " " + lowerMeasure.name, label);
            }
            if (i + 1 < EnumUtils.MEASUREMENT_UNITS.length) {
                MeasurementUnit upperMeasure = EnumUtils.MEASUREMENT_UNITS[i + 1];
                if ((lowerMeasure.above(value) && upperMeasure.below(value)) || lowerMeasure.value == value) {
                    if (isShort) {
                        return TextComponentUtil.getString(roundDecimals(negative, lowerMeasure.process(value), decimalPlaces) + symbolStr + unit.getSymbol());
                    }
                    return TextComponentUtil.build(roundDecimals(negative, lowerMeasure.process(value), decimalPlaces) + " " + lowerMeasure.name, label);
                }
            }
        }
        if (isShort) {
            return TextComponentUtil.getString(roundDecimals(negative, value, decimalPlaces) + spaceStr + unit.getSymbol());
        }
        return TextComponentUtil.build(roundDecimals(negative, value, decimalPlaces) + " ", label);
    }

    public static Component getDisplayShort(double value, TemperatureUnit unit) {
        return getDisplayShort(value, unit, true);
    }

    public static Component getDisplayShort(double value, TemperatureUnit unit, boolean shift) {
        return getDisplayShort(value, unit, shift, 2);
    }

    public static Component getDisplayShort(double value, TemperatureUnit unit, boolean shift, int decimalPlaces) {
        return getDisplay(value, unit, decimalPlaces, shift, true);
    }

    public static Component getDisplayShort(double value, RadiationUnit unit, int decimalPlaces) {
        return getDisplayBase(value, unit, decimalPlaces, true, true);
    }

    public static double roundDecimals(boolean negative, double d, int decimalPlaces) {
        return negative ? roundDecimals(-d, decimalPlaces) : roundDecimals(d, decimalPlaces);
    }

    public static double roundDecimals(double d, int decimalPlaces) {
        double multiplier = Math.pow(10, decimalPlaces);
        long j = (long) (d * multiplier);
        return j / multiplier;
    }

    public static double roundDecimals(double d) {
        return roundDecimals(d, 2);
    }

    private interface Unit {

        String getSymbol();

        ILangEntry getLabel();
    }

    public record EnergyConversionRate(CachedFloatingLongValue from, CachedFloatingLongValue to) {
    }

    public enum EnergyUnit implements IDisableableEnum<EnergyUnit>, IHasTranslationKey {
        JOULES(MekanismLang.ENERGY_JOULES, MekanismLang.ENERGY_JOULES_PLURAL, MekanismLang.ENERGY_JOULES_SHORT, "j", () -> true) {
            @Override
            protected EnergyConversionRate getConversionRate() {
                //Return null and then override usages of it as there is no conversion needed
                return null;
            }

            @Override
            public FloatingLong convertFrom(FloatingLong energy) {
                return energy;
            }

            @Override
            public FloatingLong convertToAsFloatingLong(FloatingLong joules) {
                return joules;
            }
        },
        FORGE_ENERGY(MekanismLang.ENERGY_FORGE, MekanismLang.ENERGY_FORGE, MekanismLang.ENERGY_FORGE_SHORT, "fe", () -> !MekanismConfig.general.blacklistForge.get()) {
            @Override
            protected EnergyConversionRate getConversionRate() {
                return MekanismConfig.general.FORGE_CONVERSION_RATE;
            }
        },
        ELECTRICAL_UNITS(MekanismLang.ENERGY_EU, MekanismLang.ENERGY_EU_PLURAL, MekanismLang.ENERGY_EU_SHORT, "eu", EnergyCompatUtils::useIC2) {
            @Override
            protected EnergyConversionRate getConversionRate() {
                return MekanismConfig.general.IC2_CONVERSION_RATE;
            }
        };

        private static final EnergyUnit[] TYPES = values();

        private final BooleanSupplier checkEnabled;
        private final ILangEntry singularLangEntry;
        private final ILangEntry pluralLangEntry;
        private final ILangEntry shortLangEntry;
        private final String tabName;

        EnergyUnit(ILangEntry singularLangEntry, ILangEntry pluralLangEntry, ILangEntry shortLangEntry, String tabName, BooleanSupplier checkEnabled) {
            this.singularLangEntry = singularLangEntry;
            this.pluralLangEntry = pluralLangEntry;
            this.shortLangEntry = shortLangEntry;
            this.checkEnabled = checkEnabled;
            this.tabName = tabName;
        }

        protected abstract EnergyConversionRate getConversionRate();

        public FloatingLong convertFrom(long energy) {
            return convertFrom(FloatingLong.createConst(energy));
        }

        public FloatingLong convertFrom(FloatingLong energy) {
            return energy.multiply(getConversionRate().from.get());
        }

        public int convertToAsInt(FloatingLong joules) {
            return convertToAsFloatingLong(joules).intValue();
        }

        public long convertToAsLong(FloatingLong joules) {
            return convertToAsFloatingLong(joules).longValue();
        }

        public FloatingLong convertToAsFloatingLong(FloatingLong joules) {
            return joules.multiply(getConversionRate().to.get());
        }

        @Override
        public String getTranslationKey() {
            return shortLangEntry.getTranslationKey();
        }

        @Nonnull
        @Override
        public EnergyUnit byIndex(int index) {
            return MathUtils.getByIndexMod(TYPES, index);
        }

        public String getTabName() {
            return tabName;
        }

        @Override
        public boolean isEnabled() {
            return checkEnabled.getAsBoolean();
        }

        public static EnergyUnit getConfigured() {
            EnergyUnit type = MekanismConfig.common.energyUnit.get();
            return type.isEnabled() ? type : EnergyUnit.JOULES;
        }
    }

    public enum TemperatureUnit implements IIncrementalEnum<TemperatureUnit>, IHasTranslationKey, Unit {
        KELVIN(MekanismLang.TEMPERATURE_KELVIN, MekanismLang.TEMPERATURE_KELVIN_SHORT, "K", "k", 0, 1),
        CELSIUS(MekanismLang.TEMPERATURE_CELSIUS, MekanismLang.TEMPERATURE_CELSIUS_SHORT, "\u00B0C", "c", 273.15, 1),
        RANKINE(MekanismLang.TEMPERATURE_RANKINE, MekanismLang.TEMPERATURE_RANKINE_SHORT, "R", "r", 0, 1.8),
        FAHRENHEIT(MekanismLang.TEMPERATURE_FAHRENHEIT, MekanismLang.TEMPERATURE_FAHRENHEIT_SHORT, "\u00B0F", "f", 459.67, 1.8),
        AMBIENT(MekanismLang.TEMPERATURE_AMBIENT, MekanismLang.TEMPERATURE_AMBIENT_SHORT, "+STP", "stp", 300, 1);

        private static final TemperatureUnit[] TYPES = values();

        private final ILangEntry langEntry;
        private final ILangEntry shortName;
        private final String symbol;
        private final String tabName;
        public final double zeroOffset;
        public final double intervalSize;

        TemperatureUnit(ILangEntry langEntry, ILangEntry shortName, String symbol, String tabName, double offset, double size) {
            this.langEntry = langEntry;
            this.shortName = shortName;
            this.symbol = symbol;
            this.tabName = tabName;
            this.zeroOffset = offset;
            this.intervalSize = size;
        }

        public double convertFromK(double temp, boolean shift) {
            return (temp * intervalSize) - (shift ? zeroOffset : 0);
        }

        public double convertToK(double temp, boolean shift) {
            return (temp + (shift ? zeroOffset : 0)) / intervalSize;
        }

        @Override
        public String getSymbol() {
            return symbol;
        }

        @Override
        public ILangEntry getLabel() {
            return langEntry;
        }

        @Override
        public String getTranslationKey() {
            return shortName.getTranslationKey();
        }

        public String getTabName() {
            return tabName;
        }

        @Nonnull
        @Override
        public TemperatureUnit byIndex(int index) {
            return MathUtils.getByIndexMod(TYPES, index);
        }
    }

    public enum RadiationUnit implements Unit {
        SV("Sv"),
        SVH("Sv/h");

        private final String symbol;

        RadiationUnit(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String getSymbol() {
            return symbol;
        }

        @Override
        public ILangEntry getLabel() {
            return MekanismLang.ERROR;
        }
    }

    /**
     * Metric system of measurement.
     */
    public enum MeasurementUnit {
        FEMTO("Femto", "f", 0.000000000000001D),
        PICO("Pico", "p", 0.000000000001D),
        NANO("Nano", "n", 0.000000001D),
        MICRO("Micro", "\u00B5", 0.000001D),
        MILLI("Milli", "m", 0.001D),
        BASE("", "", 1),
        KILO("Kilo", "k", 1_000D),
        MEGA("Mega", "M", 1_000_000D),
        GIGA("Giga", "G", 1_000_000_000D),
        TERA("Tera", "T", 1_000_000_000_000D),
        PETA("Peta", "P", 1_000_000_000_000_000D),
        EXA("Exa", "E", 1_000_000_000_000_000_000D),
        ZETTA("Zetta", "Z", 1_000_000_000_000_000_000_000D),
        YOTTA("Yotta", "Y", 1_000_000_000_000_000_000_000_000D);

        /**
         * long name for the unit
         */
        private final String name;

        /**
         * short unit version of the unit
         */
        private final String symbol;

        /**
         * Point by which a number is considered to be of this unit
         */
        private final double value;

        MeasurementUnit(String name, String symbol, double value) {
            this.name = name;
            this.symbol = symbol;
            this.value = value;
        }

        public String getName(boolean getShort) {
            if (getShort) {
                return symbol;
            }
            return name;
        }

        public double process(double d) {
            return d / value;
        }

        public boolean above(double d) {
            return d > value;
        }

        public boolean below(double d) {
            return d < value;
        }
    }

    /**
     * Metric system of measurement.
     */
    public enum FloatingLongMeasurementUnit {
        MILLI("Milli", "m", FloatingLong.createConst(.001)),
        BASE("", "", FloatingLong.ONE),
        KILO("Kilo", "k", FloatingLong.createConst(1_000)),
        MEGA("Mega", "M", FloatingLong.createConst(1_000_000)),
        GIGA("Giga", "G", FloatingLong.createConst(1_000_000_000)),
        TERA("Tera", "T", FloatingLong.createConst(1_000_000_000_000L)),
        PETA("Peta", "P", FloatingLong.createConst(1_000_000_000_000_000L)),
        EXA("Exa", "E", FloatingLong.createConst(1_000_000_000_000_000_000L));

        /**
         * long name for the unit
         */
        private final String name;

        /**
         * short unit version of the unit
         */
        private final String symbol;

        /**
         * Point by which a number is considered to be of this unit
         */
        private final FloatingLong value;

        FloatingLongMeasurementUnit(String name, String symbol, FloatingLong value) {
            this.name = name;
            this.symbol = symbol;
            this.value = value;
        }

        public String getName(boolean getShort) {
            if (getShort) {
                return symbol;
            }
            return name;
        }

        public FloatingLong process(FloatingLong d) {
            return d.divide(value);
        }

        public boolean above(FloatingLong d) {
            return d.greaterThan(value);
        }

        public boolean below(FloatingLong d) {
            return d.smallerThan(value);
        }
    }
}