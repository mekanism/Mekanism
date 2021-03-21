package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.PaintingCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
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
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.PigmentInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.RecipeLookupUtil;
import net.minecraft.item.ItemStack;

public class TileEntityPaintingMachine extends TileEntityProgressMachine<PaintingRecipe> {

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getPigmentInput", "getPigmentInputCapacity", "getPigmentInputNeeded",
                                                                                        "getPigmentInputFilledPercentage"})
    public IPigmentTank pigmentTank;

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;
    private final IInputHandler<@NonNull PigmentStack> pigmentInputHandler;

    private MachineEnergyContainer<TileEntityPaintingMachine> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputPigmentItem")
    private PigmentInventorySlot pigmentInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem")
    private InputInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutput")
    private OutputInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityPaintingMachine() {
        super(MekanismBlocks.PAINTING_MACHINE, 200);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.PIGMENT, TransmissionType.ENERGY);
        configComponent.setupItemIOExtraConfig(inputSlot, outputSlot, pigmentInputSlot, energySlot);
        configComponent.setupInputConfig(TransmissionType.PIGMENT, pigmentTank);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        itemInputHandler = InputHelper.getInputHandler(inputSlot);
        pigmentInputHandler = InputHelper.getInputHandler(pigmentTank);
        outputHandler = OutputHelper.getOutputHandler(outputSlot);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks() {
        ChemicalTankHelper<Pigment, PigmentStack, IPigmentTank> builder = ChemicalTankHelper.forSidePigmentWithConfig(this::getDirection, this::getConfig);
        builder.addTank(pigmentTank = ChemicalTankBuilder.PIGMENT.input(15_000, pigment -> {
            if (!inputSlot.isEmpty()) {
                ItemStack stack = inputSlot.getStack();
                return containsRecipe(recipe -> recipe.getItemInput().testType(stack) && recipe.getChemicalInput().testType(pigment));
            }
            //Otherwise return true, as we already validated the type was valid
            return true;
        }, pigment -> containsRecipe(recipe -> recipe.getChemicalInput().testType(pigment)), this));
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
        builder.addSlot(pigmentInputSlot = PigmentInventorySlot.fill(pigmentTank, this, 6, 56));
        builder.addSlot(inputSlot = InputInventorySlot.at(stack -> {
            if (!pigmentTank.isEmpty()) {
                return containsRecipe(recipe -> recipe.getChemicalInput().testType(pigmentTank.getType()) && recipe.getItemInput().testType(stack));
            }
            //Otherwise return true, as we already validated the type was valid
            return true;
        }, item -> containsRecipe(recipe -> recipe.getItemInput().testType(item)), this, 45, 35));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 116, 35));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, this, 144, 35));
        pigmentInputSlot.setSlotOverlay(SlotOverlay.MINUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        pigmentInputSlot.fillTankOrConvert();
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<PaintingRecipe> getRecipeType() {
        return MekanismRecipeType.PAINTING;
    }

    @Nullable
    @Override
    public PaintingRecipe getRecipe(int cacheIndex) {
        return RecipeLookupUtil.findItemStackChemicalRecipe(this, itemInputHandler, pigmentInputHandler);
    }

    @Nullable
    @Override
    public CachedRecipe<PaintingRecipe> createNewCachedRecipe(@Nonnull PaintingRecipe recipe, int cacheIndex) {
        return new PaintingCachedRecipe(recipe, itemInputHandler, pigmentInputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(() -> markDirty(false))
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    public MachineEnergyContainer<TileEntityPaintingMachine> getEnergyContainer() {
        return energyContainer;
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private FloatingLong getEnergyUsage() {
        return getActive() ? energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
    }
    //End methods IComputerTile
}