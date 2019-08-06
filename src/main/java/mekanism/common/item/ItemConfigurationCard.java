package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.common.Mekanism;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemConfigurationCard extends ItemMekanism {

    public ItemConfigurationCard() {
        super("configuration_card");
        setMaxStackSize(1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        super.addInformation(itemstack, world, list, flag);
        list.add(EnumColor.GREY + LangUtils.localize("gui.data") + ": " + EnumColor.INDIGO + LangUtils.localize(getDataType(itemstack)));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUseFirst(PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, Hand hand) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (CapabilityUtils.hasCapability(tileEntity, Capabilities.CONFIG_CARD_CAPABILITY, side)) {
                if (SecurityUtils.canAccess(player, tileEntity)) {
                    ItemStack stack = player.getHeldItem(hand);
                    if (player.isSneaking()) {
                        CompoundNBT data = getBaseData(tileEntity);
                        if (CapabilityUtils.hasCapability(tileEntity, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side)) {
                            ISpecialConfigData special = CapabilityUtils.getCapability(tileEntity, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side);
                            data = special.getConfigurationData(data);
                        }

                        if (data != null) {
                            data.setString("dataType", getNameFromTile(tileEntity, side));
                            setData(stack, data);
                            player.sendMessage(new StringTextComponent(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.GREY +
                                                                       LangUtils.localize("tooltip.configurationCard.got").replaceAll("%s",
                                                                             EnumColor.INDIGO + LangUtils.localize(data.getString("dataType")) + EnumColor.GREY)));
                        }
                        return ActionResultType.SUCCESS;
                    } else if (getData(stack) != null) {
                        if (getNameFromTile(tileEntity, side).equals(getDataType(stack))) {
                            setBaseData(getData(stack), tileEntity);
                            if (CapabilityUtils.hasCapability(tileEntity, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side)) {
                                ISpecialConfigData special = CapabilityUtils.getCapability(tileEntity, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side);
                                special.setConfigurationData(getData(stack));
                            }

                            player.sendMessage(new StringTextComponent(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.DARK_GREEN +
                                                                       LangUtils.localize("tooltip.configurationCard.set").replaceAll("%s",
                                                                             EnumColor.INDIGO + LangUtils.localize(getDataType(stack)) + EnumColor.DARK_GREEN)));
                        } else {
                            player.sendMessage(new StringTextComponent(EnumColor.DARK_BLUE + Mekanism.LOG_TAG + " " + EnumColor.RED +
                                                                       LangUtils.localize("tooltip.configurationCard.unequal") + "."));
                        }
                        return ActionResultType.SUCCESS;
                    }
                } else {
                    SecurityUtils.displayNoAccess(player);
                }
            }
        }
        return ActionResultType.PASS;
    }

    private CompoundNBT getBaseData(TileEntity tile) {
        CompoundNBT nbtTags = new CompoundNBT();
        if (tile instanceof IRedstoneControl) {
            nbtTags.setInteger("controlType", ((IRedstoneControl) tile).getControlType().ordinal());
        }
        if (tile instanceof ISideConfiguration) {
            ((ISideConfiguration) tile).getConfig().write(nbtTags);
            ((ISideConfiguration) tile).getEjector().write(nbtTags);
        }
        return nbtTags;
    }

    private void setBaseData(CompoundNBT nbtTags, TileEntity tile) {
        if (tile instanceof IRedstoneControl) {
            ((IRedstoneControl) tile).setControlType(RedstoneControl.values()[nbtTags.getInteger("controlType")]);
        }
        if (tile instanceof ISideConfiguration) {
            ((ISideConfiguration) tile).getConfig().read(nbtTags);
            ((ISideConfiguration) tile).getEjector().read(nbtTags);
        }
    }

    private String getNameFromTile(TileEntity tile, Direction side) {
        String ret = Integer.toString(tile.hashCode());
        if (tile instanceof TileEntityMekanism) {
            ret = tile.getBlockType().getTranslationKey() + ".name";
        }
        if (CapabilityUtils.hasCapability(tile, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side)) {
            ISpecialConfigData special = CapabilityUtils.getCapability(tile, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side);
            ret = special.getDataType();
        }
        return ret;
    }

    public void setData(ItemStack itemstack, CompoundNBT data) {
        if (data != null) {
            ItemDataUtils.setCompound(itemstack, "data", data);
        } else {
            ItemDataUtils.removeData(itemstack, "data");
        }
    }

    public CompoundNBT getData(ItemStack itemstack) {
        CompoundNBT data = ItemDataUtils.getCompound(itemstack, "data");
        if (data.isEmpty()) {
            return null;
        }
        return ItemDataUtils.getCompound(itemstack, "data");
    }

    public String getDataType(ItemStack itemstack) {
        CompoundNBT data = getData(itemstack);
        if (data != null) {
            return data.getString("dataType");
        }
        return "gui.none";
    }
}