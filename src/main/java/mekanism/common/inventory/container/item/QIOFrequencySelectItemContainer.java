package mekanism.common.inventory.container.item;

import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

public class QIOFrequencySelectItemContainer extends FrequencyItemContainer<QIOFrequency> {

    public QIOFrequencySelectItemContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack) {
        super(MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM, id, inv, hand, stack, FrequencyType.QIO);
    }

    public QIOFrequencySelectItemContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, buf.readEnumValue(Hand.class), getStackFromBuffer(buf, ItemPortableTeleporter.class));
    }

    public Hand getHand() {
        return hand;
    }
}
