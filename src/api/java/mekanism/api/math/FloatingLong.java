package mekanism.api.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    //TODO: Implement this class, and improve java docs. Organize and move all static methods either to the top or the bottom
    //TODO: Modify EnergyAPI to state what things should NOT be modified, given we stripped that out of the docs due to primitives not modifying the actual value
    private static final int DECIMAL_DIGITS = 4;//We only can support 4 digits in our decimal
    private static final short MAX_DECIMAL = 9_999;
    private static final short SINGLE_UNIT = MAX_DECIMAL + 1;
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
        short decimal = parseDecimal(Double.toString(value));
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
        short decimal = parseDecimal(Double.toString(value));
        return create(lValue, decimal);
    }

    public static FloatingLong createConst(long value) {
        return createConst(value, (short) 0);
    }

    public static FloatingLong createConst(long value, short decimal) {
        return new FloatingLong(value, decimal, true);
    }

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
            throw new IllegalStateException("Tried to modify a floating constant long");
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

    //TODO: Do we want to do the sub implementations as a copy and then the in place value
    // or is it slightly more optimized to do the calculation and then just make a new object with that value
    //TODO: We also may want to define a way of doing a set of operations all at once, and outputting a new value
    // given that way we can internally do all the calculations using primitives rather than spamming a lot of objects
    public void minusEqual(FloatingLong toSubtract) {
        checkCanModify();
        //TODO: Handle it going negative
        long newValue = value - toSubtract.value;
        short newDecimal = (short) (decimal - toSubtract.decimal);
        if (newDecimal < 0) {
            newDecimal += SINGLE_UNIT;
            newValue--;
        }
        setAndClampValues(newValue, newDecimal);
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
        //TODO: Fix potential overflow?
        long newValue = value + toAdd.value;
        short newDecimal = (short) (decimal + toAdd.decimal);
        if (newDecimal > MAX_DECIMAL) {
            newDecimal -= SINGLE_UNIT;
            newValue++;
        }
        setAndClampValues(newValue, newDecimal);
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
        //TODO: Make a more direct implementation that doesn't go through big decimal
        //TODO: Check how the multiplication works if we need to specify a scale
        BigDecimal multiplication = new BigDecimal(toString()).multiply(new BigDecimal(toMultiply.toString()));
        long value = multiplication.longValue();
        short decimal = parseDecimal(multiplication.toString());
        setAndClampValues(value, decimal);
    }

    public FloatingLong multiply(FloatingLong toMultiply) {
        FloatingLong toReturn = copy();
        toReturn.timesEqual(toMultiply);
        return toReturn;
    }

    //TODO: Evaluate this and what helpers are needed for interacting with primitives
    public FloatingLong multiply(long toMultiply) {
        //TODO: FIXME/IMPROVE
        return multiply(FloatingLong.create(toMultiply));
    }

    public FloatingLong multiply(double toMultiply) {
        //TODO: FIXME/IMPROVE
        return multiply(FloatingLong.createConst(toMultiply));
    }

    public void divideEquals(FloatingLong toDivide) {
        checkCanModify();
        //TODO: Validate toDivide is not zero
        //TODO: Make a more direct implementation that doesn't go through big decimal
        BigDecimal divide = new BigDecimal(toString()).divide(new BigDecimal(toDivide.toString()), DECIMAL_DIGITS, RoundingMode.HALF_EVEN);
        long value = divide.longValue();
        short decimal = parseDecimal(divide.toString());
        setAndClampValues(value, decimal);
    }

    public FloatingLong divide(FloatingLong toDivide) {
        FloatingLong toReturn = copy();
        toReturn.divideEquals(toDivide);
        return toReturn;
    }

    public FloatingLong divide(long toDivide) {
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

    //TODO: Note this doesn't create any new objects
    public FloatingLong max(FloatingLong other) {
        return smallerThan(other) ? other : this;
    }

    //TODO: Note this doesn't create any new objects
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
        return toString(DECIMAL_DIGITS);
    }

    public String toString(int decimalPlaces) {
        if (decimal == 0) {
            return Long.toString(value);
        }
        if (decimalPlaces > DECIMAL_DIGITS) {
            decimalPlaces = DECIMAL_DIGITS;
        }
        String valueAsString = value + ".";
        String decimalAsString = Short.toString(decimal);
        int numberDigits = decimalAsString.length();
        if (numberDigits > decimalPlaces) {
            //We need to trim it
            decimalAsString = decimalAsString.substring(0, decimalPlaces);
        } else if (numberDigits < decimalPlaces) {
            //We need to prepend some zeros
            decimalAsString = getZeros(decimalPlaces - numberDigits) + decimalAsString;
        }
        return valueAsString + decimalAsString;
    }

    //TODO: Copy and modify java docs from Long.valueOf
    public static FloatingLong parseFloatingLong(String string) {
        //TODO: IMPLEMENT AND FIX ME so that it can handle invalid strings
        long value;
        int index = string.indexOf(".");
        if (index == -1) {
            value = Long.parseLong(string);
        } else {
            value = Long.parseLong(string.substring(0, index));
        }
        short decimal = parseDecimal(string, index);
        return create(value, decimal);
    }

    private static short parseDecimal(String string) {
        return parseDecimal(string, string.indexOf("."));
    }

    private static short parseDecimal(String string, int index) {
        if (index == -1) {
            return 0;
        }
        String decimalAsString = string.substring(index + 1);
        int numberDigits = decimalAsString.length();
        if (numberDigits < DECIMAL_DIGITS) {
            //We need to pad it on the right with zeros
            decimalAsString += getZeros(DECIMAL_DIGITS - numberDigits);
        } else if (numberDigits > DECIMAL_DIGITS) {
            //We need to trim it to make sure it will be in range of a short
            decimalAsString = decimalAsString.substring(0, DECIMAL_DIGITS);
        }
        return Short.parseShort(decimalAsString);
    }

    private static String getZeros(int number) {
        StringBuilder zeros = new StringBuilder();
        for (int i = 0; i < number; i++) {
            zeros.append('0');
        }
        return zeros.toString();
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
        return MathUtils.clampToInt(value);
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return intValue() + decimal / (float) SINGLE_UNIT;
    }

    @Override
    public double doubleValue() {
        return longValue() + decimal / (double) SINGLE_UNIT;
    }
}