package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Contract;

public class BlockStateHelper {

    private BlockStateHelper() {
    }

    //Cardboard Box storage
    public static final BooleanProperty storageProperty = BooleanProperty.create("storage");
    //Fluid logged.
    //TODO - 1.18: Remove CorrectingIntegerProperty and rename the property from waterlogged to fluid_logged
    // We are keeping it as "waterlogged" so that we can properly read old values using the CorrectingIntegerProperty
    public static final IntegerProperty FLUID_LOGGED = CorrectingIntegerProperty.create(BlockStateProperties.WATERLOGGED.getName(), 0, IStateFluidLoggable.VANILLA_FLUIDS.length);
    //public static final IntegerProperty FLUID_LOGGED = IntegerProperty.create("fluid_logged", 0, IStateFluidLoggable.VANILLA_FLUIDS.length);

    public static BlockState getDefaultState(@Nonnull BlockState state) {
        Block block = state.getBlock();
        for (Attribute attr : Attribute.getAll(block)) {
            if (attr instanceof AttributeState) {
                state = ((AttributeState) attr).getDefaultState(state);
            }
        }
        if (block instanceof IStateFluidLoggable) {
            //Default the blocks to not being waterlogged, they have code to force waterlogging to true if being placed in water
            state = state.setValue(((IStateFluidLoggable) block).getFluidLoggedProperty(), 0);
        }
        return state;
    }

    public static void fillBlockStateContainer(Block block, StateDefinition.Builder<Block, BlockState> builder) {
        List<Property<?>> properties = new ArrayList<>();
        for (Attribute attr : Attribute.getAll(block)) {
            if (attr instanceof AttributeState) {
                ((AttributeState) attr).fillBlockStateContainer(block, properties);
            }
        }
        if (block instanceof IStateStorage) {
            properties.add(storageProperty);
        }
        if (block instanceof IStateFluidLoggable) {
            properties.add(((IStateFluidLoggable) block).getFluidLoggedProperty());
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
            if (block instanceof IStateFluidLoggable) {
                return ((IStateFluidLoggable) block).getFluid(state).getType().getAttributes().getLuminosity();
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
            if (attr instanceof AttributeState) {
                state = ((AttributeState) attr).getStateForPlacement(block, state, world, pos, player, face);
            }
        }
        if (block instanceof IStateFluidLoggable) {
            IStateFluidLoggable fluidLoggable = (IStateFluidLoggable) block;
            FluidState fluidState = world.getFluidState(pos);
            state = state.setValue(fluidLoggable.getFluidLoggedProperty(), fluidLoggable.getSupportedFluidPropertyIndex(fluidState.getType()));
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
            if (attr instanceof AttributeState) {
                newState = ((AttributeState) attr).copyStateData(oldState, newState);
            }
        }
        if (oldBlock instanceof IStateStorage && newBlock instanceof IStateStorage) {
            newState = newState.setValue(storageProperty, oldState.getValue(storageProperty));
        }
        if (oldBlock instanceof IStateFluidLoggable && newBlock instanceof IStateFluidLoggable) {
            IStateFluidLoggable oldFluidLoggable = (IStateFluidLoggable) oldBlock;
            IStateFluidLoggable newFluidLoggable = (IStateFluidLoggable) newBlock;
            if (oldFluidLoggable.getSupportedFluids().length == newFluidLoggable.getSupportedFluids().length) {
                //Basic check if the number of supported fluids is the same copy it over
                //TODO: Eventually maybe we want a better check? In theory they should always match but just in case
                newState = newState.setValue(newFluidLoggable.getFluidLoggedProperty(), oldState.getValue(oldFluidLoggable.getFluidLoggedProperty()));
            }
        }
        return newState;
    }
}