package mekanism.common.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IRedstoneControl;
import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemConfigurationCard extends Item {

    public ItemConfigurationCard(Properties properties) {
        super(properties.maxStackSize(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(MekanismLang.CONFIG_CARD_HAS_DATA.translateColored(EnumColor.GRAY, EnumColor.INDIGO, TextComponentUtil.translate(getDataType(stack))));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (!world.isRemote && player != null) {
            BlockPos pos = context.getPos();
            Direction side = context.getFace();
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (CapabilityUtils.getCapability(tile, Capabilities.CONFIG_CARD_CAPABILITY, side).isPresent()) {
                if (SecurityUtils.canAccess(player, tile)) {
                    ItemStack stack = player.getHeldItem(context.getHand());
                    if (player.isSneaking()) {
                        Optional<ISpecialConfigData> configData = CapabilityUtils.getCapability(tile, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side).resolve();
                        CompoundNBT data = configData.isPresent() ? configData.get().getConfigurationData(getBaseData(tile)) : getBaseData(tile);
                        if (data != null) {
                            data.putString(NBTConstants.DATA_TYPE, getNameFromTile(tile, side));
                            setData(stack, data);
                            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                                  MekanismLang.CONFIG_CARD_GOT.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                                        TextComponentUtil.translate(data.getString(NBTConstants.DATA_TYPE)))), Util.DUMMY_UUID);
                        }
                        return ActionResultType.SUCCESS;
                    }
                    CompoundNBT data = getData(stack);
                    if (data != null) {
                        if (getNameFromTile(tile, side).equals(getDataType(stack))) {
                            setBaseData(data, tile);
                            CapabilityUtils.getCapability(tile, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side).ifPresent(special -> special.setConfigurationData(data));

                            if (tile instanceof TileEntityMekanism) {
                                TileEntityMekanism mekanismTile = (TileEntityMekanism) tile;
                                mekanismTile.invalidateCachedCapabilities();
                                mekanismTile.sendUpdatePacket();
                                MekanismUtils.notifyLoadedNeighborsOfTileChange(world, pos);
                            }
                            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                                  MekanismLang.CONFIG_CARD_SET.translateColored(EnumColor.DARK_GREEN, EnumColor.INDIGO,
                                        TextComponentUtil.translate(getDataType(stack)))), Util.DUMMY_UUID);
                        } else {
                            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.RED,
                                  MekanismLang.CONFIG_CARD_UNEQUAL), Util.DUMMY_UUID);
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
            nbtTags.putInt(NBTConstants.CONTROL_TYPE, ((IRedstoneControl) tile).getControlType().ordinal());
        }
        if (tile instanceof ISideConfiguration) {
            ((ISideConfiguration) tile).getConfig().write(nbtTags);
            ((ISideConfiguration) tile).getEjector().write(nbtTags);
        }
        return nbtTags;
    }

    private void setBaseData(CompoundNBT nbtTags, TileEntity tile) {
        if (tile instanceof IRedstoneControl) {
            ((IRedstoneControl) tile).setControlType(RedstoneControl.byIndexStatic(nbtTags.getInt(NBTConstants.CONTROL_TYPE)));
        }
        if (tile instanceof ISideConfiguration) {
            ((ISideConfiguration) tile).getConfig().read(nbtTags);
            ((ISideConfiguration) tile).getEjector().read(nbtTags);
        }
    }

    private String getNameFromTile(TileEntity tile, Direction side) {
        Optional<ISpecialConfigData> capability = CapabilityUtils.getCapability(tile, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side).resolve();
        if (capability.isPresent()) {
            return capability.get().getDataType();
        }
        String ret = Integer.toString(tile.hashCode());
        if (tile instanceof TileEntityMekanism) {
            ret = ((TileEntityMekanism) tile).getBlockType().getTranslationKey();
        }
        return ret;
    }

    private void setData(ItemStack stack, CompoundNBT data) {
        if (data == null) {
            ItemDataUtils.removeData(stack, NBTConstants.DATA);
        } else {
            ItemDataUtils.setCompound(stack, NBTConstants.DATA, data);
        }
    }

    private CompoundNBT getData(ItemStack stack) {
        CompoundNBT data = ItemDataUtils.getCompound(stack, NBTConstants.DATA);
        if (data.isEmpty()) {
            return null;
        }
        return ItemDataUtils.getCompound(stack, NBTConstants.DATA);
    }

    public String getDataType(ItemStack stack) {
        CompoundNBT data = getData(stack);
        if (data == null) {
            return MekanismLang.NONE.getTranslationKey();
        }
        return data.getString(NBTConstants.DATA_TYPE);
    }
}