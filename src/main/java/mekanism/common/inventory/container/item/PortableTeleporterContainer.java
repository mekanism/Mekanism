package mekanism.common.inventory.container.item;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;

public class PortableTeleporterContainer extends MekanismItemContainer implements IEmptyContainer {

    private Hand hand = Hand.MAIN_HAND;
    private ItemStack stack = ItemStack.EMPTY;

    public PortableTeleporterContainer(int id, PlayerInventory inv) {
        super(MekanismContainerTypes.PORTABLE_TELEPORTER, id, inv);
    }

    public PortableTeleporterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        //TODO: Retrieve the hand and stack
        this(id, inv);
        stack = buf.readItemStack();
        hand = buf.readEnumValue(Hand.class);
    }

    public Hand getHand() {
        return hand;
    }

    public ItemStack getStack() {
        return stack;
    }

    public

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.portable_teleporter");
    }
}