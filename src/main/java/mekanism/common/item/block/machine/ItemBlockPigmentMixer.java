package mekanism.common.item.block.machine;

import java.util.List;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.chemical.variable.RateLimitPigmentTank;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.machine.TileEntityPigmentMixer;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockPigmentMixer extends ItemBlockMachine {

    public ItemBlockPigmentMixer(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.PIGMENT.addDefaultContainers(null, this, stack -> List.of(
              RateLimitPigmentTank.createBasicItem(TileEntityPigmentMixer.MAX_INPUT_PIGMENT,
                    ChemicalTankBuilder.PIGMENT.manualOnly, ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
                    pigment -> MekanismRecipeType.PIGMENT_MIXING.getInputCache().containsInput(null, pigment.getStack(1))
              ),
              RateLimitPigmentTank.createBasicItem(TileEntityPigmentMixer.MAX_INPUT_PIGMENT,
                    ChemicalTankBuilder.PIGMENT.manualOnly, ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
                    pigment -> MekanismRecipeType.PIGMENT_MIXING.getInputCache().containsInput(null, pigment.getStack(1))
              ),
              RateLimitPigmentTank.createBasicItem(TileEntityPigmentMixer.MAX_OUTPUT_PIGMENT,
                    ChemicalTankBuilder.PIGMENT.manualOnly, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrue
              )
        ));
    }
}