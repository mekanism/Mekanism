package mekanism.common.inventory.container.type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.type.MekanismItemContainerType.IMekanismItemContainerFactory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.IContainerFactory;

public class MekanismItemContainerType<ITEM extends Item, CONTAINER extends Container> extends BaseMekanismContainerType<ITEM, CONTAINER,
      IMekanismItemContainerFactory<ITEM, CONTAINER>> {

    public static <ITEM extends Item, CONTAINER extends Container> MekanismItemContainerType<ITEM, CONTAINER> item(Class<ITEM> type,
          IMekanismItemContainerFactory<ITEM, CONTAINER> constructor) {
        return new MekanismItemContainerType<>(type, constructor, (id, inv, buf) -> constructor.create(id, inv, buf.readEnum(Hand.class), getStackFromBuffer(buf, type)));
    }

    public static <ITEM extends Item, CONTAINER extends Container> MekanismItemContainerType<ITEM, CONTAINER> item(Class<ITEM> type,
          IMekanismSidedItemContainerFactory<ITEM, CONTAINER> constructor) {
        return new MekanismItemContainerType<>(type, constructor, (id, inv, buf) -> constructor.create(id, inv, buf.readEnum(Hand.class), getStackFromBuffer(buf, type), true));
    }

    protected MekanismItemContainerType(Class<ITEM> type, IMekanismItemContainerFactory<ITEM, CONTAINER> mekanismConstructor, IContainerFactory<CONTAINER> constructor) {
        super(type, mekanismConstructor, constructor);
    }

    @Nullable
    public CONTAINER create(int id, PlayerInventory inv, Hand hand, ItemStack stack) {
        if (!stack.isEmpty() && type.isInstance(stack.getItem())) {
            return mekanismConstructor.create(id, inv, hand, stack);
        }
        return null;
    }

    @Nullable
    public IContainerProvider create(Hand hand, ItemStack stack) {
        if (!stack.isEmpty() && type.isInstance(stack.getItem())) {
            return (id, inv, player) -> mekanismConstructor.create(id, inv, hand, stack);
        }
        return null;
    }

    @Nonnull
    private static <ITEM extends Item> ItemStack getStackFromBuffer(PacketBuffer buf, Class<ITEM> type) {
        if (buf == null) {
            throw new IllegalArgumentException("Null packet buffer");
        }
        ItemStack stack = buf.readItem();
        if (type.isInstance(stack.getItem())) {
            return stack;
        }
        throw new IllegalStateException("Client received invalid stack (" + stack.getItem().getRegistryName() + ") for item container.");
    }

    @FunctionalInterface
    public interface IMekanismItemContainerFactory<ITEM extends Item, CONTAINER extends Container> {

        CONTAINER create(int id, PlayerInventory inv, Hand hand, ItemStack stack);
    }

    @FunctionalInterface
    public interface IMekanismSidedItemContainerFactory<ITEM extends Item, CONTAINER extends Container> extends IMekanismItemContainerFactory<ITEM, CONTAINER> {


        CONTAINER create(int id, PlayerInventory inv, Hand hand, ItemStack stack, boolean remote);

        @Override
        default CONTAINER create(int id, PlayerInventory inv, Hand hand, ItemStack stack) {
            return create(id, inv, hand, stack, false);
        }
    }
}