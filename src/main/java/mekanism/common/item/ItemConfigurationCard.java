package mekanism.common.item;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.IConfigCardAccess;
import mekanism.api.SerializationConstants;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemConfigurationCard extends Item {

    private static final Codec<Block> BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec();

    public ItemConfigurationCard(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        //TODO - 26.1: Go through the various append methods we have and move some over to data component based
        // Also support TooltipDisplay#hideTooltip
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(MekanismLang.CONFIG_CARD_HAS_DATA.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getConfigCardName(getData(stack))));
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
        BlockState blockState = world.getBlockState(pos);
        IConfigCardAccess configCardAccess = WorldUtils.getCapability(world, Capabilities.CONFIG_CARD, pos, blockState, null, side);
        //TODO - 26.1: Figure out if there is any other information we want to include in the problem path
        if (configCardAccess != null) {
            if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos)) {
                return InteractionResult.FAIL;
            }
            ProblemReporter.PathElement problemPath = new ConfigurationCardPathElement(blockState.getBlock(), pos);
            ItemStack stack = context.getItemInHand();
            if (player.isShiftKeyDown()) {
                if (!world.isClientSide()) {
                    String translationKey = configCardAccess.getConfigCardName();
                    try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(problemPath, Mekanism.logger)) {
                        TagValueOutput output = TagValueOutput.createWithContext(reporter, world.registryAccess());
                        output.putString(SerializationConstants.DATA_NAME, translationKey);
                        output.store(SerializationConstants.DATA_TYPE, BLOCK_CODEC, configCardAccess.getConfigurationDataType());
                        //Note: We store the child data in a separate value output to not impose restrictions on the allowed keys
                        ValueOutput configOutput = output.child(SerializationConstants.CONFIG);
                        configCardAccess.writeConfigurationData(configOutput, player);
                        if (configOutput.isEmpty()) {
                            configOutput.discard(SerializationConstants.CONFIG);
                        }
                        stack.set(MekanismDataComponents.CONFIGURATION_DATA, output.buildResult());
                    }
                    player.sendOverlayMessage(MekanismLang.CONFIG_CARD_GOT.translate(EnumColor.INDIGO, TextComponentUtil.translate(translationKey)));
                    MekanismCriteriaTriggers.CONFIGURATION_CARD.value().trigger((ServerPlayer) player, true);
                }
            } else {
                CompoundTag data = getData(stack);
                if (data == null) {
                    return InteractionResult.PASS;
                }
                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(problemPath, Mekanism.logger)) {
                    ValueInput input = TagValueInput.create(reporter, world.registryAccess(), data);
                    Block storedType = input.read(SerializationConstants.DATA_TYPE, BLOCK_CODEC).orElse(null);
                    if (storedType == null) {
                        return InteractionResult.PASS;
                    }
                    if (!world.isClientSide()) {
                        if (configCardAccess.isConfigurationDataCompatible(storedType)) {
                            //Note: We store the child data in a separate value output to not impose restrictions on the allowed keys
                            Optional<ValueInput> configInput = input.child(SerializationConstants.CONFIG);
                            //noinspection OptionalIsPresent - Capturing lambda
                            if (configInput.isPresent()) {
                                configCardAccess.setConfigurationData(configInput.get(), player);
                            }
                            configCardAccess.configurationDataSet();
                            player.sendOverlayMessage(MekanismLang.CONFIG_CARD_SET.translate(EnumColor.INDIGO, getConfigCardName(input)));
                            MekanismCriteriaTriggers.CONFIGURATION_CARD.value().trigger((ServerPlayer) player, false);
                        } else {
                            player.sendOverlayMessage(MekanismLang.CONFIG_CARD_UNEQUAL.translateColored(EnumColor.RED));
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @NotNull
    public InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            ItemStack configCard = player.getItemInHand(usedHand);
            if (!level.isClientSide()) {
                configCard.remove(MekanismDataComponents.CONFIGURATION_DATA);
                player.sendOverlayMessage(MekanismLang.CONFIG_CARD_CLEARED.translate());
            }
            //TODO - 26.1: Does this need to use a copy of the stack rather than directly removing the component above?
            return InteractionResult.SUCCESS.heldItemTransformedTo(configCard);
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

    private Component getConfigCardName(ValueInput input) {
        return input.getString(SerializationConstants.DATA_NAME)
              .map(TextComponentUtil::translate)
              .orElseGet(MekanismLang.NONE::translate);
    }

    private Component getConfigCardName(@Nullable CompoundTag data) {
        //TODO - 26.1: Do we want to change the caller of this to go via the value input method?
        if (data == null) {
            return MekanismLang.NONE.translate();
        }
        return data.getString(SerializationConstants.DATA_NAME)
              .map(TextComponentUtil::translate)
              .orElseGet(MekanismLang.NONE::translate);
    }

    public boolean hasData(ItemStack stack) {
        CompoundTag data = getData(stack);
        return data != null && data.contains(SerializationConstants.DATA_NAME);
    }

    private record ConfigurationCardPathElement(Block block, BlockPos pos) implements ProblemReporter.PathElement {
        @NotNull
        @Override
        public String get() {
            return "configuration_card(" + RegistryUtils.getNameForReporting(BuiltInRegistries.BLOCK, block) + "@" + pos + ")";
        }
    }
}