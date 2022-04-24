package mekanism.common.capabilities.resolver;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicSidedCapabilityResolver<HANDLER, SIDED_HANDLER extends HANDLER> implements ICapabilityResolver {

    private final ProxyCreator<HANDLER, SIDED_HANDLER> proxyCreator;
    private final Map<Direction, LazyOptional<HANDLER>> handlers;
    private final List<Capability<?>> supportedCapability;
    private final SIDED_HANDLER baseHandler;
    @Nullable
    private LazyOptional<HANDLER> readOnlyHandler;

    public BasicSidedCapabilityResolver(SIDED_HANDLER baseHandler, Capability<HANDLER> supportedCapability, BasicProxyCreator<HANDLER, SIDED_HANDLER> proxyCreator) {
        this(baseHandler, supportedCapability, proxyCreator, true);
    }

    protected BasicSidedCapabilityResolver(SIDED_HANDLER baseHandler, Capability<HANDLER> supportedCapability, ProxyCreator<HANDLER, SIDED_HANDLER> proxyCreator,
          boolean canHandle) {
        this.supportedCapability = Collections.singletonList(supportedCapability);
        this.baseHandler = baseHandler;
        this.proxyCreator = proxyCreator;
        if (canHandle) {
            handlers = new EnumMap<>(Direction.class);
        } else {
            handlers = Collections.emptyMap();
        }
    }

    public SIDED_HANDLER getInternal() {
        return baseHandler;
    }

    @Override
    public List<Capability<?>> getSupportedCapabilities() {
        return supportedCapability;
    }

    @Nullable
    protected IHolder getHolder() {
        return null;
    }

    /**
     * Lazily get and cache a handler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
     */
    @Override
    public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
        if (side == null) {
            if (readOnlyHandler == null || !readOnlyHandler.isPresent()) {
                readOnlyHandler = LazyOptional.of(() -> proxyCreator.create(baseHandler, null, getHolder()));
            }
            return readOnlyHandler.cast();
        }
        LazyOptional<HANDLER> cachedCapability = handlers.get(side);
        if (cachedCapability == null || !cachedCapability.isPresent()) {
            handlers.put(side, cachedCapability = LazyOptional.of(() -> proxyCreator.create(baseHandler, side, getHolder())));
        }
        return cachedCapability.cast();
    }

    @Override
    public void invalidate(Capability<?> capability, @Nullable Direction side) {
        if (side == null) {
            invalidateReadOnly();
        } else {
            invalidate(handlers.get(side));
        }
    }

    @Override
    public void invalidateAll() {
        invalidateReadOnly();
        handlers.values().forEach(this::invalidate);
    }

    private void invalidateReadOnly() {
        if (readOnlyHandler != null && readOnlyHandler.isPresent()) {
            readOnlyHandler.invalidate();
            readOnlyHandler = null;
        }
    }

    protected void invalidate(@Nullable LazyOptional<?> cachedCapability) {
        if (cachedCapability != null && cachedCapability.isPresent()) {
            cachedCapability.invalidate();
        }
    }

    @FunctionalInterface
    public interface ProxyCreator<HANDLER, SIDED_HANDLER extends HANDLER> {

        HANDLER create(SIDED_HANDLER handler, @Nullable Direction side, @Nullable IHolder holder);
    }

    @FunctionalInterface
    public interface BasicProxyCreator<HANDLER, SIDED_HANDLER extends HANDLER> extends ProxyCreator<HANDLER, SIDED_HANDLER> {

        HANDLER create(SIDED_HANDLER handler, @Nullable Direction side);

        @Override
        default HANDLER create(SIDED_HANDLER handler, @Nullable Direction side, @Nullable IHolder holder) {
            return create(handler, side);
        }
    }
}