package mekanism.common.inventory.container.type;

import java.util.function.Predicate;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.inventory.container.type.MekanismItemContainerType.IMekanismItemContainerFactory;
import mekanism.common.network.to_client.qio.BulkQIOData;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public class MekanismItemContainerType<CONTAINER extends AbstractContainerMenu> extends BaseMekanismContainerType<CONTAINER, IMekanismItemContainerFactory<CONTAINER>> {

    public static <CONTAINER extends AbstractContainerMenu> MekanismItemContainerType<CONTAINER> item(Predicate<Item> typeValidator,
          IMekanismItemContainerFactory<CONTAINER> constructor) {
        return new MekanismItemContainerType<>(typeValidator, constructor, (id, inv, buf) -> {
            InteractionHand hand = buf.readEnum(InteractionHand.class);
            return constructor.create(id, inv, hand, ItemAccessUtils.playerHandAccess(inv.player, hand));
        });
    }

    public static <CONTAINER extends AbstractContainerMenu> MekanismItemContainerType<CONTAINER> item(Predicate<Item> typeValidator,
          IMekanismSidedItemContainerFactory<CONTAINER> constructor) {
        return new MekanismItemContainerType<>(typeValidator, constructor, (id, inv, buf) -> {
            InteractionHand hand = buf.readEnum(InteractionHand.class);
            return constructor.create(id, inv, hand, ItemAccessUtils.playerHandAccess(inv.player, hand), true);
        });
    }

    public static MekanismItemContainerType<PortableQIODashboardContainer> qioDashboard() {
        return new MekanismItemContainerType<>(MekanismItems.PORTABLE_QIO_DASHBOARD::is,
              (id, inv, hand, itemAccess) -> new PortableQIODashboardContainer(id, inv, hand, itemAccess, false, BulkQIOData.INITIAL_SERVER),
              (id, inv, buf) -> {
                  InteractionHand hand = buf.readEnum(InteractionHand.class);
                  return new PortableQIODashboardContainer(id, inv, hand, ItemAccessUtils.playerHandAccess(inv.player, hand), true, BulkQIOData.fromPacket(buf));
              }
        );
    }

    private final Predicate<Item> typeValidator;

    protected MekanismItemContainerType(Predicate<Item> typeValidator, IMekanismItemContainerFactory<CONTAINER> mekanismConstructor,
          IContainerFactory<CONTAINER> constructor) {
        super(mekanismConstructor, constructor);
        this.typeValidator = typeValidator;
    }

    @Nullable
    public CONTAINER create(int id, Inventory inv, InteractionHand hand, ItemAccess itemAccess) {
        ItemResource itemType = itemAccess.getResource();
        if (!itemType.isEmpty() && typeValidator.test(itemType.getItem())) {
            return mekanismConstructor.create(id, inv, hand, itemAccess);
        }
        return null;
    }

    @Nullable
    public MenuConstructor create(InteractionHand hand, ItemResource itemType) {
        if (!itemType.isEmpty() && typeValidator.test(itemType.getItem())) {
            return (id, inv, player) -> mekanismConstructor.create(id, inv, hand, ItemAccessUtils.playerHandAccess(player, hand));
        }
        return null;
    }

    @FunctionalInterface
    public interface IMekanismItemContainerFactory<CONTAINER extends AbstractContainerMenu> {

        CONTAINER create(int id, Inventory inv, InteractionHand hand, ItemAccess itemAccess);
    }

    @FunctionalInterface
    public interface IMekanismSidedItemContainerFactory<CONTAINER extends AbstractContainerMenu> extends IMekanismItemContainerFactory<CONTAINER> {


        CONTAINER create(int id, Inventory inv, InteractionHand hand, ItemAccess itemAccess, boolean remote);

        @Override
        default CONTAINER create(int id, Inventory inv, InteractionHand hand, ItemAccess itemAccess) {
            return create(id, inv, hand, itemAccess, false);
        }
    }
}