package mekanism.common.capabilities;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.IMekanismInfusionHandler;
import mekanism.api.chemical.pigment.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler.RateLimitGasTank;
import mekanism.common.capabilities.chemical.item.RateLimitInfusionHandler.RateLimitInfusionTank;
import mekanism.common.capabilities.chemical.item.RateLimitPigmentHandler.RateLimitPigmentTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler.RateLimitFluidTank;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GaugeDropperContentsHandler extends ItemCapability implements IMekanismFluidHandler, IFluidHandlerItem, IMekanismGasHandler, IMekanismInfusionHandler,
      IMekanismPigmentHandler {

    private static final int CAPACITY = 16 * FluidAttributes.BUCKET_VOLUME;
    //TODO: Convert this to a long and make it a config option after making fluids be able to handle longs
    private static final int TRANSFER_RATE = 256;

    public static GaugeDropperContentsHandler create() {
        return new GaugeDropperContentsHandler();
    }

    private final MergedTank mergedTank;

    private List<IPigmentTank> pigmentTanks;
    private List<IInfusionTank> infusionTanks;
    private List<IGasTank> gasTanks;
    private List<IExtendedFluidTank> fluidTanks;

    private GaugeDropperContentsHandler() {
        mergedTank = MergedTank.create(
              new RateLimitFluidTank(TRANSFER_RATE, () -> CAPACITY, this),
              new RateLimitGasTank(() -> TRANSFER_RATE, () -> CAPACITY, null, this),
              new RateLimitInfusionTank(TRANSFER_RATE, () -> CAPACITY,this),
              new RateLimitPigmentTank(TRANSFER_RATE, () -> CAPACITY,this)
        );
    }

    @Override
    protected void init() {
        this.fluidTanks = Collections.singletonList(mergedTank.getFluidTank());
        this.gasTanks = Collections.singletonList(mergedTank.getGasTank());
        this.infusionTanks = Collections.singletonList(mergedTank.getInfusionTank());
        this.pigmentTanks = Collections.singletonList(mergedTank.getPigmentTank());
    }

    @Override
    protected void load() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            DataHandlerUtils.readContainers(getFluidTanks(null), ItemDataUtils.getList(stack, NBTConstants.FLUID_TANKS));
            DataHandlerUtils.readContainers(getGasTanks(null), ItemDataUtils.getList(stack, NBTConstants.GAS_TANKS));
            DataHandlerUtils.readContainers(getInfusionTanks(null), ItemDataUtils.getList(stack, NBTConstants.INFUSION_TANKS));
            DataHandlerUtils.readContainers(getPigmentTanks(null), ItemDataUtils.getList(stack, NBTConstants.PIGMENT_TANKS));
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
    public List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
        return pigmentTanks;
    }

    @Override
    public void onContentsChanged() {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            ItemDataUtils.setList(stack, NBTConstants.FLUID_TANKS, DataHandlerUtils.writeContainers(getFluidTanks(null)));
            ItemDataUtils.setList(stack, NBTConstants.GAS_TANKS, DataHandlerUtils.writeContainers(getGasTanks(null)));
            ItemDataUtils.setList(stack, NBTConstants.INFUSION_TANKS, DataHandlerUtils.writeContainers(getInfusionTanks(null)));
            ItemDataUtils.setList(stack, NBTConstants.PIGMENT_TANKS, DataHandlerUtils.writeContainers(getPigmentTanks(null)));
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
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.PIGMENT_HANDLER_CAPABILITY, this));
    }
}