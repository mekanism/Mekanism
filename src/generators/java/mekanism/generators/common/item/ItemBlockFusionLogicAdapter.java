package mekanism.generators.common.item;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsAttachmentTypes;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter.FusionReactorLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockFusionLogicAdapter extends ItemBlockTooltip<BlockBasicMultiblock<TileEntityFusionReactorLogicAdapter>> {

    public ItemBlockFusionLogicAdapter(BlockBasicMultiblock<TileEntityFusionReactorLogicAdapter> block, Properties properties) {
        super(block, true, properties);
    }

    @Override
    protected void addDetails(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.addDetails(stack, world, tooltip, flag);
        FusionReactorLogic logicType = stack.getData(GeneratorsAttachmentTypes.FUSION_LOGIC_TYPE);
        tooltip.add(GeneratorsLang.REACTOR_LOGIC_REDSTONE_MODE.translate(logicType.getColor(), logicType));
        tooltip.add(GeneratorsLang.REACTOR_LOGIC_ACTIVE_COOLING.translate(EnumColor.RED, OnOff.of(stack.getData(GeneratorsAttachmentTypes.ACTIVE_COOLED))));
    }
}