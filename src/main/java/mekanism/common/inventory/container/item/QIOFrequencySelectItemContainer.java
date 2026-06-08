package mekanism.common.inventory.container.item;

import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class QIOFrequencySelectItemContainer extends FrequencyItemContainer<QIOFrequency> implements IEmptyContainer {

    public QIOFrequencySelectItemContainer(int id, Inventory inv, InteractionHand hand, ItemAccess itemAccess) {
        super(MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM, id, inv, hand, itemAccess);
    }

    @Override
    protected FrequencyType<QIOFrequency> getFrequencyType() {
        return FrequencyTypes.QIO;
    }

    @Override
    protected boolean isValidType(ItemResource itemType) {
        return super.isValidType(itemType) && MekanismItems.PORTABLE_QIO_DASHBOARD.is(itemType);
    }
}
