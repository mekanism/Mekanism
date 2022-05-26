package mekanism.common.tile.machine;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.ChemicalChemicalToChemicalCachedRecipe;
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
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.chemical.PigmentInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IEitherSideRecipeLookupHandler.EitherSideChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.EitherSideChemical;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.PigmentSlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class TileEntityPigmentMixer extends TileEntityRecipeMachine<PigmentMixingRecipe> implements IBoundingBlock,
      EitherSideChemicalRecipeLookupHandler<Pigment, PigmentStack, PigmentMixingRecipe> {

    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE,
          RecipeError.NOT_ENOUGH_LEFT_INPUT,
          RecipeError.NOT_ENOUGH_RIGHT_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );

    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getLeftInput", "getLeftInputCapacity", "getLeftInputNeeded",
                                                                                        "getLeftInputFilledPercentage"})
    public IPigmentTank leftInputTank;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getRightInput", "getRightInputCapacity", "getRightInputNeeded",
                                                                                        "getRightInputFilledPercentage"})
    public IPigmentTank rightInputTank;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"})
    public IPigmentTank outputTank;

    private FloatingLong clientEnergyUsed = FloatingLong.ZERO;
    private int baselineMaxOperations = 1;

    private final IOutputHandler<@NonNull PigmentStack> outputHandler;
    private final IInputHandler<@NonNull PigmentStack> leftInputHandler;
    private final IInputHandler<@NonNull PigmentStack> rightInputHandler;

    private MachineEnergyContainer<TileEntityPigmentMixer> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getLeftInputItem")
    private PigmentInventorySlot leftInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem")
    private PigmentInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getRightInputItem")
    private PigmentInventorySlot rightInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityPigmentMixer(BlockPos pos, BlockState state) {
        super(MekanismBlocks.PIGMENT_MIXER, pos, state, TRACKED_ERROR_TYPES);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.PIGMENT, TransmissionType.ENERGY);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT_1, new InventorySlotInfo(true, true, leftInputSlot));
            itemConfig.addSlotInfo(DataType.INPUT_2, new InventorySlotInfo(true, true, rightInputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(true, true, outputSlot));
            itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, leftInputSlot, rightInputSlot, outputSlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, energySlot));
            //Set default config directions
            itemConfig.setDataType(DataType.INPUT_1, RelativeSide.LEFT);
            itemConfig.setDataType(DataType.INPUT_2, RelativeSide.RIGHT);
            itemConfig.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
            itemConfig.setDataType(DataType.ENERGY, RelativeSide.BACK);
        }

        ConfigInfo pigmentConfig = configComponent.getConfig(TransmissionType.PIGMENT);
        if (pigmentConfig != null) {
            pigmentConfig.addSlotInfo(DataType.INPUT_1, new PigmentSlotInfo(true, false, leftInputTank));
            pigmentConfig.addSlotInfo(DataType.INPUT_2, new PigmentSlotInfo(true, false, rightInputTank));
            pigmentConfig.addSlotInfo(DataType.OUTPUT, new PigmentSlotInfo(false, true, outputTank));
            pigmentConfig.addSlotInfo(DataType.INPUT_OUTPUT, new PigmentSlotInfo(true, true, leftInputTank, rightInputTank, outputTank));
            pigmentConfig.setDataType(DataType.INPUT_1, RelativeSide.LEFT);
            pigmentConfig.setDataType(DataType.INPUT_2, RelativeSide.RIGHT);
            pigmentConfig.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
            pigmentConfig.setEjecting(true);
        }

        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.PIGMENT)
              .setCanTankEject(tank -> tank == outputTank);

        leftInputHandler = InputHelper.getInputHandler(leftInputTank, RecipeError.NOT_ENOUGH_LEFT_INPUT);
        rightInputHandler = InputHelper.getInputHandler(rightInputTank, RecipeError.NOT_ENOUGH_RIGHT_INPUT);
        outputHandler = OutputHelper.getOutputHandler(outputTank, RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
        ChemicalTankHelper<Pigment, PigmentStack, IPigmentTank> builder = ChemicalTankHelper.forSidePigmentWithConfig(this::getDirection, this::getConfig);
        builder.addTank(leftInputTank = ChemicalTankBuilder.PIGMENT.input(1_000, pigment -> containsRecipe(pigment, rightInputTank.getStack()),
              this::containsRecipe, recipeCacheListener));
        builder.addTank(rightInputTank = ChemicalTankBuilder.PIGMENT.input(1_000, pigment -> containsRecipe(pigment, leftInputTank.getStack()),
              this::containsRecipe, recipeCacheListener));
        builder.addTank(outputTank = ChemicalTankBuilder.PIGMENT.output(2_000, listener));
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
        builder.addSlot(leftInputSlot = PigmentInventorySlot.fill(leftInputTank, listener, 6, 56));
        builder.addSlot(rightInputSlot = PigmentInventorySlot.fill(rightInputTank, listener, 154, 56));
        builder.addSlot(outputSlot = PigmentInventorySlot.drain(outputTank, listener, 80, 65));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 154, 14));
        leftInputSlot.setSlotType(ContainerSlotType.INPUT);
        leftInputSlot.setSlotOverlay(SlotOverlay.MINUS);
        rightInputSlot.setSlotType(ContainerSlotType.INPUT);
        rightInputSlot.setSlotOverlay(SlotOverlay.MINUS);
        outputSlot.setSlotType(ContainerSlotType.OUTPUT);
        outputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        leftInputSlot.fillTank();
        rightInputSlot.fillTank();
        outputSlot.drainTank();
        clientEnergyUsed = recipeCacheLookupMonitor.updateAndProcess(energyContainer);
    }

    @Nonnull
    @ComputerMethod(nameOverride = "getEnergyUsage")
    public FloatingLong getEnergyUsed() {
        return clientEnergyUsed;
    }

    @Nonnull
    @Override
    public IMekanismRecipeTypeProvider<PigmentMixingRecipe, EitherSideChemical<Pigment, PigmentStack, PigmentMixingRecipe>> getRecipeType() {
        return MekanismRecipeType.PIGMENT_MIXING;
    }

    @Nullable
    @Override
    public PigmentMixingRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(leftInputHandler, rightInputHandler);
    }

    @Nonnull
    @Override
    public CachedRecipe<PigmentMixingRecipe> createNewCachedRecipe(@Nonnull PigmentMixingRecipe recipe, int cacheIndex) {
        return new ChemicalChemicalToChemicalCachedRecipe<>(recipe, recheckAllRecipeErrors, leftInputHandler, rightInputHandler, outputHandler)
              .setErrorsChanged(this::onErrorsChanged)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setBaselineMaxOperations(() -> baselineMaxOperations)
              .setOnFinish(this::markForSave);
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            baselineMaxOperations = (int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
        }
    }

    @Nonnull
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 2, 1));
    }

    public MachineEnergyContainer<TileEntityPigmentMixer> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> clientEnergyUsed = value));
    }
}