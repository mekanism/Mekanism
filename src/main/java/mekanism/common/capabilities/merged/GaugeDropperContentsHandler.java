package mekanism.common.capabilities.merged;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.infuse.BasicInfusionTank;
import mekanism.api.chemical.pigment.BasicPigmentTank;
import mekanism.api.chemical.slurry.BasicSlurryTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicGasHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicInfusionHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicPigmentHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicSlurryHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.InteractPredicate;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitGasTank;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitInfusionTank;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitPigmentTank;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitSlurryTank;
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
public class GaugeDropperContentsHandler extends MergedTankContentsHandler<MergedTank> implements IMekanismFluidHandler, IFluidHandlerItem {

    private static final int CAPACITY = 16 * FluidAttributes.BUCKET_VOLUME;
    //TODO: Convert this to a long and make it a config option after making fluids be able to handle longs
    private static final int TRANSFER_RATE = 256;

    public static GaugeDropperContentsHandler create() {
        return new GaugeDropperContentsHandler();
    }

    protected List<IExtendedFluidTank> fluidTanks;

    private GaugeDropperContentsHandler() {
        mergedTank = MergedTank.create(
              new RateLimitFluidTank(TRANSFER_RATE, () -> CAPACITY, this),
              new RateLimitGasTank(() -> TRANSFER_RATE, () -> CAPACITY, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrueBi, BasicGasTank.alwaysTrue, null,
                    gasHandler = new DynamicGasHandler(side -> gasTanks, InteractPredicate.ALWAYS_TRUE, InteractPredicate.ALWAYS_TRUE,
                          () -> onContentsChanged(NBTConstants.GAS_TANKS, gasTanks))),
              new RateLimitInfusionTank(() -> TRANSFER_RATE, () -> CAPACITY, BasicInfusionTank.alwaysTrueBi, BasicInfusionTank.alwaysTrueBi, BasicInfusionTank.alwaysTrue,
                    infusionHandler = new DynamicInfusionHandler(side -> infusionTanks, InteractPredicate.ALWAYS_TRUE, InteractPredicate.ALWAYS_TRUE,
                          () -> onContentsChanged(NBTConstants.INFUSION_TANKS, infusionTanks))),
              new RateLimitPigmentTank(() -> TRANSFER_RATE, () -> CAPACITY, BasicPigmentTank.alwaysTrueBi, BasicPigmentTank.alwaysTrueBi, BasicPigmentTank.alwaysTrue,
                    pigmentHandler = new DynamicPigmentHandler(side -> pigmentTanks, InteractPredicate.ALWAYS_TRUE, InteractPredicate.ALWAYS_TRUE,
                          () -> onContentsChanged(NBTConstants.PIGMENT_TANKS, pigmentTanks))),
              new RateLimitSlurryTank(() -> TRANSFER_RATE, () -> CAPACITY, BasicSlurryTank.alwaysTrueBi, BasicSlurryTank.alwaysTrueBi, BasicSlurryTank.alwaysTrue,
                    slurryHandler = new DynamicSlurryHandler(side -> slurryTanks, InteractPredicate.ALWAYS_TRUE, InteractPredicate.ALWAYS_TRUE,
                          () -> onContentsChanged(NBTConstants.SLURRY_TANKS, slurryTanks)))
        );
    }

    @Override
    protected void init() {
        super.init();
        this.fluidTanks = Collections.singletonList(mergedTank.getFluidTank());
    }

    @Override
    protected void load() {
        super.load();
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            DataHandlerUtils.readContainers(getFluidTanks(null), ItemDataUtils.getList(stack, NBTConstants.FLUID_TANKS));
        }
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Override
    public void onContentsChanged() {
        onContentsChanged(NBTConstants.FLUID_TANKS, fluidTanks);
    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return getStack();
    }

    @Override
    protected void addCapabilityResolvers(CapabilityCache capabilityCache) {
        super.addCapabilityResolvers(capabilityCache);
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, this));
    }
}