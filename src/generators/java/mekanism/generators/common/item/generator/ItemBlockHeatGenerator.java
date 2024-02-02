package mekanism.generators.common.item.generator;

import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.tags.FluidTags;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockHeatGenerator extends ItemBlockMachine {

    public ItemBlockHeatGenerator(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.FLUID.addDefaultContainer(null, this, stack -> RateLimitFluidTank.createBasicItem(MekanismGeneratorsConfig.generators.heatTankCapacity,
              BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi,
              fluidStack -> fluidStack.is(FluidTags.LAVA)
        ));
        ContainerType.HEAT.addDefaultContainer(null, this, stack -> BasicHeatCapacitor.createBasicItem(TileEntityHeatGenerator.HEAT_CAPACITY,
              TileEntityHeatGenerator.INVERSE_CONDUCTION_COEFFICIENT, TileEntityHeatGenerator.INVERSE_INSULATION_COEFFICIENT
        ));
    }
}