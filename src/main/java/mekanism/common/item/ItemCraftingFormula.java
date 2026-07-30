package mekanism.common.item;

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
import net.minecraft.world.level.Level;

public class ItemCraftingFormula extends Item {

    public ItemCraftingFormula(Properties properties) {
        super(properties.component(MekanismDataComponents.FORMULA_HOLDER, FormulaComponent.EMPTY));
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