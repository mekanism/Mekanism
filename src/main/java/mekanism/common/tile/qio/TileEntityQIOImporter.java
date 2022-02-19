package mekanism.common.tile.qio;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mekanism.api.NBTConstants;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityQIOImporter extends TileEntityQIOFilterHandler {

    private static final int MAX_DELAY = 10;
    private int delay = 0;
    private boolean importWithoutFilter = true;

    public TileEntityQIOImporter() {
        super(MekanismBlocks.QIO_IMPORTER);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (MekanismUtils.canFunction(this)) {
            if (delay > 0) {
                delay--;
                return;
            }
            tryImport();
            delay = MAX_DELAY;
        }
    }

    private void tryImport() {
        QIOFrequency freq = getQIOFrequency();
        Direction direction = getDirection();
        TileEntity back = WorldUtils.getTileEntity(getLevel(), worldPosition.relative(direction.getOpposite()));
        if (freq == null || !InventoryUtils.isItemHandler(back, direction)) {
            return;
        }
        if (!importWithoutFilter && getFilters().isEmpty()) {
            return;
        }
        Optional<IItemHandler> capability = CapabilityUtils.getCapability(back, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).resolve();
        if (!capability.isPresent()) {
            return;
        }
        IItemHandler inventory = capability.get();
        Set<HashedItem> typesAdded = new HashSet<>();
        int maxTypes = getMaxTransitTypes(), maxCount = getMaxTransitCount(), countAdded = 0;

        for (int i = inventory.getSlots() - 1; i >= 0; i--) {
            ItemStack stack = inventory.extractItem(i, maxCount - countAdded, true);
            if (stack.isEmpty()) {
                continue;
            }
            HashedItem type = HashedItem.create(stack);
            // if we don't have room for another item type, skip
            if (!typesAdded.contains(type) && typesAdded.size() == maxTypes) {
                continue;
            }
            // if we can't filter this item type, skip
            if (!canFilter(stack)) {
                continue;
            }
            ItemStack used = TransporterManager.getToUse(stack, freq.addItem(stack));
            ItemStack ret = inventory.extractItem(i, used.getCount(), false);
            if (!InventoryUtils.areItemsStackable(used, ret) || used.getCount() != ret.getCount()) {
                Mekanism.logger.error("QIO insertion error: item handler {} returned {} during simulated extraction, "
                                      + "but returned {} during execution. This is wrong!", back, stack, ret);
            }
            typesAdded.add(type);
            countAdded += used.getCount();
        }
    }

    private boolean canFilter(ItemStack stack) {
        // quickly return true if we don't have any filters installed, and we allow for filterless importing
        if (importWithoutFilter && getFilters().isEmpty()) {
            return true;
        }
        for (QIOFilter<?> filter : getFilters()) {
            if (filter.getFinder().modifies(stack)) {
                return true;
            }
        }
        return false;
    }

    @ComputerMethod
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
        container.track(SyncableBoolean.create(this::getImportWithoutFilter, value -> importWithoutFilter = value));
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
    protected void addGeneralPersistentData(CompoundNBT data) {
        super.addGeneralPersistentData(data);
        data.putBoolean(NBTConstants.AUTO, importWithoutFilter);
    }

    @Override
    protected void loadGeneralPersistentData(CompoundNBT data) {
        super.loadGeneralPersistentData(data);
        NBTUtils.setBooleanIfPresent(data, NBTConstants.AUTO, value -> importWithoutFilter = value);
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private void setImportsWithoutFilter(boolean value) throws ComputerException {
        validateSecurityIsPublic();
        if (importWithoutFilter != value) {
            toggleImportWithoutFilter();
        }
    }
    //End methods IComputerTile
}