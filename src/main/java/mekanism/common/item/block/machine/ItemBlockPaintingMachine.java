package mekanism.common.item.block.machine;

import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.chemical.variable.RateLimitPigmentTank;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.machine.TileEntityPaintingMachine;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockPaintingMachine extends ItemBlockMachine {

    public ItemBlockPaintingMachine(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.PIGMENT.addDefaultContainer(null, this, stack -> RateLimitPigmentTank.createBasicItem(TileEntityPaintingMachine.MAX_PIGMENT,
              ChemicalTankBuilder.PIGMENT.manualOnly, ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
              pigment -> MekanismRecipeType.PAINTING.getInputCache().containsInputB(null, pigment.getStack(1))
        ));
    }
}