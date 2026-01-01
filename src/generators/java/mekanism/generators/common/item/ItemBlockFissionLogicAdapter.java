package mekanism.generators.common.item;

import java.util.function.Consumer;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsDataComponents;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

public class ItemBlockFissionLogicAdapter extends ItemBlockTooltip<BlockBasicMultiblock<TileEntityFissionReactorLogicAdapter>> {

    public ItemBlockFissionLogicAdapter(BlockBasicMultiblock<TileEntityFissionReactorLogicAdapter> block, Item.Properties properties) {
        super(block, true, properties.component(GeneratorsDataComponents.FISSION_LOGIC_TYPE, FissionReactorLogic.DISABLED));
    }

    @Override
    protected void addDetails(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.addDetails(stack, context, tooltipDisplay, tooltipAdder, flag);
        FissionReactorLogic logicType = stack.getOrDefault(GeneratorsDataComponents.FISSION_LOGIC_TYPE, FissionReactorLogic.DISABLED);
        tooltipAdder.accept(GeneratorsLang.REACTOR_LOGIC_REDSTONE_MODE.translate(logicType.getColor(), logicType));
    }
}