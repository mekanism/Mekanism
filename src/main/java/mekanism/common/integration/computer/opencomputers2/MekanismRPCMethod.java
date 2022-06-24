package mekanism.common.integration.computer.opencomputers2;

import java.util.List;
import li.cil.oc2.api.bus.device.rpc.RPCInvocation;
import li.cil.oc2.api.bus.device.rpc.RPCMethod;
import li.cil.oc2.api.bus.device.rpc.RPCParameter;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.integration.computer.BoundComputerMethod;
import mekanism.common.integration.computer.BoundComputerMethod.SelectedMethodInfo;
import mekanism.common.integration.computer.BoundComputerMethod.ThreadAwareMethodHandle;
import mekanism.common.integration.computer.ComputerException;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class MekanismRPCMethod implements RPCMethod {

    private final ThreadAwareMethodHandle methodHandle;
    private final RPCParameter[] parameters;
    private final Class<?> returnType;
    final BoundComputerMethod method;
    private final String name;

    MekanismRPCMethod(String name, BoundComputerMethod method, ThreadAwareMethodHandle methodHandle) {
        this.method = method;
        this.methodHandle = methodHandle;
        this.name = name;
        this.returnType = OC2ArgumentWrapper.wrapType(methodHandle.returnType());
        List<Class<?>> parameterTypes = methodHandle.parameterTypes();
        List<String> parameterNames = methodHandle.paramNames();
        int parameterNameCount = parameterNames.size();
        int params = parameterTypes.size();
        RPCParameter[] parameters = new RPCParameter[params];
        for (int i = 0; i < params; i++) {
            Class<?> parameterType = OC2ArgumentWrapper.wrapType(parameterTypes.get(i));
            parameters[i] = new MekanismRPCParameter(parameterType, i < parameterNameCount ? parameterNames.get(i) : null);
        }
        this.parameters = parameters;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isSynchronized() {
        return !methodHandle.threadSafe();
    }

    @Override
    public Class<?> getReturnType() {
        return returnType;
    }

    @Override
    public RPCParameter[] getParameters() {
        return parameters;
    }

    @Nullable
    @Override
    public Object invoke(RPCInvocation invocation) throws ComputerException {
        //If someone ends up calling invoke on the specific method from the list of overloads directly,
        // try looking up if there is a match for this overload
        return invoke(new OC2ArgumentWrapper(invocation));
    }

    @Nullable
    Object invoke(OC2ArgumentWrapper argumentWrapper) throws ComputerException {
        SelectedMethodInfo selected = method.findMatchingImplementation(argumentWrapper, methodHandle);
        if (selected == null) {
            throw argumentWrapper.error("Parameters do not match signature of %s.", getName());
        }
        return method.run(argumentWrapper, selected);
    }
}