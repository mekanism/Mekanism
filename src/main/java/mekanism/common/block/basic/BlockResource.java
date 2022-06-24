package mekanism.common.block.basic;

import mekanism.common.block.BlockMekanism;
import mekanism.common.resource.BlockResourceInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;

public class BlockResource extends BlockMekanism {

    @NotNull
    private final BlockResourceInfo resource;

    //TODO: Isn't as "generic"? So make it be from one BlockType thing?
    public BlockResource(@NotNull BlockResourceInfo resource) {
        super(BlockBehaviour.Properties.of(resource.getMaterial(), resource.getMaterialColor()).strength(resource.getHardness(), resource.getResistance())
              .lightLevel(state -> resource.getLightValue()).requiresCorrectToolForDrops());
        this.resource = resource;
    }

    @NotNull
    public BlockResourceInfo getResourceInfo() {
        return resource;
    }

    @NotNull
    @Override
    @Deprecated
    public PushReaction getPistonPushReaction(@NotNull BlockState state) {
        return resource.getPushReaction();
    }

    @Override
    public boolean isPortalFrame(BlockState state, BlockGetter world, BlockPos pos) {
        return resource.isPortalFrame();
    }
}