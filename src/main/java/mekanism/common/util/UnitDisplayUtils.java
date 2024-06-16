package mekanism.common.util;

import io.netty.buffer.ByteBuf;
import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import mekanism.api.IDisableableEnum;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyConversion;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.ULong;
import mekanism.api.math.Unsigned;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.listener.ConfigBasedCachedSupplier;
import mekanism.common.config.value.CachedDoubleValue;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Code taken from UE and modified to fit Mekanism.
 */
public class UnitDisplayUtils {
    //TODO: Maybe at some point improve on the ITextComponents the two getDisplay methods build, and have them have better translation keys with formats
    // That would improve how well this handles en_ud as currently the order of the number and the unit is not reversed and the unit is not upside down

    /**
     * Displays the unit as text. Does not handle negative numbers, as {@link FloatingLong} does not have a concept of negatives
     */
    public static Component getDisplay(@Unsigned long value, EnergyUnit unit, int decimalPlaces, boolean isShort) {
        ILangEntry label = unit.pluralLangEntry;
        if (isShort) {
            label = unit.shortLangEntry;
        } else if (value == 1) {
            label = unit.singularLangEntry;
        }
        if (value == 0) {
            return TextComponentUtil.build(value + " ", label);
        }
        for (int i = 0; i < EnumUtils.ULONG_MEASUREMENT_UNITS.length; i++) {
            ULongMeasurementUnit lowerMeasure = EnumUtils.ULONG_MEASUREMENT_UNITS[i];
            if ((i == 0 && lowerMeasure.below(value)) ||
                i + 1 >= EnumUtils.ULONG_MEASUREMENT_UNITS.length ||
                (lowerMeasure.aboveEqual(value) && EnumUtils.ULONG_MEASUREMENT_UNITS[i + 1].below(value))) {
                //First element and it is below it (no more unit abbreviations before),
                // or last element (no more unit abbreviations past),
                // or we are within the bounds between this one and the next one
                double rounded = roundDecimals(false, lowerMeasure.process(value), decimalPlaces);
                return TextComponentUtil.build(rounded + " " + lowerMeasure.getName(isShort), label);
            }
        }
        return TextComponentUtil.build(Long.toUnsignedString(value), label);
    }

    public static Component getDisplayShort(@Unsigned long value, EnergyUnit unit) {
        return getDisplay(value, unit, 2, true);
    }

    public static Component getDisplay(double temp, TemperatureUnit unit, int decimalPlaces, boolean shift, boolean isShort, boolean spaceBetweenSymbol) {
        return getDisplayBase(unit.convertFromK(temp, shift), unit, decimalPlaces, isShort, spaceBetweenSymbol);
    }

    private static Component getDisplayBase(double value, Unit unit, int decimalPlaces, boolean isShort, boolean spaceBetweenSymbol) {
        if (value == 0) {
            if (isShort) {
                String spaceStr = spaceBetweenSymbol ? " " : "";
                return TextComponentUtil.getString(value + spaceStr + unit.getSymbol());
            }
            return TextComponentUtil.build(value, unit.getLabel());
        }
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
                return lowerMeasure.getDisplay(value, unit, decimalPlaces, isShort, spaceBetweenSymbol, negative);
            }
        }
        //Fallback, should never be reached as should have been captured by the check in the loop
        return EnumUtils.MEASUREMENT_UNITS[EnumUtils.MEASUREMENT_UNITS.length - 1].getDisplay(value, unit, decimalPlaces, isShort, spaceBetweenSymbol, negative);
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

        String getSymbol();

        ILangEntry getLabel();
    }

    @NothingNullByDefault
    public enum EnergyUnit implements IDisableableEnum<EnergyUnit>, IEnergyConversion {
        JOULES(MekanismLang.ENERGY_JOULES, MekanismLang.ENERGY_JOULES_PLURAL, MekanismLang.ENERGY_JOULES_SHORT, "j", null, ConstantPredicates.ALWAYS_TRUE) {
            @Override
            protected double getConversion() {
                //Unused but override it anyway
                return 1D;
            }

            @Override
            protected double getInverseConversion() {
                //Unused but override it anyway
                return 1D;
            }

            @Override
            public @Unsigned long convertFrom(@Unsigned long joules) {
                return joules;
            }

            @Override
            public @Unsigned long convertTo(@Unsigned long joules) {
                return joules;
            }
        },
        FORGE_ENERGY(MekanismLang.ENERGY_FORGE, MekanismLang.ENERGY_FORGE, MekanismLang.ENERGY_FORGE_SHORT, "fe", () -> MekanismConfig.general.forgeConversionRate,
              //Note: Use default value if called before configs are loaded. In general this should never happen, but third party mods may just call it regardless
              () -> !MekanismConfig.general.blacklistForge.getOrDefault());

        public static final IntFunction<EnergyUnit> BY_ID = ByIdMap.continuous(EnergyUnit::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, EnergyUnit> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, EnergyUnit::ordinal);

        private final Supplier<CachedDoubleValue> conversion;
        private final Supplier<Supplier<Double>> inverseConversion;
        private final BooleanSupplier checkEnabled;
        private final ILangEntry singularLangEntry;
        private final ILangEntry pluralLangEntry;
        private final ILangEntry shortLangEntry;
        private final String tabName;

        //Note: We ignore improper nulls as they only are null for joules which overrides the various use places
        @SuppressWarnings("ConstantConditions")
        EnergyUnit(ILangEntry singularLangEntry, ILangEntry pluralLangEntry, ILangEntry shortLangEntry, String tabName,
              @Nullable Supplier<CachedDoubleValue> conversionRate, BooleanSupplier checkEnabled) {
            this.singularLangEntry = singularLangEntry;
            this.pluralLangEntry = pluralLangEntry;
            this.shortLangEntry = shortLangEntry;
            this.checkEnabled = checkEnabled;
            this.tabName = tabName;
            this.conversion = conversionRate;
            if (this.conversion == null) {
                this.inverseConversion = null;
            } else {
                //Cache the inverse as multiplication for floating longs is more consistently fast compared to division
                //Note: We also cache the creation of our cache so that when MC is not initialized we can still create
                // this enum without having initialization errors. Use case: Unit tests
                inverseConversion = Lazy.of(() -> new ConfigBasedCachedSupplier<Double>(() -> 1D / getConversion(), this.conversion.get()));
            }
        }

        protected double getConversion() {
            //Note: Use default value if called before configs are loaded. In general this should never happen,
            // but third party mods may just call it regardless
            return conversion.get().getOrDefault();
        }

        protected double getInverseConversion() {
            return inverseConversion.get().get();
        }

        @Override
        public @Unsigned long convertFrom(@Unsigned long energy) {
            return (long) (energy * getConversion());
        }

        @Override
        public @Unsigned long convertTo(@Unsigned long joules) {
            if (joules == 0) {
                //Short circuit if energy is zero to avoid having to create any additional objects
                return 0L;
            }
            return (long) (joules * getInverseConversion());
        }

        @Override
        public String getTranslationKey() {
            return shortLangEntry.getTranslationKey();
        }

        @NotNull
        @Override
        public EnergyUnit byIndex(int index) {
            return BY_ID.apply(index);
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

    @NothingNullByDefault
    public enum TemperatureUnit implements IIncrementalEnum<TemperatureUnit>, IHasTranslationKey, Unit {
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

        private Component getDisplay(double value, Unit unit, int decimalPlaces, boolean isShort, boolean spaceBetweenSymbol, boolean negative) {
            double rounded = roundDecimals(negative, process(value), decimalPlaces);
            String name = getName(isShort);
            if (isShort) {
                if (spaceBetweenSymbol) {
                    name = " " + name;
                }
                return TextComponentUtil.getString(rounded + name + unit.getSymbol());
            }
            return TextComponentUtil.build(rounded + " " + name, unit.getLabel());
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

        public boolean aboveEqual(FloatingLong d) {
            return d.greaterOrEqual(value);
        }

        public boolean below(FloatingLong d) {
            return d.smallerThan(value);
        }
    }

    /**
     * Metric system of measurement.
     */
    public enum ULongMeasurementUnit {
        BASE("", "", 1L),
        KILO("Kilo", "k", 1_000L),
        MEGA("Mega", "M", 1_000_000L),
        GIGA("Giga", "G", 1_000_000_000L),
        TERA("Tera", "T", 1_000_000_000_000L),
        PETA("Peta", "P", 1_000_000_000_000_000L),
        EXA("Exa", "E", 1_000_000_000_000_000_000L);

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
        private final @Unsigned long value;

        ULongMeasurementUnit(String name, String symbol, @Unsigned long value) {
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

        public double process(@Unsigned long d) {
            return ULong.divide(d, value);
        }

        public boolean aboveEqual(@Unsigned long d) {
            return ULong.gte(d, value);
        }

        public boolean below(@Unsigned long d) {
            return ULong.lt(d, value);
        }
    }
}