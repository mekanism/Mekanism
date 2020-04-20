package mekanism.common.capabilities.resolver.basic;

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
public class BasicCapabilityResolver implements ICapabilityResolver {

    public static <T> BasicCapabilityResolver create(Capability<T> supportedCapability, NonNullSupplier<T> supplier) {
        return new BasicCapabilityResolver(supportedCapability, supplier);
    }

    private final List<Capability<?>> supportedCapability;
    private final NonNullSupplier<?> supplier;

    protected <T> BasicCapabilityResolver(Capability<T> supportedCapability, NonNullSupplier<T> supplier) {
        this.supportedCapability = Collections.singletonList(supportedCapability);
        this.supplier = supplier;
    }

    @Override
    public List<Capability<?>> getSupportedCapabilities() {
        return supportedCapability;
    }

    @Override
    public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
        return LazyOptional.of(supplier).cast();
    }
}