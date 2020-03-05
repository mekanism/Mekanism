package mekanism.generators.common.content.blocktype;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.api.Pos3D;
import mekanism.common.base.ILangEntry;
import mekanism.common.content.blocktype.BlockTile;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.particles.IParticleData;

public class Generator<TILE extends TileEntityMekanism> extends BlockTile<TILE> {

    protected DoubleSupplier energyStorage;

    protected GeneratorsLang description;

    protected List<Function<Random, GeneratorParticle>> particleFunctions = new ArrayList<>();

    public Generator(TileEntityTypeRegistryObject<TILE> tileEntityRegistrar, ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistrar, GeneratorsLang description) {
        super(tileEntityRegistrar);
        this.containerRegistrar = containerRegistrar;
        this.description = description;
    }

    @Nonnull
    public ILangEntry getDescription() {
        return description;
    }

    public double getConfigStorage() {
        return energyStorage.getAsDouble();
    }

    public boolean hasConfigStorage() {
        return energyStorage != null;
    }

    public List<Function<Random, GeneratorParticle>> getParticleFunctions() {
        return particleFunctions;
    }

    public static class GeneratorParticle {

        private IParticleData type;
        private Pos3D pos;

        public GeneratorParticle(IParticleData type, Pos3D pos) {
            this.type = type;
            this.pos = pos;
        }

        public IParticleData getType() {
            return type;
        }

        public Pos3D getPos() {
            return pos;
        }
    }

    public static class GeneratorBuilder<GENERATOR extends Generator<TILE>, TILE extends TileEntityMekanism, T extends GeneratorBuilder<GENERATOR, TILE, T>> extends BlockTileBuilder<GENERATOR, TILE, T> {

        protected GeneratorBuilder(GENERATOR holder) {
            super(holder);
        }

        public static <TILE extends TileEntityMekanism> GeneratorBuilder<Generator<TILE>, TILE, ?> createGenerator(TileEntityTypeRegistryObject<TILE> tileEntityRegistrar,
              ContainerTypeRegistryObject<MekanismTileContainer<TILE>> containerRegistrar, GeneratorsLang description) {
            return new GeneratorBuilder<>(new Generator<TILE>(tileEntityRegistrar, containerRegistrar, description));
        }

        public T withConfig(DoubleSupplier energyStorage) {
            holder.energyStorage = energyStorage;
            return getThis();
        }

        public T addParticleFX(IParticleData type, Function<Random, Pos3D> posSupplier) {
            holder.particleFunctions.add((random) -> new GeneratorParticle(type, posSupplier.apply(random)));
            return getThis();
        }

        @Override
        public GENERATOR build() {
            return holder;
        }
    }
}
