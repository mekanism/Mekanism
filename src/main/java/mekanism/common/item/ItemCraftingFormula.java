package mekanism.common.item;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemCraftingFormula extends Item {

    public ItemCraftingFormula(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        Map<HashedItem, Integer> stacks = FormulaAttachment.existingFormula(itemStack)
              .stream().flatMap(attachment -> attachment.inventory().stream())
              .filter(stack -> !stack.isEmpty())
              .collect(Collectors.toMap(HashedItem::raw, ItemStack::getCount, Integer::sum, LinkedHashMap::new));
        if (!stacks.isEmpty()) {
            tooltip.add(MekanismLang.INGREDIENTS.translateColored(EnumColor.GRAY));
            for (Entry<HashedItem, Integer> entry : stacks.entrySet()) {
                tooltip.add(MekanismLang.GENERIC_TRANSFER.translateColored(EnumColor.GRAY, entry.getKey().getInternalStack(), entry.getValue()));
            }
        }
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                stack.remove(MekanismDataComponents.FORMULA_HOLDER);
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        Optional<FormulaAttachment> formulaAttachment = FormulaAttachment.existingFormula(stack)
              .filter(FormulaAttachment::hasItems);
        if (formulaAttachment.isPresent()) {
            FormulaAttachment attachment = formulaAttachment.get();
            if (attachment.invalid()) {
                return TextComponentUtil.build(super.getName(stack), " ", EnumColor.DARK_RED, MekanismLang.INVALID);
            }
            return TextComponentUtil.build(super.getName(stack), " ", EnumColor.DARK_GREEN, MekanismLang.ENCODED);
        }
        return super.getName(stack);
    }
}