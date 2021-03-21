package mekanism.client.state;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.client.model.BaseBlockModelProvider;
import mekanism.common.DataGenJsonConstants;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class BaseBlockStateProvider<PROVIDER extends BaseBlockModelProvider> extends BlockStateProvider {

    private final String modid;
    private final PROVIDER modelProvider;

    public BaseBlockStateProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper,
          BiFunction<DataGenerator, ExistingFileHelper, PROVIDER> providerCreator) {
        super(generator, modid, existingFileHelper);
        this.modid = modid;
        modelProvider = providerCreator.apply(generator, existingFileHelper);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Block state provider: " + modid;
    }

    @Override
    public PROVIDER models() {
        return modelProvider;
    }

    protected VariantBlockStateBuilder getVariantBuilder(IBlockProvider blockProvider) {
        return getVariantBuilder(blockProvider.getBlock());
    }

    protected String name(Block block) {
        return block.getRegistryName().getPath();
    }

    protected void registerFluidBlockStates(List<FluidRegistryObject<?, ?, ?, ?>> fluidROs) {
        for (FluidRegistryObject<?, ?, ?, ?> fluidRO : fluidROs) {
            simpleBlock(fluidRO.getBlock(), models().getBuilder(name(fluidRO.getBlock())).texture(DataGenJsonConstants.PARTICLE,
                  fluidRO.getStillFluid().getAttributes().getStillTexture()));
        }
    }

    /**
     * Like directionalBlock but allows us to skip specific properties
     */
    protected void directionalBlock(Block block, Function<BlockState, ModelFile> modelFunc, int angleOffset, Property<?>... toSkip) {
        getVariantBuilder(block).forAllStatesExcept(state -> {
            Direction dir = state.getValue(BlockStateProperties.FACING);
            return ConfiguredModel.builder()
                  .modelFile(modelFunc.apply(state))
                  .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                  .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + angleOffset) % 360)
                  .build();
        }, toSkip);
    }
}