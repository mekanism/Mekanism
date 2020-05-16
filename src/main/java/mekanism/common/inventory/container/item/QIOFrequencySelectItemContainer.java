package mekanism.common.inventory.container.item;

import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

public class QIOFrequencySelectItemContainer extends FrequencyItemContainer<QIOFrequency> implements IEmptyContainer {

    public QIOFrequencySelectItemContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack) {
        super(MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM, id, inv, hand, stack);
    }

    public QIOFrequencySelectItemContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, buf.readEnumValue(Hand.class), getStackFromBuffer(buf, Item.class));
    }

    @Override
    public FrequencyType<QIOFrequency> getFrequencyType() {
        return FrequencyType.QIO;
    }

    public Hand getHand() {
        return hand;
    }
}
