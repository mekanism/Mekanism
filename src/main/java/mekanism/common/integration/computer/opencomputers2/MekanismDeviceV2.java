package mekanism.common.integration.computer.opencomputers2;

import li.cil.oc2.api.bus.device.rpc.*;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.integration.computer.*;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class MekanismDeviceV2<TILE extends BlockEntity & IComputerTile> extends BoundMethodHolder implements RPCDevice {
    public static <TILE extends BlockEntity & IComputerTile> MekanismDeviceV2<TILE> create(TILE tile) {
        MekanismDeviceV2<TILE> device = new MekanismDeviceV2<>(tile);
        //add helper apis first
        FactoryRegistry.bindTo(device, null, ComputerEnergyHelper.class);
        //bind the tile's methods
        tile.getComputerMethodsV2(device);
        return device;
    }

    //frozen on first retrieval
    private final Lazy<List<RPCMethodGroup>> methodGroups = Lazy.of(this::buildMethodGroups);
    private final List<String> name;
    private final WeakReference<TILE> attachedTile;

    public MekanismDeviceV2(TILE tile) {
        this.name = Collections.singletonList(tile.getComputerName());
        this.attachedTile = new WeakReference<>(tile);
    }

    @Override
    public List<String> getTypeNames() {
        return name;
    }

    @Override
    public List<RPCMethodGroup> getMethodGroups() {
        return methodGroups.get();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MekanismDeviceV2<?> other && this.attachedTile.get() == other.attachedTile.get();
    }

    private List<RPCMethodGroup> buildMethodGroups() {
        return this.methods.keySet().stream().map(key->{
            List<MethodData> overloads = this.methods.get(key);
            if (overloads.size() == 1) {
                return new Method(key, overloads.get(0));
            } else {
                Set<RPCMethod> set = new HashSet<>();
                for (MethodData md : overloads) {
                    set.add(new Method(key, md));
                }
                return new MethodGroup(key, set);
            }
        }).toList();
    }

    private static class MethodGroup implements RPCMethodGroup {
        private final String name;
        private final Set<RPCMethod> children;

        private MethodGroup(String name, Set<RPCMethod> children) {
            this.name = name;
            this.children = children;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Set<RPCMethod> getOverloads() {
            return children;
        }

        @Override
        public Optional<RPCMethod> findOverload(RPCInvocation invocation) {
            return this.children.stream().filter(m->m.getParameters().length == invocation.getParameters().size()).findFirst();
        }
    }

    private static class Method implements RPCMethod {
        private final String name;
        private final MethodData methodData;
        private final Class<?> returnType;
        private final Lazy<RPCParameter[]> params = Lazy.of(this::buildOCParams);

        private Method(String name, MethodData methodData) {
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
            return methodData.handler().apply(methodData.subject(), new OC2ComputerHelper(invocation));
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
