package mekanism.common.tile.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.RelativeSide;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.computer.computercraft.ComputerConstants;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NutritionalLiquifierIRecipe;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityNutritionalLiquifier extends TileEntityProgressMachine<ItemStackToFluidRecipe> {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    public static final int MAX_FLUID = 10_000;
    public static final int BASE_TICKS_REQUIRED = 100;

    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded",
                                                                                     "getOutputFilledPercentage"}, docPlaceholder = "output tank")
    public IExtendedFluidTank fluidTank;

    private final IOutputHandler<@NotNull FluidStack> outputHandler;
    private final IInputHandler<@NotNull ItemStack> inputHandler;

    private MachineEnergyContainer<TileEntityNutritionalLiquifier> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInput", docPlaceholder = "input slot")
    InputInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getContainerFillItem", docPlaceholder = "fillable container slot")
    FluidInventorySlot containerFillSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "filled container output slot")
    OutputInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    @Nullable
    private HashedItem lastPasteItem;
    private float lastPasteScale;

    public TileEntityNutritionalLiquifier(BlockPos pos, BlockState state) {
        super(MekanismBlocks.NUTRITIONAL_LIQUIFIER, pos, state, TRACKED_ERROR_TYPES, 100);
        configComponent.setupItemIOConfig(List.of(inputSlot, containerFillSlot), Collections.singletonList(outputSlot), energySlot, false);
        configComponent.setupOutputConfig(TransmissionType.FLUID, fluidTank, RelativeSide.RIGHT);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.FLUID);

        inputHandler = InputHelper.getInputHandler(inputSlot, RecipeError.NOT_ENOUGH_INPUT);
        outputHandler = OutputHelper.getOutputHandler(fluidTank, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
    }

    @NotNull
    @Override
    public IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addTank(fluidTank = BasicFluidTank.output(MAX_FLUID, recipeCacheUnpauseListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, recipeCacheUnpauseListener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = InputInventorySlot.at(TileEntityNutritionalLiquifier::isValidInput, recipeCacheListener, 26, 36)
        ).tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT)));
        builder.addSlot(containerFillSlot = FluidInventorySlot.drain(fluidTank, listener, 155, 25));
        builder.addSlot(outputSlot = OutputInventorySlot.at(listener, 155, 56));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 155, 5));
        containerFillSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    public static boolean isValidInput(ItemStack stack) {
        if (stack.has(DataComponents.FOOD)) {//Double-check the stack is food
            FoodProperties food = stack.getFoodProperties(null);
            //And only allow inserting foods that actually would provide paste
            return food != null && food.nutrition() > 0;
        }
        return false;
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        containerFillSlot.drainTank(outputSlot);
        recipeCacheLookupMonitor.updateAndProcess();
        float pasteScale = MekanismUtils.getScale(lastPasteScale, fluidTank);
        if (pasteScale != lastPasteScale) {
            lastPasteScale = pasteScale;
            sendUpdatePacket = true;
        }
        if (inputSlot.isEmpty()) {
            if (lastPasteItem != null) {
                lastPasteItem = null;
                sendUpdatePacket = true;
            }
        } else {
            HashedItem item = HashedItem.raw(inputSlot.getStack());
            if (!item.equals(lastPasteItem)) {
                lastPasteItem = item.recreate();
                sendUpdatePacket = true;
            }
        }
        return sendUpdatePacket;
    }

    @NotNull
    @Override
    public MekanismRecipeType<ItemStackToFluidRecipe, IInputRecipeCache> getRecipeType() {
        //TODO - V11: See comment in NutritionalLiquifierIRecipe. Note if either containsRecipe and findFirstRecipe get called a null pointer will occur
        return null;
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackToFluidRecipe> recipeViewerType() {
        return RecipeViewerRecipeType.NUTRITIONAL_LIQUIFICATION;
    }

    @Nullable
    @Override
    public ItemStackToFluidRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandler.getInput();
        if (stack.isEmpty() || !stack.has(DataComponents.FOOD)) {
            return null;
        }
        FoodProperties food = stack.getFoodProperties(null);
        if (food == null || food.nutrition() == 0) {
            //If the food provides no healing don't allow consuming it as it won't provide any paste
            return null;
        }
        return new NutritionalLiquifierIRecipe(IngredientCreatorAccess.item().from(stack, 1),
              MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(food.nutrition() * 50));
    }

    @NotNull
    @Override
    public CachedRecipe<ItemStackToFluidRecipe> createNewCachedRecipe(@NotNull ItemStackToFluidRecipe recipe, int cacheIndex) {
        return OneInputCachedRecipe.itemToFluid(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
              .setErrorsChanged(this::onErrorsChanged)
              .setCanHolderFunction(this::canFunction)
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    public MachineEnergyContainer<TileEntityNutritionalLiquifier> getEnergyContainer() {
        return energyContainer;
    }

    /**
     * @apiNote Do not modify the returned stack.
     */
    public ItemStack getRenderStack() {
        if (lastPasteItem == null) {
            return ItemStack.EMPTY;
        }
        return lastPasteItem.getInternalStack();
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.put(SerializationConstants.FLUID_STORED, fluidTank.serializeNBT(provider));
        CompoundTag item = new CompoundTag();
        if (lastPasteItem != null) {
            lastPasteItem.getInternalStack().save(provider, item);
        }
        updateTag.put(SerializationConstants.ITEM, item);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        NBTUtils.setCompoundIfPresent(tag, SerializationConstants.FLUID_STORED, nbt -> fluidTank.deserializeNBT(provider, nbt));
        NBTUtils.setCompoundIfPresent(tag, SerializationConstants.ITEM, nbt -> {
            if (nbt.isEmpty()) {
                lastPasteItem = null;
            } else {
                lastPasteItem = HashedItem.raw(ItemStack.parseOptional(provider, nbt));
            }
        });
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = ComputerConstants.DESCRIPTION_GET_ENERGY_USAGE)
    public FloatingLong getEnergyUsage() {
        return getActive() ? energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
    }
    //End methods IComputerTile
}
