package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IPersonalStorage;
import mekanism.common.inventory.container.item.PersonalStorageItemContainer;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.SecurityUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;

public class ItemBlockPersonalStorage<BLOCK extends Block & IHasDescription & IPersonalStorage> extends ItemBlockTooltip<BLOCK> implements IItemSustainedInventory,
      IGuiItem {

    private final ResourceLocation openStat;

    public ItemBlockPersonalStorage(BLOCK block, ResourceLocation openStat) {
        super(block);
        this.openStat = openStat;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {
        return SecurityUtils.INSTANCE.claimOrOpenGui(world, player, hand, (p, h, s) -> {
            getContainerType().tryOpenGui(p, h, s);
            p.awardStat(Stats.CUSTOM.get(openStat));
        });
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        //Like super.onItemUse, except we validate the player is not null, and pass the onItemRightClick regardless of if
        // we are food or not (as we know the personal chest is never food). This allows us to open the personal chest's
        // GUI if we didn't interact with a block that caused something to happen like opening a GUI.
        InteractionResult result = place(new BlockPlaceContext(context));
        Player player = context.getPlayer();
        return result.consumesAction() || player == null ? result : use(context.getLevel(), player, context.getHand()).getResult();
    }

    @Override
    protected boolean canPlace(@Nonnull BlockPlaceContext context, @Nonnull BlockState state) {
        Player player = context.getPlayer();
        //Only allow placing if there is no player, it is a fake player, or the player is sneaking
        return (player == null || player instanceof FakePlayer || player.isShiftKeyDown()) && super.canPlace(context, state);
    }

    @Override
    public ContainerTypeRegistryObject<PersonalStorageItemContainer> getContainerType() {
        return MekanismContainerTypes.PERSONAL_STORAGE_ITEM;
    }
}