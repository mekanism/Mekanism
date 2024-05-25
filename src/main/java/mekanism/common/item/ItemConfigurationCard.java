package mekanism.common.item;

import java.util.List;
import mekanism.api.IConfigCardAccess;
import mekanism.api.SerializationConstants;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemConfigurationCard extends Item {

    public ItemConfigurationCard(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(MekanismLang.CONFIG_CARD_HAS_DATA.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getConfigCardName(getData(stack))));
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        IConfigCardAccess configCardAccess = WorldUtils.getCapability(world, Capabilities.CONFIG_CARD, pos, side);
        if (configCardAccess != null) {
            if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos)) {
                return InteractionResult.FAIL;
            }
            ItemStack stack = context.getItemInHand();
            if (player.isShiftKeyDown()) {
                if (!world.isClientSide) {
                    String translationKey = configCardAccess.getConfigCardName();
                    CompoundTag data = configCardAccess.getConfigurationData(world.registryAccess(), player);
                    data.putString(SerializationConstants.DATA_NAME, translationKey);
                    NBTUtils.writeRegistryEntry(data, SerializationConstants.DATA_TYPE, BuiltInRegistries.BLOCK, configCardAccess.getConfigurationDataType());
                    stack.set(MekanismDataComponents.CONFIGURATION_DATA, data);
                    player.displayClientMessage(MekanismLang.CONFIG_CARD_GOT.translate(EnumColor.INDIGO, TextComponentUtil.translate(translationKey)), true);
                    MekanismCriteriaTriggers.CONFIGURATION_CARD.value().trigger((ServerPlayer) player, true);
                }
            } else {
                CompoundTag data = getData(stack);
                Block storedType = getStoredType(data);
                if (storedType == null) {
                    return InteractionResult.PASS;
                }
                if (!world.isClientSide) {
                    if (configCardAccess.isConfigurationDataCompatible(storedType)) {
                        configCardAccess.setConfigurationData(world.registryAccess(), player, data);
                        configCardAccess.configurationDataSet();
                        player.displayClientMessage(MekanismLang.CONFIG_CARD_SET.translate(EnumColor.INDIGO,
                              getConfigCardName(data)), true);
                        MekanismCriteriaTriggers.CONFIGURATION_CARD.value().trigger((ServerPlayer) player, false);
                    } else {
                        player.displayClientMessage(MekanismLang.CONFIG_CARD_UNEQUAL.translateColored(EnumColor.RED), true);
                    }
                }
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            ItemStack configCard = player.getItemInHand(usedHand);
            if (!level.isClientSide) {
                configCard.remove(MekanismDataComponents.CONFIGURATION_DATA);
                player.displayClientMessage(MekanismLang.CONFIG_CARD_CLEARED.translate(), true);
            }
            return InteractionResultHolder.sidedSuccess(configCard, level.isClientSide);
        }
        return super.use(level, player, usedHand);
    }

    @Nullable
    private CompoundTag getData(ItemStack stack) {
        CompoundTag data = stack.get(MekanismDataComponents.CONFIGURATION_DATA);
        if (data == null || data.isEmpty()) {
            return null;
        }
        return data;
    }

    @Nullable
    @Contract("null -> null")
    private Block getStoredType(@Nullable CompoundTag data) {
        if (data == null || !data.contains(SerializationConstants.DATA_TYPE, Tag.TAG_STRING)) {
            return null;
        }
        ResourceLocation blockRegistryName = ResourceLocation.tryParse(data.getString(SerializationConstants.DATA_TYPE));
        return blockRegistryName == null ? null : BuiltInRegistries.BLOCK.get(blockRegistryName);
    }

    private Component getConfigCardName(@Nullable CompoundTag data) {
        if (data == null || !data.contains(SerializationConstants.DATA_NAME, Tag.TAG_STRING)) {
            return MekanismLang.NONE.translate();
        }
        return TextComponentUtil.translate(data.getString(SerializationConstants.DATA_NAME));
    }

    public boolean hasData(ItemStack stack) {
        CompoundTag data = getData(stack);
        return data != null && data.contains(SerializationConstants.DATA_NAME, Tag.TAG_STRING);
    }
}