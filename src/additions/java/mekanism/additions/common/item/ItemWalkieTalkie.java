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
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.core.TypedInstance;
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
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ItemWalkieTalkie extends Item implements IModeItem {

    public static final int MAX_CHANNEL = 9;

    public ItemWalkieTalkie(Item.Properties properties) {
        super(properties.stacksTo(1).component(AdditionsDataComponents.WALKIE_DATA, WalkieData.DEFAULT));
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        stack.addToTooltip(AdditionsDataComponents.WALKIE_DATA.get(), context, tooltipDisplay, tooltipAdder, flag);
        if (!MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            tooltipAdder.accept(AdditionsLang.WALKIE_DISABLED.translateColored(EnumColor.DARK_RED));
        }
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        WalkieData data = stack.getOrDefault(AdditionsDataComponents.WALKIE_DATA, WalkieData.DEFAULT);
        stack.set(AdditionsDataComponents.WALKIE_DATA, new WalkieData(data.channel(), !data.running()));
        return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void changeMode(Player player, ItemAccess itemAccess, int shift, DisplayChange displayChange, TransactionContext transaction) {
        ItemResource resource = itemAccess.getResource();
        WalkieData data = resource.getOrDefault(AdditionsDataComponents.WALKIE_DATA, WalkieData.DEFAULT);
        if (data.running()) {
            int newChannel = Math.floorMod(data.channel() + shift - 1, MAX_CHANNEL - 1) + 1;
            if (data.channel() != newChannel && ItemAccessUtils.exchange(itemAccess, resource.with(AdditionsDataComponents.WALKIE_DATA, new WalkieData(newChannel, true)), transaction)) {
                displayChange.sendMessage(player, newChannel, AdditionsLang.CHANNEL_CHANGE::translate);
            }
        }
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> Component getScrollTextComponent(ITEM instance) {
        WalkieData data = instance.getOrDefault(AdditionsDataComponents.WALKIE_DATA, WalkieData.DEFAULT);
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
        public void addToTooltip(TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
            tooltipAdder.accept(OnOff.of(running(), true).getTextComponent());
            tooltipAdder.accept(AdditionsLang.CHANNEL.translateColored(EnumColor.DARK_AQUA, EnumColor.GRAY, channel()));
        }

        /// A stream of possible data values, with running = true, for datagen
        public static Stream<WalkieData> runningChannels() {
            return IntStream.range(1, MAX_CHANNEL).mapToObj(chan -> new WalkieData(chan, true));
        }
    }
}