package mekanism.common.inventory.container.item;

import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

public class PortableTeleporterContainer extends MekanismItemContainer implements IEmptyContainer {

    public PortableTeleporterContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack) {
        super(MekanismContainerTypes.PORTABLE_TELEPORTER, id, inv, hand, stack);
    }

    public PortableTeleporterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, buf.readEnumValue(Hand.class), getStackFromBuffer(buf, ItemPortableTeleporter.class));
    }

    public Hand getHand() {
        return hand;
    }

    public ItemStack getStack() {
        return stack;
    }
}