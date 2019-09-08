package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.common.Mekanism;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITileNetwork;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemConfigurationCard extends ItemMekanism {

    public ItemConfigurationCard() {
        super();
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        super.addInformation(itemstack, world, list, flag);
        list.add(EnumColor.GREY + LangUtils.localize("gui.data") + ": " + EnumColor.INDIGO + LangUtils.localize(getDataType(itemstack)));
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (CapabilityUtils.hasCapability(tileEntity, Capabilities.CONFIG_CARD_CAPABILITY, side)) {
                if (SecurityUtils.canAccess(player, tileEntity)) {
                    ItemStack stack = player.getHeldItem(hand);
                    if (player.isSneaking()) {
                        NBTTagCompound data = getBaseData(tileEntity);
                        if (CapabilityUtils.hasCapability(tileEntity, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side)) {
                            ISpecialConfigData special = CapabilityUtils.getCapability(tileEntity, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side);
                            data = special.getConfigurationData(data);
                        }

                        if (data != null) {
                            data.setString("dataType", getNameFromTile(tileEntity, side));
                            setData(stack, data);
                            player.sendMessage(new TextComponentString(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.GREY +
                                                                       LangUtils.localize("tooltip.configurationCard.got").replaceAll("%s",
                                                                             EnumColor.INDIGO + LangUtils.localize(data.getString("dataType")) + EnumColor.GREY)));
                        }
                        return EnumActionResult.SUCCESS;
                    }
                    NBTTagCompound data = getData(stack);
                    if (data != null) {
                        if (getNameFromTile(tileEntity, side).equals(getDataType(stack))) {
                            setBaseData(data, tileEntity);
                            if (CapabilityUtils.hasCapability(tileEntity, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side)) {
                                ISpecialConfigData special = CapabilityUtils.getCapability(tileEntity, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side);
                                special.setConfigurationData(data);
                            }
                            updateTile(tileEntity);
                            player.sendMessage(new TextComponentString(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.DARK_GREEN +
                                                                       LangUtils.localize("tooltip.configurationCard.set").replaceAll("%s",
                                                                             EnumColor.INDIGO + LangUtils.localize(getDataType(stack)) + EnumColor.DARK_GREEN)));
                        } else {
                            player.sendMessage(new TextComponentString(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.RED +
                                                                       LangUtils.localize("tooltip.configurationCard.unequal") + "."));
                        }
                        return EnumActionResult.SUCCESS;
                    }
                } else {
                    SecurityUtils.displayNoAccess(player);
                }
            }
        }
        return EnumActionResult.PASS;
    }

    private <TILE extends TileEntity & ITileNetwork> void updateTile(TileEntity tileEntity) {
        //Check the capability in case for some reason the tile doesn't want to expose the fact it has it
        ITileNetwork network = CapabilityUtils.getCapability(tileEntity, Capabilities.TILE_NETWORK_CAPABILITY, null);
        if (network instanceof TileEntity) {
            //Ensure the implementation is still a tile entity
            Mekanism.packetHandler.sendUpdatePacket((TILE) network);
        }
    }

    private NBTTagCompound getBaseData(TileEntity tile) {
        NBTTagCompound nbtTags = new NBTTagCompound();
        if (tile instanceof IRedstoneControl) {
            nbtTags.setInteger("controlType", ((IRedstoneControl) tile).getControlType().ordinal());
        }
        if (tile instanceof ISideConfiguration) {
            ((ISideConfiguration) tile).getConfig().write(nbtTags);
            ((ISideConfiguration) tile).getEjector().write(nbtTags);
        }
        return nbtTags;
    }

    private void setBaseData(NBTTagCompound nbtTags, TileEntity tile) {
        if (tile instanceof IRedstoneControl) {
            ((IRedstoneControl) tile).setControlType(RedstoneControl.values()[nbtTags.getInteger("controlType")]);
        }
        if (tile instanceof ISideConfiguration) {
            ((ISideConfiguration) tile).getConfig().read(nbtTags);
            ((ISideConfiguration) tile).getEjector().read(nbtTags);
        }
    }

    private String getNameFromTile(TileEntity tile, EnumFacing side) {
        String ret = Integer.toString(tile.hashCode());
        if (tile instanceof TileEntityContainerBlock) {
            ret = tile.getBlockType().getTranslationKey() + "." + ((TileEntityContainerBlock) tile).fullName + ".name";
        }
        if (CapabilityUtils.hasCapability(tile, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side)) {
            ISpecialConfigData special = CapabilityUtils.getCapability(tile, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side);
            ret = special.getDataType();
        }
        return ret;
    }

    public void setData(ItemStack itemstack, NBTTagCompound data) {
        if (data != null) {
            ItemDataUtils.setCompound(itemstack, "data", data);
        } else {
            ItemDataUtils.removeData(itemstack, "data");
        }
    }

    public NBTTagCompound getData(ItemStack itemstack) {
        NBTTagCompound data = ItemDataUtils.getCompound(itemstack, "data");
        if (data.isEmpty()) {
            return null;
        }
        return ItemDataUtils.getCompound(itemstack, "data");
    }

    public String getDataType(ItemStack itemstack) {
        NBTTagCompound data = getData(itemstack);
        if (data != null) {
            return data.getString("dataType");
        }
        return "gui.none";
    }
}