package mekanism.generators.common.item;

import java.util.List;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.registries.GeneratorsAttachmentTypes;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockFissionLogicAdapter extends ItemBlockTooltip<BlockBasicMultiblock<TileEntityFissionReactorLogicAdapter>> {

    public ItemBlockFissionLogicAdapter(BlockBasicMultiblock<TileEntityFissionReactorLogicAdapter> block, Item.Properties properties) {
        super(block, true, properties);
    }

    @Override
    protected void addDetails(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.addDetails(stack, world, tooltip, flag);
        FissionReactorLogic logicType = stack.getData(GeneratorsAttachmentTypes.FISSION_LOGIC_TYPE);
        tooltip.add(GeneratorsLang.REACTOR_LOGIC_REDSTONE_MODE.translate(logicType.getColor(), logicType));
    }
}