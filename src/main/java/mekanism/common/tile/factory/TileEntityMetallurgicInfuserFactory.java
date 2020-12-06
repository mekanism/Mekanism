package mekanism.common.tile.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.MetallurgicInfuserCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.chemical.InfusionInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.upgrade.MetallurgicInfuserUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;

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
        //If the tank's contents change make sure to call our extended content listener that also marks sorting as being needed
        // as maybe the valid recipes have changed and we need to sort again
        builder.addTank(infusionTank = ChemicalTankBuilder.INFUSION.create(TileEntityMetallurgicInfuser.MAX_INFUSE * tier.processes,
              type -> containsRecipe(recipe -> recipe.getInfusionInput().testType(type)), this::onContentsChangedUpdateSorting));
        return builder.build();
    }

    @Override
    protected void addSlots(InventorySlotHelper builder, IContentsListener updateSortingListener) {
        super.addSlots(builder, updateSortingListener);
        //Note: We care about the infusion tank not the slot when it comes to recipes and updating sorting
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
    protected int getNeededInput(MetallurgicInfuserRecipe recipe, ItemStack inputStack) {
        return MathUtils.clampToInt(recipe.getItemInput().getNeededAmount(inputStack));
    }

    @Override
    protected boolean isCachedRecipeValid(@Nullable CachedRecipe<MetallurgicInfuserRecipe> cached, @Nonnull ItemStack stack) {
        if (cached != null) {
            MetallurgicInfuserRecipe cachedRecipe = cached.getRecipe();
            return cachedRecipe.getItemInput().testType(stack) && (infusionTank.isEmpty() || cachedRecipe.getInfusionInput().testType(infusionTank.getType()));
        }
        return false;
    }

    @Override
    protected MetallurgicInfuserRecipe findRecipe(int process, @Nonnull ItemStack fallbackInput, @Nonnull IInventorySlot outputSlot,
          @Nullable IInventorySlot secondaryOutputSlot) {
        long stored = infusionTank.getStored();
        InfuseType type = infusionTank.getType();
        ItemStack output = outputSlot.getStack();
        return findFirstRecipe(recipe -> {
            //Check the infusion type before the ItemStack type as it a quicker easier compare check
            if (stored == 0 || recipe.getInfusionInput().testType(type)) {
                return recipe.getItemInput().testType(fallbackInput) && InventoryUtils.areItemsStackable(recipe.getOutput(infusionTank.getStack(), fallbackInput), output);
            }
            return false;
        });
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
            //Generic factory upgrade data handling
            super.parseUpgradeData(upgradeData);
            MetallurgicInfuserUpgradeData data = (MetallurgicInfuserUpgradeData) upgradeData;
            //Copy the contents using NBT so that if it is not actually valid due to a reload we don't crash
            infusionTank.deserializeNBT(data.stored.serializeNBT());
            extraSlot.deserializeNBT(data.infusionSlot.serializeNBT());
        } else {
            Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
        }
    }

    @Nonnull
    @Override
    public MetallurgicInfuserUpgradeData getUpgradeData() {
        return new MetallurgicInfuserUpgradeData(redstone, getControlType(), getEnergyContainer(), progress, infusionTank, extraSlot, energySlot,
              inputSlots, outputSlots, isSorting(), getComponents());
    }

    @Override
    public void dump() {
        infusionTank.setEmpty();
    }
}