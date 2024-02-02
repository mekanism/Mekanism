package mekanism.common.item.block.machine;

import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.chemical.variable.RateLimitInfusionTank;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockMetallurgicInfuser extends ItemBlockMachine {

    public ItemBlockMetallurgicInfuser(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.INFUSION.addDefaultContainer(null, this, stack -> RateLimitInfusionTank.createBasicItem(TileEntityMetallurgicInfuser.MAX_INFUSE,
              ChemicalTankBuilder.INFUSION.manualOnly, ChemicalTankBuilder.INFUSION.alwaysTrueBi,
              infuseType -> MekanismRecipeType.METALLURGIC_INFUSING.getInputCache().containsInputB(null, infuseType.getStack(1))
        ));
    }
}