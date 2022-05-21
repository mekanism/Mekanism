package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Contract;

public class BlockStateHelper {

    private BlockStateHelper() {
    }

    //Cardboard Box storage
    public static final BooleanProperty storageProperty = BooleanProperty.create("storage");
    //Fluid logged.
    public static final EnumProperty<FluidLogType> FLUID_LOGGED = EnumProperty.create("fluid_logged", FluidLogType.class);

    public static final BlockBehaviour.StatePredicate NEVER_PREDICATE = (state, world, pos) -> false;

    public static BlockState getDefaultState(@Nonnull BlockState state) {
        Block block = state.getBlock();
        for (Attribute attr : Attribute.getAll(block)) {
            if (attr instanceof AttributeState atr) {
                state = atr.getDefaultState(state);
            }
        }
        if (block instanceof IStateFluidLoggable fluidLoggable) {
            //Default the blocks to not being waterlogged, they have code to force waterlogging to true if being placed in water
            state = fluidLoggable.setState(state, Fluids.EMPTY);
        }
        return state;
    }

    public static void fillBlockStateContainer(Block block, StateDefinition.Builder<Block, BlockState> builder) {
        List<Property<?>> properties = new ArrayList<>();
        for (Attribute attr : Attribute.getAll(block)) {
            if (attr instanceof AttributeState atr) {
                atr.fillBlockStateContainer(block, properties);
            }
        }
        if (block instanceof IStateStorage) {
            properties.add(storageProperty);
        }
        if (block instanceof IStateFluidLoggable fluidLoggable) {
            properties.add(fluidLoggable.getFluidLoggedProperty());
        }
        if (!properties.isEmpty()) {
            builder.add(properties.toArray(new Property[0]));
        }
    }

    /**
     * Helper to "hack" in and modify the light value precalculator for states to be able to use as a base level the value already set, but also modify it based on which
     * fluid a block may be fluid logged with and then use that light level instead if it is higher.
     */
    public static BlockBehaviour.Properties applyLightLevelAdjustments(BlockBehaviour.Properties properties) {
        return applyLightLevelAdjustments(properties, state -> {
            Block block = state.getBlock();
            if (block instanceof IStateFluidLoggable fluidLoggable) {
                return fluidLoggable.getFluid(state).getType().getAttributes().getLuminosity();
            }
            return 0;
        });
    }

    /**
     * Helper to "hack" in and modify the light value precalculator for states to be able to use as a base level the value already set, but also modify it based on
     * another function to allow for compounding the light values and then using that light level instead if it is higher.
     */
    public static BlockBehaviour.Properties applyLightLevelAdjustments(BlockBehaviour.Properties properties, ToIntFunction<BlockState> toApply) {
        //Cache what the current light level function is
        ToIntFunction<BlockState> existingLightLevelFunction = properties.lightEmission;
        //And override the one in the properties in a way that we can modify if we have state information that should adjust it
        return properties.lightLevel(state -> Math.max(existingLightLevelFunction.applyAsInt(state), toApply.applyAsInt(state)));
    }

    @Contract("_, null, _ -> null")
    public static BlockState getStateForPlacement(Block block, @Nullable BlockState state, BlockPlaceContext context) {
        return getStateForPlacement(block, state, context.getLevel(), context.getClickedPos(), context.getPlayer(), context.getClickedFace());
    }

    @Contract("_, null, _, _, _, _ -> null")
    public static BlockState getStateForPlacement(Block block, @Nullable BlockState state, @Nonnull LevelAccessor world, @Nonnull BlockPos pos, @Nullable Player player, @Nonnull Direction face) {
        if (state == null) {
            return null;
        }
        for (Attribute attr : Attribute.getAll(block)) {
            if (attr instanceof AttributeState atr) {
                state = atr.getStateForPlacement(block, state, world, pos, player, face);
            }
        }
        if (block instanceof IStateFluidLoggable fluidLoggable) {
            FluidState fluidState = world.getFluidState(pos);
            state = fluidLoggable.setState(state, fluidState.getType());
        }
        return state;
    }

    public static BlockState copyStateData(BlockState oldState, IBlockProvider newBlockProvider) {
        return copyStateData(oldState, newBlockProvider.getBlock().defaultBlockState());
    }

    public static BlockState copyStateData(BlockState oldState, BlockState newState) {
        Block oldBlock = oldState.getBlock();
        Block newBlock = newState.getBlock();
        for (Attribute attr : Attribute.getAll(oldBlock)) {
            if (attr instanceof AttributeState atr) {
                newState = atr.copyStateData(oldState, newState);
            }
        }
        if (oldBlock instanceof IStateStorage && newBlock instanceof IStateStorage) {
            newState = newState.setValue(storageProperty, oldState.getValue(storageProperty));
        }
        if (newBlock instanceof IStateFluidLoggable newFluidLoggable) {
            FluidState oldFluidState = oldState.getFluidState();
            if (!oldFluidState.isEmpty()) {
                //Try to set the new state to the same as the old one had if the old one was not empty
                newState = newFluidLoggable.setState(newState, oldFluidState.getType());
            }
        }
        return newState;
    }
}