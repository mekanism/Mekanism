package mekanism.generators.common.item.generator;

import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidTank;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockBioGenerator extends ItemBlockMachine {

    public ItemBlockBioGenerator(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.FLUID.addDefaultContainer(null, this, stack -> RateLimitFluidTank.createBasicItem(MekanismGeneratorsConfig.generators.bioTankCapacity,
              BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi,
              fluidStack -> fluidStack.is(GeneratorTags.Fluids.BIOETHANOL)
        ));
    }
}