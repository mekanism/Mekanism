package mekanism.common.inventory.container.type;

import mekanism.common.inventory.container.type.MekanismItemContainerType.IMekanismItemContainerFactory;
import mekanism.common.util.RegistryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.IContainerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismItemContainerType<ITEM extends Item, CONTAINER extends AbstractContainerMenu> extends BaseMekanismContainerType<ITEM, CONTAINER,
      IMekanismItemContainerFactory<ITEM, CONTAINER>> {

    public static <ITEM extends Item, CONTAINER extends AbstractContainerMenu> MekanismItemContainerType<ITEM, CONTAINER> item(Class<ITEM> type,
          IMekanismItemContainerFactory<ITEM, CONTAINER> constructor) {
        return new MekanismItemContainerType<>(type, constructor, (id, inv, buf) -> constructor.create(id, inv, buf.readEnum(InteractionHand.class), getStackFromBuffer(buf, type)));
    }

    public static <ITEM extends Item, CONTAINER extends AbstractContainerMenu> MekanismItemContainerType<ITEM, CONTAINER> item(Class<ITEM> type,
          IMekanismSidedItemContainerFactory<ITEM, CONTAINER> constructor) {
        return new MekanismItemContainerType<>(type, constructor, (id, inv, buf) -> constructor.create(id, inv, buf.readEnum(InteractionHand.class), getStackFromBuffer(buf, type), true));
    }

    protected MekanismItemContainerType(Class<ITEM> type, IMekanismItemContainerFactory<ITEM, CONTAINER> mekanismConstructor, IContainerFactory<CONTAINER> constructor) {
        super(type, mekanismConstructor, constructor);
    }

    @Nullable
    public CONTAINER create(int id, Inventory inv, InteractionHand hand, ItemStack stack) {
        if (!stack.isEmpty() && type.isInstance(stack.getItem())) {
            return mekanismConstructor.create(id, inv, hand, stack);
        }
        return null;
    }

    @Nullable
    public MenuConstructor create(InteractionHand hand, ItemStack stack) {
        if (!stack.isEmpty() && type.isInstance(stack.getItem())) {
            return (id, inv, player) -> mekanismConstructor.create(id, inv, hand, stack);
        }
        return null;
    }

    @NotNull
    private static <ITEM extends Item> ItemStack getStackFromBuffer(FriendlyByteBuf buf, Class<ITEM> type) {
        if (buf == null) {
            throw new IllegalArgumentException("Null packet buffer");
        }
        ItemStack stack = buf.readItem();
        if (type.isInstance(stack.getItem())) {
            return stack;
        }
        throw new IllegalStateException("Client received invalid stack (" + RegistryUtils.getName(stack.getItem()) + ") for item container.");
    }

    @FunctionalInterface
    public interface IMekanismItemContainerFactory<ITEM extends Item, CONTAINER extends AbstractContainerMenu> {

        CONTAINER create(int id, Inventory inv, InteractionHand hand, ItemStack stack);
    }

    @FunctionalInterface
    public interface IMekanismSidedItemContainerFactory<ITEM extends Item, CONTAINER extends AbstractContainerMenu> extends IMekanismItemContainerFactory<ITEM, CONTAINER> {


        CONTAINER create(int id, Inventory inv, InteractionHand hand, ItemStack stack, boolean remote);

        @Override
        default CONTAINER create(int id, Inventory inv, InteractionHand hand, ItemStack stack) {
            return create(id, inv, hand, stack, false);
        }
    }
}