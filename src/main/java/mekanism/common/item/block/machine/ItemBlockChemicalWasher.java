package mekanism.common.item.block.machine;

import java.util.List;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.chemical.variable.RateLimitSlurryTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidTank;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.machine.TileEntityChemicalWasher;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockChemicalWasher extends ItemBlockMachine {

    public ItemBlockChemicalWasher(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.FLUID.addDefaultContainer(null, this, stack -> RateLimitFluidTank.createBasicItem(TileEntityChemicalWasher.MAX_FLUID,
              BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi,
              fluid -> MekanismRecipeType.WASHING.getInputCache().containsInputA(null, fluid)
        ));
        ContainerType.SLURRY.addDefaultContainers(null, this, stack -> List.of(
              RateLimitSlurryTank.createBasicItem(TileEntityChemicalWasher.MAX_SLURRY,
                    ChemicalTankBuilder.SLURRY.manualOnly, ChemicalTankBuilder.SLURRY.alwaysTrueBi,
                    slurry -> MekanismRecipeType.WASHING.getInputCache().containsInputB(null, slurry.getStack(1))
              ),
              RateLimitSlurryTank.createBasicItem(TileEntityChemicalWasher.MAX_SLURRY,
                    ChemicalTankBuilder.SLURRY.manualOnly, ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrue
              )
        ));
    }
}