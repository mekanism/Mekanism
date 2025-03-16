package mekanism.common.inventory.container.type;

import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.inventory.container.type.MekanismItemContainerType.IMekanismItemContainerFactory;
import mekanism.common.item.ItemPortableQIODashboard;
import mekanism.common.network.to_client.qio.BulkQIOData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.IContainerFactory;
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

    public static MekanismItemContainerType<ItemPortableQIODashboard, PortableQIODashboardContainer> qioDashboard() {
        return new MekanismItemContainerType<>(ItemPortableQIODashboard.class,
              (id, inv, hand, stack) -> new PortableQIODashboardContainer(id, inv, hand, stack, false, BulkQIOData.INITIAL_SERVER),
              (id, inv, buf) -> new PortableQIODashboardContainer(id, inv, buf.readEnum(InteractionHand.class),
                    getStackFromBuffer(buf, ItemPortableQIODashboard.class), true, BulkQIOData.fromPacket(buf))
        );
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
    private static <ITEM extends Item> ItemStack getStackFromBuffer(RegistryFriendlyByteBuf buffer, Class<ITEM> type) {
        if (buffer == null) {
            throw new IllegalArgumentException("Null packet buffer");
        }
        ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        if (type.isInstance(stack.getItem())) {
            return stack;
        }
        throw new IllegalStateException("Client received invalid stack (" + stack.getItem() + ") for item container.");
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