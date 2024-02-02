package mekanism.common.item.block.machine;

import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.chemical.variable.RateLimitGasTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidTank;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockRotaryCondensentrator extends ItemBlockMachine {

    public ItemBlockRotaryCondensentrator(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.FLUID.addDefaultContainer(null, this, stack -> RateLimitFluidTank.createBasicItem(TileEntityRotaryCondensentrator.CAPACITY,
              BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi,
              fluid -> MekanismRecipeType.ROTARY.getInputCache().containsInput(null, fluid)
        ));
        ContainerType.GAS.addDefaultContainer(null, this, stack -> RateLimitGasTank.createBasicItem(TileEntityRotaryCondensentrator.CAPACITY,
              ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
              gas -> MekanismRecipeType.ROTARY.getInputCache().containsInput(null, gas.getStack(1))
        ));
    }
}