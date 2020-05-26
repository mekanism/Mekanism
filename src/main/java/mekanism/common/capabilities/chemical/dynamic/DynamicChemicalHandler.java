package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.capabilities.resolver.manager.chemical.ChemicalHandlerManager;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.NonNullSupplier;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DynamicChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {

    protected final NonNullSupplier<ChemicalHandlerManager<CHEMICAL, STACK, TANK, ?, ?>> handlerManager;
    private final Runnable onContentsChanged;

    //TODO: Note that the supplier should basically just return a constant. Or do we want to just change it to NonNullLazy
    protected DynamicChemicalHandler(NonNullSupplier<ChemicalHandlerManager<CHEMICAL, STACK, TANK, ?, ?>> handlerManager, Runnable onContentsChanged) {
        this.handlerManager = handlerManager;
        this.onContentsChanged = onContentsChanged;
    }

    @Override
    public List<TANK> getChemicalTanks(@Nullable Direction side) {
        return handlerManager.get().getContainers(side);
    }

    @Override
    public void onContentsChanged() {
        onContentsChanged.run();
    }
}