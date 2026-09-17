package mekanism.common.content.tank;

import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
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
        input.read(SerializationConstants.EDIT_MODE, ContainerEditMode.CODEC).ifPresent(mode -> editMode = mode);
    }

    @Override
    public void save(ValueOutput output) {
        super.save(output);
        output.store(SerializationConstants.EDIT_MODE, ContainerEditMode.CODEC, editMode);
    }
}