package mekanism.common.inventory.container.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.DistExecutor;

public abstract class MekanismItemContainer extends MekanismContainer {

    protected Hand hand;
    protected ItemStack stack;

    protected MekanismItemContainer(@Nullable ContainerType<?> type, int id, @Nullable PlayerInventory inv, Hand hand, ItemStack stack) {
        super(type, id, inv);
        this.hand = hand;
        this.stack = stack;
        addSlotsAndOpen();
    }

    @Nonnull
    public static <ITEM extends Item> ItemStack getStackFromBuffer(PacketBuffer buf, Class<ITEM> type) {
        if (buf == null) {
            return ItemStack.EMPTY;
        }
        //TODO: Handle it being client side only better?
        return DistExecutor.runForDist(() -> () -> {
            ItemStack stack = buf.readItemStack();
            if (type.isInstance(stack.getItem())) {
                return stack;
            }
            return ItemStack.EMPTY;
        }, () -> () -> {
            throw new RuntimeException("Shouldn't be called on server!");
        });
    }
}