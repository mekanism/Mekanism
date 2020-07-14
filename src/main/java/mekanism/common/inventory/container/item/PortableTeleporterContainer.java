package mekanism.common.inventory.container.item;

import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketPortableTeleporterGui;
import mekanism.common.network.PacketPortableTeleporterGui.PortableTeleporterPacketType;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

public class PortableTeleporterContainer extends FrequencyItemContainer<TeleporterFrequency> implements IEmptyContainer {

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

    @Override
    public FrequencyType<TeleporterFrequency> getFrequencyType() {
        return FrequencyType.TELEPORTER;
    }

    @Override
    public void handleCacheUpdate(List<TeleporterFrequency> publicCache, List<TeleporterFrequency> privateCache, TeleporterFrequency frequency) {
        super.handleCacheUpdate(publicCache, privateCache, frequency);
        if (inv != null && inv.player.world.isRemote()) {
            //Request a data update if the frequency changed
            Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterGui(PortableTeleporterPacketType.DATA_REQUEST, getHand(), getFrequency()));
        }
    }
}