package mekanism.generators.common.item;

import java.util.function.Consumer;
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
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class ItemBlockFusionLogicAdapter extends ItemBlockTooltip<BlockBasicMultiblock<TileEntityFusionReactorLogicAdapter>> {

    public ItemBlockFusionLogicAdapter(BlockBasicMultiblock<TileEntityFusionReactorLogicAdapter> block, Properties properties) {
        super(block, true, properties.component(GeneratorsDataComponents.FUSION_LOGIC_TYPE, FusionReactorLogic.DISABLED));
    }

    @Override
    protected void addDetails(ItemStack stack, ItemAccess itemAccess, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
          Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.addDetails(stack, itemAccess, context, tooltipDisplay, tooltipAdder, flag);
        FusionReactorLogic logicType = stack.getOrDefault(GeneratorsDataComponents.FUSION_LOGIC_TYPE, FusionReactorLogic.DISABLED);
        tooltipAdder.accept(GeneratorsLang.REACTOR_LOGIC_REDSTONE_MODE.translate(logicType.getColor(), logicType));
        tooltipAdder.accept(GeneratorsLang.REACTOR_LOGIC_ACTIVE_COOLING.translate(EnumColor.RED, OnOff.of(stack.getOrDefault(GeneratorsDataComponents.ACTIVE_COOLED, false))));
    }
}