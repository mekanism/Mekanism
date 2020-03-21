package mekanism.api.math;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.NBTConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A class representing a value defined by a long, and a floating point number stored in a short
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FloatingLong extends Number implements Comparable<FloatingLong>, INBTSerializable<CompoundNBT> {

    //TODO: Implement this class, and improve java docs
    //TODO: Modify EnergyAPI to state what things should NOT be modified, given we stripped that out of the docs due to primitives not modifying the actual value
    private static final int DECIMAL_DIGITS = 4;//We only can support 4 digits in our decimal
    private static final short MAX_DECIMAL = 9_999;
    public static final FloatingLong ZERO = createConst(0);
    public static final FloatingLong ONE = createConst(1);
    //TODO: Util method so that it is easier to declare you want a short representing say 0.001
    public static final FloatingLong MAX_VALUE = createConst(Long.MAX_VALUE, MAX_DECIMAL);

    public static FloatingLong getNewZero() {
        return create(0);
    }

    public static FloatingLong create(double value) {
        //TODO: Try to optimize/improve this at the very least it rounds incorrectly
        long lValue = (long) value;
        String valueAsString = Double.toString(value);
        int index = valueAsString.indexOf(".");
        short decimal;
        if (index == -1) {
            decimal = 0;
        } else {
            decimal = Short.parseShort(valueAsString.substring(index, Math.min(index + DECIMAL_DIGITS, valueAsString.length())));
        }
        return create(lValue, decimal);
    }

    public static FloatingLong create(long value) {
        return create(value, (short) 0);
    }

    public static FloatingLong create(long value, short decimal) {
        return new FloatingLong(value, decimal, false);
    }

    public static FloatingLong createConst(double value) {
        //TODO: Try to optimize/improve this at the very least it rounds incorrectly
        long lValue = (long) value;
        String valueAsString = Double.toString(value);
        int index = valueAsString.indexOf(".");
        short decimal;
        if (index == -1) {
            decimal = 0;
        } else {
            decimal = Short.parseShort(valueAsString.substring(index + 1, Math.min(index + 1 + DECIMAL_DIGITS, valueAsString.length())));
        }
        return create(lValue, decimal);
    }

    public static FloatingLong createConst(long value) {
        return createConst(value, (short) 0);
    }

    public static FloatingLong createConst(long value, short decimal) {
        return new FloatingLong(value, decimal, true);
    }

    //TODO: Max short value: 32,767
    //TODO: max decimal we can easily represent: 0.9_999?
    private long value;
    private short decimal;
    private final boolean isConstant;

    private FloatingLong(long value, short decimal, boolean isConstant) {
        this.isConstant = isConstant;
        setAndClampValues(value, decimal);
    }

    public long getValue() {
        return value;
    }

    public short getDecimal() {
        return decimal;
    }

    private void checkCanModify() {
        if (isConstant) {
            //TODO: Throw an exception
        }
    }

    //DO NOT CALL THIS IF YOU ARE A CONSTANT
    private void setAndClampValues(long value, short decimal) {
        if (value < 0) {
            //TODO: Remove this clamp for value and allow it to be an unsigned long
            value = 0;
        }
        if (decimal < 0) {
            decimal = 0;
        } else if (decimal > MAX_DECIMAL) {
            decimal = MAX_DECIMAL;
        }
        this.value = value;
        this.decimal = decimal;
    }

    public boolean isEmpty() {
        return value <= 0 && decimal <= 0;
    }

    public FloatingLong copy() {
        return new FloatingLong(value, decimal, false);
    }

    public FloatingLong modulo(long mod) {
        return create(getValue() % mod);
    }

    //TODO: Do we want to do the sub implementations as a copy and then the in place value
    // or is it slightly more optimized to do the calculation and then just make a new object with that value
    //TODO: We also may want to define a way of doing a set of operations all at once, and outputting a new value
    // given that way we can internally do all the calculations using primitives rather than spamming a lot of objects
    public void minusEqual(FloatingLong toSubtract) {
        checkCanModify();
        //TODO: Take the decimal into account
        setAndClampValues(value - toSubtract.value, decimal);
    }

    //TODO: NOTE: We probably need to look through this to make sure we don't accidentally go negative??
    // Or was that just an edge case for how we did calculations for the induction matrix
    public FloatingLong subtract(FloatingLong toSubtract) {
        FloatingLong toReturn = copy();
        toReturn.minusEqual(toSubtract);
        return toReturn;
    }

    public void plusEqual(FloatingLong toAdd) {
        checkCanModify();
        //TODO: Take the decimal into account
        setAndClampValues(value + toAdd.value, decimal);
    }

    public FloatingLong add(FloatingLong toAdd) {
        FloatingLong toReturn = copy();
        toReturn.plusEqual(toAdd);
        return toReturn;
    }

    public void timesEqual(double toMultiply) {
        //TODO: FIXME/IMPROVE
        timesEqual(FloatingLong.createConst(toMultiply));
    }

    public void timesEqual(FloatingLong toMultiply) {
        checkCanModify();
        //TODO: Take the decimal into account
        setAndClampValues(value * toMultiply.value, decimal);
    }

    public FloatingLong multiply(FloatingLong toMultiply) {
        FloatingLong toReturn = copy();
        toReturn.timesEqual(toMultiply);
        return toReturn;
    }

    //TODO: Evaluate this and what helpers are needed for interacting with primitives
    public FloatingLong multiply(int toMultiply) {
        //TODO: FIXME/IMPROVE
        return multiply(FloatingLong.create(toMultiply));
    }

    public FloatingLong multiply(double toMultiply) {
        //TODO: FIXME/IMPROVE
        return multiply(FloatingLong.createConst(toMultiply));
    }

    public FloatingLong multiply(float toMultiply) {
        //TODO: FIXME/IMPROVE
        return multiply(FloatingLong.createConst(toMultiply));
    }

    public void divideEquals(FloatingLong toDivide) {
        checkCanModify();
        //TODO: Validate toDivide is not zero
        //TODO: Take the decimal into account
        setAndClampValues(value / toDivide.value, decimal);
    }

    public FloatingLong divide(FloatingLong toDivide) {
        FloatingLong toReturn = copy();
        toReturn.divideEquals(toDivide);
        return toReturn;
    }

    public FloatingLong divide(int toDivide) {
        //TODO: FIXME/IMPROVE
        return divide(FloatingLong.create(toDivide));
    }

    public FloatingLong divide(double toDivide) {
        //TODO: FIXME/IMPROVE
        return divide(FloatingLong.create(toDivide));
    }

    public double divideToLevel(FloatingLong toDivide) {
        //TODO: optimize out creating another object
        return toDivide.isEmpty() ? 1 : divide(toDivide).doubleValue();
    }

    //TODO: Note this doesn't create any new objects (or do we want it to)
    // We need to go through usages and see what would be best, we may just want to copy only if we would be returning this
    public FloatingLong max(FloatingLong other) {
        return smallerThan(other) ? other : this;
    }

    //TODO: Note this doesn't create any new objects (or do we want it to)
    // We need to go through usages and see what would be best, we may just want to copy only if we would be returning this
    public FloatingLong min(FloatingLong other) {
        return greaterThan(other) ? other : this;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putLong(NBTConstants.VALUE, value);
        nbt.putShort(NBTConstants.DECIMAL, decimal);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        checkCanModify();
        setAndClampValues(nbt.getLong(NBTConstants.VALUE), nbt.getShort(NBTConstants.DECIMAL));
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote zero if equal to toCompare
     * <br>
     * less than zero if smaller than toCompare
     * <br>
     * greater than zero if bigger than toCompare
     * @implNote {@code 2} or {@code -2} if the overall value is different
     * <br>
     * {@code 1} or {@code -1} if the value is the same but the decimal is different
     */
    @Override
    public int compareTo(FloatingLong toCompare) {
        //TODO: Can the values technically be negative and if so do we need to stop them from being so
        if (value < toCompare.value) {
            //If our primary value is smaller than toCompare's value we are always less than
            return -2;
        } else if (value > toCompare.value) {
            //If our primary value is bigger than toCompare's value we are always greater than
            return 2;
        }
        //Primary value is equal, check the decimal
        //TODO: Check if this is even the correct way to compare the decimal as I am not fully sure how we are going to encode it yet
        if (decimal < toCompare.decimal) {
            //If our primary value is equal, but our decimal smaller than toCompare's we are less than
            return -1;
        } else if (decimal > toCompare.decimal) {
            //If our primary value is equal, but our decimal bigger than toCompare's we are greater than
            return 1;
        }
        //Else we are equal
        return 0;
    }

    public boolean smallerThan(FloatingLong toCompare) {
        return compareTo(toCompare) < 0;
    }

    public boolean greaterThan(FloatingLong toCompare) {
        return compareTo(toCompare) > 0;
    }

    public boolean equals(FloatingLong other) {
        return value == other.value && decimal == other.decimal;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof FloatingLong && equals((FloatingLong) other);
    }

    @Override
    public int hashCode() {
        //TODO: Do we want to modify this in some way
        return Objects.hash(value, decimal);
    }

    @Override
    public String toString() {
        //TODO: Do the decimal more properly, as I somehow doubt it can just be directly appended
        return value + "." + decimal;
    }

    public String toString(int decimalPlaces) {
        //TODO: Do the decimal more properly, as I somehow doubt it can just be directly appended
        return value + "." + decimal;
    }

    //TODO: Copy and modify java docs from Long.valueOf
    public static FloatingLong parseFloatingLong(String value) {
        //TODO: IMPLEMENT AND FIX ME
        return FloatingLong.ZERO;
    }

    public static FloatingLong readFromNBT(@Nullable CompoundNBT nbtTags) {
        if (nbtTags == null || nbtTags.isEmpty()) {
            return ZERO;
        }
        return create(nbtTags.getLong(NBTConstants.VALUE), nbtTags.getShort(NBTConstants.DECIMAL));
    }

    public static FloatingLong fromBuffer(PacketBuffer buffer) {
        return new FloatingLong(buffer.readVarLong(), buffer.readShort(), false);
    }

    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeVarLong(value);
        buffer.writeShort(decimal);
    }

    @Override
    public int intValue() {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }

    @Override
    public long longValue() {
        return getValue();
    }

    @Override
    public float floatValue() {
        //TODO: Store the 10_000 constant somewhere?
        return intValue() + decimal / 10_000F;
    }

    @Override
    public double doubleValue() {
        return longValue() + decimal / 10_000D;
    }
}