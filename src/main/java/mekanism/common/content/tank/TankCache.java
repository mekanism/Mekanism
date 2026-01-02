package mekanism.common.content.tank;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class TankCache extends MultiblockCache<TankMultiblockData> {

    private ContainerEditMode editMode = ContainerEditMode.BOTH;

    @Override
    public void merge(MultiblockCache<TankMultiblockData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        editMode = ((TankCache) mergeCache).editMode;
    }

    @Override
    public void apply(HolderLookup.Provider provider, TankMultiblockData data) {
        super.apply(provider, data);
        data.editMode = editMode;
    }

    @Override
    public void sync(TankMultiblockData data) {
        super.sync(data);
        editMode = data.editMode;
    }

    @Override
    public void load(@NotNull ValueInput input) {
        super.load(input);
        NBTUtils.setEnumIfPresent(input, SerializationConstants.EDIT_MODE, ContainerEditMode.BY_ID, mode -> editMode = mode);
    }

    @Override
    public void save(@NotNull ValueOutput output) {
        super.save(output);
        NBTUtils.writeEnum(output, SerializationConstants.EDIT_MODE, editMode);
    }
}