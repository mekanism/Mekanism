package mekanism.common.item.block.machine;

import java.util.List;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.chemical.variable.RateLimitGasTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidTank;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockPressurizedReactionChamber extends ItemBlockMachine {

    public ItemBlockPressurizedReactionChamber(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.FLUID.addDefaultContainer(null, this, stack -> RateLimitFluidTank.createBasicItem(TileEntityPressurizedReactionChamber.MAX_FLUID,
              BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi,
              fluid -> MekanismRecipeType.REACTION.getInputCache().containsInputB(null, fluid)
        ));
        ContainerType.GAS.addDefaultContainers(null, this, stack -> List.of(
              RateLimitGasTank.createBasicItem(TileEntityPressurizedReactionChamber.MAX_GAS,
                    ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                    gas -> MekanismRecipeType.REACTION.getInputCache().containsInputC(null, gas.getStack(1))
              ),
              RateLimitGasTank.createBasicItem(TileEntityPressurizedReactionChamber.MAX_GAS,
                    ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue
              )
        ));
    }
}