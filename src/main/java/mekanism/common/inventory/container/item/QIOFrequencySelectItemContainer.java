package mekanism.common.inventory.container.item;

import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class QIOFrequencySelectItemContainer extends FrequencyItemContainer<QIOFrequency> implements IEmptyContainer {

    public QIOFrequencySelectItemContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack) {
        super(MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM, id, inv, hand, stack);
    }

    @Override
    public FrequencyType<QIOFrequency> getFrequencyType() {
        return FrequencyType.QIO;
    }
}
