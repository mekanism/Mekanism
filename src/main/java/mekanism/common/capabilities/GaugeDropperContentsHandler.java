package mekanism.common.capabilities;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.chemical.infuse.BasicInfusionTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.IMekanismInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler.RateLimitGasTank;
import mekanism.common.capabilities.chemical.item.RateLimitInfusionHandler.RateLimitInfusionTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler.RateLimitFluidTank;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GaugeDropperContentsHandler extends ItemCapability implements IMekanismFluidHandler, IFluidHandlerItem, IMekanismGasHandler, IMekanismInfusionHandler {

    private static final int CAPACITY = 16 * FluidAttributes.BUCKET_VOLUME;
    //TODO: Convert this to a long and make it a config option after making fluids be able to handle longs
    private static final int TRANSFER_RATE = 256;

    public static GaugeDropperContentsHandler create() {
        return new GaugeDropperContentsHandler();
    }

    private final IInfusionTank infusionTank;
    private final IGasTank gasTank;
    private final IExtendedFluidTank fluidTank;

    private List<IInfusionTank> infusionTanks;
    private List<IGasTank> gasTanks;
    private List<IExtendedFluidTank> fluidTanks;

    private GaugeDropperContentsHandler() {
        fluidTank = new RateLimitFluidTank(TRANSFER_RATE, () -> CAPACITY, BasicFluidTank.alwaysTrueBi, this::canInsert, BasicFluidTank.alwaysTrue, this);
        gasTank = new RateLimitGasTank(() -> TRANSFER_RATE, () -> CAPACITY, BasicGasTank.alwaysTrueBi, this::canInsert, BasicGasTank.alwaysTrue, null, this);
        infusionTank = new RateLimitInfusionTank(TRANSFER_RATE, () -> CAPACITY, BasicInfusionTank.alwaysTrueBi, this::canInsert, BasicInfusionTank.alwaysTrue, this);
    }

    private boolean canInsert(FluidStack fluidStack, AutomationType automationType) {
        return gasTank.isEmpty() && infusionTank.isEmpty();
    }

    private boolean canInsert(Gas gas, AutomationType automationType) {
        return fluidTank.isEmpty() && infusionTank.isEmpty();
    }

    private boolean canInsert(InfuseType infuseType, AutomationType automationType) {
        return fluidTank.isEmpty() && gasTank.isEmpty();
    }

    @Override
    protected void init() {
        this.fluidTanks = Collections.singletonList(fluidTank);
        this.gasTanks = Collections.singletonList(gasTank);
        this.infusionTanks = Collections.singletonList(infusionTank);
    }

    @Override
    protected void load() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            DataHandlerUtils.readTanks(getFluidTanks(null), ItemDataUtils.getList(stack, NBTConstants.FLUID_TANKS));
            DataHandlerUtils.readTanks(getGasTanks(null), ItemDataUtils.getList(stack, NBTConstants.GAS_TANKS));
            DataHandlerUtils.readTanks(getInfusionTanks(null), ItemDataUtils.getList(stack, NBTConstants.INFUSION_TANKS));
        }
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Override
    public List<IGasTank> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Override
    public List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
        return infusionTanks;
    }

    @Override
    public void onContentsChanged() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            ItemDataUtils.setList(stack, NBTConstants.FLUID_TANKS, DataHandlerUtils.writeTanks(getFluidTanks(null)));
            ItemDataUtils.setList(stack, NBTConstants.GAS_TANKS, DataHandlerUtils.writeTanks(getGasTanks(null)));
            ItemDataUtils.setList(stack, NBTConstants.INFUSION_TANKS, DataHandlerUtils.writeTanks(getInfusionTanks(null)));
        }
    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return getStack();
    }

    @Override
    protected void addCapabilityResolvers(CapabilityCache capabilityCache) {
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, this));
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.GAS_HANDLER_CAPABILITY, this));
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.INFUSION_HANDLER_CAPABILITY, this));
    }
}