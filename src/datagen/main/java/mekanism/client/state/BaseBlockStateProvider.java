package mekanism.client.state;

import java.util.function.Function;
import mekanism.client.model.BaseBlockModelProvider;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

public abstract class BaseBlockStateProvider<PROVIDER extends BaseBlockModelProvider> /*extends BlockStateProvider */ {

    private final String modid;
    private final PROVIDER modelProvider;

    public BaseBlockStateProvider(PackOutput output, String modid,
          /*Bi*/Function<PackOutput, /*ExistingFileHelper,*/ PROVIDER> providerCreator) {
        //super(output, modid, existingFileHelper);
        this.modid = modid;
        modelProvider = providerCreator.apply(output);
    }

    @NotNull
    //@Override
    public String getName() {
        return "Block state provider: " + modid;
    }

    //@Override
    public PROVIDER models() {
        return modelProvider;
    }

    protected String getPath(Holder<Block> holder) {
        return RegistryUtils.getName(holder, BuiltInRegistries.BLOCK).getPath();
    }

    protected /*VariantBlockStateBuilder*/Object getVariantBuilder(Holder<Block> blockProvider) {
        return null;/*getVariantBuilder(blockProvider.value());*/
    }

    public void simpleBlock(Holder<Block> block, /*ModelFile*/ Object model) {
        //simpleBlock(block.value(), model);
    }

    public void directionalBlock(Holder<Block> block, Function<BlockState, /*ModelFile*/Object> modelFunc) {
        //directionalBlock(block.value(), modelFunc);
    }

    protected void registerFluidBlockStates(FluidDeferredRegister register) {
        // -> mekanism.client.model.BaseModelProvider.registerFluidBlockStates
    }

    /**
     * Like directionalBlock but allows us to skip specific properties
     */
    protected void directionalBlock(Holder<Block> block, Function<BlockState, /*ModelFile*/Object> modelFunc, int angleOffset, Property<?>... toSkip) {
        /*getVariantBuilder(block).forAllStatesExcept(state -> {
            Direction dir = state.getValue(BlockStateProperties.FACING);
            return ConfiguredModel.builder()
                  .modelFile(modelFunc.apply(state))
                  .rotationX(dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0)
                  .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + angleOffset) % 360)
                  .build();
        }, toSkip);*/
    }
}