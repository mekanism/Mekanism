package mekanism.common.integration.computer.opencomputers2;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import li.cil.oc2.api.bus.device.rpc.RPCParameter;

public class MekanismRPCParameter implements RPCParameter {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<String> name;
    private final Class<?> type;

    MekanismRPCParameter(Class<?> type, @Nullable String name) {
        this.type = type;
        this.name = Optional.ofNullable(name);
    }

    @Nonnull
    @Override
    public Class<?> getType() {
        return type;
    }

    @Nonnull
    @Override
    public Optional<String> getName() {
        return name;
    }
}