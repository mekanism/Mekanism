package mekanism.common.capabilities.merged;

import java.util.Collections;
import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.DynamicHandler.InteractPredicate;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicGasHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicInfusionHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicPigmentHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicSlurryHandler;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitGasTank;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitInfusionTank;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitPigmentTank;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitSlurryTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler.RateLimitFluidTank;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class GaugeDropperContentsHandler extends MergedTankContentsHandler<MergedTank> implements IMekanismFluidHandler, IFluidHandlerItem {

    private static final int CAPACITY = 16 * FluidType.BUCKET_VOLUME;
    //TODO: Convert this to a long and make it a config option after making fluids be able to handle longs. Also make the gauge dropper override areCapabilityConfigsLoaded
    private static final int TRANSFER_RATE = 256;

    public static void attachCapsToItem(RegisterCapabilitiesEvent event, Item item) {
        //TODO - 1.20.2: Figure out a better way to do this
        event.registerItem(Capabilities.GAS_HANDLER.item(), (stack, ctx) -> new GaugeDropperContentsHandler(stack).gasHandler, item);
        event.registerItem(Capabilities.INFUSION_HANDLER.item(), (stack, ctx) -> new GaugeDropperContentsHandler(stack).infusionHandler, item);
        event.registerItem(Capabilities.PIGMENT_HANDLER.item(), (stack, ctx) -> new GaugeDropperContentsHandler(stack).pigmentHandler, item);
        event.registerItem(Capabilities.SLURRY_HANDLER.item(), (stack, ctx) -> new GaugeDropperContentsHandler(stack).slurryHandler, item);
        event.registerItem(FluidHandler.ITEM, (stack, ctx) -> new GaugeDropperContentsHandler(stack), item);
    }

    protected final List<IExtendedFluidTank> fluidTanks;

    private GaugeDropperContentsHandler(ItemStack stack) {
        super(stack);
        this.fluidTanks = Collections.singletonList(mergedTank.getFluidTank());
        ItemDataUtils.readContainers(this.stack, NBTConstants.FLUID_TANKS, getFluidTanks(null));
    }

    @Override
    protected MergedTank createMergedTank() {
        return MergedTank.create(
              new RateLimitFluidTank(() -> TRANSFER_RATE, () -> CAPACITY, this),
              new RateLimitGasTank(() -> TRANSFER_RATE, () -> CAPACITY, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrueBi,
                    ChemicalTankBuilder.GAS.alwaysTrue, null, gasHandler = new DynamicGasHandler(side -> gasTanks, InteractPredicate.ALWAYS_TRUE,
                    InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.GAS_TANKS, gasTanks))),
              new RateLimitInfusionTank(() -> TRANSFER_RATE, () -> CAPACITY, ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrueBi,
                    ChemicalTankBuilder.INFUSION.alwaysTrue, infusionHandler = new DynamicInfusionHandler(side -> infusionTanks, InteractPredicate.ALWAYS_TRUE,
                    InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.INFUSION_TANKS, infusionTanks))),
              new RateLimitPigmentTank(() -> TRANSFER_RATE, () -> CAPACITY, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
                    ChemicalTankBuilder.PIGMENT.alwaysTrue, pigmentHandler = new DynamicPigmentHandler(side -> pigmentTanks, InteractPredicate.ALWAYS_TRUE,
                    InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.PIGMENT_TANKS, pigmentTanks))),
              new RateLimitSlurryTank(() -> TRANSFER_RATE, () -> CAPACITY, ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrueBi,
                    ChemicalTankBuilder.SLURRY.alwaysTrue, slurryHandler = new DynamicSlurryHandler(side -> slurryTanks, InteractPredicate.ALWAYS_TRUE,
                    InteractPredicate.ALWAYS_TRUE, () -> onContentsChanged(NBTConstants.SLURRY_TANKS, slurryTanks)))
        );
    }

    @NotNull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Override
    public void onContentsChanged() {
        onContentsChanged(NBTConstants.FLUID_TANKS, fluidTanks);
    }

    @NotNull
    @Override
    public ItemStack getContainer() {
        return stack;
    }
}