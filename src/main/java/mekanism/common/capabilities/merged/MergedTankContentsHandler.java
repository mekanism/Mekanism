package mekanism.common.capabilities.merged;

import java.util.Collections;
import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicGasHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicInfusionHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicPigmentHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler.DynamicSlurryHandler;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * @apiNote Make sure to set the merged tank, and dynamic handlers
 */
@ParametersAreNotNullByDefault
@MethodsReturnNonnullByDefault
public abstract class MergedTankContentsHandler<MERGED extends MergedChemicalTank> {

    protected final ItemStack stack;
    protected final MERGED mergedTank;
    protected DynamicGasHandler gasHandler;
    protected DynamicInfusionHandler infusionHandler;
    protected DynamicPigmentHandler pigmentHandler;
    protected DynamicSlurryHandler slurryHandler;

    protected List<ISlurryTank> slurryTanks;
    protected List<IPigmentTank> pigmentTanks;
    protected List<IInfusionTank> infusionTanks;
    protected List<IGasTank> gasTanks;

    protected MergedTankContentsHandler(ItemStack stack) {
        this.stack = stack;
        this.mergedTank = createMergedTank();

        this.gasTanks = Collections.singletonList(mergedTank.getGasTank());
        this.infusionTanks = Collections.singletonList(mergedTank.getInfusionTank());
        this.pigmentTanks = Collections.singletonList(mergedTank.getPigmentTank());
        this.slurryTanks = Collections.singletonList(mergedTank.getSlurryTank());

        ItemDataUtils.readContainers(this.stack, NBTConstants.GAS_TANKS, gasTanks);
        ItemDataUtils.readContainers(this.stack, NBTConstants.INFUSION_TANKS, infusionTanks);
        ItemDataUtils.readContainers(this.stack, NBTConstants.PIGMENT_TANKS, pigmentTanks);
        ItemDataUtils.readContainers(this.stack, NBTConstants.SLURRY_TANKS, slurryTanks);
    }

    protected abstract MERGED createMergedTank();

    protected void onContentsChanged(String key, List<? extends INBTSerializable<CompoundTag>> containers) {
        ItemDataUtils.writeContainers(stack, key, containers);
    }
}