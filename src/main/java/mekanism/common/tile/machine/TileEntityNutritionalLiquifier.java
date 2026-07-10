package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe.FluidOptionalItemOutput;
import mekanism.api.recipes.basic.BasicItemStackToFluidOptionalItemRecipe;
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
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.capabilities.holder.single.SingleConfigHolder;
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
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NutritionalLiquifierRecipe;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class TileEntityNutritionalLiquifier extends TileEntityProgressMachine<BasicItemStackToFluidOptionalItemRecipe> {

    public static final RecipeError NOT_ENOUGH_SPACE_ITEM_OUTPUT_ERROR = RecipeError.create();
    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          NOT_ENOUGH_SPACE_ITEM_OUTPUT_ERROR,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    public static final long MAX_FLUID = 10L * FluidType.BUCKET_VOLUME;
    public static final int BASE_TICKS_REQUIRED = 5 * SharedConstants.TICKS_PER_SECOND;

    @UnknownNullability//Initialized via getInitialFluidTanks
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded",
                                                                                     "getOutputFilledPercentage"}, docPlaceholder = "output tank")
    public IFluidTank fluidTank;

    private final IOutputHandler<FluidOptionalItemOutput> outputHandler;
    private final IInputHandler<Item, ItemStack> inputHandler;

    @UnknownNullability//Initialized via getInitialEnergyContainer
    private MachineEnergyContainer<TileEntityNutritionalLiquifier> energyContainer;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInput", docPlaceholder = "input slot")
    InputInventorySlot inputSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "output slot")
    OutputInventorySlot outputSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getContainerFillItem", docPlaceholder = "fillable container slot")
    FluidInventorySlot containerFillSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getContainerOutputItem", docPlaceholder = "filled container output slot")
    OutputInventorySlot containerOutputSlot;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    private ItemResource lastPasteItem = ItemResource.EMPTY;
    private float lastPasteScale;

    public TileEntityNutritionalLiquifier(BlockPos pos, BlockState state) {
        super(MekanismBlocks.NUTRITIONAL_LIQUIFIER, pos, state, TRACKED_ERROR_TYPES, BASE_TICKS_REQUIRED);
        configComponent.setupItemIOConfig(List.of(inputSlot, containerFillSlot), List.of(outputSlot, containerOutputSlot), energySlot, false);
        configComponent.setupOutputConfig(TransmissionType.FLUID, fluidTank);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent.setOutputData(configComponent, TransmissionType.FLUID);

        inputHandler = InputHelper.getInputHandler(inputSlot, RecipeError.NOT_ENOUGH_INPUT);
        outputHandler = OutputHelper.getOutputHandler(fluidTank, RecipeError.NOT_ENOUGH_OUTPUT_SPACE, outputSlot, NOT_ENOUGH_SPACE_ITEM_OUTPUT_ERROR);
    }

    @Override
    public IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        MekContainerHelper<IFluidTank> builder = MekContainerHelper.forSideWithFluidConfig(this);
        builder.addContainer(fluidTank = BasicFluidTank.output(MAX_FLUID, recipeCacheUnpauseListener));
        return builder.build();
    }

    @Override
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        energyContainer = MachineEnergyContainer.input(this, recipeCacheUnpauseListener);
        return SingleConfigHolder.energy(energyContainer, this);
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener, IContentsListener recipeCacheUnpauseListener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSideWithItemConfig(this);
        builder.addContainer(inputSlot = InputInventorySlot.at(TileEntityNutritionalLiquifier::isValidInput, recipeCacheListener, 26, 36))
              .tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT)));
        builder.addContainer(outputSlot = OutputInventorySlot.at(listener, 110, 36))
              .tracksWarnings(slot -> slot.warning(WarningType.NO_SPACE_IN_OUTPUT, getWarningCheck(NOT_ENOUGH_SPACE_ITEM_OUTPUT_ERROR)));
        builder.addContainer(containerFillSlot = FluidInventorySlot.drain(fluidTank, listener, 155, 25));
        builder.addContainer(containerOutputSlot = OutputInventorySlot.at(listener, 155, 56));
        builder.addContainer(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 155, 5));
        containerFillSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    public static boolean isValidInput(ItemResource itemType) {
        FoodProperties food = itemType.get(DataComponents.FOOD);
        //And only allow inserting foods that actually would provide paste
        return food != null && food.nutrition() > 0;
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        energySlot.fillContainerOrConvert(null);
        containerFillSlot.drainTankIntoSlot(containerOutputSlot, null);
        recipeCacheLookupMonitor.updateAndProcess(level.registryAccess());
        float pasteScale = MekanismUtils.getScale(lastPasteScale, fluidTank);
        if (MekanismUtils.scaleChanged(pasteScale, lastPasteScale)) {
            lastPasteScale = pasteScale;
            sendUpdatePacket = true;
        }
        ItemResource itemType = inputSlot.resource();
        if (!itemType.equals(lastPasteItem)) {
            lastPasteItem = itemType;
            sendUpdatePacket = true;
        }
        return sendUpdatePacket;
    }

    @Override
    public MekanismRecipeType<SingleRecipeInput, BasicItemStackToFluidOptionalItemRecipe, IInputRecipeCache> getRecipeType() {
        //TODO - V11: See comment in NutritionalLiquifierIRecipe. Note if either containsRecipe and findFirstRecipe get called this will throw
        throw new UnsupportedOperationException();
    }

    @Override
    public IRecipeViewerRecipeType<BasicItemStackToFluidOptionalItemRecipe> recipeViewerType() {
        return RecipeViewerRecipeType.NUTRITIONAL_LIQUIFICATION;
    }

    @Nullable
    @Override
    public BasicItemStackToFluidOptionalItemRecipe getRecipe(int cacheIndex) {
        return getRecipe(inputHandler.getInput());
    }

    @Nullable
    public static BasicItemStackToFluidOptionalItemRecipe getRecipe(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null || food.nutrition() <= 0) {
            //If the food provides no healing don't allow consuming it as it won't provide any paste
            return null;
        }
        UseRemainder remainder = stack.get(DataComponents.USE_REMAINDER);
        return new NutritionalLiquifierRecipe(
              IngredientCreatorAccess.item().from(stack, 1),
              MekanismFluids.NUTRITIONAL_PASTE.asTemplate(food.nutrition() * 50),
              remainder == null ? null : remainder.convertInto()
        );
    }

    @Override
    public CachedRecipe<BasicItemStackToFluidOptionalItemRecipe> createNewCachedRecipe(BasicItemStackToFluidOptionalItemRecipe recipe, int cacheIndex) {
        return new OneInputCachedRecipe<>(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
              .setErrorsChanged(this::onErrorsChanged)
              .setCanHolderFunction(this::canFunction)
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setOperatingTicksChanged(this::setOperatingTicks)
              .setBaselineMaxOperations(this::getOperationsPerTick);
    }

    public MachineEnergyContainer<TileEntityNutritionalLiquifier> energyContainer() {
        return energyContainer;
    }

    /// @apiNote Do not modify the returned stack.
    public ItemStack getRenderStack() {
        return lastPasteItem.toStack();
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        NBTUtils.storeNonEmpty(output, SerializationConstants.FLUID, fluidTank);
        if (!lastPasteItem.isEmpty()) {
            output.store(SerializationConstants.ITEM, ItemResource.CODEC, lastPasteItem);
        }
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);
        NBTUtils.readOrEmpty(input, SerializationConstants.FLUID, fluidTank);
        lastPasteItem = input.read(SerializationConstants.ITEM, ItemResource.CODEC).orElse(ItemResource.EMPTY);
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = ComputerConstants.DESCRIPTION_GET_ENERGY_USAGE)
    public long getEnergyUsage() {
        return getActive() ? energyContainer.getEnergyPerTick() : 0L;
    }
    //End methods IComputerTile
}
