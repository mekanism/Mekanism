package mekanism.common.integration.computer.computercraft;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.ComputerArgumentHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class CCArgumentWrapper extends ComputerArgumentHandler<LuaException, MethodResult> {

    private static final double MAX_FLOATING_LONG_AS_DOUBLE = Double.parseDouble(FloatingLong.MAX_VALUE.toString());
    private final IArguments arguments;

    public CCArgumentWrapper(IArguments arguments) {
        this.arguments = arguments;
    }

    @Override
    public int getCount() {
        return arguments.count();
    }

    @Override
    public LuaException error(String messageFormat, Object... args) {
        return new LuaException(formatError(messageFormat, args));
    }

    @Nullable
    @Override
    public Object getArgument(int index) {
        return arguments.get(index);
    }

    @Override
    public Object sanitizeArgument(Class<?> expectedType, Class<?> argumentType, Object argument) {
        //Types ComputerCraft is likely to send us that we want to do validation on:
        // - Boolean (no extra validation on our part is needed for this)
        // - Double (Allow casting into all sorts of numbers)
        // - String (Allow implicitly casting strings to char arrays and single characters)
        // - Map (Table in LUA, currently ignored)
        if (argumentType == Double.class) {
            //Note: We only check it against Double and not the primitive type as ComputerCraft only passes us a Double object
            double d = (double) argument;
            if (Double.isFinite(d)) {
                //Only allow finite doubles
                //Note: Our implementations of these clamp the values to within the range of the various types
                // if the value is out of range, then we just don't accept it as a value
                if (expectedType == Byte.TYPE || expectedType == Byte.class) {
                    if (d >= Byte.MIN_VALUE && d <= Byte.MAX_VALUE) {
                        return (byte) d;
                    }
                } else if (expectedType == Short.TYPE || expectedType == Short.class) {
                    if (d >= Short.MIN_VALUE && d <= Short.MAX_VALUE) {
                        return (short) d;
                    }
                } else if (expectedType == Integer.TYPE || expectedType == Integer.class) {
                    if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
                        return (int) d;
                    }
                } else if (expectedType == Long.TYPE || expectedType == Long.class) {
                    if (d >= Long.MIN_VALUE && d <= Long.MAX_VALUE) {
                        return (long) d;
                    }
                } else if (expectedType == Float.TYPE || expectedType == Float.class) {
                    if (d >= -Float.MAX_VALUE && d <= Float.MAX_VALUE) {
                        //Note: MIN_VALUE on float is smallest positive number
                        return (float) d;
                    }
                } else if (expectedType == FloatingLong.class) {
                    if (d >= 0 && d <= MAX_FLOATING_LONG_AS_DOUBLE) {
                        //Only allow positive numbers as floating longs and ones that will fit within the bounds
                        // even though basically all should fit within the bounds
                        return FloatingLong.createConst(d);
                    }
                }
            }
        } else if (argumentType == String.class) {
            if (expectedType == char[].class) {
                //Allow casting string to character arrays
                return ((String) argument).toCharArray();
            } else if (expectedType == Character.TYPE || expectedType == Character.class) {
                String string = (String) argument;
                if (string.length() == 1) {
                    //If our input string is of length one allow "casting" it to a char/Character
                    return string.charAt(0);
                }
            }
        } /*else if (Map.class.isAssignableFrom(argumentType)) {
            //Note: If we ever end up having any cases where we actually care about or want map based input arguments
            // and they need some form of wrapping then we should implement support for them here
        }*/
        return super.sanitizeArgument(expectedType, argumentType, argument);
    }

    @Override
    public MethodResult noResult() {
        return MethodResult.of();
    }

    @Override
    public MethodResult wrapResult(Object result) {
        //Note: If we ever end up having computer methods for returning arrays, maps, lists, or some arbitrary collections
        // we probably will want to properly convert the inner parts of said things here. That is also why wrapReturnType
        // is in its own method to allow for easier conversion
        return MethodResult.of(wrapReturnType(result));
    }

    private static Object wrapReturnType(Object result) {
        if (result instanceof ForgeRegistryEntry<?>) {
            return getName((ForgeRegistryEntry<?>) result);
        } else if (result instanceof ChemicalStack<?>) {
            ChemicalStack<?> stack = (ChemicalStack<?>) result;
            Map<String, Object> wrapped = new HashMap<>(2);
            wrapped.put("name", getName(stack.getType()));
            wrapped.put("amount", stack.getAmount());
            return wrapped;
        } else if (result instanceof FluidStack) {
            FluidStack stack = (FluidStack) result;
            Map<String, Object> wrapped = new HashMap<>(2);
            wrapped.put("name", getName(stack.getFluid()));
            wrapped.put("amount", stack.getAmount());
            return wrapped;
        } else if (result instanceof ItemStack) {
            ItemStack stack = (ItemStack) result;
            Map<String, Object> wrapped = new HashMap<>(2);
            wrapped.put("name", getName(stack.getItem()));
            wrapped.put("count", stack.getCount());
            return wrapped;
        } else if (result instanceof Direction) {
            return ((Direction) result).getName2();
        }
        //TODO - 10.1: Add any other wrappers we may end up wanting/needing. For example it may be of use to be able
        // to have filter objects automatically wrapped into a map
        return result;
    }

    private static String getName(ForgeRegistryEntry<?> entry) {
        ResourceLocation registryName = entry.getRegistryName();
        return registryName == null ? null : registryName.toString();
    }
}