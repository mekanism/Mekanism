package mekanism.common.capabilities.holder.chemical;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.util.Direction;

public class ChemicalTankHelper<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {

    private final IChemicalTankHolder<CHEMICAL, STACK, TANK> slotHolder;
    private boolean built;

    private ChemicalTankHelper(IChemicalTankHolder<CHEMICAL, STACK, TANK> slotHolder) {
        this.slotHolder = slotHolder;
    }

    public static ChemicalTankHelper<Gas, GasStack, IGasTank> forSideGas(Supplier<Direction> facingSupplier) {
        return new ChemicalTankHelper<Gas, GasStack, IGasTank>(new ChemicalTankHolder<>(facingSupplier));
    }

    public static ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> forSideInfusion(Supplier<Direction> facingSupplier) {
        return new ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank>(new ChemicalTankHolder<>(facingSupplier));
    }

    public static ChemicalTankHelper<Gas, GasStack, IGasTank> forSideGasWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        return new ChemicalTankHelper<>(new ConfigGasTankHolder(facingSupplier, configSupplier));
    }

    public void addTank(@Nonnull TANK tank) {
        if (built) {
            throw new RuntimeException("Builder has already built.");
        }
        if (slotHolder instanceof ChemicalTankHolder) {
            ((ChemicalTankHolder<CHEMICAL, STACK, TANK>) slotHolder).addTank(tank);
        } else if (slotHolder instanceof ConfigChemicalTankHolder) {
            ((ConfigChemicalTankHolder<CHEMICAL, STACK, TANK>) slotHolder).addTank(tank);
        }
        //TODO: Else warning?
    }

    public void addTank(@Nonnull TANK tank, RelativeSide... sides) {
        if (built) {
            throw new RuntimeException("Builder has already built.");
        }
        if (slotHolder instanceof ChemicalTankHolder) {
            ((ChemicalTankHolder<CHEMICAL, STACK, TANK>) slotHolder).addTank(tank, sides);
        }
        //TODO: Else warning?
    }

    public IChemicalTankHolder<CHEMICAL, STACK, TANK> build() {
        built = true;
        return slotHolder;
    }
}