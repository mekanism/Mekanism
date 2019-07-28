package mekanism.common.item;

import javax.annotation.Nonnull;
import mcmultipart.api.multipart.IMultipart;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockGlowPanel;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.tile.TileEntityGlowPanel;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

public class ItemBlockGlowPanel extends ItemBlockMultipartAble {

    public ItemBlockGlowPanel(Block block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY,
          float hitZ, @Nonnull IBlockState state) {
        boolean place = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state);
        if (place) {
            TileEntityGlowPanel tileEntity = (TileEntityGlowPanel) world.getTileEntity(pos);
            EnumColor col = getColor(stack);
            BlockPos pos1 = pos.offset(side.getOpposite());
            if (world.isSideSolid(pos1, side)) {
                tileEntity.setOrientation(side.getOpposite());
            }
            tileEntity.setColour(col);
            if (!world.isRemote) {
                Mekanism.packetHandler.sendUpdatePacket(tileEntity);
            }
        }
        return place;
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        EnumColor color = getColor(stack);
        String colorName;
        if (LangUtils.canLocalize(getTranslationKey(stack) + "." + color.dyeName)) {
            return LangUtils.localize(getTranslationKey(stack) + "." + color.dyeName);
        }
        if (color == EnumColor.BLACK) {
            colorName = EnumColor.DARK_GREY + color.getDyeName();
        } else {
            colorName = color.getDyedName();
        }
        return colorName + " " + super.getItemStackDisplayName(stack);
    }

    private EnumColor getColor(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockGlowPanel) {
            BlockGlowPanel glowPanel = (BlockGlowPanel) (((ItemBlockGlowPanel) item).block);
            return glowPanel.getColor();
        }
        return EnumColor.BLACK;
    }

    @Override
    public boolean shouldRotateAroundWhenRendering() {
        return true;
    }

    @Override
    @Optional.Method(modid = MekanismHooks.MCMULTIPART_MOD_ID)
    protected IMultipart getMultiPart() {
        return MultipartMekanism.GLOWPANEL_MP;
    }
}