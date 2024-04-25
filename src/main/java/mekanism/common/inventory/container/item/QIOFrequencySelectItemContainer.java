package mekanism.common.inventory.container.item;

import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class QIOFrequencySelectItemContainer extends FrequencyItemContainer<QIOFrequency> implements IEmptyContainer {

    public QIOFrequencySelectItemContainer(int id, Inventory inv, InteractionHand hand, ItemStack stack) {
        super(MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM, id, inv, hand, stack);
    }

    @Override
    protected FrequencyType<QIOFrequency> getFrequencyType() {
        return FrequencyType.QIO;
    }
}
