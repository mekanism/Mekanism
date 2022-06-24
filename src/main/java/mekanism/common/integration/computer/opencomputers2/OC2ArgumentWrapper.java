package mekanism.common.integration.computer.opencomputers2;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import li.cil.oc2.api.bus.device.rpc.RPCInvocation;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.ComputerArgumentHandler;
import mekanism.common.integration.computer.ComputerException;
import org.jetbrains.annotations.Nullable;

public class OC2ArgumentWrapper extends ComputerArgumentHandler<ComputerException, Object> {

    private final RPCInvocation invocation;

    OC2ArgumentWrapper(RPCInvocation invocation) {
        this.invocation = invocation;
    }

    @Override
    public int getCount() {
        return invocation.getParameters().size();
    }

    @Override
    public ComputerException error(String messageFormat, Object... args) {
        return new ComputerException(formatError(messageFormat, args));
    }

    @Nullable
    @Override
    public JsonElement getArgument(int index) {
        return invocation.getParameters().get(index);
    }

    @Override
    public Object sanitizeArgument(Class<?> expectedType, Class<?> argumentType, Object argument) {
        //I believe this is always a json element but check it anyway
        if (argument instanceof JsonElement element) {
            if (expectedType == FloatingLong.class && element.isJsonPrimitive()) {
                JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
                if (jsonPrimitive.isNumber()) {
                    Number number = jsonPrimitive.getAsNumber();
                    if (number instanceof FloatingLong fl) {
                        return fl;
                    } else if (number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long) {
                        long value = number.longValue();
                        if (value < 0) {
                            //Clamp negative values at zero, and don't allow unsigned longs via OC2
                            return FloatingLong.ZERO;
                        }
                        //If it isn't a floating point number use the more accurate way to create it with a long
                        return FloatingLong.createConst(value);
                    }
                    //Note: If value is negative this is clamped at zero
                    return FloatingLong.createConst(number.doubleValue());
                } else if (jsonPrimitive.isString()) {
                    try {
                        return FloatingLong.parseFloatingLong(jsonPrimitive.getAsString());
                    } catch (NumberFormatException e) {
                        return super.sanitizeArgument(expectedType, argumentType, argument);
                    }
                }
            }
            //TODO: If people report any other types that aren't handled quite well (as we have only done limited testing),
            // handle them either via OC2's system or via our system here
            try {
                return invocation.getGson().fromJson(element, expectedType);
            } catch (Throwable ignored) {
            }
        }
        return super.sanitizeArgument(expectedType, argumentType, argument);
    }

    @Override
    public Object noResult() {
        return null;
    }

    @Override
    public Object wrapResult(Object result) {
        if (result instanceof FloatingLong fl) {
            return fl.doubleValue();
        }
        return result;
    }

    static Class<?> wrapType(Class<?> clazz) {
        if (clazz == FloatingLong.class) {
            return Double.TYPE;
        }
        return clazz;
    }
}