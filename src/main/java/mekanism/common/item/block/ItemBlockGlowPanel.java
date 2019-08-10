package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mcmultipart.api.multipart.IMultipart;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockGlowPanel;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.item.IItemRedirectedModel;
import mekanism.common.tile.TileEntityGlowPanel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

//TODO: Maybe somehow make this extend ItemBlockColoredName
public class ItemBlockGlowPanel extends ItemBlockMultipartAble implements IItemRedirectedModel {

    public ItemBlockGlowPanel(Block block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull PlayerEntity player, World world, @Nonnull BlockPos pos, Direction side, float hitX, float hitY,
          float hitZ, @Nonnull BlockState state) {
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state)) {
            TileEntityGlowPanel tile = (TileEntityGlowPanel) world.getTileEntity(pos);
            if (tile != null) {
                BlockPos pos1 = pos.offset(side.getOpposite());
                if (world.isSideSolid(pos1, side)) {
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
            color = EnumColor.DARK_GREY;
        }
        return color + super.getDisplayName(stack);
    }

    public EnumColor getColor(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockGlowPanel) {
            return ((BlockGlowPanel) (((ItemBlockGlowPanel) item).block)).getColor();
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

    @Nonnull
    @Override
    public String getRedirectLocation() {
        return "glow_panel";
    }
}