package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.BasicGasTank;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ChemicalInfuserCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.base.IChemicalTankHolder;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.holder.ChemicalTankHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityChemicalInfuser extends TileEntityMekanism implements IGasHandler, ISustainedData, ITankManager, ITileCachedRecipeHolder<ChemicalInfuserRecipe> {

    public static final int MAX_GAS = 10_000;
    public BasicGasTank leftTank;
    public BasicGasTank rightTank;
    public BasicGasTank centerTank;
    public int gasOutput = 256;

    public CachedRecipe<ChemicalInfuserRecipe> cachedRecipe;

    public double clientEnergyUsed;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull GasStack> leftInputHandler;
    private final IInputHandler<@NonNull GasStack> rightInputHandler;

    private GasInventorySlot leftInputSlot;
    private GasInventorySlot outputSlot;
    private GasInventorySlot rightInputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityChemicalInfuser() {
        super(MekanismBlocks.CHEMICAL_INFUSER);
        leftInputHandler = InputHelper.getInputHandler(leftTank);
        rightInputHandler = InputHelper.getInputHandler(rightTank);
        outputHandler = OutputHelper.getOutputHandler(centerTank);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack> builder = ChemicalTankHelper.forSideGas(this::getDirection);
        builder.addTank(leftTank = BasicGasTank.input(MAX_GAS, this::isValidGas, this), RelativeSide.LEFT);
        builder.addTank(rightTank = BasicGasTank.input(MAX_GAS, this::isValidGas, this), RelativeSide.RIGHT);
        builder.addTank(centerTank = BasicGasTank.output(MAX_GAS, this), RelativeSide.FRONT);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        //TODO: Should our gas checking, also check the other tank's contents so we don't let putting the same gas in on both sides
        builder.addSlot(leftInputSlot = GasInventorySlot.fill(leftTank, this::isValidGas, this, 5, 56), RelativeSide.LEFT);
        builder.addSlot(rightInputSlot = GasInventorySlot.fill(rightTank, this::isValidGas, this, 155, 56), RelativeSide.RIGHT);
        builder.addSlot(outputSlot = GasInventorySlot.drain(centerTank, this, 80, 65), RelativeSide.FRONT);
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 155, 5), RelativeSide.BOTTOM, RelativeSide.TOP);
        leftInputSlot.setSlotType(ContainerSlotType.INPUT);
        leftInputSlot.setSlotOverlay(SlotOverlay.MINUS);
        rightInputSlot.setSlotType(ContainerSlotType.INPUT);
        rightInputSlot.setSlotOverlay(SlotOverlay.MINUS);
        outputSlot.setSlotType(ContainerSlotType.OUTPUT);
        outputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    public boolean isValidGas(@Nonnull Gas gas) {
        return containsRecipe(recipe -> recipe.getLeftInput().testType(gas) || recipe.getRightInput().testType(gas));
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
            leftInputSlot.fillTank();
            rightInputSlot.fillTank();
            outputSlot.drainTank();
            double prev = getEnergy();
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
            //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
            clientEnergyUsed = prev - getEnergy();
            TileUtils.emitGas(this, centerTank, gasOutput, getDirection());
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ChemicalInfuserRecipe> getRecipeType() {
        return MekanismRecipeType.CHEMICAL_INFUSING;
    }

    @Nullable
    @Override
    public CachedRecipe<ChemicalInfuserRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ChemicalInfuserRecipe getRecipe(int cacheIndex) {
        GasStack leftGas = leftInputHandler.getInput();
        if (leftGas.isEmpty()) {
            return null;
        }
        GasStack rightGas = rightInputHandler.getInput();
        if (rightGas.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(leftGas, rightGas));
    }

    @Nullable
    @Override
    public CachedRecipe<ChemicalInfuserRecipe> createNewCachedRecipe(@Nonnull ChemicalInfuserRecipe recipe, int cacheIndex) {
        return new ChemicalInfuserCachedRecipe(recipe, leftInputHandler, rightInputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
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
    public void writeSustainedData(ItemStack itemStack) {
        if (!leftTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "leftTank", leftTank.getStack().write(new CompoundNBT()));
        }
        if (!rightTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "rightTank", rightTank.getStack().write(new CompoundNBT()));
        }
        if (!centerTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "centerTank", centerTank.getStack().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        leftTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "leftTank")));
        rightTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "rightTank")));
        centerTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "centerTank")));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put("leftTank.stored", "leftTank");
        remap.put("rightTank.stored", "rightTank");
        remap.put("centerTank.stored", "centerTank");
        return remap;
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{leftTank, rightTank, centerTank};
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(() -> clientEnergyUsed, value -> clientEnergyUsed = value));
    }
}