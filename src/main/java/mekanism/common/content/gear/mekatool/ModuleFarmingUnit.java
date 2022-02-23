package mekanism.common.content.gear.mekatool;

import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.network.to_client.PacketLightningRender.LightningPreset;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.Lazy;

@ParametersAreNonnullByDefault
public class ModuleFarmingUnit implements ICustomModule<ModuleFarmingUnit> {

    private IModuleConfigItem<FarmingRadius> farmingRadius;

    @Override
    public void init(IModule<ModuleFarmingUnit> module, ModuleConfigItemCreator configItemCreator) {
        farmingRadius = configItemCreator.createConfigItem("farming_radius", MekanismLang.MODULE_FARMING_RADIUS,
              new ModuleEnumData<>(FarmingRadius.class, module.getInstalledCount() + 1, FarmingRadius.LOW));
    }

    @Nonnull
    @Override
    public InteractionResult onItemUse(IModule<ModuleFarmingUnit> module, UseOnContext context) {
        //Start with doing common logic to the module before we get onto specific logic for the different ways the module can be used
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            //Skip if we don't have a player, or they are sneaking
            return InteractionResult.PASS;
        }
        int diameter = farmingRadius.get().getRadius();
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
              useAxeAOE(context, lazyClickedState, energyContainer, diameter, player, stack, ToolActions.AXE_STRIP, SoundEvents.AXE_STRIP, -1),
              () -> useAxeAOE(context, lazyClickedState, energyContainer, diameter, player, stack, ToolActions.AXE_SCRAPE, SoundEvents.AXE_SCRAPE, LevelEvent.PARTICLES_SCRAPE),
              () -> useAxeAOE(context, lazyClickedState, energyContainer, diameter, player, stack, ToolActions.AXE_WAX_OFF, SoundEvents.AXE_WAX_OFF, LevelEvent.PARTICLES_WAX_OFF),
              //Then as a shovel
              () -> flattenAOE(context, lazyClickedState, energyContainer, diameter, player, stack),
              () -> dowseCampfire(context, lazyClickedState, energyContainer),
              //Finally, as a hoe
              () -> tillAOE(context, lazyClickedState, energyContainer, diameter, player, stack, MekanismConfig.gear.mekaToolEnergyUsageHoe.get())
        );
    }

    @Override
    public boolean canPerformAction(IModule<ModuleFarmingUnit> module, ToolAction action) {
        return ToolActions.DEFAULT_AXE_ACTIONS.contains(action) || ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(action) || ToolActions.DEFAULT_HOE_ACTIONS.contains(action);
    }

    public enum FarmingRadius implements IHasTextComponent {
        OFF(0),
        LOW(1),
        MED(3),
        HIGH(5),
        ULTRA(7);

        private final int radius;
        private final Component label;

        FarmingRadius(int radius) {
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
    }

    private InteractionResult dowseCampfire(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer) {
        FloatingLong energy = energyContainer.getEnergy();
        FloatingLong energyUsage = MekanismConfig.gear.mekaToolEnergyUsageShovel.get();
        if (energy.smallerThan(energyUsage)) {
            //Fail if we don't have enough energy or using the item failed
            return InteractionResult.FAIL;
        }
        BlockState clickedState = lazyClickedState.get();
        if (clickedState.getBlock() instanceof CampfireBlock && clickedState.getValue(CampfireBlock.LIT)) {
            Level world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            if (!world.isClientSide()) {
                world.levelEvent(null, LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
            }
            CampfireBlock.dowse(context.getPlayer(), world, pos, clickedState);
            if (!world.isClientSide()) {
                world.setBlock(pos, clickedState.setValue(CampfireBlock.LIT, Boolean.FALSE), Block.UPDATE_ALL_IMMEDIATE);
                energyContainer.extract(energyUsage, Action.EXECUTE, AutomationType.MANUAL);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult tillAOE(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer, int diameter, Player player,
          ItemStack stack, FloatingLong energyUsage) {
        FloatingLong energy = energyContainer.getEnergy();
        if (energy.smallerThan(energyUsage)) {
            //Fail if we don't have enough energy or using the item failed
            return InteractionResult.FAIL;
        }
        Block type = lazyClickedState.get().getBlock();
        //TODO: Hopefully one day make this not go based off of the internal map
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> conversion = HoeItem.TILLABLES.get(type);
        if (conversion == null) {
            //Skip tilling the blocks if the one we clicked cannot be tilled
            return InteractionResult.PASS;
        }
        Predicate<UseOnContext> canConvert = conversion.getFirst();
        if (!canConvert.test(context)) {
            //Skip tilling the blocks if the one we clicked cannot be tilled
            return InteractionResult.PASS;
        }
        Level world = context.getLevel();
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Consumer<UseOnContext> converter = conversion.getSecond();
        converter.accept(context);
        BlockPos pos = context.getClickedPos();
        world.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        //Note: We don't need to copy this as we add to it in a non modifying way
        FloatingLong energyUsed = energyUsage;
        int radius = (diameter - 1) / 2;
        for (BlockPos newPos : BlockPos.betweenClosed(pos.offset(-radius, 0, -radius), pos.offset(radius, 0, radius))) {
            if (pos.equals(newPos)) {
                //Skip the source position as we manually handled it before the loop
                continue;
            }
            FloatingLong nextEnergyUsed = energyUsed.add(energyUsage);
            if (nextEnergyUsed.greaterThan(energy)) {
                break;
            }
            //Note: Unfortunately we no longer can compare this based on output state as we do not have easy access to it,
            // so instead we have to instead go based on the input block
            if (world.getBlockState(newPos).is(type)) {
                //Some of the below methods don't behave properly when the BlockPos is mutable, so now that we are onto ones where it may actually
                // matter we make sure to get an immutable instance of newPos
                newPos = newPos.immutable();
                //Create a new used context based on the original one to try and pass the proper information to the conversion
                UseOnContext adjustedContext = new UseOnContext(world, player, context.getHand(), stack, new BlockHitResult(
                      context.getClickLocation().add(newPos.getX() - pos.getX(), newPos.getY() - pos.getY(), newPos.getZ() - pos.getZ()),
                      context.getClickedFace(), newPos, context.isInside()));
                if (canConvert.test(adjustedContext)) {
                    //Update energy cost
                    energyUsed = nextEnergyUsed;
                    //Apply the conversion
                    converter.accept(adjustedContext);
                    world.playSound(null, newPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    Mekanism.packetHandler().sendToAllTracking(new PacketLightningRender(LightningPreset.TOOL_AOE, Objects.hash(pos, newPos),
                          Vec3.upFromBottomCenterOf(pos, 0.94), Vec3.upFromBottomCenterOf(newPos, 0.94), 10), world, pos);
                }
            }
        }
        energyContainer.extract(energyUsed, Action.EXECUTE, AutomationType.MANUAL);
        return InteractionResult.CONSUME;
    }

    private InteractionResult flattenAOE(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer, int diameter, Player player, ItemStack stack) {
        Direction sideHit = context.getClickedFace();
        if (sideHit == Direction.DOWN) {
            //Don't allow flattening a block from underneath
            return InteractionResult.PASS;
        }
        return useAOE(context, lazyClickedState, energyContainer, diameter, player, stack, ToolActions.SHOVEL_FLATTEN, SoundEvents.SHOVEL_FLATTEN, -1,
              MekanismConfig.gear.mekaToolEnergyUsageShovel.get(), new ShovelToolAOEData());
    }

    private InteractionResult useAxeAOE(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer, int diameter, Player player,
          ItemStack stack, ToolAction action, SoundEvent sound, int particle) {
        return useAOE(context, lazyClickedState, energyContainer, diameter, player, stack, action, sound, particle, MekanismConfig.gear.mekaToolEnergyUsageAxe.get(),
              new AxeToolAOEData());
    }

    private InteractionResult useAOE(UseOnContext context, Lazy<BlockState> lazyClickedState, IEnergyContainer energyContainer, int diameter, Player player,
          ItemStack stack, ToolAction action, SoundEvent sound, int particle, FloatingLong energyUsage, IToolAOEData toolAOEData) {
        FloatingLong energy = energyContainer.getEnergy();
        if (energy.smallerThan(energyUsage)) {
            //Fail if we don't have enough energy or using the item failed
            return InteractionResult.FAIL;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState clickedState = lazyClickedState.get();
        BlockState modifiedState = clickedState.getToolModifiedState(world, pos, player, stack, action);
        if (modifiedState == null || !toolAOEData.isValid(world, pos, clickedState)) {
            //Skip modifying the blocks if the one we clicked cannot be modified
            // or if there is something we think is invalid about the position in the world in general
            return InteractionResult.PASS;
        } else if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        //Process the block we interacted with initially and play the sound
        world.setBlock(pos, modifiedState, Block.UPDATE_ALL_IMMEDIATE);
        world.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (particle != -1) {
            world.levelEvent(null, particle, pos, 0);
        }
        Direction side = context.getClickedFace();
        toolAOEData.persistData(world, pos, clickedState, side);
        //Note: We don't need to copy this as we add to it in a non modifying way
        FloatingLong energyUsed = energyUsage;
        for (BlockPos newPos : toolAOEData.getTargetPositions(pos, side, (diameter - 1) / 2)) {
            if (pos.equals(newPos)) {
                //Skip the source position as we manually handled it before the loop
                continue;
            }
            FloatingLong nextEnergyUsed = energyUsed.add(energyUsage);
            if (nextEnergyUsed.greaterThan(energy)) {
                break;
            }
            //Check to make that the result we would get from modifying the other block is the same as the one we got on the initial block we interacted with
            // Also make sure that it is properly valid
            BlockState state = world.getBlockState(newPos);
            if (toolAOEData.isValid(world, newPos, state) && modifiedState == state.getToolModifiedState(world, newPos, player, stack, action)) {
                //Some of the below methods don't behave properly when the BlockPos is mutable, so now that we are onto ones where it may actually
                // matter we make sure to get an immutable instance of newPos
                newPos = newPos.immutable();
                //Update energy cost
                energyUsed = nextEnergyUsed;
                //Replace the block. Note it just directly sets it (in the same way the normal tools do).
                world.setBlock(newPos, modifiedState, Block.UPDATE_ALL_IMMEDIATE);
                world.playSound(null, newPos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (particle != -1) {
                    world.levelEvent(null, particle, newPos, 0);
                }
                Mekanism.packetHandler().sendToAllTracking(new PacketLightningRender(LightningPreset.TOOL_AOE, Objects.hash(pos, newPos),
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

    private static class ShovelToolAOEData implements IToolAOEData {

        @Override
        public boolean isValid(Level level, BlockPos pos, BlockState state) {
            BlockPos abovePos = pos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            //Allow flattening a block when the above block is air
            if (aboveState.isAir()) {
                return true;
            }
            //Or it is a replaceable plant that is also not solid (such as tall grass)
            Material material = aboveState.getMaterial();
            if (material == Material.REPLACEABLE_PLANT || material == Material.REPLACEABLE_FIREPROOF_PLANT) {
                return !aboveState.isSolidRender(level, abovePos);
            }
            return false;
        }

        @Override
        public Iterable<BlockPos> getTargetPositions(BlockPos pos, Direction side, int radius) {
            return BlockPos.betweenClosed(pos.offset(-radius, 0, -radius), pos.offset(radius, 0, radius));
        }

        @Override
        public Vec3 getLightningPos(BlockPos pos) {
            return Vec3.upFromBottomCenterOf(pos, 0.94);
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
            AABB box = switch (side) {
                case EAST, WEST -> new AABB(pos.getX(), pos.getY() - radius, pos.getZ() - radius, pos.getX(), pos.getY() + radius, pos.getZ() + radius);
                case UP, DOWN -> new AABB(pos.getX() - radius, pos.getY(), pos.getZ() - radius, pos.getX() + radius, pos.getY(), pos.getZ() + radius);
                case SOUTH, NORTH -> new AABB(pos.getX() - radius, pos.getY() - radius, pos.getZ(), pos.getX() + radius, pos.getY() + radius, pos.getZ());
            };
            return BlockPos.betweenClosed(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ));
        }

        @Nullable
        private Axis getAxis(BlockState state) {
            return state.hasProperty(RotatedPillarBlock.AXIS) ? state.getValue(RotatedPillarBlock.AXIS) : null;
        }

        @Override
        public Vec3 getLightningPos(BlockPos pos) {
            return Vec3.atCenterOf(pos).add(offset);
        }
    }
}