package mekanism.common.capabilities.resolver;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicSidedCapabilityResolver<HANDLER> implements ICapabilityResolver<@Nullable Direction> {

    private final ProxyCreator<HANDLER> proxyCreator;
    private final Map<Direction, HANDLER> handlers;
    private final List<BlockCapability<?, @Nullable Direction>> supportedCapability;
    @Nullable
    private HANDLER readOnlyHandler;

    public BasicSidedCapabilityResolver(BlockCapability<HANDLER, @Nullable Direction> supportedCapability, BasicProxyCreator<HANDLER> proxyCreator) {
        this(supportedCapability, true, proxyCreator);
    }

    protected BasicSidedCapabilityResolver(BlockCapability<HANDLER, @Nullable Direction> supportedCapability, boolean canHandle, ProxyCreator<HANDLER> proxyCreator) {
        this.supportedCapability = Collections.singletonList(supportedCapability);
        this.proxyCreator = proxyCreator;
        if (canHandle) {
            handlers = new EnumMap<>(Direction.class);
        } else {
            handlers = Collections.emptyMap();
        }
    }

    @Override
    public List<BlockCapability<?, @Nullable Direction>> getSupportedCapabilities() {
        return supportedCapability;
    }

    @Nullable
    protected IHolder getHolder() {
        return null;
    }

    /**
     * Lazily get and cache a handler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
     */
    @Nullable
    @Override
    public <T> T resolve(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
        if (side == null) {
            if (readOnlyHandler == null) {
                readOnlyHandler = proxyCreator.create(null, getHolder());
            }
            return (T) readOnlyHandler;
        }
        HANDLER handler = handlers.get(side);
        if (handler == null) {
            handler = proxyCreator.create(side, getHolder());
            handlers.put(side, handler);
        }
        return (T) handler;
    }

    @Override
    public void invalidate(BlockCapability<?, @Nullable Direction> capability, @Nullable Direction side) {
        if (side == null) {
            readOnlyHandler = null;
        } else {
            handlers.remove(side);
        }
    }

    @Override
    public void invalidateAll() {
        readOnlyHandler = null;
        handlers.clear();
    }

    @FunctionalInterface
    public interface ProxyCreator<HANDLER> {

        HANDLER create(@Nullable Direction side, @Nullable IHolder holder);
    }

    @FunctionalInterface
    public interface BasicProxyCreator<HANDLER> extends ProxyCreator<HANDLER> {

        HANDLER create(@Nullable Direction side);

        @Override
        default HANDLER create(@Nullable Direction side, @Nullable IHolder holder) {
            return create(side);
        }
    }
}