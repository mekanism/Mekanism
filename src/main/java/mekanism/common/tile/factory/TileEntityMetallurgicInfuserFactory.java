package mekanism.common.tile.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.MetallurgicInfuserCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.chemical.InfusionInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.upgrade.MetallurgicInfuserUpgradeData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntityMetallurgicInfuserFactory extends TileEntityItemToItemFactory<MetallurgicInfuserRecipe> implements IHasDumpButton {

    private final IInputHandler<@NonNull InfusionStack> infusionInputHandler;

    private InfusionInventorySlot extraSlot;
    private IInfusionTank infusionTank;

    public TileEntityMetallurgicInfuserFactory(IBlockProvider blockProvider) {
        super(blockProvider);
        infusionInputHandler = InputHelper.getInputHandler(infusionTank);
        configComponent.addSupported(TransmissionType.INFUSION);
        configComponent.setupIOConfig(TransmissionType.INFUSION, infusionTank, infusionTank, RelativeSide.RIGHT).setCanEject(false);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks() {
        ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> builder = ChemicalTankHelper.forSideInfusionWithConfig(this::getDirection, this::getConfig);
        builder.addTank(infusionTank = ChemicalTankBuilder.INFUSION.create(TileEntityMetallurgicInfuser.MAX_INFUSE * tier.processes,
              type -> containsRecipe(recipe -> recipe.getInfusionInput().testType(type)), this));
        return builder.build();
    }

    @Override
    protected void addSlots(InventorySlotHelper builder) {
        super.addSlots(builder);
        builder.addSlot(extraSlot = InfusionInventorySlot.fillOrConvert(infusionTank, this::getWorld, this, 7, 57));
    }

    public IInfusionTank getInfusionTank() {
        return infusionTank;
    }

    @Nullable
    @Override
    protected InfusionInventorySlot getExtraSlot() {
        return extraSlot;
    }

    @Override
    public boolean isValidInputItem(@Nonnull ItemStack stack) {
        return containsRecipe(recipe -> recipe.getItemInput().testType(stack));
    }

    @Override
    public boolean inputProducesOutput(int process, @Nonnull ItemStack fallbackInput, @Nonnull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot,
          boolean updateCache) {
        if (outputSlot.isEmpty()) {
            return true;
        }
        CachedRecipe<MetallurgicInfuserRecipe> cached = getCachedRecipe(process);
        if (cached != null) {
            MetallurgicInfuserRecipe cachedRecipe = cached.getRecipe();
            if (cachedRecipe.getItemInput().testType(fallbackInput) && (infusionTank.isEmpty() || cachedRecipe.getInfusionInput().testType(infusionTank.getType()))) {
                //Our input matches the recipe we have cached for this slot
                return true;
            }
            //If there is no cached item input or it doesn't match our fallback then it is an out of date cache, so we ignore the fact that we have a cache
        }
        long stored = infusionTank.getStored();
        InfuseType type = infusionTank.getType();
        ItemStack output = outputSlot.getStack();
        MetallurgicInfuserRecipe foundRecipe = findFirstRecipe(recipe -> {
            //Check the infusion type before the ItemStack type as it a quicker easier compare check
            if (stored == 0 || recipe.getInfusionInput().testType(type)) {
                return recipe.getItemInput().testType(fallbackInput) && ItemHandlerHelper.canItemStacksStack(recipe.getOutput(infusionTank.getStack(), fallbackInput), output);
            }
            return false;
        });
        if (foundRecipe == null) {
            //We could not find any valid recipe for the given item that matches the items in the current output slots
            return false;
        }
        if (updateCache) {
            //If we want to update the cache, then create a new cache with the recipe we found
            CachedRecipe<MetallurgicInfuserRecipe> newCachedRecipe = createNewCachedRecipe(foundRecipe, process);
            if (newCachedRecipe == null) {
                //If we want to update the cache but failed to create a new cache then return that the item is not valid for the slot as something goes wrong
                // I believe we can actually make createNewCachedRecipe Nonnull which will remove this if statement
                return false;
            }
            updateCachedRecipe(newCachedRecipe, process);
        }
        return true;
    }

    @Override
    protected void handleSecondaryFuel() {
        extraSlot.fillTankOrConvert();
    }

    @Override
    public boolean hasSecondaryResourceBar() {
        return true;
    }

    @Nonnull
    @Override
    public MekanismRecipeType<MetallurgicInfuserRecipe> getRecipeType() {
        return MekanismRecipeType.METALLURGIC_INFUSING;
    }

    @Nullable
    @Override
    public MetallurgicInfuserRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandlers[cacheIndex].getInput();
        if (stack.isEmpty()) {
            return null;
        }
        InfusionStack infusionStack = infusionInputHandler.getInput();
        if (infusionStack.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(infusionStack, stack));
    }

    @Override
    public CachedRecipe<MetallurgicInfuserRecipe> createNewCachedRecipe(@Nonnull MetallurgicInfuserRecipe recipe, int cacheIndex) {
        return new MetallurgicInfuserCachedRecipe(recipe, infusionInputHandler, inputHandlers[cacheIndex], outputHandlers[cacheIndex])
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(() -> markDirty(false))
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks);
    }

    @Override
    public void parseUpgradeData(@Nonnull IUpgradeData upgradeData) {
        if (upgradeData instanceof MetallurgicInfuserUpgradeData) {
            MetallurgicInfuserUpgradeData data = (MetallurgicInfuserUpgradeData) upgradeData;
            redstone = data.redstone;
            setControlType(data.controlType);
            getEnergyContainer().setEnergy(data.energyContainer.getEnergy());
            sorting = data.sorting;
            infusionTank.setStack(data.stored);
            extraSlot.setStack(data.infusionSlot.getStack());
            energySlot.setStack(data.energySlot.getStack());
            System.arraycopy(data.progress, 0, progress, 0, data.progress.length);
            for (int i = 0; i < data.inputSlots.size(); i++) {
                inputSlots.get(i).setStack(data.inputSlots.get(i).getStack());
            }
            for (int i = 0; i < data.outputSlots.size(); i++) {
                outputSlots.get(i).setStack(data.outputSlots.get(i).getStack());
            }
            for (ITileComponent component : getComponents()) {
                component.read(data.components);
            }
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public MetallurgicInfuserUpgradeData getUpgradeData() {
        return new MetallurgicInfuserUpgradeData(redstone, getControlType(), getEnergyContainer(), progress, infusionTank.getStack(), extraSlot, energySlot,
              inputSlots, outputSlots, isSorting(), getComponents());
    }

    @Override
    public void dump() {
        infusionTank.setEmpty();
    }
}