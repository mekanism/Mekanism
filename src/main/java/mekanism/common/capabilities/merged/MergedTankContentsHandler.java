package mekanism.common.capabilities.merged;

import java.util.Collections;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicGasHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicInfusionHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicPigmentHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicSlurryHandler;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * @apiNote Make sure to set the merged tank, and dynamic handlers
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MergedTankContentsHandler<MERGED extends MergedChemicalTank> extends ItemCapability {

    protected MERGED mergedTank;
    protected DynamicGasHandler gasHandler;
    protected DynamicInfusionHandler infusionHandler;
    protected DynamicPigmentHandler pigmentHandler;
    protected DynamicSlurryHandler slurryHandler;

    protected List<ISlurryTank> slurryTanks;
    protected List<IPigmentTank> pigmentTanks;
    protected List<IInfusionTank> infusionTanks;
    protected List<IGasTank> gasTanks;

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

    protected void onContentsChanged(String key, List<? extends INBTSerializable<CompoundNBT>> containers) {
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            ItemDataUtils.setList(stack, key, DataHandlerUtils.writeContainers(containers));
        }
    }

    @Override
    protected void addCapabilityResolvers(CapabilityCache capabilityCache) {
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.GAS_HANDLER_CAPABILITY, gasHandler));
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.INFUSION_HANDLER_CAPABILITY, infusionHandler));
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.PIGMENT_HANDLER_CAPABILITY, pigmentHandler));
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.SLURRY_HANDLER_CAPABILITY, slurryHandler));
    }
}