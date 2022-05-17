package mekanism.common.tile.machine;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NonNull;
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
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityNutritionalLiquifier extends TileEntityProgressMachine<ItemStackToFluidRecipe> {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    private static final int MAX_FLUID = 10_000;

    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"})
    public IExtendedFluidTank fluidTank;

    private final IOutputHandler<@NonNull FluidStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> inputHandler;

    private MachineEnergyContainer<TileEntityNutritionalLiquifier> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInput")
    private InputInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getContainerFillItem")
    private FluidInventorySlot containerFillSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem")
    private OutputInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    @Nullable
    private HashedItem lastPasteItem;
    private float lastPasteScale;

    public TileEntityNutritionalLiquifier(BlockPos pos, BlockState state) {
        super(MekanismBlocks.NUTRITIONAL_LIQUIFIER, pos, state, TRACKED_ERROR_TYPES, 100);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.ENERGY);
        configComponent.setupItemIOConfig(List.of(inputSlot, containerFillSlot), Collections.singletonList(outputSlot), energySlot, false);
        configComponent.setupOutputConfig(TransmissionType.FLUID, fluidTank, RelativeSide.RIGHT);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.FLUID);

        inputHandler = InputHelper.getInputHandler(inputSlot, RecipeError.NOT_ENOUGH_INPUT);
        outputHandler = OutputHelper.getOutputHandler(fluidTank, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
    }

    @Nonnull
    @Override
    public IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addTank(fluidTank = BasicFluidTank.output(MAX_FLUID, listener));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = InputInventorySlot.at(stack -> {
                  if (stack.getItem().isEdible()) {//Double-check the stack is food
                      FoodProperties food = stack.getFoodProperties(null);
                      //And only allow inserting foods that actually would provide paste
                      return food != null && food.getNutrition() > 0;
                  }
                  return false;
              }, recipeCacheListener, 26, 36)
        ).tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT)));
        builder.addSlot(containerFillSlot = FluidInventorySlot.drain(fluidTank, listener, 155, 25));
        builder.addSlot(outputSlot = OutputInventorySlot.at(listener, 155, 56));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 155, 5));
        containerFillSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        containerFillSlot.drainTank(outputSlot);
        recipeCacheLookupMonitor.updateAndProcess();
        boolean needsPacket = false;
        float pasteScale = MekanismUtils.getScale(lastPasteScale, fluidTank);
        if (pasteScale != lastPasteScale) {
            lastPasteScale = pasteScale;
            needsPacket = true;
        }
        if (inputSlot.isEmpty()) {
            if (lastPasteItem != null) {
                lastPasteItem = null;
                needsPacket = true;
            }
        } else {
            HashedItem item = HashedItem.raw(inputSlot.getStack());
            if (!item.equals(lastPasteItem)) {
                lastPasteItem = HashedItem.create(item.getStack());
                needsPacket = true;
            }
        }
        if (needsPacket) {
            sendUpdatePacket();
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackToFluidRecipe, IInputRecipeCache> getRecipeType() {
        //TODO - V11: See comment in NutritionalLiquifierIRecipe. Note if either containsRecipe and findFirstRecipe get called a null pointer will occur
        return null;
    }

    @Nullable
    @Override
    public ItemStackToFluidRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandler.getInput();
        if (stack.isEmpty() || !stack.getItem().isEdible()) {
            return null;
        }
        FoodProperties food = stack.getFoodProperties(null);
        if (food == null || food.getNutrition() == 0) {
            //If the food provides no healing don't allow consuming it as it won't provide any paste
            return null;
        }
        return new NutritionalLiquifierIRecipe(stack.getItem(), IngredientCreatorAccess.item().from(stack, 1),
              MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(food.getNutrition() * 50));
    }

    @Nonnull
    @Override
    public CachedRecipe<ItemStackToFluidRecipe> createNewCachedRecipe(@Nonnull ItemStackToFluidRecipe recipe, int cacheIndex) {
        return OneInputCachedRecipe.itemToFluid(recipe, recheckAllRecipeErrors, inputHandler, outputHandler)
              .setErrorsChanged(this::onErrorsChanged)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
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
        return lastPasteItem.getStack();
    }

    @Nonnull
    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        updateTag.put(NBTConstants.FLUID_STORED, fluidTank.serializeNBT());
        CompoundTag item = new CompoundTag();
        if (lastPasteItem != null) {
            NBTUtils.writeRegistryEntry(item, NBTConstants.ID, lastPasteItem.getStack().getItem());
            CompoundTag tag = lastPasteItem.getStack().getTag();
            if (tag != null) {
                item.put(NBTConstants.TAG, tag.copy());
            }
        }
        updateTag.put(NBTConstants.ITEM, item);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setCompoundIfPresent(tag, NBTConstants.FLUID_STORED, nbt -> fluidTank.deserializeNBT(nbt));
        NBTUtils.setCompoundIfPresent(tag, NBTConstants.ITEM, nbt -> {
            if (nbt.isEmpty()) {
                lastPasteItem = null;
            } else if (nbt.contains(NBTConstants.ID, Tag.TAG_STRING)) {
                ResourceLocation id = ResourceLocation.tryParse(nbt.getString(NBTConstants.ID));
                if (id != null) {
                    Item item = ForgeRegistries.ITEMS.getValue(id);
                    if (item != null && item != Items.AIR) {
                        ItemStack stack = new ItemStack(item);
                        if (nbt.contains(NBTConstants.TAG, Tag.TAG_COMPOUND)) {
                            stack.setTag(nbt.getCompound(NBTConstants.TAG));
                        }
                        //Use raw because we have a new stack, so we don't need to bother copying it
                        lastPasteItem = HashedItem.raw(stack);
                    }
                }
            }
        });
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private FloatingLong getEnergyUsage() {
        return getActive() ? energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
    }
    //End methods IComputerTile
}