package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.GasToGasCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.RainType;

public class TileEntitySolarNeutronActivator extends TileEntityMekanism implements IBoundingBlock, ITankManager, ITileCachedRecipeHolder<GasToGasRecipe> {

    public static final int MAX_GAS = 10_000;

    public BasicGasTank inputTank;
    public BasicGasTank outputTank;

    public int gasOutput = 256;

    private CachedRecipe<GasToGasRecipe> cachedRecipe;

    private boolean settingsChecked;
    private boolean needsRainCheck;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull GasStack> inputHandler;

    private GasInventorySlot inputSlot;
    private GasInventorySlot outputSlot;

    public TileEntitySolarNeutronActivator() {
        super(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR);
        inputHandler = InputHelper.getInputHandler(inputTank);
        outputHandler = OutputHelper.getOutputHandler(outputTank);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack> builder = ChemicalTankHelper.forSideGas(this::getDirection);
        builder.addTank(inputTank = BasicGasTank.input(MAX_GAS, gas -> containsRecipe(recipe -> recipe.getInput().testType(gas)), this), RelativeSide.BOTTOM);
        builder.addTank(outputTank = BasicGasTank.output(MAX_GAS, this), RelativeSide.FRONT);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(inputSlot = GasInventorySlot.fill(inputTank, this, 5, 56), RelativeSide.BOTTOM, RelativeSide.TOP, RelativeSide.RIGHT,
              RelativeSide.LEFT, RelativeSide.BACK);
        builder.addSlot(outputSlot = GasInventorySlot.drain(outputTank, this, 155, 56), RelativeSide.FRONT);
        inputSlot.setSlotType(ContainerSlotType.INPUT);
        inputSlot.setSlotOverlay(SlotOverlay.MINUS);
        outputSlot.setSlotType(ContainerSlotType.OUTPUT);
        outputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    protected void recheckSettings() {
        Biome b = world.getBiomeManager().getBiome(getPos());
        needsRainCheck = b.getPrecipitation() != RainType.NONE;
        settingsChecked = true;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (!settingsChecked) {
            recheckSettings();
        }
        inputSlot.fillTank();
        outputSlot.drainTank();
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
        GasUtils.emitGas(this, outputTank, gasOutput, getDirection());
    }

    @Nonnull
    @Override
    public MekanismRecipeType<GasToGasRecipe> getRecipeType() {
        return MekanismRecipeType.ACTIVATING;
    }

    @Nullable
    @Override
    public CachedRecipe<GasToGasRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public GasToGasRecipe getRecipe(int cacheIndex) {
        GasStack gas = inputHandler.getInput();
        if (gas.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(gas));
    }

    private boolean canFunction(BlockPos positionAbove) {
        // TODO: Ideally the neutron activator should use the sky brightness to determine throughput; but
        // changing this would dramatically affect a lot of setups with Fusion reactors which can take
        // a long time to relight. I don't want to be chased by a mob right now, so just doing basic
        // rain checks.
        boolean seesSun = world.isDaytime() && world.canBlockSeeSky(positionAbove) && !world.getDimension().isNether();
        if (needsRainCheck) {
            seesSun &= !(world.isRaining() || world.isThundering());
        }
        return seesSun && MekanismUtils.canFunction(this);
    }

    @Nullable
    @Override
    public CachedRecipe<GasToGasRecipe> createNewCachedRecipe(@Nonnull GasToGasRecipe recipe, int cacheIndex) {
        BlockPos positionAbove = getPos().up();
        return new GasToGasCachedRecipe(recipe, inputHandler, outputHandler)
              .setCanHolderFunction(() -> canFunction(positionAbove))
              .setActive(this::setActive)
              .setOnFinish(this::markDirty)
              .setPostProcessOperations(currentMax -> {
                  if (currentMax <= 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return currentMax;
                  }
                  return Math.min((int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), currentMax);
              });
    }

    @Override
    public void onPlace() {
        MekanismUtils.makeBoundingBlock(getWorld(), getPos().up(), getPos());
    }

    @Override
    public void onBreak() {
        World world = getWorld();
        if (world != null) {
            world.removeBlock(getPos().up(), false);
            world.removeBlock(getPos(), false);
        }
    }

    @Override
    public Object[] getManagedTanks() {
        return new Object[]{inputTank, outputTank};
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos, pos.add(1, 2, 1));
    }

    public double getProgress() {
        if (getActive()) {
            return .16 * (1 + (world.getDayTime() % 6));
        }
        return 0;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getStored(), inputTank.getCapacity());
    }
}