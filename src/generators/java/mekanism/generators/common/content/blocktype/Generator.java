package mekanism.generators.common.content.blocktype;

import java.util.function.Supplier;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;

public class Generator<TILE extends TileEntityMekanism> extends BlockTypeTile<TILE> {

    public Generator(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description) {
        super(tileEntityRegistrar, description);
        add(Attributes.ACTIVE_LIGHT, new AttributeStateFacing(), Attributes.SECURITY, Attributes.INVENTORY, Attributes.REDSTONE, Attributes.COMPARATOR);
    }

    public static class GeneratorBuilder<GENERATOR extends Generator<TILE>, TILE extends TileEntityMekanism, T extends GeneratorBuilder<GENERATOR, TILE, T>> extends BlockTileBuilder<GENERATOR, TILE, T> {

        protected GeneratorBuilder(GENERATOR holder) {
            super(holder);
        }

        public static <TILE extends TileEntityMekanism> GeneratorBuilder<Generator<TILE>, TILE, ?> createGenerator(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar,
              ILangEntry description) {
            return new GeneratorBuilder<>(new Generator<>(tileEntityRegistrar, description));
        }
    }
}
