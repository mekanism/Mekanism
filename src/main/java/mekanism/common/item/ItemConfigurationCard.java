package mekanism.common.item;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Contract;

public class ItemConfigurationCard extends Item {

    public ItemConfigurationCard(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, World world, List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(MekanismLang.CONFIG_CARD_HAS_DATA.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getConfigCardName(getData(stack))));
    }

    @Nonnull
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        TileEntity tile = WorldUtils.getTileEntity(world, pos);
        Optional<IConfigCardAccess> configCardSupport = CapabilityUtils.getCapability(tile, Capabilities.CONFIG_CARD_CAPABILITY, side).resolve();
        if (configCardSupport.isPresent()) {
            if (SecurityUtils.canAccess(player, tile)) {
                ItemStack stack = context.getItemInHand();
                if (player.isShiftKeyDown()) {
                    if (!world.isClientSide) {
                        IConfigCardAccess configCardAccess = configCardSupport.get();
                        String translationKey = configCardAccess.getConfigCardName();
                        CompoundNBT data = configCardAccess.getConfigurationData(player);
                        data.putString(NBTConstants.DATA_NAME, translationKey);
                        data.putString(NBTConstants.DATA_TYPE, configCardAccess.getConfigurationDataType().getRegistryName().toString());
                        ItemDataUtils.setCompound(stack, NBTConstants.DATA, data);
                        player.sendMessage(MekanismUtils.logFormat(MekanismLang.CONFIG_CARD_GOT.translate(EnumColor.INDIGO, TextComponentUtil.translate(translationKey))),
                              Util.NIL_UUID);
                    }
                } else {
                    CompoundNBT data = getData(stack);
                    TileEntityType<?> storedType = getStoredTileType(data);
                    if (storedType == null) {
                        return ActionResultType.PASS;
                    }
                    if (!world.isClientSide) {
                        IConfigCardAccess configCardAccess = configCardSupport.get();
                        if (configCardAccess.isConfigurationDataCompatible(storedType)) {
                            configCardAccess.setConfigurationData(player, data);
                            configCardAccess.configurationDataSet();
                            player.sendMessage(MekanismUtils.logFormat(EnumColor.DARK_GREEN, MekanismLang.CONFIG_CARD_SET.translate(EnumColor.INDIGO,
                                  getConfigCardName(data))), Util.NIL_UUID);
                        } else {
                            player.sendMessage(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.CONFIG_CARD_UNEQUAL), Util.NIL_UUID);
                        }
                    }
                }
                return ActionResultType.SUCCESS;
            } else {
                SecurityUtils.displayNoAccess(player);
            }
        }
        return ActionResultType.PASS;
    }

    private CompoundNBT getData(ItemStack stack) {
        CompoundNBT data = ItemDataUtils.getCompound(stack, NBTConstants.DATA);
        return data.isEmpty() ? null : data;
    }

    @Nullable
    @Contract("null -> null")
    private TileEntityType<?> getStoredTileType(@Nullable CompoundNBT data) {
        if (data == null || !data.contains(NBTConstants.DATA_TYPE, NBT.TAG_STRING)) {
            return null;
        }
        ResourceLocation tileRegistryName = ResourceLocation.tryParse(data.getString(NBTConstants.DATA_TYPE));
        return tileRegistryName == null ? null : ForgeRegistries.TILE_ENTITIES.getValue(tileRegistryName);
    }

    private ITextComponent getConfigCardName(@Nullable CompoundNBT data) {
        if (data == null || !data.contains(NBTConstants.DATA_NAME, NBT.TAG_STRING)) {
            return MekanismLang.NONE.translate();
        }
        return TextComponentUtil.translate(data.getString(NBTConstants.DATA_NAME));
    }

    public boolean hasData(ItemStack stack) {
        CompoundNBT data = getData(stack);
        return data != null && data.contains(NBTConstants.DATA_NAME, NBT.TAG_STRING);
    }
}