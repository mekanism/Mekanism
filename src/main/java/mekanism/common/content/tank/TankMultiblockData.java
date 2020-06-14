package mekanism.common.content.tank;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.HybridInventorySlot;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

public class TankMultiblockData extends MultiblockData implements IValveHandler {

    public static final int FLUID_PER_TANK = 64_000;

    @ContainerSync
    public final MergedTank mergedTank;
    @ContainerSync
    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    private HybridInventorySlot inputSlot, outputSlot;
    private int tankCapacity;
    public float prevScale;

    public TankMultiblockData(TileEntityDynamicTank tile) {
        super(tile);
        mergedTank = MergedTank.create(
              MultiblockFluidTank.create(this, tile, this::getTankCapacity, BasicFluidTank.alwaysTrue),
              MultiblockChemicalTankBuilder.GAS.create(this, tile, this::getTankCapacity, ChemicalTankBuilder.GAS.alwaysTrue),
              MultiblockChemicalTankBuilder.INFUSION.create(this, tile, this::getTankCapacity, ChemicalTankBuilder.INFUSION.alwaysTrue),
              MultiblockChemicalTankBuilder.PIGMENT.create(this, tile, this::getTankCapacity, ChemicalTankBuilder.PIGMENT.alwaysTrue),
              MultiblockChemicalTankBuilder.SLURRY.create(this, tile, this::getTankCapacity, ChemicalTankBuilder.SLURRY.alwaysTrue)
        );
        fluidTanks.add(mergedTank.getFluidTank());
        gasTanks.add(mergedTank.getGasTank());
        infusionTanks.add(mergedTank.getInfusionTank());
        pigmentTanks.add(mergedTank.getPigmentTank());
        slurryTanks.add(mergedTank.getSlurryTank());
        inventorySlots.addAll(createBaseInventorySlots());
    }

    private List<IInventorySlot> createBaseInventorySlots() {
        List<IInventorySlot> inventorySlots = new ArrayList<>();
        inventorySlots.add(inputSlot = HybridInventorySlot.inputOrDrain(mergedTank, this, 146, 21));
        inventorySlots.add(outputSlot = HybridInventorySlot.outputOrFill(mergedTank, this, 146, 51));
        inputSlot.setSlotType(ContainerSlotType.INPUT);
        outputSlot.setSlotType(ContainerSlotType.OUTPUT);
        return inventorySlots;
    }

    @Override
    public boolean tick(World world) {
        boolean needsPacket = super.tick(world);
        CurrentType type = mergedTank.getCurrentType();
        if (type == CurrentType.EMPTY) {
            inputSlot.handleTank(outputSlot, editMode);
            inputSlot.drainChemicalTanks();
            outputSlot.fillChemicalTanks();
        } else if (type == CurrentType.FLUID) {
            inputSlot.handleTank(outputSlot, editMode);
        } else { //Chemicals
            inputSlot.drainChemicalTank(type);
            outputSlot.fillChemicalTank(type);
        }
        float scale = getScale();
        if (scale != prevScale) {
            prevScale = scale;
            needsPacket = true;
        }
        return needsPacket;
    }

    @Override
    public void readUpdateTag(CompoundNBT tag) {
        super.readUpdateTag(tag);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevScale = scale);
        mergedTank.readFromUpdateTag(tag);
        readValves(tag);
    }

    @Override
    public void writeUpdateTag(CompoundNBT tag) {
        super.writeUpdateTag(tag);
        tag.putFloat(NBTConstants.SCALE, prevScale);
        mergedTank.addToUpdateTag(tag);
        writeValves(tag);
    }

    private float getScale() {
        switch (mergedTank.getCurrentType()) {
            case FLUID:
                return MekanismUtils.getScale(prevScale, getFluidTank());
            case GAS:
                return MekanismUtils.getScale(prevScale, getGasTank());
            case INFUSION:
                return MekanismUtils.getScale(prevScale, getInfusionTank());
            case PIGMENT:
                return MekanismUtils.getScale(prevScale, getPigmentTank());
            case SLURRY:
                return MekanismUtils.getScale(prevScale, getSlurryTank());
        }
        return MekanismUtils.getScale(prevScale, 0, getTankCapacity(), true);
    }

    public int getTankCapacity() {
        return tankCapacity;
    }

    @Override
    public void setVolume(int volume) {
        super.setVolume(volume);
        tankCapacity = getVolume() * FLUID_PER_TANK;
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(getStoredAmount(), getTankCapacity());
    }

    private long getStoredAmount() {
        switch (mergedTank.getCurrentType()) {
            case FLUID:
                return getFluidTank().getFluidAmount();
            case GAS:
                return getGasTank().getStored();
            case INFUSION:
                return getInfusionTank().getStored();
            case PIGMENT:
                return getPigmentTank().getStored();
            case SLURRY:
                return getSlurryTank().getStored();
        }
        return 0;
    }

    public IExtendedFluidTank getFluidTank() {
        return mergedTank.getFluidTank();
    }

    public IGasTank getGasTank() {
        return mergedTank.getGasTank();
    }

    public IInfusionTank getInfusionTank() {
        return mergedTank.getInfusionTank();
    }

    public IPigmentTank getPigmentTank() {
        return mergedTank.getPigmentTank();
    }

    public ISlurryTank getSlurryTank() {
        return mergedTank.getSlurryTank();
    }

    public boolean isEmpty() {
        return mergedTank.getCurrentType() == CurrentType.EMPTY;
    }
}