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
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
        event.registerItem(Capabilities.GAS.item(), (stack, ctx) -> getOrAttachHandler(stack).gasHandler, item);
        event.registerItem(Capabilities.INFUSION.item(), (stack, ctx) -> getOrAttachHandler(stack).infusionHandler, item);
        event.registerItem(Capabilities.PIGMENT.item(), (stack, ctx) -> getOrAttachHandler(stack).pigmentHandler, item);
        event.registerItem(Capabilities.SLURRY.item(), (stack, ctx) -> getOrAttachHandler(stack).slurryHandler, item);
        event.registerItem(Capabilities.FLUID.item(), (stack, ctx) -> getOrAttachHandler(stack), item);
    }

    /**
     * Gets or attaches a new handler to the stack so that we can have a single instance reach across capabilities. That way if the gas and infusion capabilities are
     * gotten, and we insert into gas and then try to insert into infusion we will properly not allow that.
     */
    private static GaugeDropperContentsHandler getOrAttachHandler(ItemStack stack) {
        if (stack.hasData(MekanismAttachmentTypes.GAUGE_DROPPER_CONTENTS_HANDLER)) {
            return stack.getData(MekanismAttachmentTypes.GAUGE_DROPPER_CONTENTS_HANDLER);
        }
        GaugeDropperContentsHandler handler = new GaugeDropperContentsHandler(stack);
        stack.setData(MekanismAttachmentTypes.GAUGE_DROPPER_CONTENTS_HANDLER, handler);
        return handler;
    }

    public static GaugeDropperContentsHandler createDummy() {
        return new GaugeDropperContentsHandler(MekanismBlocks.BASIC_CHEMICAL_TANK.getItemStack());
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