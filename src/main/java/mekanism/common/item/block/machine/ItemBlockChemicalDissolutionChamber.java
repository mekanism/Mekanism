package mekanism.common.item.block.machine;

import java.util.function.Consumer;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemBlockChemicalDissolutionChamber extends ItemBlockMachine {

    public ItemBlockChemicalDissolutionChamber(BlockTile<TileEntityChemicalDissolutionChamber, Machine<TileEntityChemicalDissolutionChamber>> block) {
        super(block);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.dissolution());
    }
}