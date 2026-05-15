package mekanism.common.item;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;

public class ItemCraftingFormula extends Item {

    public ItemCraftingFormula(Properties properties) {
        super(properties.component(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY));
    }

    @Override
    @Deprecated
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        Map<ItemResource, Integer> stacks = stack.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY).nonEmptyItems()
              .collect(Collectors.toMap(Function.identity(), _ -> 1, Integer::sum, LinkedHashMap::new));
        if (!stacks.isEmpty()) {
            tooltipAdder.accept(MekanismLang.INGREDIENTS.translateColored(EnumColor.GRAY));
            for (Entry<ItemResource, Integer> entry : stacks.entrySet()) {
                tooltipAdder.accept(MekanismLang.GENERIC_TRANSFER.translateColored(EnumColor.GRAY, entry.getKey(), entry.getValue()));
            }
        }
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!world.isClientSide()) {
                stack.set(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY);
            }
            return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(stack);
        }
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        FormulaAttachment attachment = stack.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY);
        if (attachment.hasItems()) {
            if (attachment.invalid()) {
                return TextComponentUtil.build(super.getName(stack), " ", EnumColor.DARK_RED, MekanismLang.INVALID);
            }
            return TextComponentUtil.build(super.getName(stack), " ", EnumColor.DARK_GREEN, MekanismLang.ENCODED);
        }
        return super.getName(stack);
    }
}