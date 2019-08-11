package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TextComponentUtil;
import mekanism.common.util.TextComponentUtil.Translation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemDictionary extends ItemMekanism {

    public ItemDictionary() {
        super("dictionary", new Item.Properties().maxStackSize(1));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (player != null && !player.isSneaking()) {
            BlockPos pos = context.getPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (world.isRemote) {
                ItemStack testStack = new ItemStack(block);
                List<String> names = OreDictCache.getOreDictName(testStack);
                if (!names.isEmpty()) {
                    player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GREY,
                          Translation.of("mekanism.tooltip.keysFound"), ":"));
                    for (String name : names) {
                        player.sendMessage(TextComponentUtil.build(EnumColor.DARK_GREEN, " - " + name));
                    }
                } else {
                    player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GREY,
                          Translation.of("mekanism.tooltip.noKey"), "."));
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entityplayer, @Nonnull Hand hand) {
        ItemStack itemstack = entityplayer.getHeldItem(hand);
        if (entityplayer.isSneaking()) {
            MekanismUtils.openItemGui(entityplayer, hand, 0);
            return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
        }
        return new ActionResult<>(ActionResultType.PASS, itemstack);
    }
}