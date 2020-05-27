package mekanism.common.capabilities.resolver.manager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasHandler.ISidedGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionHandler.ISidedInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentHandler.ISidedPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryHandler.ISidedSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler.ProxyGasHandler;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler.ProxyInfusionHandler;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler.ProxyPigmentHandler;
import mekanism.common.capabilities.proxy.ProxyChemicalHandler.ProxySlurryHandler;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Helper class to make reading instead of having as messy generics
 */
@ParametersAreNonnullByDefault
public class ChemicalHandlerManager<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>,
      HANDLER extends IChemicalHandler<CHEMICAL, STACK>, SIDED_HANDLER extends HANDLER> extends CapabilityHandlerManager<IChemicalTankHolder<CHEMICAL, STACK, TANK>,
      TANK, HANDLER, SIDED_HANDLER> {

    protected ChemicalHandlerManager(@Nullable IChemicalTankHolder<CHEMICAL, STACK, TANK> holder, SIDED_HANDLER baseHandler, Capability<HANDLER> supportedCapability,
          ProxyCreator<HANDLER, SIDED_HANDLER> proxyCreator) {
        super(holder, baseHandler, supportedCapability, proxyCreator, IChemicalTankHolder::getTanks);
    }

    public static class GasHandlerManager extends ChemicalHandlerManager<Gas, GasStack, IGasTank, IGasHandler, ISidedGasHandler> {

        public GasHandlerManager(@Nullable IChemicalTankHolder<Gas, GasStack, IGasTank> holder, @Nonnull ISidedGasHandler baseHandler) {
            super(holder, baseHandler, Capabilities.GAS_HANDLER_CAPABILITY, ProxyGasHandler::new);
        }
    }

    public static class InfusionHandlerManager extends ChemicalHandlerManager<InfuseType, InfusionStack, IInfusionTank, IInfusionHandler, ISidedInfusionHandler> {

        public InfusionHandlerManager(@Nullable IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> holder, @Nonnull ISidedInfusionHandler baseHandler) {
            super(holder, baseHandler, Capabilities.INFUSION_HANDLER_CAPABILITY, ProxyInfusionHandler::new);
        }
    }

    public static class PigmentHandlerManager extends ChemicalHandlerManager<Pigment, PigmentStack, IPigmentTank, IPigmentHandler, ISidedPigmentHandler> {

        public PigmentHandlerManager(@Nullable IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> holder, @Nonnull ISidedPigmentHandler baseHandler) {
            super(holder, baseHandler, Capabilities.PIGMENT_HANDLER_CAPABILITY, ProxyPigmentHandler::new);
        }
    }

    public static class SlurryHandlerManager extends ChemicalHandlerManager<Slurry, SlurryStack, ISlurryTank, ISlurryHandler, ISidedSlurryHandler> {

        public SlurryHandlerManager(@Nullable IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> holder, @Nonnull ISidedSlurryHandler baseHandler) {
            super(holder, baseHandler, Capabilities.SLURRY_HANDLER_CAPABILITY, ProxySlurryHandler::new);
        }
    }
}