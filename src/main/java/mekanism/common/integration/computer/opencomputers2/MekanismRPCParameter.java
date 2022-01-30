package mekanism.common.integration.computer.opencomputers2;

import java.util.Optional;
import javax.annotation.Nonnull;
import li.cil.oc2.api.bus.device.rpc.RPCParameter;

public class MekanismRPCParameter implements RPCParameter {

    private final Class<?> type;

    MekanismRPCParameter(Class<?> type) {
        this.type = type;
    }

    @Nonnull
    @Override
    public Class<?> getType() {
        return type;
    }

    @Nonnull
    @Override
    public Optional<String> getName() {
        //TODO - 1.18: Evaluate either using an annotation processor like we do with CrT to collect param names,
        // or just adding that data to the annotations, and then track it via ComputerMethodMapper to allow the
        // parameters to have "clean" names instead of names like arg1
        return Optional.empty();
    }
}