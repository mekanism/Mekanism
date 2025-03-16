package mekanism.common.integration.computer.opencomputers2;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import li.cil.oc2.api.bus.device.rpc.RPCInvocation;
import li.cil.oc2.api.bus.device.rpc.RPCMethod;
import li.cil.oc2.api.bus.device.rpc.RPCMethodGroup;
import li.cil.oc2.api.bus.device.rpc.RPCParameter;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.BoundMethodHolder;
import mekanism.common.integration.computer.ComputerEnergyHelper;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.IComputerTile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault//TODO: reimplement RPCDevice if OC updates to NF
public class MekanismDevice<TILE extends BlockEntity & IComputerTile> extends BoundMethodHolder /*implements RPCDevice*/ {

    public static <TILE extends BlockEntity & IComputerTile> MekanismDevice<TILE> create(TILE tile) {
        MekanismDevice<TILE> device = new MekanismDevice<>(tile);
        //add helper apis first
        FactoryRegistry.bindTo(device, null, ComputerEnergyHelper.class);
        //bind the tile's methods
        tile.getComputerMethods(device);
        return device;
    }

    //frozen on first retrieval
    private final Lazy<List<RPCMethodGroup>> methodGroups = Lazy.of(this::buildMethodGroups);
    private final List<String> name;
    private final WeakReference<TILE> attachedTile;

    public MekanismDevice(TILE tile) {
        this.name = Collections.singletonList(tile.getComputerName());
        this.attachedTile = new WeakReference<>(tile);
    }

    //@Override
    public List<String> getTypeNames() {
        return name;
    }

    //@Override
    public List<RPCMethodGroup> getMethodGroups() {
        return methodGroups.get();
    }

    @Override
    public boolean equals(Object obj) {
        TILE attached = this.attachedTile.get();
        // only will really be creating a single instance of this, and other instances of the same tile may
        // be invalid if it is not persistent such as for multiblocks
        return obj == this || (obj instanceof MekanismDevice<?> other && attached != null && attached == other.attachedTile.get() && methods.equals(other.methods));
    }

    @Override
    public int hashCode() {
        int result = methodGroups.hashCode();
        result = 31 * result + name.hashCode();
        TILE tile = attachedTile.get();
        result = 31 * result + (tile == null ? 0 : tile.hashCode());
        result = 31 & result + methods.hashCode();
        return result;
    }

    private List<RPCMethodGroup> buildMethodGroups() {
        return this.methods.keySet().stream().map(key -> {
            List<BoundMethodData<?>> overloads = this.methods.get(key);
            if (overloads.size() == 1) {
                return new Method(key, overloads.getFirst());
            }
            Set<RPCMethod> set = new HashSet<>(overloads.size());
            for (BoundMethodData<?> md : overloads) {
                set.add(new Method(key, md));
            }
            return new MethodGroup(key, set);
        }).toList();
    }

    private record MethodGroup(String getName, Set<RPCMethod> getOverloads) implements RPCMethodGroup {

        @Override
        public Optional<RPCMethod> findOverload(RPCInvocation invocation) {
            Set<RPCMethod> children = getOverloads();
            if (!children.isEmpty()) {
                int parameters = invocation.getParameters().size();
                for (RPCMethod m : children) {
                    if (m.getParameters().length == parameters) {
                        return Optional.of(m);
                    }
                }
            }
            return Optional.empty();
        }
    }

    private static class Method implements RPCMethod {

        private final String name;
        private final BoundMethodData<?> methodData;
        private final Class<?> returnType;
        private final Lazy<RPCParameter[]> params = Lazy.of(this::buildOCParams);

        private Method(String name, BoundMethodData<?> methodData) {
            this.name = name;
            this.methodData = methodData;
            this.returnType = BaseComputerHelper.convertType(methodData.returnType());
        }

        @Override
        public boolean isSynchronized() {
            return !methodData.threadSafe();
        }

        @Override
        public Class<?> getReturnType() {
            return this.returnType;
        }

        @Override
        public RPCParameter[] getParameters() {
            return params.get();
        }

        @Nullable
        @Override
        public Object invoke(RPCInvocation invocation) throws Throwable {
            return methodData.call(new OC2ComputerHelper(invocation));
        }

        @Override
        public String getName() {
            return name;
        }

        private RPCParameter[] buildOCParams() {
            RPCParameter[] parameters = new RPCParameter[methodData.argumentNames().length];
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = new Param(methodData.argumentNames()[i], BaseComputerHelper.convertType(methodData.argClasses()[i]));
            }
            return parameters;
        }

        @Override
        public Optional<RPCMethod> findOverload(RPCInvocation invocation) {
            return Optional.ofNullable(invocation.getParameters().size() == methodData.argumentNames().length ? this : null);
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.ofNullable(methodData.method().methodDescription());
        }
    }

    private static class Param implements RPCParameter {

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final Optional<String> name;
        private final Class<?> returnType;

        private Param(String name, Class<?> returnType) {
            this.name = Optional.of(name);
            this.returnType = returnType;
        }

        @Override
        public Class<?> getType() {
            return returnType;
        }

        @Override
        public Optional<String> getName() {
            return name;
        }
    }
}
