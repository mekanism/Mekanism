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
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemConfigurationCard extends ItemMekanism {

    public ItemConfigurationCard() {
        super("configuration_card", new Item.Properties().maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(TextComponentUtil.build(EnumColor.GREY, Translation.of("mekanism.gui.data"), ": ", EnumColor.INDIGO, Translation.of(getDataType(itemstack))));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (!world.isRemote && player != null) {
            BlockPos pos = context.getPos();
            Direction side = context.getFace();
            TileEntity tileEntity = world.getTileEntity(pos);
            if (CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.CONFIG_CARD_CAPABILITY, side).isPresent()) {
                if (SecurityUtils.canAccess(player, tileEntity)) {
                    ItemStack stack = player.getHeldItem(context.getHand());
                    if (player.isSneaking()) {
                        CompoundNBT data = CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side).getIfPresentElseDo(
                              special -> special.getConfigurationData(getBaseData(tileEntity)),
                              () -> getBaseData(tileEntity)
                        );

                        if (data != null) {
                            data.putString("dataType", getNameFromTile(tileEntity, side));
                            setData(stack, data);
                            player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GREY,
                                  Translation.of("tooltip.configurationCard.got",
                                        TextComponentUtil.build(EnumColor.INDIGO, Translation.of(data.getString("dataType")))
                                  )));
                        }
                        return ActionResultType.SUCCESS;
                    } else if (getData(stack) != null) {
                        if (getNameFromTile(tileEntity, side).equals(getDataType(stack))) {
                            setBaseData(getData(stack), tileEntity);
                            CapabilityUtils.getCapabilityHelper(tileEntity, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side).ifPresent(
                                  special -> special.setConfigurationData(getData(stack))
                            );
                            player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.DARK_GREEN,
                                  Translation.of("tooltip.configurationCard.set", TextComponentUtil.build(EnumColor.INDIGO, Translation.of(getDataType(stack)))
                                  )));
                        } else {
                            player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.RED,
                                  Translation.of("tooltip.configurationCard.unequal"), "."));
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
            nbtTags.putInt("controlType", ((IRedstoneControl) tile).getControlType().ordinal());
        }
        if (tile instanceof ISideConfiguration) {
            ((ISideConfiguration) tile).getConfig().write(nbtTags);
            ((ISideConfiguration) tile).getEjector().write(nbtTags);
        }
        return nbtTags;
    }

    private void setBaseData(CompoundNBT nbtTags, TileEntity tile) {
        if (tile instanceof IRedstoneControl) {
            ((IRedstoneControl) tile).setControlType(RedstoneControl.values()[nbtTags.getInt("controlType")]);
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
        return CapabilityUtils.getCapabilityHelper(tile, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side).getIfPresentElse(
              ISpecialConfigData::getDataType,
              ret
        );
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
        return "mekanism.gui.none";
    }
}