package mekanism.common.tile.qio;

import java.util.Map;
import mekanism.api.NBTConstants;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityQIOImporter extends TileEntityQIOFilterHandler {

    private boolean importWithoutFilter;

    public TileEntityQIOImporter() {
        super(MekanismBlocks.QIO_IMPORTER);
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
    }

    public boolean getImportWithoutFilter() {
        return importWithoutFilter;
    }

    public void toggleImportWithoutFilter() {
        importWithoutFilter = !importWithoutFilter;
        markDirty(false);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableBoolean.create(this::getImportWithoutFilter, (value) -> importWithoutFilter = value));
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        super.writeSustainedData(itemStack);
        ItemDataUtils.setBoolean(itemStack, NBTConstants.AUTO, importWithoutFilter);
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        super.readSustainedData(itemStack);
        importWithoutFilter = ItemDataUtils.getBoolean(itemStack, NBTConstants.AUTO);
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = super.getTileDataRemap();
        remap.put(NBTConstants.AUTO, NBTConstants.AUTO);
        return remap;
    }

    @Override
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        super.getConfigurationData(nbtTags);
        nbtTags.putBoolean(NBTConstants.AUTO, importWithoutFilter);
        return nbtTags;
    }

    @Override
    public void setConfigurationData(CompoundNBT nbtTags) {
        super.setConfigurationData(nbtTags);
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.AUTO, (value) -> importWithoutFilter = value);
    }
}
