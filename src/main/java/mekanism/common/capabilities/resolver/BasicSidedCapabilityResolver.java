package mekanism.common.capabilities.resolver;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jspecify.annotations.Nullable;

public class BasicSidedCapabilityResolver<HOLDER extends IHolder, HANDLER> implements ICapabilityResolver<@Nullable Direction> {

    private final ProxyCreator<HOLDER, HANDLER> proxyCreator;
    private final Map<Direction, HANDLER> handlers = new EnumMap<>(Direction.class);
    private final List<BlockCapability<?, @Nullable Direction>> supportedCapability;
    private final HOLDER holder;
    @Nullable
    private HANDLER readOnlyHandler;

    public BasicSidedCapabilityResolver(HOLDER holder, BlockCapability<HANDLER, @Nullable Direction> supportedCapability, ProxyCreator<HOLDER, HANDLER> proxyCreator) {
        this.supportedCapability = Collections.singletonList(supportedCapability);
        this.proxyCreator = proxyCreator;
        this.holder = holder;
    }

    @Override
    public List<BlockCapability<?, @Nullable Direction>> getSupportedCapabilities() {
        return supportedCapability;
    }

    protected HOLDER getHolder() {
        return holder;
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
    public interface ProxyCreator<HOLDER extends IHolder, HANDLER> {

        HANDLER create(@Nullable Direction side, HOLDER holder);
    }
}