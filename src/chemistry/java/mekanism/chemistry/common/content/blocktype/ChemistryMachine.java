package mekanism.chemistry.common.content.blocktype;

import java.util.function.Supplier;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;

public class ChemistryMachine<TILE extends TileEntityMekanism> extends BlockTypeTile<TILE> {
    public ChemistryMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description) {
        super(tileEntityRegistrar, description);
        add(Attributes.ACTIVE_LIGHT, new AttributeStateFacing(), Attributes.SECURITY, Attributes.INVENTORY, Attributes.REDSTONE, Attributes.COMPARATOR);
    }

    public static class ChemistryMachineBuilder<MACHINE extends ChemistryMachine<TILE>, TILE extends TileEntityMekanism, T extends ChemistryMachineBuilder<MACHINE, TILE, T>> extends BlockTileBuilder<MACHINE, TILE, T> {
        protected ChemistryMachineBuilder(MACHINE holder) {
            super(holder);
        }

        public static <TILE extends TileEntityMekanism> ChemistryMachineBuilder<ChemistryMachine<TILE>, TILE, ?> createChemistryMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar,
              ILangEntry description) {
            return new ChemistryMachineBuilder<>(new ChemistryMachine<>(tileEntityRegistrar, description));
        }
    }
}
