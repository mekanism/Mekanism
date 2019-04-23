package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemDictionary extends ItemMekanism {

    public ItemDictionary() {
        super();
        setMaxStackSize(1);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
          float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!player.isSneaking()) {
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (world.isRemote) {
                ItemStack testStack = new ItemStack(block, 1, block.getMetaFromState(state));
                List<String> names = OreDictCache.getOreDictName(testStack);

                if (!names.isEmpty()) {
                    player.sendMessage(new TextComponentString(
                          EnumColor.DARK_BLUE + Mekanism.LOG_TAG + EnumColor.GREY + " " + LangUtils
                                .localize("tooltip.keysFound") + ":"));

                    for (String name : names) {
                        player.sendMessage(new TextComponentString(EnumColor.DARK_GREEN + " - " + name));
                    }
                } else {
                    player.sendMessage(new TextComponentString(
                          EnumColor.DARK_BLUE + Mekanism.LOG_TAG + EnumColor.GREY + " " + LangUtils
                                .localize("tooltip.noKey") + "."));
                }
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entityplayer, @Nonnull EnumHand hand) {
        ItemStack itemstack = entityplayer.getHeldItem(hand);

        if (entityplayer.isSneaking()) {
            entityplayer.openGui(Mekanism.instance, 0, world, 0, 0, 0);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        }

        return new ActionResult<>(EnumActionResult.PASS, itemstack);
    }
}
