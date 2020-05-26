package mekanism.common.capabilities;

import java.util.Collections;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.InteractPredicate;
import mekanism.common.capabilities.chemical.dynamic.DynamicGasHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicInfusionHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicPigmentHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicSlurryHandler;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler.RateLimitGasTank;
import mekanism.common.capabilities.chemical.item.RateLimitInfusionHandler.RateLimitInfusionTank;
import mekanism.common.capabilities.chemical.item.RateLimitPigmentHandler.RateLimitPigmentTank;
import mekanism.common.capabilities.chemical.item.RateLimitSlurryHandler.RateLimitSlurryTank;
import mekanism.common.capabilities.fluid.item.ItemStackMekanismFluidHandler;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler.RateLimitFluidTank;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidAttributes;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GaugeDropperContentsHandler extends ItemStackMekanismFluidHandler {

    private static final int CAPACITY = 16 * FluidAttributes.BUCKET_VOLUME;
    //TODO: Convert this to a long and make it a config option after making fluids be able to handle longs
    private static final int TRANSFER_RATE = 256;

    public static GaugeDropperContentsHandler create() {
        return new GaugeDropperContentsHandler();
    }

    private final MergedTank mergedTank;

    private final DynamicGasHandler gasHandler;
    private final DynamicInfusionHandler infusionHandler;
    private final DynamicPigmentHandler pigmentHandler;
    private final DynamicSlurryHandler slurryHandler;

    private List<ISlurryTank> slurryTanks;
    private List<IPigmentTank> pigmentTanks;
    private List<IInfusionTank> infusionTanks;
    private List<IGasTank> gasTanks;

    private GaugeDropperContentsHandler() {
        mergedTank = MergedTank.create(
              new RateLimitFluidTank(TRANSFER_RATE, () -> CAPACITY, this),
              new RateLimitGasTank(() -> TRANSFER_RATE, () -> CAPACITY, null, gasHandler = new DynamicGasHandler(side -> gasTanks,
                    InteractPredicate.ALWAYS_TRUE, InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.GAS_TANKS, gasTanks))),
              new RateLimitInfusionTank(TRANSFER_RATE, () -> CAPACITY, infusionHandler = new DynamicInfusionHandler(side -> infusionTanks,
                    InteractPredicate.ALWAYS_TRUE, InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.INFUSION_TANKS, infusionTanks))),
              new RateLimitPigmentTank(TRANSFER_RATE, () -> CAPACITY, pigmentHandler = new DynamicPigmentHandler(side -> pigmentTanks,
                    InteractPredicate.ALWAYS_TRUE, InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.PIGMENT_TANKS, pigmentTanks))),
              new RateLimitSlurryTank(TRANSFER_RATE, () -> CAPACITY, slurryHandler = new DynamicSlurryHandler(side -> slurryTanks,
                    InteractPredicate.ALWAYS_TRUE, InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.SLURRY_TANKS, slurryTanks)))
        );
    }

    @Override
    protected List<IExtendedFluidTank> getInitialTanks() {
        return Collections.singletonList(mergedTank.getFluidTank());
    }

    @Override
    protected void init() {
        super.init();
        this.gasTanks = Collections.singletonList(mergedTank.getGasTank());
        this.infusionTanks = Collections.singletonList(mergedTank.getInfusionTank());
        this.pigmentTanks = Collections.singletonList(mergedTank.getPigmentTank());
        this.slurryTanks = Collections.singletonList(mergedTank.getSlurryTank());
    }

    @Override
    protected void load() {
        super.load();
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            DataHandlerUtils.readContainers(gasTanks, ItemDataUtils.getList(stack, NBTConstants.GAS_TANKS));
            DataHandlerUtils.readContainers(infusionTanks, ItemDataUtils.getList(stack, NBTConstants.INFUSION_TANKS));
            DataHandlerUtils.readContainers(pigmentTanks, ItemDataUtils.getList(stack, NBTConstants.PIGMENT_TANKS));
            DataHandlerUtils.readContainers(slurryTanks, ItemDataUtils.getList(stack, NBTConstants.SLURRY_TANKS));
        }
    }

    private void onContentsChanged(String key, List<? extends INBTSerializable<CompoundNBT>> containers) {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            ItemDataUtils.setList(stack, key, DataHandlerUtils.writeContainers(containers));
        }
    }

    @Override
    protected void addCapabilityResolvers(CapabilityCache capabilityCache) {
        super.addCapabilityResolvers(capabilityCache);
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.GAS_HANDLER_CAPABILITY, gasHandler));
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.INFUSION_HANDLER_CAPABILITY, infusionHandler));
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.PIGMENT_HANDLER_CAPABILITY, pigmentHandler));
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.SLURRY_HANDLER_CAPABILITY, slurryHandler));
    }
}