package mekanism.common.capabilities.manager;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CapabilityHandlerManager<HOLDER extends IHolder, CONTAINER, HANDLER, SIDED_HANDLER extends HANDLER> {

    private final ProxyCreator<HANDLER, SIDED_HANDLER> proxyCreator;
    private final BiFunction<HOLDER, Direction, List<CONTAINER>> containerGetter;
    private final Map<Direction, HANDLER> handlers;
    private final SIDED_HANDLER baseHandler;
    private final boolean canHandle;
    @Nullable
    private HANDLER readOnlyHandler;
    @Nullable
    protected final HOLDER holder;

    protected CapabilityHandlerManager(@Nullable HOLDER holder, SIDED_HANDLER baseHandler, ProxyCreator<HANDLER, SIDED_HANDLER> proxyCreator,
          BiFunction<HOLDER, Direction, List<CONTAINER>> containerGetter) {
        this(holder, true, baseHandler, proxyCreator, containerGetter);
    }

    protected CapabilityHandlerManager(@Nullable HOLDER holder, boolean canHandle, SIDED_HANDLER baseHandler, ProxyCreator<HANDLER, SIDED_HANDLER> proxyCreator,
          BiFunction<HOLDER, Direction, List<CONTAINER>> containerGetter) {
        this.holder = holder;
        this.canHandle = canHandle && this.holder != null;
        this.baseHandler = baseHandler;
        this.proxyCreator = proxyCreator;
        this.containerGetter = containerGetter;
        if (this.canHandle) {
            handlers = new EnumMap<>(Direction.class);
        } else {
            handlers = Collections.emptyMap();
        }
    }

    public boolean canHandle() {
        return canHandle;
    }

    public List<CONTAINER> getContainers(@Nullable Direction side) {
        return canHandle() ? containerGetter.apply(holder, side) : Collections.emptyList();
    }

    public LazyOptional<HANDLER> getCapability(@Nullable Direction side) {
        if (!canHandle() || getContainers(side).isEmpty()) {
            //If we can't handle this type or there are no containers accessible from that side, don't return a handler
            return LazyOptional.empty();
        }
        //TODO: Cache the LazyOptional where possible as recommended in ICapabilityProvider
        return LazyOptional.of(() -> getHandler(side));
    }

    /**
     * Lazily get and cache a handler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
     *
     * @apiNote Assumes that {@link #canHandle} has been called before this and that it was {@code true}.
     */
    protected HANDLER getHandler(@Nullable Direction side) {
        if (side == null) {
            if (readOnlyHandler == null) {
                readOnlyHandler = proxyCreator.create(baseHandler, null, holder);
            }
            return readOnlyHandler;
        }
        HANDLER handler = handlers.get(side);
        if (handler == null) {
            handlers.put(side, handler = proxyCreator.create(baseHandler, side, holder));
        }
        return handler;
    }

    @FunctionalInterface
    public interface ProxyCreator<HANDLER, SIDED_HANDLER extends HANDLER> {

        HANDLER create(SIDED_HANDLER handler, @Nullable Direction side, @Nullable IHolder holder);
    }
}