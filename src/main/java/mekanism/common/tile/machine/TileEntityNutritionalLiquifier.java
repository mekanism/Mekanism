package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.chemical.ItemStackToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NutritionalLiquifierIRecipe;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityNutritionalLiquifier extends TileEntityProgressMachine<ItemStackToGasRecipe> {

    public static final int MAX_GAS = 10_000;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"})
    public IGasTank gasTank;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> inputHandler;

    private MachineEnergyContainer<TileEntityNutritionalLiquifier> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInput")
    private InputInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem")
    private GasInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    @Nullable
    private HashedItem lastPasteItem;
    private float lastPasteScale;

    public TileEntityNutritionalLiquifier() {
        super(MekanismBlocks.NUTRITIONAL_LIQUIFIER, 100);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.ENERGY);
        configComponent.setupItemIOConfig(inputSlot, outputSlot, energySlot);
        configComponent.setupOutputConfig(TransmissionType.GAS, gasTank, RelativeSide.RIGHT);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.GAS);

        inputHandler = InputHelper.getInputHandler(inputSlot);
        outputHandler = OutputHelper.getOutputHandler(gasTank);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(gasTank = ChemicalTankBuilder.GAS.output(MAX_GAS, this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = InputInventorySlot.at(stack -> {
            Item item = stack.getItem();
            if (item.isEdible()) {//Double-check the stack is food
                Food food = item.getFoodProperties();
                //And only allow inserting foods that actually would provide paste
                return food != null && food.getNutrition() > 0;
            }
            return false;
        }, recipeCacheLookupMonitor, 26, 36));
        builder.addSlot(outputSlot = GasInventorySlot.drain(gasTank, this, 155, 25));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, this, 155, 5));
        outputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        outputSlot.drainTank();
        recipeCacheLookupMonitor.updateAndProcess();
        boolean needsPacket = false;
        float pasteScale = MekanismUtils.getScale(lastPasteScale, gasTank);
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
    public MekanismRecipeType<ItemStackToGasRecipe, IInputRecipeCache> getRecipeType() {
        //TODO - V11: See comment in NutritionalLiquifierIRecipe. Note if either containsRecipe and findFirstRecipe get called a null pointer will occur
        return null;
    }

    @Nullable
    @Override
    public ItemStackToGasRecipe getRecipe(int cacheIndex) {
        ItemStack stack = inputHandler.getInput();
        if (stack.isEmpty() || !stack.getItem().isEdible()) {
            return null;
        }
        Food food = stack.getItem().getFoodProperties();
        if (food == null || food.getNutrition() == 0) {
            //If the food provides no healing don't allow consuming it as it won't provide any paste
            return null;
        }
        //TODO: If food eventually becomes stack sensitive make this use stack instead of stack.getItem as the ingredient
        return new NutritionalLiquifierIRecipe(stack.getItem(), ItemStackIngredient.from(stack.getItem()),
              MekanismGases.NUTRITIONAL_PASTE.getStack(food.getNutrition() * 50L));
    }

    @Nonnull
    @Override
    public CachedRecipe<ItemStackToGasRecipe> createNewCachedRecipe(@Nonnull ItemStackToGasRecipe recipe, int cacheIndex) {
        return new ItemStackToChemicalCachedRecipe<>(recipe, inputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(() -> markDirty(false))
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
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.put(NBTConstants.GAS_STORED, gasTank.serializeNBT());
        CompoundNBT item = new CompoundNBT();
        if (lastPasteItem != null) {
            item.putString(NBTConstants.ID, lastPasteItem.getStack().getItem().getRegistryName().toString());
            CompoundNBT tag = lastPasteItem.getStack().getTag();
            if (tag != null) {
                item.put(NBTConstants.TAG, tag.copy());
            }
        }
        updateTag.put(NBTConstants.ITEM, item);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        NBTUtils.setCompoundIfPresent(tag, NBTConstants.GAS_STORED, nbt -> gasTank.deserializeNBT(nbt));
        NBTUtils.setCompoundIfPresent(tag, NBTConstants.ITEM, nbt -> {
            if (nbt.isEmpty()) {
                lastPasteItem = null;
            } else if (nbt.contains(NBTConstants.ID, NBT.TAG_STRING)) {
                ResourceLocation id = ResourceLocation.tryParse(nbt.getString(NBTConstants.ID));
                if (id != null) {
                    Item item = ForgeRegistries.ITEMS.getValue(id);
                    if (item != null && item != Items.AIR) {
                        ItemStack stack = new ItemStack(item);
                        if (nbt.contains(NBTConstants.TAG, NBT.TAG_COMPOUND)) {
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