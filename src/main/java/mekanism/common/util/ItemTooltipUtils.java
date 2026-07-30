package mekanism.common.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.tooltip.TooltipAppender;
import org.jspecify.annotations.Nullable;

public class ItemTooltipUtils {

    private ItemTooltipUtils() {
    }

    public static boolean shouldDisplayDetails(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag) {
        TooltipDisplayType displayType = shouldDisplay(context, player, flag);
        //Always allow the server to view the details as if it was displaying all information
        return displayType != TooltipDisplayType.CLIENT || MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey);
    }

    public static boolean shouldDisplayDescription(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag) {
        TooltipDisplayType displayType = shouldDisplay(context, player, flag);
        //Always allow the server to view the details as if it was displaying all information
        return displayType != TooltipDisplayType.CLIENT || MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey);
    }

    private static TooltipDisplayType shouldDisplay(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag) {
        if (flag.shouldDisplayAllInformation()) {
            return TooltipDisplayType.ALWAYS;
        }
        Level level = context.level();
        if (level == null) {
            //If we don't have anywhere to see if the tooltip is being retrieved on the client side, fall back to grabbing it from the dist
            return TooltipDisplayType.of(player == null ? FMLEnvironment.getDist().isClient() : player.level().isClientSide());
        }
        return TooltipDisplayType.of(level.isClientSide());
    }

    //TODO - 26.2: Do we maybe want to add a client config to control if the key actually needs to be pressed?
    private static boolean shouldHideDetails(TooltipHideType hideType, ItemStack stack, Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag flag) {
        //Treat all information as not hiding details
        if (hideType.checkDetails() && !flag.shouldDisplayAllInformation() && stack.has(MekanismDataComponents.DETAILS)) {
            return !display.shows(MekanismDataComponents.DETAILS.get()) || !shouldDisplayDetails(context, player, flag);
        }
        return false;
    }

    /// Like [TooltipAppender#createComponentAppender(DataComponentType)] but allows for us to add any extra checks we want to make before displaying our components.
    public static <TYPE extends TooltipProvider> TooltipAppender createComponentAppender(Supplier<? extends DataComponentType<TYPE>> type) {
        return createSimpleAppender(type, TooltipDataAppender.TOOLTIP_PROVIDER);
    }

    public static <TYPE> TooltipAppender createTrivialAppender(Supplier<? extends DataComponentType<TYPE>> type, Function<TYPE, Component> tooltip) {
        return createSimpleAppender(type, (_, value, _, _, _, _, builder) -> builder.accept(tooltip.apply(value)));
    }

    public static <TYPE> TooltipAppender createSimpleAppender(Supplier<? extends DataComponentType<TYPE>> typeSupplier, TooltipDataAppender<? super TYPE> appender) {
        return createSimpleAppender(typeSupplier, TooltipHideType.ANY, appender);
    }

    /// Based off of [ItemStack#addToTooltip(DataComponentType, Item.TooltipContext, TooltipDisplay, Consumer, TooltipFlag)] that validates the component is present and
    /// should be displayed.
    public static <TYPE> TooltipAppender createSimpleAppender(Supplier<? extends DataComponentType<TYPE>> typeSupplier, TooltipHideType hideType, TooltipDataAppender<? super TYPE> appender) {
        DataComponentType<TYPE> type = typeSupplier.get();
        return (stack, context, display, player, flag, builder) -> {
            TYPE component = stack.get(type);
            if (component != null && display.shows(type)) {
                if (!shouldHideDetails(hideType, stack, context, display, player, flag)) {
                    appender.append(stack, component, context, display, player, flag, builder);
                }
            }
        };
    }

    @FunctionalInterface
    public interface TooltipDataAppender<TYPE> {

        TooltipDataAppender<TooltipProvider> TOOLTIP_PROVIDER = (stack, provider, context, _, _, flag, builder) -> provider.addToTooltip(context, builder, flag, stack.getComponents());

        void append(ItemStack stack, TYPE type, TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag flag, Consumer<Component> builder);
    }

    public enum TooltipHideType {
        NONE,
        DETAILS,
        DESCRIPTION,//TODO - 26.2: Figure out how we want to handle hiding of things when displaying the description
        ANY;

        public boolean checkDetails() {
            return this == DETAILS || this == ANY;
        }
    }

    private enum TooltipDisplayType {
        ALWAYS,
        CLIENT,
        SERVER;

        public static TooltipDisplayType of(boolean isClient) {
            return isClient ? CLIENT : SERVER;
        }
    }
}