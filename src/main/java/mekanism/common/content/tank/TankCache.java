package mekanism.common.content.tank;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.DataHandlerUtils;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.base.ContainerEditMode;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.multiblock.InventoryMultiblockCache;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class TankCache extends InventoryMultiblockCache<SynchronizedTankData> implements IMekanismFluidHandler {

    //Note: We don't care about any restrictions here as it is just for making it be persistent
    private final List<IExtendedFluidTank> fluidTanks = Collections.singletonList(BasicFluidTank.create(Integer.MAX_VALUE, this));

    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    @Override
    public void apply(SynchronizedTankData data) {
        data.setInventoryData(inventorySlots);
        data.setTankData(fluidTanks);
        data.editMode = editMode;
    }

    @Override
    public void sync(SynchronizedTankData data) {
        List<IInventorySlot> slotsToCopy = data.getInventorySlots(null);
        for (int i = 0; i < slotsToCopy.size(); i++) {
            if (i < inventorySlots.size()) {
                //Just directly set it as we don't have any restrictions on our slots here
                inventorySlots.get(i).setStack(slotsToCopy.get(i).getStack());
            }
        }
        List<IExtendedFluidTank> tanksToCopy = data.getFluidTanks(null);
        for (int i = 0; i < tanksToCopy.size(); i++) {
            if (i < fluidTanks.size()) {
                //Just directly set it as we don't have any restrictions on our tanks here
                fluidTanks.get(i).setStack(tanksToCopy.get(i).getFluid());
            }
        }
        editMode = data.editMode;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        editMode = ContainerEditMode.byIndexStatic(nbtTags.getInt("editMode"));
        DataHandlerUtils.readSlots(getInventorySlots(null), nbtTags.getList("Items", NBT.TAG_COMPOUND));
        DataHandlerUtils.readTanks(getFluidTanks(null), nbtTags.getList("FluidTanks", NBT.TAG_COMPOUND));
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        nbtTags.putInt("editMode", editMode.ordinal());
        nbtTags.put("Items", DataHandlerUtils.writeSlots(getInventorySlots(null)));
        nbtTags.put("FluidTanks", DataHandlerUtils.writeTanks(getFluidTanks(null)));
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }
}