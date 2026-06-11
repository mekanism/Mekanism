package mekanism.common.util;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.jspecify.annotations.Nullable;

/// Code taken from UE and modified to fit Mekanism.
public class UnitDisplayUtils {
    //TODO: Maybe at some point improve on the ITextComponents the two getDisplay methods build, and have them have better translation keys with formats
    // That would improve how well this handles en_ud as currently the order of the number and the unit is not reversed and the unit is not upside down

    private static final Unit IGNORED_UNIT = new Unit() {

        @Override
        public Component appendTo(Object existing, boolean isShort, boolean spaceBetweenSymbol, boolean singular) {
            return TextComponentUtil.build(existing);
        }

        @Nullable
        @Override
        public Object getSymbol(boolean singular) {
            return null;
        }

        @Nullable
        @Override
        public ILangEntry getLabel(boolean singular) {
            return null;
        }
    };
    private static final Unit FORGE_ENERGY = new Unit() {

        @Override
        public Object getSymbol(boolean singular) {
            return MekanismLang.ENERGY_FORGE_SHORT;
        }

        @Override
        public ILangEntry getLabel(boolean singular) {
            return MekanismLang.ENERGY_FORGE;
        }
    };

    public static Component getEnergyDisplayShort(long value) {
        //TODO - 26.1: Re-evaluate this, getDisplayBase takes a double, can we make it more accurate for higher values by creating a variant for longs?
        return getDisplayBase(value, FORGE_ENERGY, 2, true, true);
    }

    public static Component getDisplay(double temp, TemperatureUnit unit, int decimalPlaces, boolean shift, boolean isShort, boolean spaceBetweenSymbol) {
        return getDisplayBase(unit.convertFromK(temp, shift), unit, decimalPlaces, isShort, spaceBetweenSymbol);
    }

    public static Component getDisplay(double value, int decimalPlaces) {
        return getDisplayBase(value, IGNORED_UNIT, decimalPlaces, true, false);
    }

    private static Component getDisplayBase(double value, Unit unit, int decimalPlaces, boolean isShort, boolean spaceBetweenSymbol) {
        if (value == 0) {
            return unit.appendTo("0", isShort, spaceBetweenSymbol, false);
        }
        boolean singular = Mth.equal(value, 1);
        boolean negative = value < 0;
        if (negative) {
            value = Math.abs(value);
        }
        for (int i = 0; i < EnumUtils.MEASUREMENT_UNITS.length; i++) {
            MeasurementUnit lowerMeasure = EnumUtils.MEASUREMENT_UNITS[i];
            if ((i == 0 && lowerMeasure.below(value)) ||
                i + 1 >= EnumUtils.MEASUREMENT_UNITS.length ||
                (lowerMeasure.aboveEqual(value) && EnumUtils.MEASUREMENT_UNITS[i + 1].below(value))) {
                //First element and it is below it (no more unit abbreviations before),
                // or last element (no more unit abbreviations past),
                // or we are within the bounds between this one and the next one
                return lowerMeasure.getDisplay(value, unit, decimalPlaces, isShort, spaceBetweenSymbol, negative, singular);
            }
        }
        //Fallback, should never be reached as should have been captured by the check in the loop
        return EnumUtils.MEASUREMENT_UNITS[EnumUtils.MEASUREMENT_UNITS.length - 1].getDisplay(value, unit, decimalPlaces, isShort, spaceBetweenSymbol, negative, singular);
    }

    public static Component getDisplayShort(double value, TemperatureUnit unit) {
        return getDisplayShort(value, unit, true);
    }

    public static Component getDisplayShort(double value, TemperatureUnit unit, boolean shift) {
        return getDisplayShort(value, unit, shift, 2);
    }

    public static Component getDisplayShort(double value, TemperatureUnit unit, boolean shift, int decimalPlaces) {
        return getDisplay(value, unit, decimalPlaces, shift, true, false);
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

        default Component appendTo(Object existing, boolean isShort, boolean spaceBetweenSymbol, boolean singular) {
            if (isShort) {
                if (spaceBetweenSymbol) {
                    return TextComponentUtil.build(existing + " ", getSymbol(singular));
                }
                return TextComponentUtil.build(existing, getSymbol(singular));
            }
            return TextComponentUtil.build(existing, getLabel(singular));
        }

        @Nullable
        Object getSymbol(boolean singular);

        @Nullable
        ILangEntry getLabel(boolean singular);
    }

    public enum TemperatureUnit implements IIncrementalEnum<TemperatureUnit>, IHasTranslationKey, Unit, TranslatableEnum {
        KELVIN(MekanismLang.TEMPERATURE_KELVIN, MekanismLang.TEMPERATURE_KELVIN_SHORT, "K", "k", 0, 1),
        CELSIUS(MekanismLang.TEMPERATURE_CELSIUS, MekanismLang.TEMPERATURE_CELSIUS_SHORT, "°C", "c", 273.15, 1),
        RANKINE(MekanismLang.TEMPERATURE_RANKINE, MekanismLang.TEMPERATURE_RANKINE_SHORT, "R", "r", 0, 1.8),
        FAHRENHEIT(MekanismLang.TEMPERATURE_FAHRENHEIT, MekanismLang.TEMPERATURE_FAHRENHEIT_SHORT, "°F", "f", 459.67, 1.8),
        AMBIENT(MekanismLang.TEMPERATURE_AMBIENT, MekanismLang.TEMPERATURE_AMBIENT_SHORT, "+STP", "stp", 300, 1);

        public static final IntFunction<TemperatureUnit> BY_ID = ByIdMap.continuous(TemperatureUnit::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, TemperatureUnit> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, TemperatureUnit::ordinal);

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
        public String getSymbol(boolean singular) {
            return symbol;
        }

        @Override
        public ILangEntry getLabel(boolean singular) {
            return langEntry;
        }

        @Override
        public Component getTranslatedName() {
            return getLabel(false).translate();
        }

        @Override
        public String getTranslationKey() {
            return shortName.getTranslationKey();
        }

        public String getTabName() {
            return tabName;
        }

        @Override
        public TemperatureUnit byIndex(int index) {
            return BY_ID.apply(index);
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
        public String getSymbol(boolean singular) {
            return symbol;
        }

        @Override
        public ILangEntry getLabel(boolean singular) {
            return MekanismLang.ERROR;
        }
    }

    /// Metric system of measurement.
    public enum MeasurementUnit {
        FEMTO("Femto", "f", 0.000_000_000_000_001D),
        PICO("Pico", "p", 0.000_000_000_001D),
        NANO("Nano", "n", 0.000_000_001D),
        MICRO("Micro", "µ", 0.000_001D),
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

        /// long name for the unit
        private final String name;

        /// short unit version of the unit
        private final String symbol;

        /// Point by which a number is considered to be of this unit
        private final double value;

        MeasurementUnit(String name, String symbol, double value) {
            this.name = name;
            this.symbol = symbol;
            this.value = value;
        }

        public String getName(boolean isShort) {
            if (isShort) {
                return symbol;
            }
            return name;
        }

        public double process(double d) {
            return d / value;
        }

        public boolean aboveEqual(double d) {
            return d >= value;
        }

        public boolean below(double d) {
            return d < value;
        }

        private Component getDisplay(double value, Unit unit, int decimalPlaces, boolean isShort, boolean spaceBetweenSymbol, boolean negative, boolean singular) {
            double rounded = roundDecimals(negative, process(value), decimalPlaces);
            String name = getName(isShort);
            if (spaceBetweenSymbol || !isShort) {
                name = " " + name;
            }
            String roundedStr = Double.toString(rounded);
            if (roundedStr.endsWith(".0")) {
                roundedStr = roundedStr.substring(0, roundedStr.length() - 2);
            }
            //Note: We handle the space between symbols above
            return unit.appendTo(roundedStr + name, isShort, false, singular);
        }
    }
}