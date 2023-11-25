package mekanism.common.capabilities.chemical;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.lib.transmitter.acceptor.AbstractAcceptorCache;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class BoxedChemicalHandler {

    private final Map<ChemicalType, BlockCapabilityCache<? extends IChemicalHandler<?, ?>, @Nullable Direction>> handlers = new EnumMap<>(ChemicalType.class);

    //TODO: Re-evaluate this
    public BoxedChemicalHandler(ServerLevel level, BlockPos pos, Direction side, AbstractAcceptorCache.RefreshListener refreshListener) {
        for (ChemicalType chemicalType : EnumUtils.CHEMICAL_TYPES) {
            handlers.put(chemicalType, ChemicalUtil.getCapabilityForChemical(chemicalType).createCache(level, pos, side, refreshListener, refreshListener));
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> IChemicalHandler<CHEMICAL, STACK> getHandlerFor(ChemicalType chemicalType) {
        BlockCapabilityCache<? extends IChemicalHandler<?, ?>, @Nullable Direction> cache = handlers.get(chemicalType);
        if (cache != null) {
            return (IChemicalHandler<CHEMICAL, STACK>) cache.getCapability();
        }
        return null;
    }
}