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
import mekanism.common.component.FormulaComponent;
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

public class ItemCraftingFormula extends Item {

    public ItemCraftingFormula(Properties properties) {
        super(properties.component(MekanismDataComponents.FORMULA_HOLDER, FormulaComponent.EMPTY));
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        Map<ItemResource, Integer> stacks = stack.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaComponent.EMPTY).nonEmptyItems()
              .collect(Collectors.toMap(Function.identity(), _ -> 1, Integer::sum, LinkedHashMap::new));
        if (!stacks.isEmpty()) {
            tooltipAdder.accept(MekanismLang.INGREDIENTS.translateColored(EnumColor.GRAY));
            for (Entry<ItemResource, Integer> entry : stacks.entrySet()) {
                tooltipAdder.accept(MekanismLang.GENERIC_TRANSFER.translateColored(EnumColor.GRAY, entry.getKey(), entry.getValue()));
            }
        }
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        stack.set(MekanismDataComponents.FORMULA_HOLDER, FormulaComponent.EMPTY);
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        FormulaComponent attachment = stack.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaComponent.EMPTY);
        if (attachment.hasItems()) {
            if (attachment.invalid()) {
                return TextComponentUtil.build(super.getName(stack), " ", EnumColor.DARK_RED, MekanismLang.INVALID);
            }
            return TextComponentUtil.build(super.getName(stack), " ", EnumColor.DARK_GREEN, MekanismLang.ENCODED);
        }
        return super.getName(stack);
    }
}