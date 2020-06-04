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
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.holder.chemical.ConfigChemicalTankHolder.ConfigGasTankHolder;
import mekanism.common.capabilities.holder.chemical.ConfigChemicalTankHolder.ConfigInfusionTankHolder;
import mekanism.common.capabilities.holder.chemical.ConfigChemicalTankHolder.ConfigPigmentTankHolder;
import mekanism.common.capabilities.holder.chemical.ConfigChemicalTankHolder.ConfigSlurryTankHolder;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.util.Direction;

public class ChemicalTankHelper<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> {

    private final IChemicalTankHolder<CHEMICAL, STACK, TANK> slotHolder;
    private boolean built;

    private ChemicalTankHelper(IChemicalTankHolder<CHEMICAL, STACK, TANK> slotHolder) {
        this.slotHolder = slotHolder;
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
    ChemicalTankHelper<CHEMICAL, STACK, TANK> forSide(Supplier<Direction> facingSupplier) {
        return new ChemicalTankHelper<CHEMICAL, STACK, TANK>(new ChemicalTankHolder<>(facingSupplier));
    }

    public static ChemicalTankHelper<Gas, GasStack, IGasTank> forSideGasWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        return new ChemicalTankHelper<>(new ConfigGasTankHolder(facingSupplier, configSupplier));
    }

    public static ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> forSideInfusionWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        return new ChemicalTankHelper<>(new ConfigInfusionTankHolder(facingSupplier, configSupplier));
    }

    public static ChemicalTankHelper<Pigment, PigmentStack, IPigmentTank> forSidePigmentWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        return new ChemicalTankHelper<>(new ConfigPigmentTankHolder(facingSupplier, configSupplier));
    }

    public static ChemicalTankHelper<Slurry, SlurryStack, ISlurryTank> forSideSlurryWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        return new ChemicalTankHelper<>(new ConfigSlurryTankHolder(facingSupplier, configSupplier));
    }

    public void addTank(@Nonnull TANK tank) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof ChemicalTankHolder) {
            ((ChemicalTankHolder<CHEMICAL, STACK, TANK>) slotHolder).addTank(tank);
        } else if (slotHolder instanceof ConfigChemicalTankHolder) {
            ((ConfigChemicalTankHolder<CHEMICAL, STACK, TANK>) slotHolder).addTank(tank);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add tanks");
        }
    }

    public void addTank(@Nonnull TANK tank, RelativeSide... sides) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof ChemicalTankHolder) {
            ((ChemicalTankHolder<CHEMICAL, STACK, TANK>) slotHolder).addTank(tank, sides);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add tanks on specific sides");
        }
    }

    public IChemicalTankHolder<CHEMICAL, STACK, TANK> build() {
        built = true;
        return slotHolder;
    }
}