package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemDictionary extends ItemMekanism {

    public ItemDictionary() {
        super("dictionary");
        setMaxStackSize(1);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseFirst(PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, Hand hand) {
        if (!player.isSneaking()) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (world.isRemote) {
                ItemStack testStack = new ItemStack(block, 1, block.getMetaFromState(state));
                List<String> names = OreDictCache.getOreDictName(testStack);
                if (!names.isEmpty()) {
                    player.sendMessage(new TextComponentString(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + EnumColor.GREY + " " + LangUtils.localize("tooltip.keysFound") + ":"));
                    for (String name : names) {
                        player.sendMessage(new TextComponentString(EnumColor.DARK_GREEN + " - " + name));
                    }
                } else {
                    player.sendMessage(new TextComponentString(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + EnumColor.GREY + " " + LangUtils.localize("tooltip.noKey") + "."));
                }
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entityplayer, @Nonnull Hand hand) {
        ItemStack itemstack = entityplayer.getHeldItem(hand);
        if (entityplayer.isSneaking()) {
            MekanismUtils.openItemGui(entityplayer, hand, 0);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        }
        return new ActionResult<>(EnumActionResult.PASS, itemstack);
    }
}