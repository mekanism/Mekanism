package mekanism.common.tile.factory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.ItemStackConstantChemicalToObjectCachedRecipe;
import mekanism.api.recipes.cache.ItemStackConstantChemicalToObjectCachedRecipe.ChemicalUsageMultiplier;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler.ItemChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.ConstantUsageRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.DoubleInputRecipeCache.CheckRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.upgrade.AdvancedMachineUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//Compressing, injecting, purifying, infusing
public class TileEntityItemStackChemicalToItemStackFactory extends TileEntityItemToItemFactory<ItemStackChemicalToItemStackRecipe> implements IHasDumpButton,
      ItemChemicalRecipeLookupHandler<ItemStackChemicalToItemStackRecipe>, ConstantUsageRecipeLookupHandler {

    protected static final CheckRecipeType<ItemStack, ChemicalStack, ItemStackChemicalToItemStackRecipe, ItemStack> OUTPUT_CHECK =
          (recipe, input, extra, output) -> InventoryUtils.areItemsStackable(recipe.getOutput(input, extra), output);
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

    private final ILongInputHandler<@NotNull ChemicalStack> chemicalInputHandler;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getChemicalItem", docPlaceholder = "chemical item (extra) slot")
    ChemicalInventorySlot extraSlot;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getChemical", "getChemicalCapacity", "getChemicalNeeded",
                                                                                        "getChemicalFilledPercentage"}, docPlaceholder = "chemical tank")
    IChemicalTank chemicalTank;
    private final ChemicalUsageMultiplier chemicalUsageMultiplier;
    private final long[] usedSoFar;
    private double chemicalPerTickMeanMultiplier = 1;
    private long baseTotalUsage;

    public TileEntityItemStackChemicalToItemStackFactory(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state, TRACKED_ERROR_TYPES, GLOBAL_ERROR_TYPES);
        chemicalInputHandler = InputHelper.getConstantInputHandler(chemicalTank);
        if (allowExtractingChemical()) {
            configComponent.setupIOConfig(TransmissionType.CHEMICAL, chemicalTank, RelativeSide.RIGHT).setCanEject(false);
        } else {
            configComponent.setupInputConfig(TransmissionType.CHEMICAL, chemicalTank);
        }
        baseTotalUsage = BASE_TICKS_REQUIRED;
        usedSoFar = new long[tier.processes];
        if (useStatisticalMechanics()) {
            //Note: Statistical mechanics works best by just using the mean gas usage we want to target
            // rather than adjusting the mean each time to try and reach a given target
            chemicalUsageMultiplier = (usedSoFar, operatingTicks) -> StatUtils.inversePoisson(chemicalPerTickMeanMultiplier);
        } else {
            chemicalUsageMultiplier = ChemicalUsageMultiplier.constantUse(() -> baseTotalUsage, this::getTicksRequired);
        }
    }

    @NotNull
    @Override
    public IChemicalTankHolder getInitialChemicalTanks(IContentsListener listener) {
        ChemicalTankHelper builder = ChemicalTankHelper.forSideWithConfig(this);
        //If the tank's contents change make sure to call our extended content listener that also marks sorting as being needed
        // as maybe the valid recipes have changed, and we need to sort again and have all recipes know they may need to be rechecked
        // if they are not still valid
        long capacity = Attribute.getOrThrow(getBlockHolder(), AttributeFactoryType.class).getFactoryType() == FactoryType.INFUSING ? TileEntityMetallurgicInfuser.MAX_INFUSE : TileEntityAdvancedElectricMachine.MAX_GAS;
        if (allowExtractingChemical()) {
            chemicalTank = BasicChemicalTank.createModern(capacity * tier.processes, this::containsRecipeB, markAllMonitorsChanged(listener));
        } else {
            chemicalTank = BasicChemicalTank.inputModern(capacity * tier.processes, this::containsRecipeB, markAllMonitorsChanged(listener));
        }
        builder.addTank(chemicalTank);
        return builder.build();
    }

    @Override
    protected void addSlots(InventorySlotHelper builder, IContentsListener listener, IContentsListener updateSortingListener) {
        super.addSlots(builder, listener, updateSortingListener);
        //Note: We care about the gas tank not the slot when it comes to recipes and updating sorting
        builder.addSlot(extraSlot = ChemicalInventorySlot.fillOrConvert(chemicalTank, this::getLevel, listener, 7, 57));
    }

    public IChemicalTank getChemicalTank() {
        return chemicalTank;
    }

    @Nullable
    @Override
    protected ChemicalInventorySlot getExtraSlot() {
        return extraSlot;
    }

    @Override
    public boolean isItemValidForSlot(@NotNull ItemStack stack) {
        return containsRecipeAB(stack, chemicalTank.getStack());
    }

    @Override
    public boolean isValidInputItem(@NotNull ItemStack stack) {
        return containsRecipeA(stack);
    }

    @Override
    protected int getNeededInput(ItemStackChemicalToItemStackRecipe recipe, ItemStack inputStack) {
        return MathUtils.clampToInt(recipe.getItemInput().getNeededAmount(inputStack));
    }

    @Override
    protected boolean isCachedRecipeValid(@Nullable CachedRecipe<ItemStackChemicalToItemStackRecipe> cached, @NotNull ItemStack stack) {
        if (cached != null) {
            ItemStackChemicalToItemStackRecipe cachedRecipe = cached.getRecipe();
            return cachedRecipe.getItemInput().testType(stack) && (chemicalTank.isEmpty() || cachedRecipe.getChemicalInput().testType(chemicalTank.getTypeHolder()));
        }
        return false;
    }

    @Override
    protected ItemStackChemicalToItemStackRecipe findRecipe(int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot,
          @Nullable IInventorySlot secondaryOutputSlot) {
        //TODO: Give it something that is not empty when we don't have a stored gas stack for getting the output?
        return getRecipeType().getInputCache().findTypeBasedRecipe(level, fallbackInput, chemicalTank.getStack(), outputSlot.getStack(), OUTPUT_CHECK);
    }

    @Override
    protected void handleSecondaryFuel() {
        extraSlot.fillTankOrConvert();
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> getRecipeType() {
        return switch (type) {
            case COMPRESSING -> MekanismRecipeType.COMPRESSING;
            case INFUSING -> MekanismRecipeType.METALLURGIC_INFUSING;
            case INJECTING -> MekanismRecipeType.INJECTING;
            case PURIFYING -> MekanismRecipeType.PURIFYING;
            default -> throw new IllegalStateException("Unhandled factory type");
        };
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackChemicalToItemStackRecipe> recipeViewerType() {
        return switch (type) {
            case COMPRESSING -> RecipeViewerRecipeType.COMPRESSING;
            case INFUSING -> RecipeViewerRecipeType.METALLURGIC_INFUSING;
            case INJECTING -> RecipeViewerRecipeType.INJECTING;
            case PURIFYING -> RecipeViewerRecipeType.PURIFYING;
            default -> throw new IllegalStateException("Unhandled factory type");
        };
    }

    private boolean allowExtractingChemical() {
        //Note: We can't use type directly as when this is being used for creating the chemical tank the type field hasn't been set yet
        FactoryType factoryType = Attribute.getOrThrow(getBlockHolder(), AttributeFactoryType.class).getFactoryType();
        return factoryType == FactoryType.COMPRESSING || factoryType == FactoryType.INFUSING;
    }

    private boolean useStatisticalMechanics() {
        return MekanismConfig.usage.randomizedConsumption.get() && (type == FactoryType.INJECTING || type == FactoryType.PURIFYING);
    }

    @Nullable
    @Override
    public ItemStackChemicalToItemStackRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandlers[cacheIndex], chemicalInputHandler);
    }

    @NotNull
    @Override
    public CachedRecipe<ItemStackChemicalToItemStackRecipe> createNewCachedRecipe(@NotNull ItemStackChemicalToItemStackRecipe recipe, int cacheIndex) {
        CachedRecipe<ItemStackChemicalToItemStackRecipe> cachedRecipe;
        if (recipe.perTickUsage()) {
            cachedRecipe = ItemStackConstantChemicalToObjectCachedRecipe.toItem(recipe, recheckAllRecipeErrors[cacheIndex], inputHandlers[cacheIndex], chemicalInputHandler,
                  chemicalUsageMultiplier, used -> usedSoFar[cacheIndex] = used, outputHandlers[cacheIndex]);
        } else {
            cachedRecipe = TwoInputCachedRecipe.itemChemicalToItem(recipe, recheckAllRecipeErrors[cacheIndex], inputHandlers[cacheIndex], chemicalInputHandler, outputHandlers[cacheIndex]);
        }
        return cachedRecipe
              .setErrorsChanged(errors -> errorTracker.onErrorsChanged(errors, cacheIndex))
              .setCanHolderFunction(this::canFunction)
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks)
              .setBaselineMaxOperations(this::getOperationsPerTick);
    }

    @Override
    public boolean hasSecondaryResourceBar() {
        return true;
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        if (nbt.contains(SerializationConstants.USED_SO_FAR, Tag.TAG_LONG_ARRAY)) {
            long[] savedUsed = nbt.getLongArray(SerializationConstants.USED_SO_FAR);
            if (tier.processes != savedUsed.length) {
                Arrays.fill(usedSoFar, 0);
            }
            for (int i = 0; i < tier.processes && i < savedUsed.length; i++) {
                usedSoFar[i] = savedUsed[i];
            }
        } else {
            Arrays.fill(usedSoFar, 0);
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        nbtTags.putLongArray(SerializationConstants.USED_SO_FAR, Arrays.copyOf(usedSoFar, usedSoFar.length));
    }

    @Override
    public long getSavedUsedSoFar(int cacheIndex) {
        return usedSoFar[cacheIndex];
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED || upgrade == Upgrade.CHEMICAL && supportsUpgrade(Upgrade.CHEMICAL)) {
            if (useStatisticalMechanics()) {
                chemicalPerTickMeanMultiplier = MekanismUtils.getGasPerTickMeanMultiplier(this);
            } else {
                baseTotalUsage = MekanismUtils.getBaseUsage(this, BASE_TICKS_REQUIRED);
            }
        }
    }

    @Override
    public void parseUpgradeData(HolderLookup.Provider provider, @NotNull IUpgradeData upgradeData) {
        if (upgradeData instanceof AdvancedMachineUpgradeData data) {
            //Generic factory upgrade data handling
            super.parseUpgradeData(provider, upgradeData);
            //Copy the contents using NBT so that if it is not actually valid due to a reload we don't crash
            chemicalTank.deserializeNBT(provider, data.stored.serializeNBT(provider));
            extraSlot.deserializeNBT(provider, data.chemicalSlot.serializeNBT(provider));
            System.arraycopy(data.usedSoFar, 0, usedSoFar, 0, data.usedSoFar.length);
        } else {
            Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
        }
    }

    @NotNull
    @Override
    public AdvancedMachineUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new AdvancedMachineUpgradeData(provider, redstone, getControlType(), getEnergyContainer(), progress, usedSoFar, chemicalTank, extraSlot, energySlot,
              inputSlots, outputSlots, isSorting(), getComponents());
    }

    @Override
    public void dump() {
        chemicalTank.setEmpty();
    }

    //Methods relating to IComputerTile
    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Empty the contents of the chemical tank into the environment")
    void dumpChemical() throws ComputerException {
        validateSecurityIsPublic();
        dump();
    }
    //End methods IComputerTile
}
