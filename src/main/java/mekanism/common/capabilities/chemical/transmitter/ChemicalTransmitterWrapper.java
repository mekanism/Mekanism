package mekanism.common.capabilities.chemical.transmitter;

import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
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
import mekanism.api.inventory.AutomationType;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.lib.transmitter.ConnectionType;
import net.minecraft.util.Direction;

public abstract class ChemicalTransmitterWrapper<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {

    private final BoxedPressurizedTube tube;
    private final Function<Direction, List<TANK>> tankGetter;

    protected ChemicalTransmitterWrapper(BoxedPressurizedTube tube, Function<Direction, List<TANK>> tankGetter) {
        this.tube = tube;
        this.tankGetter = tankGetter;
    }

    @Nonnull
    @Override
    public List<TANK> getChemicalTanks(@Nullable Direction side) {
        return tankGetter.apply(side);
    }

    @Nonnull
    @Override
    public STACK insertChemical(int tank, @Nonnull STACK stack, @Nullable Direction side, @Nonnull Action action) {
        TANK chemicalTank = getChemicalTank(tank, side);
        if (chemicalTank == null) {
            return stack;
        } else if (side == null) {
            return chemicalTank.insert(stack, action, AutomationType.INTERNAL);
        }
        //If we have a side only allow inserting if our connection allows it
        ConnectionType connectionType = tube.getConnectionType(side);
        if (connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PULL) {
            return chemicalTank.insert(stack, action, AutomationType.EXTERNAL);
        }
        return stack;
    }

    @Override
    public void onContentsChanged() {
        tube.onContentsChanged();
    }

    public static class GasTransmitterWrapper extends ChemicalTransmitterWrapper<Gas, GasStack, IGasTank> implements IMekanismGasHandler {

        public GasTransmitterWrapper(BoxedPressurizedTube tube, Function<Direction, List<IGasTank>> tankGetter) {
            super(tube, tankGetter);
        }
    }

    public static class InfusionTransmitterWrapper extends ChemicalTransmitterWrapper<InfuseType, InfusionStack, IInfusionTank> implements IMekanismInfusionHandler {

        public InfusionTransmitterWrapper(BoxedPressurizedTube tube, Function<Direction, List<IInfusionTank>> tankGetter) {
            super(tube, tankGetter);
        }
    }

    public static class PigmentTransmitterWrapper extends ChemicalTransmitterWrapper<Pigment, PigmentStack, IPigmentTank> implements IMekanismPigmentHandler {

        public PigmentTransmitterWrapper(BoxedPressurizedTube tube, Function<Direction, List<IPigmentTank>> tankGetter) {
            super(tube, tankGetter);
        }
    }

    public static class SlurryTransmitterWrapper extends ChemicalTransmitterWrapper<Slurry, SlurryStack, ISlurryTank> implements IMekanismSlurryHandler {

        public SlurryTransmitterWrapper(BoxedPressurizedTube tube, Function<Direction, List<ISlurryTank>> tankGetter) {
            super(tube, tankGetter);
        }
    }
}