package mekanism.common.capabilities.resolver.advanced;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedCapabilityResolver implements ICapabilityResolver {

    public static <T> AdvancedCapabilityResolver create(Capability<T> supportedCapability, NonNullSupplier<T> supplier, NonNullSupplier<T> readOnlySupplier) {
        return new AdvancedCapabilityResolver(supportedCapability, supplier, readOnlySupplier);
    }

    private final List<Capability<?>> supportedCapability;
    private final NonNullSupplier<?> supplier;
    private final NonNullSupplier<?> readOnlySupplier;

    protected <T> AdvancedCapabilityResolver(Capability<T> supportedCapability, NonNullSupplier<T> supplier, NonNullSupplier<T> readOnlySupplier) {
        this.supportedCapability = Collections.singletonList(supportedCapability);
        this.supplier = supplier;
        this.readOnlySupplier = readOnlySupplier;
    }

    @Override
    public List<Capability<?>> getSupportedCapabilities() {
        return supportedCapability;
    }

    @Override
    public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
        if (side == null) {
            return LazyOptional.of(readOnlySupplier).cast();
        }
        return LazyOptional.of(supplier).cast();
    }
}