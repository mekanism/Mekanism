package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IPersonalStorage;
import mekanism.common.inventory.container.item.PersonalStorageItemContainer;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;

public class ItemBlockPersonalStorage<BLOCK extends Block & IHasDescription & IPersonalStorage> extends ItemBlockTooltip<BLOCK> implements IItemSustainedInventory,
      ISecurityItem, IGuiItem {

    public ItemBlockPersonalStorage(BLOCK block) {
        super(block, true, ItemDeferredRegister.getMekBaseProperties().stacksTo(1));
    }

    @Override
    protected void addDetails(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        SecurityUtils.addSecurityTooltip(stack, tooltip);
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (getOwnerUUID(stack) == null) {
            if (!world.isClientSide) {
                SecurityUtils.claimItem(player, stack);
            }
        } else if (SecurityUtils.canAccess(player, stack)) {
            if (!world.isClientSide) {
                getContainerType().tryOpenGui((ServerPlayer) player, hand, stack);
                player.awardStat(Stats.CUSTOM.get(Stats.OPEN_CHEST));
            }
        } else {
            if (!world.isClientSide) {
                SecurityUtils.displayNoAccess(player);
            }
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
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