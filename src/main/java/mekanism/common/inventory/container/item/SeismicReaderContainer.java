package mekanism.common.inventory.container.item;

import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

public class SeismicReaderContainer extends MekanismItemContainer implements IEmptyContainer {

    public SeismicReaderContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack) {
        super(MekanismContainerTypes.SEISMIC_READER, id, inv, hand, stack);
    }

    public SeismicReaderContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, buf.readEnumValue(Hand.class), getStackFromBuffer(buf, ItemSeismicReader.class));
    }
}