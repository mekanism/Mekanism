package mekanism.client.state;

import java.util.function.BiFunction;
import java.util.function.Function;
import mekanism.api.providers.IBlockProvider;
import mekanism.client.model.BaseBlockModelProvider;
import mekanism.common.DataGenJsonConstants;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidDeferredRegister.MekanismFluidType;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public abstract class BaseBlockStateProvider<PROVIDER extends BaseBlockModelProvider> extends BlockStateProvider {

    private final String modid;
    private final PROVIDER modelProvider;

    public BaseBlockStateProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper,
          BiFunction<PackOutput, ExistingFileHelper, PROVIDER> providerCreator) {
        super(output, modid, existingFileHelper);
        this.modid = modid;
        modelProvider = providerCreator.apply(output, existingFileHelper);
    }

    @NotNull
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

    protected void registerFluidBlockStates(FluidDeferredRegister register) {
        for (Holder<Block> blockEntry : register.getBlockEntries()) {
            //Note: We expect this to always be the case
            if (blockEntry.value() instanceof LiquidBlock block && block.fluid.getFluidType() instanceof MekanismFluidType fluidType) {
                simpleBlock(block, models().getBuilder(RegistryUtils.getPath(block)).texture(DataGenJsonConstants.PARTICLE, fluidType.stillTexture));
            }
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

    protected void simpleBlockItem(IBlockProvider block, ModelFile model) {
        super.simpleBlockItem(block.getBlock(), model);
    }
}