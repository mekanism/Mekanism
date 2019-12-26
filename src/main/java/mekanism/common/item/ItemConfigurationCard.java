package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITileNetwork;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
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

public class ItemConfigurationCard extends Item {

    public ItemConfigurationCard(Properties properties) {
        super(properties.maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(MekanismLang.HAS_DATA.translateColored(EnumColor.GRAY, EnumColor.INDIGO, Translation.of(getDataType(stack))));
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
            if (CapabilityUtils.getCapabilityHelper(tile, Capabilities.CONFIG_CARD_CAPABILITY, side).isPresent()) {
                if (SecurityUtils.canAccess(player, tile)) {
                    ItemStack stack = player.getHeldItem(context.getHand());
                    if (player.func_225608_bj_()) {
                        CompoundNBT data = CapabilityUtils.getCapabilityHelper(tile, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side).getIfPresentElseDo(
                              special -> special.getConfigurationData(getBaseData(tile)),
                              () -> getBaseData(tile)
                        );

                        if (data != null) {
                            data.putString("dataType", getNameFromTile(tile, side));
                            setData(stack, data);
                            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                                  MekanismLang.CONFIG_CARD_GOT.translateColored(EnumColor.GRAY, EnumColor.INDIGO, Translation.of(data.getString("dataType")))));
                        }
                        return ActionResultType.SUCCESS;
                    }
                    CompoundNBT data = getData(stack);
                    if (data != null) {
                        if (getNameFromTile(tile, side).equals(getDataType(stack))) {
                            setBaseData(data, tile);
                            CapabilityUtils.getCapabilityHelper(tile, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side).ifPresent(
                                  special -> special.setConfigurationData(data)
                            );
                            updateTile(tile);
                            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                                  MekanismLang.CONFIG_CARD_SET.translateColored(EnumColor.DARK_GREEN, EnumColor.INDIGO, Translation.of(getDataType(stack)))));
                        } else {
                            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                                  MekanismLang.CONFIG_CARD_UNEQUAL.translateColored(EnumColor.RED)));
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

    private <TILE extends TileEntity & ITileNetwork> void updateTile(TileEntity tile) {
        //Check the capability in case for some reason the tile doesn't want to expose the fact it has it
        CapabilityUtils.getCapabilityHelper(tile, Capabilities.TILE_NETWORK_CAPABILITY, null).ifPresent(network -> {
            if (network instanceof TileEntity) {
                //Ensure the implementation is still a tile entity
                Mekanism.packetHandler.sendUpdatePacket((TILE) network);
            }
        });
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
            ((IRedstoneControl) tile).setControlType(RedstoneControl.byIndexStatic(nbtTags.getInt("controlType")));
        }
        if (tile instanceof ISideConfiguration) {
            ((ISideConfiguration) tile).getConfig().read(nbtTags);
            ((ISideConfiguration) tile).getEjector().read(nbtTags);
        }
    }

    private String getNameFromTile(TileEntity tile, Direction side) {
        String ret = Integer.toString(tile.hashCode());
        if (tile instanceof TileEntityMekanism) {
            ret = ((TileEntityMekanism) tile).getBlockType().getTranslationKey();
        }
        return CapabilityUtils.getCapabilityHelper(tile, Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, side).getIfPresentElse(
              ISpecialConfigData::getDataType,
              ret
        );
    }

    public void setData(ItemStack stack, CompoundNBT data) {
        if (data != null) {
            ItemDataUtils.setCompound(stack, "data", data);
        } else {
            ItemDataUtils.removeData(stack, "data");
        }
    }

    public CompoundNBT getData(ItemStack stack) {
        CompoundNBT data = ItemDataUtils.getCompound(stack, "data");
        if (data.isEmpty()) {
            return null;
        }
        return ItemDataUtils.getCompound(stack, "data");
    }

    public String getDataType(ItemStack stack) {
        CompoundNBT data = getData(stack);
        if (data != null) {
            return data.getString("dataType");
        }
        return MekanismLang.NONE.getTranslationKey();
    }
}