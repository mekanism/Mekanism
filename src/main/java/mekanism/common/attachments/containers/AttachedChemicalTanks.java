package mekanism.common.attachments.containers;

import java.util.List;
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
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class AttachedChemicalTanks<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
      extends AttachedContainers<TANK> implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {

    protected AttachedChemicalTanks(List<TANK> tanks) {
        super(tanks);
    }

    @Override
    public List<TANK> getChemicalTanks(@Nullable Direction side) {
        return containers;
    }

    public static class AttachedGasTanks extends AttachedChemicalTanks<Gas, GasStack, IGasTank> implements IMekanismGasHandler {

        public AttachedGasTanks(List<IGasTank> tanks) {
            super(tanks);
        }
    }

    public static class AttachedInfusionTanks extends AttachedChemicalTanks<InfuseType, InfusionStack, IInfusionTank> implements IMekanismInfusionHandler {

        public AttachedInfusionTanks(List<IInfusionTank> tanks) {
            super(tanks);
        }
    }

    public static class AttachedPigmentTanks extends AttachedChemicalTanks<Pigment, PigmentStack, IPigmentTank> implements IMekanismPigmentHandler {

        public AttachedPigmentTanks(List<IPigmentTank> tanks) {
            super(tanks);
        }
    }

    public static class AttachedSlurryTanks extends AttachedChemicalTanks<Slurry, SlurryStack, ISlurryTank> implements IMekanismSlurryHandler {

        public AttachedSlurryTanks(List<ISlurryTank> tanks) {
            super(tanks);
        }
    }
}