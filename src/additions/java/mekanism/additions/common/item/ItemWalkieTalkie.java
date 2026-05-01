package mekanism.additions.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import mekanism.additions.common.AdditionsLang;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.registries.AdditionsDataComponents;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemWalkieTalkie extends Item implements IModeItem {

    public static int MAX_CHANNEL = 9;

    public ItemWalkieTalkie(Item.Properties properties) {
        super(properties.stacksTo(1).component(AdditionsDataComponents.WALKIE_DATA, WalkieData.DEFAULT));
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        stack.addToTooltip(AdditionsDataComponents.WALKIE_DATA.get(), context, tooltipDisplay, tooltipAdder, flag);
        if (!MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            tooltipAdder.accept(AdditionsLang.WALKIE_DISABLED.translateColored(EnumColor.DARK_RED));
        }
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            WalkieData data = stack.getOrDefault(AdditionsDataComponents.WALKIE_DATA, WalkieData.DEFAULT);
            stack.set(AdditionsDataComponents.WALKIE_DATA, new WalkieData(data.channel(), !data.running()));
            return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        WalkieData data = stack.getOrDefault(AdditionsDataComponents.WALKIE_DATA, WalkieData.DEFAULT);
        if (data.running()) {
            int newChannel = Math.floorMod(data.channel() + shift - 1, (MAX_CHANNEL - 1)) + 1;
            if (data.channel() != newChannel) {
                stack.set(AdditionsDataComponents.WALKIE_DATA, new WalkieData(newChannel, true));
                displayChange.sendMessage(player, newChannel, AdditionsLang.CHANNEL_CHANGE::translate);
            }
        }
    }

    @NotNull
    @Override
    public Component getScrollTextComponent(@NotNull ItemStack stack) {
        WalkieData data = stack.getOrDefault(AdditionsDataComponents.WALKIE_DATA, WalkieData.DEFAULT);
        return AdditionsLang.CHANNEL.translateColored(EnumColor.GRAY, EnumColor.WHITE, data.channel());
    }

    public record WalkieData(int channel, boolean running) implements TooltipProvider {

        public static final WalkieData DEFAULT = new WalkieData(1, false);

        public static final Codec<WalkieData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              ExtraCodecs.intRange(1, MAX_CHANNEL).fieldOf(SerializationConstants.CHANNEL).forGetter(WalkieData::channel),
              Codec.BOOL.fieldOf(SerializationConstants.RUNNING).forGetter(WalkieData::running)
        ).apply(instance, WalkieData::new));
        public static final StreamCodec<ByteBuf, WalkieData> STREAM_CODEC = StreamCodec.composite(
              ByteBufCodecs.VAR_INT, WalkieData::channel,
              ByteBufCodecs.BOOL, WalkieData::running,
              WalkieData::new
        );

        @Override
        public void addToTooltip(@NotNull TooltipContext context, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag, @NotNull DataComponentGetter componentGetter) {
            tooltipAdder.accept(OnOff.of(running(), true).getTextComponent());
            tooltipAdder.accept(AdditionsLang.CHANNEL.translateColored(EnumColor.DARK_AQUA, EnumColor.GRAY, channel()));
        }

        /// A stream of possible data values, with running = true, for datagen
        public static Stream<WalkieData> runningChannels() {
            return IntStream.range(1, MAX_CHANNEL).mapToObj(chan -> new WalkieData(chan, true));
        }
    }
}