package mekanism.common.inventory.container.item;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.util.SecurityUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public abstract class MekanismItemContainer extends MekanismContainer {

    protected final Hand hand;
    protected final ItemStack stack;

    protected MekanismItemContainer(ContainerTypeRegistryObject<?> type, int id, PlayerInventory inv, Hand hand, ItemStack stack) {
        super(type, id, inv);
        this.hand = hand;
        this.stack = stack;
        addSlotsAndOpen();
    }

    @Override
    public ISecurityObject getSecurityObject() {
        return SecurityUtils.wrapSecurityItem(stack);
    }

    @Nonnull
    public static <ITEM extends Item> ItemStack getStackFromBuffer(PacketBuffer buf, Class<ITEM> type) {
        if (buf == null) {
            throw new IllegalArgumentException("Null packet buffer");
        }
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            ItemStack stack = buf.readItem();
            if (type.isInstance(stack.getItem())) {
                return stack;
            }
            throw new IllegalStateException("Client received invalid stack (" + stack.getItem().getRegistryName() + ") for item container.");
        });
    }
}