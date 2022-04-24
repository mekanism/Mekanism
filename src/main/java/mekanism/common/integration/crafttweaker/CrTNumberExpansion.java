package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.math.FloatingLong;
import org.openzen.zencode.java.ZenCodeType;

public class CrTNumberExpansion {

    private CrTNumberExpansion() {
    }

    @ZenRegister
    @ZenCodeType.Expansion("byte")
    public static class ByteExpansion {

        private ByteExpansion() {
        }

        /**
         * Allows for casting ints to {@link FloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static FloatingLong asFloatingLong(byte _this) {
            return CrTFloatingLong.create(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion("short")
    public static class ShortExpansion {

        private ShortExpansion() {
        }

        /**
         * Allows for casting ints to {@link FloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static FloatingLong asFloatingLong(short _this) {
            return CrTFloatingLong.create(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion("int")
    public static class IntExpansion {

        private IntExpansion() {
        }

        /**
         * Allows for casting ints to {@link FloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static FloatingLong asFloatingLong(int _this) {
            return CrTFloatingLong.create(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion("long")
    public static class LongExpansion {

        private LongExpansion() {
        }

        /**
         * Allows for casting longs to {@link FloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static FloatingLong asFloatingLong(long _this) {
            return CrTFloatingLong.create(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion("float")
    public static class FloatExpansion {

        private FloatExpansion() {
        }

        /**
         * Allows for casting floats to {@link FloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static FloatingLong asFloatingLong(float _this) {
            return CrTFloatingLong.create(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion("double")
    public static class DoubleExpansion {

        private DoubleExpansion() {
        }

        /**
         * Allows for casting doubles to {@link FloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static FloatingLong asFloatingLong(double _this) {
            return CrTFloatingLong.create(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion("string")
    public static class StringExpansion {

        private StringExpansion() {
        }

        /**
         * Allows for casting strings to {@link FloatingLong} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static FloatingLong asFloatingLong(String _this) {
            return CrTFloatingLong.create(_this);
        }
    }
}