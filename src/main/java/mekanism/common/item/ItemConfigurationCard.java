package mekanism.common.item;

import com.mojang.serialization.Codec;
import java.util.Optional;
import mekanism.api.IConfigCardAccess;
import mekanism.api.SerializationConstants;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.component.ConfigurationData;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class ItemConfigurationCard extends Item {

    private static final Codec<Block> BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec();

    public ItemConfigurationCard(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON).component(MekanismDataComponents.CONFIGURATION_DATA.get(), ConfigurationData.NONE));
    }

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
        if (configCardAccess == null) {
            return InteractionResult.PASS;
        } else if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos)) {
            return InteractionResult.FAIL;
        }
        //TODO - 26.2: Figure out if there is any other information we want to include in the problem path
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
                    stack.set(MekanismDataComponents.CONFIGURATION_DATA, new ConfigurationData(output.buildResult()));
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
                } else if (!world.isClientSide()) {
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

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, usedHand);
        } else if (!level.isClientSide()) {
            player.sendOverlayMessage(MekanismLang.CONFIG_CARD_CLEARED.translate());
        }
        ItemStack configCard = player.getItemInHand(usedHand);
        configCard.set(MekanismDataComponents.CONFIGURATION_DATA, ConfigurationData.NONE);
        //TODO - 26.2: Does this need to use a copy of the stack rather than directly removing the component above? Check other implementations of use as well
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(configCard);
    }

    @Nullable
    private CompoundTag getData(ItemStack stack) {
        return stack.getOrDefault(MekanismDataComponents.CONFIGURATION_DATA, ConfigurationData.NONE).configuration();
    }

    private Component getConfigCardName(ValueInput input) {
        return input.getString(SerializationConstants.DATA_NAME)
              .map(TextComponentUtil::translate)
              .orElseGet(MekanismLang.NONE::translate);
    }

    public boolean hasData(ItemStack stack) {
        CompoundTag data = getData(stack);
        return data != null && data.contains(SerializationConstants.DATA_NAME);
    }

    private record ConfigurationCardPathElement(Block block, BlockPos pos) implements ProblemReporter.PathElement {
        @Override
        public String get() {
            return "configuration_card(" + RegistryUtils.getNameForReporting(BuiltInRegistries.BLOCK, block) + "@" + pos + ")";
        }
    }
}