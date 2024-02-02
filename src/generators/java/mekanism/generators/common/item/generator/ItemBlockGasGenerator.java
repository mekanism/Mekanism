package mekanism.generators.common.item.generator;

import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.attribute.GasAttributes.Fuel;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.chemical.variable.RateLimitGasTank;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockGasGenerator extends ItemBlockMachine {

    public ItemBlockGasGenerator(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.GAS.addDefaultContainer(null, this, stack -> RateLimitGasTank.createBasicItem(MekanismGeneratorsConfig.generators.gbgTankCapacity,
              ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
              gas -> gas.has(Fuel.class)
        ));
    }
}