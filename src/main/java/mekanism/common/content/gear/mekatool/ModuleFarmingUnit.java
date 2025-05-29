package mekanism.common.content.gear.mekatool;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.network.to_client.PacketLightningRender.LightningPreset;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public record ModuleFarmingUnit(FarmingRadius farmingRadius) implements ICustomModule<ModuleFarmingUnit> {

    public static final ResourceLocation FARMING_RADIUS = Mekanism.rl("farming_radius");

    public ModuleFarmingUnit(IModule<ModuleFarmingUnit> module) {
        this(module.<FarmingRadius>getConfigOrThrow(FARMING_RADIUS).get());
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(IModule<ModuleFarmingUnit> module, UseOnContext context) {
        //Start with doing common logic to the module before we get onto specific logic for the different ways the module can be used
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            //Skip if we don't have a player, or they are sneaking
            return InteractionResult.PASS;
        }
        int diameter = farmingRadius.getRadius();
        if (diameter == 0) {
            //If we don't have any blocks we are going to want to do, then skip it
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer == null) {
            return InteractionResult.FAIL;
        }
        //Lazily lookup the state so we only have to query it once
        Lazy<BlockState> lazyClickedState = Lazy.of(() -> context.getLevel().getBlockState(context.getClickedPos()));
        return MekanismUtils.performActions(
              //First try to use the disassembler as an axe
              useAxeAOE(context, lazyClickedState, energyContainer, diameter, ItemAbilities.AXE_STRIP, SoundEvents.AXE_STRIP, -1),
              () -> useAxeAOE(context, lazyClickedState, energyContainer, diameter, ItemAbilities.AXE_SCRAPE, SoundEvents.AXE_SCRAPE, LevelEvent.PARTICLES_SCRAPE),
              () -> useAxeAOE(context, lazyClickedState, energyContainer, diameter, ItemAbilities.AXE_WAX_OFF, SoundEvents.AXE_WAX_OFF, LevelEvent.PARTICLES_WAX_OFF),
              //Then as a shovel
              () -> flattenAOE(context, lazyClickedState, energyContainer, diameter),
              () -> douseBlock(context, lazyClickedState, energyContainer, diameter),
              //Finally, as a hoe
              () -> tillAOE(context, lazyClickedState, energyContainer, diameter)
        );
    }

    @Override
    public boolean canPerformAction(IModule<ModuleFarmingUnit> module, IModuleContainer moduleContainer, ItemStack stack, ItemAbility action) {
        if (action == ItemAbilities.AXE_STRIP || action == ItemAbilities.AXE_SCRAPE || action == ItemAbilities.AXE_WAX_OFF) {
            return module.hasEnoughEnergy(stack, MekanismConfig.gear.mekaToolEnergyUsageAxe);
        } else if (action == ItemAbilities.SHOVEL_FLATTEN) {
            return module.hasEnoughEnergy(stack, MekanismConfig.gear.mekaToolEnergyUsageShovel);
        } else if (action == ItemAbilities.HOE_TILL) {
            return module.hasEnoughEnergy(stack, MekanismConfig.gear.mekaToolEnergyUsageHoe);
        }
        //Note: In general when we get here there will be no tool actions known unless mods add more default tool actions
        // This is because we special case the known vanilla types above and the dig variants are already handled by the Meka-Tool itself before
        // it even checks the installed modules
        return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(action) || ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(action) || ItemAbilities.DEFAULT_HOE_ACTIONS.contains(action);
    }

    @NothingNullByDefault
    public enum FarmingRadius implements IHasTextComponent, StringRepresentable {
        OFF(0),
        LOW(1),
        MED(3),
        HIGH(5),
        ULTRA(7);

        public static final Codec<FarmingRadius> CODEC = StringRepresentable.fromEnum(FarmingRadius::values);
        public static final IntFunction<FarmingRadius> BY_ID = ByIdMap.continuous(FarmingRadius::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, FarmingRadius> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FarmingRadius::ordinal);

        private final String serializedName;
        private final int radius;
        private final Component label;

        FarmingRadius(int radius) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.radius = radius;
            this.label = TextComponentUtil.getString(Integer.toString(radius));
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public int getRadius() {
            return radius;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    private InteractionResult douseBlock(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer, int diameter) {
        return useAOE(context, lazyClickedState, energyContainer, diameter, ItemAbilities.SHOVEL_DOUSE, null, LevelEvent.SOUND_EXTINGUISH_FIRE,
              MekanismConfig.gear.mekaToolEnergyUsageShovel.get(), new SimpleToolAOEData());
    }

    private InteractionResult tillAOE(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer, int diameter) {
        return useAOE(context, lazyClickedState, energyContainer, diameter, ItemAbilities.HOE_TILL, SoundEvents.HOE_TILL, -1,
              MekanismConfig.gear.mekaToolEnergyUsageHoe.get(), new SimpleToolAOEData());
    }

    private InteractionResult flattenAOE(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer, int diameter) {
        Direction sideHit = context.getClickedFace();
        if (sideHit == Direction.DOWN) {
            //Don't allow flattening a block from underneath
            return InteractionResult.PASS;
        }
        return useAOE(context, lazyClickedState, energyContainer, diameter, ItemAbilities.SHOVEL_FLATTEN, SoundEvents.SHOVEL_FLATTEN, -1,
              MekanismConfig.gear.mekaToolEnergyUsageShovel.get(), new ShovelToolAOEData());
    }

    private InteractionResult useAxeAOE(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer, int diameter, ItemAbility action,
          SoundEvent sound, int particle) {
        return useAOE(context, lazyClickedState, energyContainer, diameter, action, sound, particle, MekanismConfig.gear.mekaToolEnergyUsageAxe.get(),
              new AxeToolAOEData());
    }

    private InteractionResult useAOE(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer, int diameter, ItemAbility action,
          @Nullable SoundEvent sound, int particle, long energyUsage, IToolAOEData toolAOEData) {
        long energy = energyContainer.getEnergy();
        if (energy < energyUsage) {
            //Fail if we don't have enough energy or using the item failed
            return InteractionResult.FAIL;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState clickedState = lazyClickedState.get();
        if (!toolAOEData.isValid(world, pos, clickedState)) {
            //Skip modifying the blocks if there is something we think is invalid about the position in the world in general
            return InteractionResult.PASS;
        }
        BlockState modifiedState = clickedState.getToolModifiedState(context, action, false);
        if (modifiedState == null) {
            //Skip modifying the blocks if the one we clicked cannot be modified
            return InteractionResult.PASS;
        } else if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ServerPlayer player = null;
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            player = serverPlayer;
        }
        //Process the block we interacted with initially and play the sound
        if (player != null) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, pos, context.getItemInHand());
        }
        world.setBlock(pos, modifiedState, Block.UPDATE_ALL_IMMEDIATE);
        if (sound != null) {
            world.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (particle != -1) {
            world.levelEvent(null, particle, pos, 0);
        }
        Direction side = context.getClickedFace();
        toolAOEData.persistData(world, pos, clickedState, side);
        //Note: We don't need to copy this as we add to it in a non modifying way
        long energyUsed = energyUsage;
        for (BlockPos newPos : toolAOEData.getTargetPositions(pos, side, (diameter - 1) / 2)) {
            if (pos.equals(newPos)) {
                //Skip the source position as we manually handled it before the loop
                continue;
            }
            long nextEnergyUsed = energyUsed + energyUsage;
            if (nextEnergyUsed > energy) {
                break;
            }
            //Check to make that the result we would get from modifying the other block is the same as the one we got on the initial block we interacted with
            // Also make sure that it is properly valid
            BlockState state = world.getBlockState(newPos);
            //Create a new used context based on the original one to try and pass the proper information to the conversion
            UseOnContext adjustedContext = new UseOnContext(world, context.getPlayer(), context.getHand(), context.getItemInHand(), new BlockHitResult(
                  context.getClickLocation().add(newPos.getX() - pos.getX(), newPos.getY() - pos.getY(), newPos.getZ() - pos.getZ()),
                  context.getClickedFace(), newPos, context.isInside()));
            if (toolAOEData.isValid(world, newPos, state) && modifiedState == state.getToolModifiedState(adjustedContext, action, true)) {
                //Some of the below methods don't behave properly when the BlockPos is mutable, so now that we are onto ones where it may actually
                // matter we make sure to get an immutable instance of newPos
                newPos = newPos.immutable();
                //Update energy cost
                energyUsed = nextEnergyUsed;
                if (player != null) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, newPos, context.getItemInHand());
                }
                //Run it without simulation in case there are any side effects
                state.getToolModifiedState(adjustedContext, action, false);
                //Replace the block. Note it just directly sets it (in the same way the normal tools do).
                world.setBlock(newPos, modifiedState, Block.UPDATE_ALL_IMMEDIATE);
                if (sound != null) {
                    world.playSound(null, newPos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                if (particle != -1) {
                    world.levelEvent(null, particle, newPos, 0);
                }
                PacketUtils.sendToAllTracking(new PacketLightningRender(LightningPreset.TOOL_AOE, 31 * pos.hashCode() + newPos.hashCode(),
                      toolAOEData.getLightningPos(pos), toolAOEData.getLightningPos(newPos), 10), world, pos);
            }
        }
        energyContainer.extract(energyUsed, Action.EXECUTE, AutomationType.MANUAL);
        return InteractionResult.CONSUME;
    }

    private interface IToolAOEData {

        boolean isValid(Level level, BlockPos pos, BlockState state);

        default void persistData(Level level, BlockPos pos, BlockState state, Direction side) {
        }

        Iterable<BlockPos> getTargetPositions(BlockPos pos, Direction side, int radius);

        Vec3 getLightningPos(BlockPos pos);
    }

    private abstract static class FlatToolAOEData implements IToolAOEData {

        @Override
        public Iterable<BlockPos> getTargetPositions(BlockPos pos, Direction side, int radius) {
            return BlockPos.betweenClosed(pos.offset(-radius, 0, -radius), pos.offset(radius, 0, radius));
        }

        @Override
        public Vec3 getLightningPos(BlockPos pos) {
            return Vec3.upFromBottomCenterOf(pos, 0.94);
        }
    }

    private static class SimpleToolAOEData extends FlatToolAOEData {

        @Override
        public boolean isValid(Level level, BlockPos pos, BlockState state) {
            //Always return that we are valid if we could find a conversion, we unfortunately are no longer able
            // to allow conversions when there is plants on top of it as then the tool modified state won't return
            // anything
            return true;
        }
    }

    private static class ShovelToolAOEData extends FlatToolAOEData {

        @Override
        public boolean isValid(Level level, BlockPos pos, BlockState state) {
            BlockPos abovePos = pos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            //Allow flattening a block when the above block is air
            if (aboveState.isAir()) {
                return true;
            }
            //Or it is a replaceable plant that is also not solid (such as tall grass)
            //Note: This may not be the most optimal way of checking this, but it gives a decent enough estimate of it
            //TODO - 1.21: Do we want to try and come up with a better tag or check for if it is a replaceable plant?
            if (aboveState.is(MekanismTags.Blocks.FARMING_OVERRIDE) || aboveState.canBeReplaced() && aboveState.is(BlockTags.REPLACEABLE_BY_TREES)) {
                return aboveState.getFluidState().isEmpty() && !aboveState.isSolidRender(level, abovePos);
            }
            return false;
        }
    }

    private static class AxeToolAOEData implements IToolAOEData {

        @Nullable
        private Axis axis;
        private boolean isSet;
        private Vec3 offset = Vec3.ZERO;

        @Override
        public boolean isValid(Level level, BlockPos blockPos, BlockState state) {
            return !isSet || axis == getAxis(state);
        }

        @Override
        public void persistData(Level level, BlockPos pos, BlockState state, Direction side) {
            axis = getAxis(state);
            isSet = true;
            offset = Vec3.atLowerCornerOf(side.getNormal()).scale(0.5);
        }

        @Override
        public Iterable<BlockPos> getTargetPositions(BlockPos pos, Direction side, int radius) {
            Vec3i adjustment = switch (side) {
                case EAST, WEST -> new Vec3i(0, radius, radius);
                case UP, DOWN -> new Vec3i(radius, 0, radius);
                case SOUTH, NORTH -> new Vec3i(radius, radius, 0);
            };
            BlockPos first = pos.subtract(adjustment);
            BlockPos second = pos.offset(adjustment);
            AABB box = new AABB(first.getX(), first.getY(), first.getZ(), second.getX(), second.getY(), second.getZ());
            return BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ));
        }

        @Nullable
        private Axis getAxis(BlockState state) {
            return state.hasProperty(RotatedPillarBlock.AXIS) ? state.getValue(RotatedPillarBlock.AXIS) : null;
        }

        @Override
        public Vec3 getLightningPos(BlockPos pos) {
            return pos.getCenter().add(offset);
        }
    }
}