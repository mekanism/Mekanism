package mekanism.common.inventory.container.item;

import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.frequency.Frequency.FrequencyIdentity;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.frequency.IFrequencyItem;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.item.ItemPortableItemDashboard;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

public class PortableItemDashboardContainer extends QIOItemViewerContainer {

    protected Hand hand;
    protected ItemStack stack;

    public PortableItemDashboardContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack) {
        super(MekanismContainerTypes.PORTABLE_ITEM_DASHBOARD, id, inv);
        this.hand = hand;
        this.stack = stack;
        addSlotsAndOpen();
    }

    public PortableItemDashboardContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, buf.readEnumValue(Hand.class), MekanismItemContainer.getStackFromBuffer(buf, ItemPortableItemDashboard.class));
    }

    public Hand getHand() {
        return hand;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public PortableItemDashboardContainer recreate() {
        return new PortableItemDashboardContainer(windowId, inv, hand, stack);
    }

    @Override
    public QIOFrequency getFrequency() {
        if (!inv.player.world.isRemote()) {
            FrequencyIdentity identity = ((IFrequencyItem) stack.getItem()).getFrequency(stack);
            if (identity == null)
                return null;
            FrequencyManager<QIOFrequency> manager = identity.isPublic() ? FrequencyType.QIO.getManager(null) : FrequencyType.QIO.getManager(inv.player.getUniqueID());
            QIOFrequency freq = manager.getFrequency(identity.getKey());
            // if this frequency no longer exists, remove the reference from the stack
            if (freq == null) {
                ((IFrequencyItem) stack.getItem()).setFrequency(stack, null);
            }
            return freq;
        }
        return null;
    }
}
