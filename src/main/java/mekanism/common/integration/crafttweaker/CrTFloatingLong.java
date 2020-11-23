package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.math.FloatingLong;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_FLOATING_LONG)
public class CrTFloatingLong {

    @ZenCodeType.Method
    public static CrTFloatingLong create(long value) {
        //TODO - 10.1: Decide if we want this to be create(@ZenCodeType.Unsigned long value)
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

    public FloatingLong getInternalAsConst() {
        //Ensure that the floating long we put into the recipe is a constant, as it may not be if operators were used on it
        return internal.copyAsConst();
    }

    @ZenCodeType.Caster(implicit = true)
    public String asString() {
        return internal.toString();
    }

    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.ADD)
    public CrTFloatingLong add(CrTFloatingLong toAdd) {
        return new CrTFloatingLong(internal.add(toAdd.internal));
    }

    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.SUB)
    public CrTFloatingLong subtract(CrTFloatingLong toSubtract) {
        return new CrTFloatingLong(internal.add(toSubtract.internal));
    }

    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    public CrTFloatingLong multiply(CrTFloatingLong toMultiply) {
        return new CrTFloatingLong(internal.multiply(toMultiply.internal));
    }

    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.DIV)
    public CrTFloatingLong divide(CrTFloatingLong toDivide) {
        return new CrTFloatingLong(internal.divide(toDivide.internal));
    }

    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.EQUALS)
    public boolean isEqual(CrTFloatingLong other) {
        return internal.equals(other.internal);
    }

    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.COMPARE)
    public int compareTo(CrTFloatingLong other) {
        return internal.compareTo(other.internal);
    }
}