package mekanism.common.content.tank;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class TankCache extends MultiblockCache<TankMultiblockData> {

    private ContainerEditMode editMode = ContainerEditMode.BOTH;

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
    public void load(CompoundTag nbtTags) {
        super.load(nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.EDIT_MODE, ContainerEditMode::byIndexStatic, mode -> editMode = mode);
    }

    @Override
    public void save(CompoundTag nbtTags) {
        super.save(nbtTags);
        NBTUtils.writeEnum(nbtTags, NBTConstants.EDIT_MODE, editMode);
    }
}