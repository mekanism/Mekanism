package mekanism.common.capabilities.item;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class TransporterCapabilityResolver implements ICapabilityResolver<@Nullable Direction> {

        private static final List<BlockCapability<?, @Nullable Direction>> SUPPORTED_CAPABILITY = Collections.singletonList(Capabilities.ITEM.block());

        private final Map<Direction, TransporterItemHandler> transporterHandlers = new EnumMap<>(Direction.class);
        private final Map<Direction, ResourceHandler<ItemResource>> handlers = new EnumMap<>(Direction.class);
        private final LogisticalTransporterBase transporter;

        public TransporterCapabilityResolver(LogisticalTransporterBase transporter) {
            this.transporter = transporter;
        }

        @Override
        public List<BlockCapability<?, @Nullable Direction>> getSupportedCapabilities() {
            return SUPPORTED_CAPABILITY;
        }

        /**
         * Lazily get and cache a handler instance for the given side, and make it be read only if something else is trying to interact with us using the null side
         */
        @Nullable
        @Override
        public <T> T resolve(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
            if (side == null) {
                //We provide no readonly item handler view
                return null;
            }
            ResourceHandler<ItemResource> cachedCapability = handlers.get(side);
            if (cachedCapability == null) {
                //Note: We check here whether it exposes the cap rather than in the cap itself as we invalidate the cached cap whenever this changes
                if (transporter.exposesInsertCap(side)) {
                    TransporterItemHandler cached = transporterHandlers.get(side);
                    if (cached == null) {
                        cached = new TransporterItemHandler(transporter, WorldUtils.relativePos(transporter.getWorldPositionLong(), side));
                        transporterHandlers.put(side, cached);
                    }
                    handlers.put(side, cached);
                    return (T) cached;
                }
            }
            return (T) cachedCapability;
        }

        @Override
        public void invalidate(BlockCapability<?, @Nullable Direction> capability, @Nullable Direction side) {
            if (side != null) {
                handlers.remove(side);
            }
        }

        @Override
        public void invalidateAll() {
            handlers.clear();
        }
    }