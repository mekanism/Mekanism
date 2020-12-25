package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.item.PersonalChestItemContainer;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemBlockPersonalChest extends ItemBlockTooltip<BlockTileModel<?, ?>> implements IItemSustainedInventory, ISecurityItem, IGuiItem {

    public ItemBlockPersonalChest(BlockTileModel<?, ?> block) {
        super(block, true, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1));
    }

    @Override
    public void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        SecurityUtils.addSecurityTooltip(stack, tooltip);
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (getOwnerUUID(stack) == null) {
            if (!world.isRemote) {
                SecurityUtils.claimItem(player, stack);
            }
        } else if (SecurityUtils.canAccess(player, stack)) {
            if (!world.isRemote) {
                NetworkHooks.openGui((ServerPlayerEntity) player, getContainerProvider(stack, hand), buf -> {
                    buf.writeEnumValue(hand);
                    buf.writeItemStack(stack);
                });
            }
        } else {
            if (!world.isRemote) {
                SecurityUtils.displayNoAccess(player);
            }
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    protected boolean canPlace(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        PlayerEntity player = context.getPlayer();
        //Only allow placing if there is no player, it is a fake player, or the player is sneaking
        return (player == null || player instanceof FakePlayer || player.isSneaking()) && super.canPlace(context, state);
    }

    @Override
    public INamedContainerProvider getContainerProvider(ItemStack stack, Hand hand) {
        return new ContainerProvider(stack.getDisplayName(), (i, inv, p) -> new PersonalChestItemContainer(i, inv, hand, stack));
    }
}