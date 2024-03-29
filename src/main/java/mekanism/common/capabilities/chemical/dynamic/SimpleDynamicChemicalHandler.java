package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import java.util.function.Function;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler.IMekanismGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionHandler.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentHandler.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryHandler.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.SimpleDynamicHandler;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class SimpleDynamicChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      extends SimpleDynamicHandler<TANK> implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {

    protected SimpleDynamicChemicalHandler(Function<Direction, List<TANK>> tankSupplier, @Nullable IContentsListener listener) {
        super(tankSupplier, listener);
    }

    @Override
    public List<TANK> getChemicalTanks(@Nullable Direction side) {
        return containerSupplier.apply(side);
    }

    public static class SimpleDynamicGasHandler extends SimpleDynamicChemicalHandler<Gas, GasStack, IGasTank> implements IMekanismGasHandler {

        public SimpleDynamicGasHandler(Function<Direction, List<IGasTank>> tankSupplier, @Nullable IContentsListener listener) {
            super(tankSupplier, listener);
        }
    }

    public static class SimpleDynamicInfusionHandler extends SimpleDynamicChemicalHandler<InfuseType, InfusionStack, IInfusionTank> implements IMekanismInfusionHandler {

        public SimpleDynamicInfusionHandler(Function<Direction, List<IInfusionTank>> tankSupplier, @Nullable IContentsListener listener) {
            super(tankSupplier, listener);
        }
    }

    public static class SimpleDynamicPigmentHandler extends SimpleDynamicChemicalHandler<Pigment, PigmentStack, IPigmentTank> implements IMekanismPigmentHandler {

        public SimpleDynamicPigmentHandler(Function<Direction, List<IPigmentTank>> tankSupplier, @Nullable IContentsListener listener) {
            super(tankSupplier, listener);
        }
    }

    public static class SimpleDynamicSlurryHandler extends SimpleDynamicChemicalHandler<Slurry, SlurryStack, ISlurryTank> implements IMekanismSlurryHandler {

        public SimpleDynamicSlurryHandler(Function<Direction, List<ISlurryTank>> tankSupplier, @Nullable IContentsListener listener) {
            super(tankSupplier, listener);
        }
    }
}