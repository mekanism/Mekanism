package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.math.FloatingLong;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_FLOATING_LONG)
public class CrTFloatingLong {

    @ZenCodeType.Method
    public static CrTFloatingLong create(long value) {
        //TODO: Decide if we want this to be create(@ZenCodeType.Unsigned long value)
        // so that it is then an unsigned long given that is what floating longs can handle
        if (value < 0) {
            throw new IllegalArgumentException("Floating Longs do not support negative numbers.");
        }
        return new CrTFloatingLong(FloatingLong.createConst(value));
    }

    @ZenCodeType.Method
    public static CrTFloatingLong create(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Floating Longs do not support negative numbers.");
        }
        return new CrTFloatingLong(FloatingLong.createConst(value));
    }

    @ZenCodeType.Method
    public static CrTFloatingLong create(String value) {
        return new CrTFloatingLong(FloatingLong.parseFloatingLong(value, true));
    }

    private final FloatingLong internal;

    private CrTFloatingLong(FloatingLong internal) {
        this.internal = internal;
    }

    public FloatingLong getInternal() {
        return internal;
    }
}