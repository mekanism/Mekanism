package mekanism.common.registration.impl;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IFluidProvider;
import mekanism.common.registration.INamedEntry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class FluidRegistryObject<TYPE extends FluidType, STILL extends Fluid, FLOWING extends Fluid, BLOCK extends LiquidBlock, BUCKET extends BucketItem>
    extends DeferredHolder<Fluid, STILL> implements IFluidProvider, INamedEntry {

    private final DeferredHolder<FluidType, TYPE> fluidType;
    private final DeferredHolder<Fluid, FLOWING> flowing;
    private final DeferredHolder<Item, BUCKET> bucket;
    private final DeferredHolder<Block, BLOCK> block;

    FluidRegistryObject(DeferredHolder<FluidType, TYPE> fluidType, DeferredHolder<Fluid, STILL> still, DeferredHolder<Fluid, FLOWING> flowing,
          DeferredHolder<Item, BUCKET> bucket, DeferredHolder<Block, BLOCK> block) {
        //Default our fluid to being the still variant
        super(still.getKey());
        this.fluidType = fluidType;
        this.flowing = flowing;
        this.bucket = bucket;
        this.block = block;
    }

    public TYPE getFluidType() {
        return fluidType.get();
    }

    public DeferredHolder<Fluid, FLOWING> getFlowingFluid() {
        return flowing;
    }

    public BLOCK getBlock() {
        return block.get();
    }

    public BUCKET getBucket() {
        return bucket.get();
    }

    @Override
    public STILL getFluid() {
        return get();
    }

    @NotNull
    @Override
    public Holder<Fluid> getFluidHolder() {
        return this;
    }

    @Override
    public String getName() {
        return INamedEntry.super.getName();
    }

    @NotNull
    @Override
    public ResourceLocation getRegistryName() {
        return getId();
    }
}