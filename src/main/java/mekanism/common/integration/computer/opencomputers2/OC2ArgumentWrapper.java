package mekanism.common.integration.computer.opencomputers2;

import com.google.gson.JsonElement;
import javax.annotation.Nullable;
import li.cil.oc2.api.bus.device.rpc.RPCInvocation;
import mekanism.common.integration.computer.ComputerArgumentHandler;
import mekanism.common.integration.computer.ComputerException;

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
            //TODO - 1.18: How well does this handle it, do we have any other cases we need to handle more manually
            try {
                return invocation.getGson().fromJson(element, expectedType);
            } catch (Throwable ignored) {
            }
        }
        return argument;
    }

    @Override
    public Object noResult() {
        return null;
    }

    @Override
    public Object wrapResult(Object result) {
        //TODO - 1.18: Check if we need to wrap any of these cases
        return result;
    }
}