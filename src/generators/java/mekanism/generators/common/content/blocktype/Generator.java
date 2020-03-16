package mekanism.generators.common.content.blocktype;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.content.blocktype.BlockTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.generators.common.GeneratorsLang;

public class Generator<TILE extends TileEntityMekanism> extends BlockTile<TILE> {

    protected DoubleSupplier energyStorage;

    protected GeneratorsLang description;

    public Generator(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description) {
        super(tileEntityRegistrar, description);

        attributeMap.put(AttributeStateActive.class, new AttributeStateActive());
    }

    public static class GeneratorBuilder<GENERATOR extends Generator<TILE>, TILE extends TileEntityMekanism, T extends GeneratorBuilder<GENERATOR, TILE, T>> extends BlockTileBuilder<GENERATOR, TILE, T> {

        protected GeneratorBuilder(GENERATOR holder) {
            super(holder);
        }

        public static <TILE extends TileEntityMekanism> GeneratorBuilder<Generator<TILE>, TILE, ?> createGenerator(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar,
              ILangEntry description) {
            return new GeneratorBuilder<>(new Generator<TILE>(tileEntityRegistrar, description));
        }
    }
}
