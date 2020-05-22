package mekanism.common.content.tank;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.BasicInfusionTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.BasicPigmentTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.MergedTank;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.capabilities.chemical.MultiblockInfusionTank;
import mekanism.common.capabilities.chemical.MultiblockPigmentTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.HybridInventorySlot;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.World;

public class TankMultiblockData extends MultiblockData {

    @ContainerSync
    public final MergedTank mergedTank;
    @ContainerSync
    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    private int tankCapacity;
    public float prevScale;

    public TankMultiblockData(TileEntityDynamicTank tile) {
        super(tile);
        mergedTank = MergedTank.create(
              MultiblockFluidTank.create(this, tile, this::getTankCapacity, BasicFluidTank.alwaysTrue),
              MultiblockGasTank.create(this, tile, this::getTankCapacity, BasicGasTank.alwaysTrue),
              MultiblockInfusionTank.create(this, tile, this::getTankCapacity, BasicInfusionTank.alwaysTrue),
              MultiblockPigmentTank.create(this, tile, this::getTankCapacity, BasicPigmentTank.alwaysTrue)
        );
        fluidTanks.add(mergedTank.getFluidTank());
        gasTanks.add(mergedTank.getGasTank());
        infusionTanks.add(mergedTank.getInfusionTank());
        pigmentTanks.add(mergedTank.getPigmentTank());
        inventorySlots.addAll(createBaseInventorySlots());
    }

    private List<IInventorySlot> createBaseInventorySlots() {
        List<IInventorySlot> inventorySlots = new ArrayList<>();
        HybridInventorySlot input, output;
        inventorySlots.add(input = HybridInventorySlot.inputOrDrain(mergedTank, this, 146, 21));
        inventorySlots.add(output = HybridInventorySlot.outputOrFill(mergedTank, this, 146, 51));
        input.setSlotType(ContainerSlotType.INPUT);
        output.setSlotType(ContainerSlotType.OUTPUT);
        return inventorySlots;
    }

    @Override
    public boolean tick(World world) {
        boolean needsPacket = super.tick(world);
        //TODO: No magic numbers??
        HybridInventorySlot inputSlot = (HybridInventorySlot) inventorySlots.get(0);
        HybridInventorySlot outputSlot = (HybridInventorySlot) inventorySlots.get(1);
        inputSlot.handleTank(outputSlot, editMode);
        inputSlot.drainChemicalTank();
        outputSlot.fillChemicalTank();
        //TODO: FIXME, make this easier to extend/grow
        float scale = Math.max(MekanismUtils.getScale(prevScale, getFluidTank()), MekanismUtils.getScale(prevScale, getGasTank()));
        if (scale != prevScale) {
            needsPacket = true;
            prevScale = scale;
        }
        return needsPacket;
    }

    public int getTankCapacity() {
        return tankCapacity;
    }

    @Override
    public void setVolume(int volume) {
        super.setVolume(volume);
        tankCapacity = getVolume() * TankUpdateProtocol.FLUID_PER_TANK;
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        //TODO: FIXME So that if fluid is null it checks gas, etc
        return MekanismUtils.redstoneLevelFromContents(getFluidTank().getFluidAmount(), getTankCapacity());
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

    public boolean isEmpty() {
        return getFluidTank().isEmpty() && getGasTank().isEmpty() && getInfusionTank().isEmpty() && getPigmentTank().isEmpty();
    }
}