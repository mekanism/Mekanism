package mekanism.common.registration.impl;

import java.util.Objects;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.providers.IFluidProvider;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

@ParametersAreNotNullByDefault
@MethodsReturnNonnullByDefault
public class FluidRegistryObject<TYPE extends FluidType, STILL extends Fluid, FLOWING extends Fluid, BLOCK extends LiquidBlock, BUCKET extends BucketItem>
      implements IFluidProvider {

    private DeferredHolder<FluidType, TYPE> fluidTypeRO;
    private DeferredHolder<Fluid, STILL> stillRO;
    private DeferredHolder<Fluid, FLOWING> flowingRO;
    private DeferredHolder<Block, BLOCK> blockRO;
    private DeferredHolder<Item, BUCKET> bucketRO;

    public TYPE getFluidType() {
        return fluidTypeRO.get();
    }

    public STILL getStillFluid() {
        return stillRO.get();
    }

    public FLOWING getFlowingFluid() {
        return flowingRO.get();
    }

    public BLOCK getBlock() {
        return blockRO.get();
    }

    public BUCKET getBucket() {
        return bucketRO.get();
    }

    //Make sure these update methods are package local as only the FluidDeferredRegister should be messing with them
    void updateFluidType(DeferredHolder<FluidType, TYPE> fluidTypeRO) {
        this.fluidTypeRO = Objects.requireNonNull(fluidTypeRO);
    }

    void updateStill(DeferredHolder<Fluid, STILL> stillRO) {
        this.stillRO = Objects.requireNonNull(stillRO);
    }

    void updateFlowing(DeferredHolder<Fluid, FLOWING> flowingRO) {
        this.flowingRO = Objects.requireNonNull(flowingRO);
    }

    void updateBlock(DeferredHolder<Block, BLOCK> blockRO) {
        this.blockRO = Objects.requireNonNull(blockRO);
    }

    void updateBucket(DeferredHolder<Item, BUCKET> bucketRO) {
        this.bucketRO = Objects.requireNonNull(bucketRO);
    }

    @Override
    public STILL getFluid() {
        //Default our fluid to being the still variant
        return getStillFluid();
    }
}