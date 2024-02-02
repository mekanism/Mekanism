package mekanism.common.item.block.machine;

import java.util.List;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.chemical.variable.RateLimitGasTank;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockChemicalDissolutionChamber extends ItemBlockMachine {

    public ItemBlockChemicalDissolutionChamber(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.GAS.addDefaultContainers(null, this, stack -> List.of(
              RateLimitGasTank.createBasicItem(TileEntityChemicalDissolutionChamber.MAX_CHEMICAL,
                    ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
                    gas -> MekanismRecipeType.DISSOLUTION.getInputCache().containsInputB(null, gas.getStack(1))
              ),
              stack.getData(MekanismAttachmentTypes.CDC_CONTENTS_HANDLER).getGasTank()
        ));
        ContainerType.INFUSION.addDefaultContainer(null, this, stack -> stack.getData(MekanismAttachmentTypes.CDC_CONTENTS_HANDLER).getInfusionTank());
        ContainerType.PIGMENT.addDefaultContainer(null, this, stack -> stack.getData(MekanismAttachmentTypes.CDC_CONTENTS_HANDLER).getPigmentTank());
        ContainerType.SLURRY.addDefaultContainer(null, this, stack -> stack.getData(MekanismAttachmentTypes.CDC_CONTENTS_HANDLER).getSlurryTank());
    }
}