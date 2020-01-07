package mekanism.common.util;

import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.util.text.ITextComponent;

/**
 * Code taken from UE and modified to fit Mekanism.
 */
public class UnitDisplayUtils {//TODO: Maybe at some point improve on the ITextComponents the two getDisplay methods build, and have them have better translation keys with formats

    /**
     * Displays the unit as text. Does handle negative numbers, and will place a negative sign in front of the output string showing this. Use string.replace to remove
     * the negative sign if unwanted
     */
    public static ITextComponent getDisplay(double value, ElectricUnit unit, int decimalPlaces, boolean isShort) {
        ILangEntry label = unit.pluralLangEntry;
        String prefix = "";
        if (value < 0) {
            value = Math.abs(value);
            prefix = "-";
        }
        if (isShort) {
            label = unit.shortLangEntry;
        } else if (value == 1 || value == -1) {
            label = unit.singularLangEntry;
        }
        if (value == 0) {
            return TextComponentUtil.build(value + " ", label);
        }
        for (int i = 0; i < EnumUtils.MEASUREMENT_UNITS.length; i++) {
            MeasurementUnit lowerMeasure = EnumUtils.MEASUREMENT_UNITS[i];
            if (lowerMeasure.below(value) && lowerMeasure.ordinal() == 0) {
                return TextComponentUtil.build(prefix + roundDecimals(lowerMeasure.process(value), decimalPlaces) + " " + lowerMeasure.getName(isShort), label);
            }
            if (lowerMeasure.ordinal() + 1 >= EnumUtils.MEASUREMENT_UNITS.length) {
                return TextComponentUtil.build(prefix + roundDecimals(lowerMeasure.process(value), decimalPlaces) + " " + lowerMeasure.getName(isShort), label);
            }
            if (i + 1 < EnumUtils.MEASUREMENT_UNITS.length) {
                MeasurementUnit upperMeasure = EnumUtils.MEASUREMENT_UNITS[i + 1];
                if ((lowerMeasure.above(value) && upperMeasure.below(value)) || lowerMeasure.value == value) {
                    return TextComponentUtil.build(prefix + roundDecimals(lowerMeasure.process(value), decimalPlaces) + " " + lowerMeasure.getName(isShort), label);
                }
            }
        }
        return TextComponentUtil.build(prefix + roundDecimals(value, decimalPlaces), label);
    }

    public static ITextComponent getDisplayShort(double value, ElectricUnit unit) {
        return getDisplay(value, unit, 2, true);
    }

    public static ITextComponent getDisplay(double T, TemperatureUnit unit, int decimalPlaces, boolean shift, boolean isShort) {
        ILangEntry label = unit.langEntry;
        String prefix = "";
        double value = unit.convertFromK(T, shift);
        if (value < 0) {
            value = Math.abs(value);
            prefix = "-";
        }
        if (value == 0) {
            return isShort ? TextComponentUtil.getString(value + unit.symbol) : TextComponentUtil.build(value, label);
        }
        for (int i = 0; i < EnumUtils.MEASUREMENT_UNITS.length; i++) {
            MeasurementUnit lowerMeasure = EnumUtils.MEASUREMENT_UNITS[i];
            if (lowerMeasure.below(value) && lowerMeasure.ordinal() == 0) {
                if (isShort) {
                    return TextComponentUtil.getString(prefix + roundDecimals(lowerMeasure.process(value), decimalPlaces) + lowerMeasure.symbol + unit.symbol);
                }
                return TextComponentUtil.build(prefix + roundDecimals(lowerMeasure.process(value), decimalPlaces) + " " + lowerMeasure.name, label);
            }
            if (lowerMeasure.ordinal() + 1 >= EnumUtils.MEASUREMENT_UNITS.length) {
                if (isShort) {
                    return TextComponentUtil.getString(prefix + roundDecimals(lowerMeasure.process(value), decimalPlaces) + lowerMeasure.symbol + unit.symbol);
                }
                return TextComponentUtil.build(prefix + roundDecimals(lowerMeasure.process(value), decimalPlaces) + " " + lowerMeasure.name, label);
            }
            if (i + 1 < EnumUtils.MEASUREMENT_UNITS.length) {
                MeasurementUnit upperMeasure = EnumUtils.MEASUREMENT_UNITS[i + 1];
                if ((lowerMeasure.above(value) && upperMeasure.below(value)) || lowerMeasure.value == value) {
                    if (isShort) {
                        return TextComponentUtil.getString(prefix + roundDecimals(lowerMeasure.process(value), decimalPlaces) + lowerMeasure.symbol + unit.symbol);
                    }
                    return TextComponentUtil.build(prefix + roundDecimals(lowerMeasure.process(value), decimalPlaces) + " " + lowerMeasure.name, label);
                }
            }
        }
        if (isShort) {
            return TextComponentUtil.getString(prefix + roundDecimals(value, decimalPlaces) + unit.symbol);
        }
        return TextComponentUtil.build(prefix + roundDecimals(value, decimalPlaces) + " ", label);
    }

    public static ITextComponent getDisplayShort(double value, TemperatureUnit unit) {
        return getDisplayShort(value, unit, true);
    }

    public static ITextComponent getDisplayShort(double value, TemperatureUnit unit, boolean shift) {
        return getDisplayShort(value, unit, shift, 2);
    }

    public static ITextComponent getDisplayShort(double value, TemperatureUnit unit, boolean shift, int decimalPlaces) {
        return getDisplay(value, unit, decimalPlaces, shift, true);
    }

    public static double roundDecimals(double d, int decimalPlaces) {
        int j = (int) (d * Math.pow(10, decimalPlaces));
        return j / Math.pow(10, decimalPlaces);
    }

    public static double roundDecimals(double d) {
        return roundDecimals(d, 2);
    }

    public enum ElectricUnit {
        JOULES(MekanismLang.ENERGY_JOULES, MekanismLang.ENERGY_JOULES_PLURAL, MekanismLang.ENERGY_JOULES_SHORT),
        FORGE_ENERGY(MekanismLang.ENERGY_FORGE, MekanismLang.ENERGY_FORGE, MekanismLang.ENERGY_FORGE_SHORT),
        ELECTRICAL_UNITS(MekanismLang.ENERGY_EU, MekanismLang.ENERGY_EU_PLURAL, MekanismLang.ENERGY_EU_SHORT);

        private final ILangEntry singularLangEntry;
        private final ILangEntry pluralLangEntry;
        private final ILangEntry shortLangEntry;

        ElectricUnit(ILangEntry singularLangEntry, ILangEntry pluralLangEntry, ILangEntry shortLangEntry) {
            this.singularLangEntry = singularLangEntry;
            this.pluralLangEntry = pluralLangEntry;
            this.shortLangEntry = shortLangEntry;
        }
    }

    public enum TemperatureUnit {
        KELVIN(MekanismLang.TEMPERATURE_KELVIN, "K", 0, 1),
        CELSIUS(MekanismLang.TEMPERATURE_CELSIUS, "°C", 273.15, 1),
        RANKINE(MekanismLang.TEMPERATURE_RANKINE, "R", 0, 1.8),
        FAHRENHEIT(MekanismLang.TEMPERATURE_FAHRENHEIT, "°F", 459.67, 1.8),
        AMBIENT(MekanismLang.TEMPERATURE_AMBIENT, "+STP", 300, 1);

        private final ILangEntry langEntry;
        //TODO: Do we want to make the symbol be localized?
        private final String symbol;
        public final double zeroOffset;
        public final double intervalSize;

        TemperatureUnit(ILangEntry langEntry, String symbol, double offset, double size) {
            this.langEntry = langEntry;
            this.symbol = symbol;
            this.zeroOffset = offset;
            this.intervalSize = size;
        }

        public double convertFromK(double T, boolean shift) {
            return (T * intervalSize) - (shift ? zeroOffset : 0);
        }

        public double convertToK(double T, boolean shift) {
            return (T + (shift ? zeroOffset : 0)) / intervalSize;
        }
    }

    /**
     * Metric system of measurement.
     */
    public enum MeasurementUnit {
        FEMTO("Femto", "f", 0.000000000000001D),
        PICO("Pico", "p", 0.000000000001D),
        NANO("Nano", "n", 0.000000001D),
        MICRO("Micro", "u", 0.000001D),
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
         * Point by which a number is consider to be of this unit
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

    public enum EnergyType implements IIncrementalEnum<EnergyType>, IHasTranslationKey {
        J(MekanismLang.ENERGY_JOULES_SHORT),
        FE(MekanismLang.ENERGY_FORGE_SHORT),
        EU(MekanismLang.ENERGY_EU_SHORT);

        private static final EnergyType[] TYPES = values();
        private final ILangEntry langEntry;

        EnergyType(ILangEntry langEntry) {
            this.langEntry = langEntry;
        }

        @Override
        public String getTranslationKey() {
            return langEntry.getTranslationKey();
        }

        @Nonnull
        @Override
        public EnergyType byIndex(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return TYPES[Math.floorMod(index, TYPES.length)];
        }
    }

    public enum TempType implements IIncrementalEnum<TempType>, IHasTranslationKey {
        K(MekanismLang.TEMPERATURE_KELVIN_SHORT),
        C(MekanismLang.TEMPERATURE_CELSIUS_SHORT),
        R(MekanismLang.TEMPERATURE_RANKINE_SHORT),
        F(MekanismLang.TEMPERATURE_FAHRENHEIT_SHORT),
        STP(MekanismLang.TEMPERATURE_AMBIENT_SHORT);

        private static final TempType[] TYPES = values();
        private final ILangEntry langEntry;

        TempType(ILangEntry langEntry) {
            this.langEntry = langEntry;
        }

        @Override
        public String getTranslationKey() {
            return langEntry.getTranslationKey();
        }

        @Nonnull
        @Override
        public TempType byIndex(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return TYPES[Math.floorMod(index, TYPES.length)];
        }
    }
}