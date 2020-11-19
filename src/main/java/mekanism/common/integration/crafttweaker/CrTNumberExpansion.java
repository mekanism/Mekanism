package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import org.openzen.zencode.java.ZenCodeType;

public class CrTNumberExpansion {

    private CrTNumberExpansion() {
    }

    @ZenRegister
    @ZenCodeType.Expansion("byte")
    public static class ByteExpansion {

        /**
         * Allows for casting ints to {@link CrTFloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTFloatingLong asFloatingLong(byte _this) {
            return CrTFloatingLong.create(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion("short")
    public static class ShortExpansion {

        /**
         * Allows for casting ints to {@link CrTFloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTFloatingLong asFloatingLong(short _this) {
            return CrTFloatingLong.create(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion("int")
    public static class IntExpansion {

        /**
         * Allows for casting ints to {@link CrTFloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTFloatingLong asFloatingLong(int _this) {
            return CrTFloatingLong.create(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion("long")
    public static class LongExpansion {

        /**
         * Allows for casting longs to {@link CrTFloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTFloatingLong asFloatingLong(long _this) {
            return CrTFloatingLong.create(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion("float")
    public static class FloatExpansion {

        /**
         * Allows for casting floats to {@link CrTFloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTFloatingLong asFloatingLong(float _this) {
            return CrTFloatingLong.create(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion("double")
    public static class DoubleExpansion {

        /**
         * Allows for casting doubles to {@link CrTFloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTFloatingLong asFloatingLong(double _this) {
            return CrTFloatingLong.create(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion("string")
    public static class StringExpansion {

        /**
         * Allows for casting strings to {@link CrTFloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTFloatingLong asFloatingLong(String _this) {
            return CrTFloatingLong.create(_this);
        }
    }
}