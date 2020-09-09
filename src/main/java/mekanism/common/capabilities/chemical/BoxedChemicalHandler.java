package mekanism.common.capabilities.chemical;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.common.util.CapabilityUtils;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

@ParametersAreNonnullByDefault
public class BoxedChemicalHandler {

    private final Map<ChemicalType, LazyOptional<? extends IChemicalHandler<?, ?>>> handlers = new EnumMap<>(ChemicalType.class);

    @Nullable
    public <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> IChemicalHandler<CHEMICAL, STACK> getHandlerFor(ChemicalType chemicalType) {
        if (handlers.containsKey(chemicalType)) {
            Optional<? extends IChemicalHandler<?, ?>> handler = handlers.get(chemicalType).resolve();
            if (handler.isPresent()) {
                return (IChemicalHandler<CHEMICAL, STACK>) handler.get();
            }
        }
        return null;
    }

    public void addGasHandler(LazyOptional<IGasHandler> lazyHandler) {
        handlers.put(ChemicalType.GAS, lazyHandler);
    }

    public void addInfusionHandler(LazyOptional<IInfusionHandler> lazyHandler) {
        handlers.put(ChemicalType.INFUSION, lazyHandler);
    }

    public void addPigmentHandler(LazyOptional<IPigmentHandler> lazyHandler) {
        handlers.put(ChemicalType.PIGMENT, lazyHandler);
    }

    public void addSlurryHandler(LazyOptional<ISlurryHandler> lazyHandler) {
        handlers.put(ChemicalType.SLURRY, lazyHandler);
    }

    public boolean sameHandlers(BoxedChemicalHandler other) {
        return this == other || handlers.size() == other.handlers.size() && handlers.entrySet().stream().noneMatch(entry -> entry.getValue() != other.handlers.get(entry.getKey()));
    }

    public void addRefreshListeners(NonNullConsumer<LazyOptional<BoxedChemicalHandler>> refreshListener) {
        //TODO - V11: Make the listener only have to invalidate specific sub pieces
        for (LazyOptional<? extends IChemicalHandler<?, ?>> sourceAcceptor : handlers.values()) {
            //Use unchecked generics to add the listener to the source acceptor
            CapabilityUtils.addListener(sourceAcceptor, refreshListener);
        }
    }
}