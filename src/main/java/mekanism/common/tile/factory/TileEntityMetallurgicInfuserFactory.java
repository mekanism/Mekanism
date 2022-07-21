package mekanism.common.tile.factory;

import java.util.List;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.chemical.InfusionInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler.ItemChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.upgrade.MetallurgicInfuserUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityMetallurgicInfuserFactory extends TileEntityItemToItemFactory<MetallurgicInfuserRecipe> implements IHasDumpButton,
      ItemChemicalRecipeLookupHandler<InfuseType, InfusionStack, MetallurgicInfuserRecipe> {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    private static final Set<RecipeError> GLOBAL_ERROR_TYPES = Set.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_SECONDARY_INPUT
    );

    private final IInputHandler<@NotNull InfusionStack> infusionInputHandler;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInfuseTypeItem")
    private InfusionInventorySlot extraSlot;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getInfuseType", "getInfuseTypeCapacity", "getInfuseTypeNeeded",
                                                                                        "getInfuseTypeFilledPercentage"})
    private IInfusionTank infusionTank;

    public TileEntityMetallurgicInfuserFactory(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state, TRACKED_ERROR_TYPES, GLOBAL_ERROR_TYPES);
        infusionInputHandler = InputHelper.getInputHandler(infusionTank, RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
        configComponent.addSupported(TransmissionType.INFUSION);
        configComponent.setupIOConfig(TransmissionType.INFUSION, infusionTank, RelativeSide.RIGHT).setCanEject(false);
    }

    @NotNull
    @Override
    public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(IContentsListener listener) {
        ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> builder = ChemicalTankHelper.forSideInfusionWithConfig(this::getDirection, this::getConfig);
        //If the tank's contents change make sure to call our extended content listener that also marks sorting as being needed
        // as maybe the valid recipes have changed, and we need to sort again and have all recipes know they may need to be rechecked
        // if they are not still valid
        builder.addTank(infusionTank = ChemicalTankBuilder.INFUSION.create(TileEntityMetallurgicInfuser.MAX_INFUSE * tier.processes, this::containsRecipeB,
              markAllMonitorsChanged(listener)));
        return builder.build();
    }

    @Override
    protected void addSlots(InventorySlotHelper builder, IContentsListener listener, IContentsListener updateSortingListener) {
        super.addSlots(builder, listener, updateSortingListener);
        //Note: We care about the infusion tank not the slot when it comes to recipes and updating sorting
        builder.addSlot(extraSlot = InfusionInventorySlot.fillOrConvert(infusionTank, this::getLevel, listener, 7, 57));
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
    public boolean isValidInputItem(@NotNull ItemStack stack) {
        return containsRecipeA(stack);
    }

    @Override
    protected int getNeededInput(MetallurgicInfuserRecipe recipe, ItemStack inputStack) {
        return MathUtils.clampToInt(recipe.getItemInput().getNeededAmount(inputStack));
    }

    @Override
    protected boolean isCachedRecipeValid(@Nullable CachedRecipe<MetallurgicInfuserRecipe> cached, @NotNull ItemStack stack) {
        if (cached != null) {
            MetallurgicInfuserRecipe cachedRecipe = cached.getRecipe();
            return cachedRecipe.getItemInput().testType(stack) && (infusionTank.isEmpty() || cachedRecipe.getChemicalInput().testType(infusionTank.getType()));
        }
        return false;
    }

    @Override
    protected MetallurgicInfuserRecipe findRecipe(int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot,
          @Nullable IInventorySlot secondaryOutputSlot) {
        InfusionStack stored = infusionTank.getStack();
        ItemStack output = outputSlot.getStack();
        //TODO: Give it something that is not empty when we don't have a stored infusion stack for getting the output?
        return getRecipeType().getInputCache().findTypeBasedRecipe(level, fallbackInput, stored,
              recipe -> InventoryUtils.areItemsStackable(recipe.getOutput(fallbackInput, stored), output));
    }

    @Override
    protected void handleSecondaryFuel() {
        extraSlot.fillTankOrConvert();
    }

    @Override
    public boolean hasSecondaryResourceBar() {
        return true;
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<MetallurgicInfuserRecipe, ItemChemical<InfuseType, InfusionStack, MetallurgicInfuserRecipe>> getRecipeType() {
        return MekanismRecipeType.METALLURGIC_INFUSING;
    }

    @Nullable
    @Override
    public MetallurgicInfuserRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandlers[cacheIndex], infusionInputHandler);
    }

    @NotNull
    @Override
    public CachedRecipe<MetallurgicInfuserRecipe> createNewCachedRecipe(@NotNull MetallurgicInfuserRecipe recipe, int cacheIndex) {
        return TwoInputCachedRecipe.itemChemicalToItem(recipe, recheckAllRecipeErrors[cacheIndex], inputHandlers[cacheIndex], infusionInputHandler,
                    outputHandlers[cacheIndex])
              .setErrorsChanged(errors -> errorTracker.onErrorsChanged(errors, cacheIndex))
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks);
    }

    @Override
    public void parseUpgradeData(@NotNull IUpgradeData upgradeData) {
        if (upgradeData instanceof MetallurgicInfuserUpgradeData data) {
            //Generic factory upgrade data handling
            super.parseUpgradeData(upgradeData);
            //Copy the contents using NBT so that if it is not actually valid due to a reload we don't crash
            infusionTank.deserializeNBT(data.stored.serializeNBT());
            extraSlot.deserializeNBT(data.infusionSlot.serializeNBT());
        } else {
            Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
        }
    }

    @NotNull
    @Override
    public MetallurgicInfuserUpgradeData getUpgradeData() {
        return new MetallurgicInfuserUpgradeData(redstone, getControlType(), getEnergyContainer(), progress, infusionTank, extraSlot, energySlot,
              inputSlots, outputSlots, isSorting(), getComponents());
    }

    @Override
    public void dump() {
        infusionTank.setEmpty();
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private void dumpInfuseType() throws ComputerException {
        validateSecurityIsPublic();
        dump();
    }
    //End methods IComputerTile
}