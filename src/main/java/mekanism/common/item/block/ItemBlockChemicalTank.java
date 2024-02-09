package mekanism.common.item.block;

import java.util.List;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockChemicalTank extends ItemBlockTooltip<BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>> {

    public ItemBlockChemicalTank(BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>> block) {
        super(block);
    }

    @Override
    public ChemicalTankTier getTier() {
        return Attribute.getTier(getBlock(), ChemicalTankTier.class);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        ChemicalTankTier tier = getTier();
        StorageUtils.addStoredSubstance(stack, tooltip, tier == ChemicalTankTier.CREATIVE);
        if (tier == ChemicalTankTier.CREATIVE) {
            tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
        } else {
            tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getStorage())));
        }
        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        // No bar for empty containers as bars are drawn on top of stack count number
        return ChemicalUtil.hasGas(stack) ||
               ChemicalUtil.hasChemical(stack, ConstantPredicates.alwaysTrue(), Capabilities.INFUSION.item()) ||
               ChemicalUtil.hasChemical(stack, ConstantPredicates.alwaysTrue(), Capabilities.PIGMENT.item()) ||
               ChemicalUtil.hasChemical(stack, ConstantPredicates.alwaysTrue(), Capabilities.SLURRY.item());
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return ChemicalUtil.getRGBDurabilityForDisplay(stack);
    }
}