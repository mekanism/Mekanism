package mekanism.common.integration.computer.opencomputers2;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import li.cil.oc2.api.bus.device.rpc.RPCInvocation;
import li.cil.oc2.api.bus.device.rpc.RPCMethod;
import li.cil.oc2.api.bus.device.rpc.RPCMethodGroup;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.integration.computer.BoundComputerMethod;
import mekanism.common.integration.computer.BoundComputerMethod.SelectedMethodInfo;
import mekanism.common.integration.computer.BoundComputerMethod.ThreadAwareMethodHandle;
import mekanism.common.integration.computer.ComputerException;

@NothingNullByDefault
public class MekanismRPCMethodGroup implements RPCMethodGroup {

    private final Map<ThreadAwareMethodHandle, MekanismRPCMethod> mappedMethods;
    private final BoundComputerMethod method;
    private final Set<RPCMethod> overloads;
    private final String name;

    MekanismRPCMethodGroup(String name, BoundComputerMethod method) {
        this.name = name;
        this.method = method;
        List<ThreadAwareMethodHandle> implementations = method.getImplementations();
        this.mappedMethods = new HashMap<>(implementations.size());
        for (ThreadAwareMethodHandle methodHandle : implementations) {
            mappedMethods.put(methodHandle, new MekanismRPCMethod(name, method, methodHandle));
        }
        overloads = ImmutableSet.copyOf(mappedMethods.values());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<RPCMethod> findOverload(RPCInvocation invocation) {
        OC2ArgumentWrapper argumentWrapper = new OC2ArgumentWrapper(invocation);
        try {
            SelectedMethodInfo selected = method.findMatchingImplementation(argumentWrapper);
            MekanismRPCMethod impl = mappedMethods.get(selected.getMethod());
            if (impl != null) {
                return Optional.of(new SelectedRPCMethod(impl, selected, invocation));
            }
        } catch (ComputerException ignored) {
            //Ignore exceptions as maybe the overload is provided by another device
        }
        return Optional.empty();
    }

    @Override
    public Set<RPCMethod> getOverloads() {
        return overloads;
    }
}