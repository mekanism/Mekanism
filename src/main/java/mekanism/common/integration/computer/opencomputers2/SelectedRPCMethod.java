package mekanism.common.integration.computer.opencomputers2;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import li.cil.oc2.api.bus.device.rpc.RPCInvocation;
import li.cil.oc2.api.bus.device.rpc.RPCMethod;
import li.cil.oc2.api.bus.device.rpc.RPCParameter;
import mekanism.common.integration.computer.BoundComputerMethod.SelectedMethodInfo;
import mekanism.common.integration.computer.ComputerException;
import net.minecraft.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public class SelectedRPCMethod implements RPCMethod {

    private final RPCInvocation originalInvocation;
    private final SelectedMethodInfo selected;
    private final MekanismRPCMethod impl;

    SelectedRPCMethod(MekanismRPCMethod impl, SelectedMethodInfo selected, RPCInvocation originalInvocation) {
        this.impl = impl;
        this.selected = selected;
        this.originalInvocation = originalInvocation;
    }

    @Override
    public boolean isSynchronized() {
        return impl.isSynchronized();
    }

    @Override
    public Class<?> getReturnType() {
        return impl.getReturnType();
    }

    @Override
    public RPCParameter[] getParameters() {
        return impl.getParameters();
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull RPCInvocation invocation) throws ComputerException {
        OC2ArgumentWrapper argumentWrapper = new OC2ArgumentWrapper(invocation);
        if (originalInvocation.equals(invocation)) {
            //If the invocation matches (most likely this will be the case), just run our selected method
            // with the parameters it already wrapped as needed
            return impl.method.run(argumentWrapper, selected);
        }
        //Otherwise, fallback and try to match the parameters to the specific overload
        return impl.invoke(argumentWrapper);
    }

    @Override
    public Optional<String> getDescription() {
        return impl.getDescription();
    }

    @Override
    public Optional<String> getReturnValueDescription() {
        return impl.getReturnValueDescription();
    }

    @Override
    public String getName() {
        return impl.getName();
    }
}