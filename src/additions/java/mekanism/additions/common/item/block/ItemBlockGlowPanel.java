package mekanism.additions.common.item.block;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.additions.common.block.BlockGlowPanel;
import mekanism.common.item.block.ItemBlockMultipartAble;
import mekanism.additions.common.tile.TileEntityGlowPanel;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

//TODO: Maybe somehow make this extend ItemBlockColoredName
public class ItemBlockGlowPanel extends ItemBlockMultipartAble<BlockGlowPanel> {

    public ItemBlockGlowPanel(BlockGlowPanel block) {
        super(block);
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        if (super.placeBlock(context, state)) {
            World world = context.getWorld();
            BlockPos pos = context.getPos();
            TileEntityGlowPanel tile = (TileEntityGlowPanel) world.getTileEntity(pos);
            if (tile != null) {
                Direction side = context.getFace();
                BlockPos pos1 = pos.offset(side.getOpposite());
                if (Block.hasSolidSide(world.getBlockState(pos1), world, pos1, side)) {
                    tile.setOrientation(side.getOpposite());
                }
                if (!world.isRemote) {
                    Mekanism.packetHandler.sendUpdatePacket(tile);
                }
            }
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        EnumColor color = getColor(stack);
        if (color == EnumColor.BLACK) {
            color = EnumColor.DARK_GRAY;
        }
        return TextComponentUtil.build(color, super.getDisplayName(stack));
    }

    public EnumColor getColor(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockGlowPanel) {
            return ((ItemBlockGlowPanel) item).getBlock().getColor();
        }
        return EnumColor.BLACK;
    }

    //TODO: Multipart
    /*@Override
    public boolean shouldRotateAroundWhenRendering() {
        return true;
    }

    @Override
    @Optional.Method(modid = MekanismHooks.MCMULTIPART_MOD_ID)
    protected IMultipart getMultiPart() {
        return MultipartMekanism.GLOWPANEL_MP;
    }*/
}