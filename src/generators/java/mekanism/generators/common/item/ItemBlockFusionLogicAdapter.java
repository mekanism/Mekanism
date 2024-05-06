package mekanism.generators.common.item;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsDataComponents;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter.FusionReactorLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

public class ItemBlockFusionLogicAdapter extends ItemBlockTooltip<BlockBasicMultiblock<TileEntityFusionReactorLogicAdapter>> {

    public ItemBlockFusionLogicAdapter(BlockBasicMultiblock<TileEntityFusionReactorLogicAdapter> block, Properties properties) {
        super(block, true, properties.component(GeneratorsDataComponents.FUSION_LOGIC_TYPE, FusionReactorLogic.DISABLED));
    }

    @Override
    protected void addDetails(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.addDetails(stack, context, tooltip, flag);
        FusionReactorLogic logicType = stack.getOrDefault(GeneratorsDataComponents.FUSION_LOGIC_TYPE, FusionReactorLogic.DISABLED);
        tooltip.add(GeneratorsLang.REACTOR_LOGIC_REDSTONE_MODE.translate(logicType.getColor(), logicType));
        tooltip.add(GeneratorsLang.REACTOR_LOGIC_ACTIVE_COOLING.translate(EnumColor.RED, OnOff.of(stack.getOrDefault(GeneratorsDataComponents.ACTIVE_COOLED, false))));
    }
}