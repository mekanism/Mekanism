package mekanism.common.item;

import javax.annotation.Nonnull;
import mcmultipart.api.multipart.IMultipart;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.api.TileNetworkList;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityGlowPanel;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

public class ItemBlockGlowPanel extends ItemBlockMultipartAble {

    public Block metaBlock;

    public ItemBlockGlowPanel(Block block) {
        super(block);
        metaBlock = block;
        setHasSubtypes(true);
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Override
    public int getMetadata(int i) {
        return i;
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world,
          @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState state) {
        if (stack.getItemDamage() >= EnumColor.DYES.length) {
            return false;
        }
        boolean place = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state);

        if (place) {
            TileEntityGlowPanel tileEntity = (TileEntityGlowPanel) world.getTileEntity(pos);
            EnumColor col = EnumColor.DYES[stack.getItemDamage()];

            BlockPos pos1 = pos.offset(side.getOpposite());

            if (world.isSideSolid(pos1, side)) {
                tileEntity.setOrientation(side.getOpposite());
            }

            tileEntity.setColour(col);

            if (!world.isRemote) {
                Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity),
                      tileEntity.getNetworkedData(new TileNetworkList())), new Range4D(Coord4D.get(tileEntity)));
            }
        }

        return place;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> listToAddTo) {
        if (!isInCreativeTab(tab)) {
            return;
        }
        for (EnumColor color : EnumColor.DYES) {
            listToAddTo.add(new ItemStack(this, 1, color.getMetaValue()));
        }
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        int itemDamage = stack.getItemDamage();
        if (itemDamage >= EnumColor.DYES.length) {
            return "Invalid Damage: " + itemDamage;
        }
        EnumColor colour = EnumColor.DYES[itemDamage];
        String colourName;

        if (I18n.canTranslate(getTranslationKey(stack) + "." + colour.dyeName)) {
            return LangUtils.localize(getTranslationKey(stack) + "." + colour.dyeName);
        }

        if (colour == EnumColor.BLACK) {
            colourName = EnumColor.DARK_GREY + colour.getDyeName();
        } else {
            colourName = colour.getDyedName();
        }

        return colourName + " " + super.getItemStackDisplayName(stack);
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
