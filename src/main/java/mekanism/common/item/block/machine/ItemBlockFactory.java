package mekanism.common.item.block.machine;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.prefab.BlockFactoryMachine.BlockFactory;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.tier.FactoryTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlockFactory extends ItemBlockTooltip<BlockTile<?, ?>> {

    public ItemBlockFactory(BlockFactory<?> block) {
        super(block);
    }

    @Override
    public FactoryTier getTier() {
        return Attribute.getTier(getBlock(), FactoryTier.class);
    }

    @Override
    protected void addTypeDetails(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        //Should always be present but validate it just in case
        AttributeFactoryType factoryType = Attribute.get(getBlock(), AttributeFactoryType.class);
        if (factoryType != null) {
            tooltip.add(MekanismLang.FACTORY_TYPE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, factoryType.getFactoryType()));
        }
        super.addTypeDetails(stack, world, tooltip, flag);
    }
}