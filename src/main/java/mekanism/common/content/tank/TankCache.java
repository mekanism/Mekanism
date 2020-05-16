package mekanism.common.content.tank;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class TankCache extends MultiblockCache<TankMultiblockData> {

    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    @Override
    public void merge(MultiblockCache<TankMultiblockData> mergeCache, List<ItemStack> rejectedItems) {
        super.merge(mergeCache, rejectedItems);
        editMode = ((TankCache) mergeCache).editMode;
    }

    @Override
    public void apply(TankMultiblockData data) {
        super.apply(data);
        data.editMode = editMode;
    }

    @Override
    public void sync(TankMultiblockData data) {
        super.sync(data);
        editMode = data.editMode;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        super.load(nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.EDIT_MODE, ContainerEditMode::byIndexStatic, mode -> editMode = mode);
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        super.save(nbtTags);
        nbtTags.putInt(NBTConstants.EDIT_MODE, editMode.ordinal());
    }
}