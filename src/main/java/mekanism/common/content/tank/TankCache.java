package mekanism.common.content.tank;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.util.NBTUtils;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class TankCache extends MultiblockCache<TankMultiblockData> {

    private ContainerEditMode editMode = ContainerEditMode.BOTH;

    @Override
    public void merge(MultiblockCache<TankMultiblockData> mergeCache, RejectContents rejectContents) {
        super.merge(mergeCache, rejectContents);
        editMode = ((TankCache) mergeCache).editMode;
    }

    @Override
    public void apply(TankMultiblockData data, TransactionContext transaction) {
        super.apply(data, transaction);
        data.editMode = editMode;
    }

    @Override
    public void sync(TankMultiblockData data, TransactionContext transaction) {
        super.sync(data, transaction);
        editMode = data.editMode;
    }

    @Override
    public void load(ValueInput input) {
        super.load(input);
        NBTUtils.setEnumIfPresent(input, SerializationConstants.EDIT_MODE, ContainerEditMode.BY_ID, mode -> editMode = mode);
    }

    @Override
    public void save(ValueOutput output) {
        super.save(output);
        NBTUtils.writeEnum(output, SerializationConstants.EDIT_MODE, editMode);
    }
}