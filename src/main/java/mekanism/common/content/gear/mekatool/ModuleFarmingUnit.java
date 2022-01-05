package mekanism.common.content.gear.mekatool;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.api.AutomationType;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

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
        return MekanismUtils.performActions(
              //First try to use the disassembler as an axe
              stripLogsAOE(context),
              //Then as a shovel
              //Fire a generic use event, if we are allowed to use the tool return zero otherwise return -1
              // This is to mirror how onHoeUse returns of 0 if allowed, -1 if not allowed, and 1 if processing happened in the event
              () -> tillAOE(context, ToolActions.SHOVEL_FLATTEN, SoundEvents.SHOVEL_FLATTEN, MekanismConfig.gear.mekaToolEnergyUsageShovel.get())//,
              //Finally, as a hoe
              //TODO - 1.18: Implement hoe
              //() -> tillAOE(context, ToolType.HOE, SoundEvents.HOE_TILL, MekanismConfig.gear.mekaToolEnergyUsageHoe.get())
        );
    }

    @Nonnull
    @Override
    public Collection<ToolAction> getProvidedToolActions(IModule<ModuleFarmingUnit> module) {
        Set<ToolAction> actions = new HashSet<>(ToolActions.DEFAULT_AXE_ACTIONS);
        actions.addAll(ToolActions.DEFAULT_SHOVEL_ACTIONS);
        actions.addAll(ToolActions.DEFAULT_HOE_ACTIONS);
        return actions;
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

    private InteractionResult tillAOE(UseOnContext context, ToolAction toolAction, SoundEvent sound, FloatingLong energyUsage) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            //Skip if we don't have a player, or they are sneaking
            return InteractionResult.PASS;
        }
        Direction sideHit = context.getClickedFace();
        if (sideHit == Direction.DOWN) {
            //Don't allow tilling a block from underneath
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
        FloatingLong energy = energyContainer.getEnergy();
        if (energy.smallerThan(energyUsage)) {
            //Fail if we don't have enough energy or using the item failed
            return InteractionResult.FAIL;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState tilledState = world.getBlockState(pos).getToolModifiedState(world, pos, player, stack, toolAction);
        if (tilledState == null) {
            //Skip tilling the blocks if the one we clicked cannot be tilled
            return InteractionResult.PASS;
        }
        BlockPos abovePos = pos.above();
        BlockState aboveState = world.getBlockState(abovePos);
        //Check to make sure the block above is not opaque
        if (aboveState.isSolidRender(world, abovePos)) {
            //If the block above our source is opaque, just skip tiling in general
            return InteractionResult.PASS;
        }
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        //Processing did not happen, so we need to process it
        world.setBlock(pos, tilledState, Block.UPDATE_ALL_IMMEDIATE);
        Material aboveMaterial = aboveState.getMaterial();
        if (aboveMaterial == Material.PLANT || aboveMaterial == Material.REPLACEABLE_PLANT) {
            world.destroyBlock(abovePos, true);
        }
        world.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        FloatingLong energyUsed = energyUsage.copy();
        int radius = (diameter - 1) / 2;
        for (BlockPos newPos : BlockPos.betweenClosed(pos.offset(-radius, 0, -radius), pos.offset(radius, 0, radius))) {
            if (pos.equals(newPos)) {
                //Skip the source position as it is free, and we manually handled it before the loop
                continue;
            } else if (energyUsed.add(energyUsage).greaterThan(energy)) {
                break;
            }
            BlockState stateAbove = world.getBlockState(newPos.above());
            //Check to make sure the block above is not opaque and that the result we would get from tilling the other block is
            // the same as the one we got on the initial block we interacted with
            if (!stateAbove.isSolidRender(world, newPos.above()) && tilledState == world.getBlockState(newPos).getToolModifiedState(world, newPos, player, stack, toolAction)) {
                //Some of the below methods don't behave properly when the BlockPos is mutable, so now that we are onto ones where it may actually
                // matter we make sure to get an immutable instance of newPos
                newPos = newPos.immutable();
                //Add energy cost
                energyUsed = energyUsed.plusEqual(energyUsage);
                //Replace the block. Note it just directly sets it (in the same way that HoeItem/ShovelItem do)
                world.setBlock(newPos, tilledState, Block.UPDATE_ALL_IMMEDIATE);
                aboveMaterial = stateAbove.getMaterial();
                if (aboveMaterial == Material.PLANT || aboveMaterial == Material.REPLACEABLE_PLANT) {
                    //If the block above the one we tilled is a plant, then we try to remove it
                    world.destroyBlock(newPos.above(), true);
                }
                world.playSound(null, newPos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                Mekanism.packetHandler().sendToAllTracking(new PacketLightningRender(LightningPreset.TOOL_AOE, Objects.hash(pos, newPos),
                      Vec3.upFromBottomCenterOf(pos, 0.94), Vec3.upFromBottomCenterOf(newPos, 0.94), 10), world, pos);
            }
        }
        energyContainer.extract(energyUsed, Action.EXECUTE, AutomationType.MANUAL);
        return InteractionResult.SUCCESS;
    }

    //TODO - 1.18: add support for other axe actions and fix some potential issues (see fix in PE)
    private InteractionResult stripLogsAOE(UseOnContext context) {
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
        FloatingLong energy = energyContainer.getEnergy();
        FloatingLong energyUsage = MekanismConfig.gear.mekaToolEnergyUsageAxe.get();
        if (energy.smallerThan(energyUsage)) {
            //Fail if we don't have enough energy or using the item failed
            return InteractionResult.FAIL;
        }
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState clickedState = world.getBlockState(pos);
        BlockState strippedState = clickedState.getToolModifiedState(world, pos, player, stack, ToolActions.AXE_STRIP);
        if (strippedState == null) {
            //Skip stripping the blocks if the one we clicked cannot be stripped
            return InteractionResult.PASS;
        } else if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Axis axis = clickedState.getValue(RotatedPillarBlock.AXIS);
        //Process the block we interacted with initially and play the sound
        world.setBlock(pos, strippedState, Block.UPDATE_ALL_IMMEDIATE);
        world.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
        Direction side = context.getClickedFace();
        FloatingLong energyUsed = energyUsage.copy();
        Vec3 offset = Vec3.atLowerCornerOf(side.getNormal()).scale(0.44);
        for (BlockPos newPos : getStrippingArea(pos, side, (diameter - 1) / 2)) {
            if (pos.equals(newPos)) {
                //Skip the source position as it is free, and we manually handled it before the loop
                continue;
            } else if (energyUsed.add(energyUsage).greaterThan(energy)) {
                break;
            }
            //Check to make that the result we would get from stripping the other block is the same as the one we got on the initial block we interacted with
            // Also make sure that it is on the same axis as the block we initially clicked
            BlockState state = world.getBlockState(newPos);
            if (strippedState == state.getToolModifiedState(world, newPos, player, stack, ToolActions.AXE_STRIP) && axis == state.getValue(RotatedPillarBlock.AXIS)) {
                //Some of the below methods don't behave properly when the BlockPos is mutable, so now that we are onto ones where it may actually
                // matter we make sure to get an immutable instance of newPos
                newPos = newPos.immutable();
                //Add energy cost
                energyUsed = energyUsed.plusEqual(energyUsage);
                //Replace the block. Note it just directly sets it (in the same way that AxeItem does).
                world.setBlock(newPos, strippedState, Block.UPDATE_ALL_IMMEDIATE);
                world.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                Mekanism.packetHandler().sendToAllTracking(new PacketLightningRender(LightningPreset.TOOL_AOE, Objects.hash(pos, newPos),
                      Vec3.atCenterOf(pos).add(offset), Vec3.atCenterOf(newPos).add(offset), 10), world, pos);
            }
        }
        energyContainer.extract(energyUsed, Action.EXECUTE, AutomationType.MANUAL);
        return InteractionResult.SUCCESS;
    }

    private static Iterable<BlockPos> getStrippingArea(BlockPos pos, Direction direction, int radius) {
        AABB box;
        switch (direction) {
            case EAST:
            case WEST:
                box = new AABB(pos.getX(), pos.getY() - radius, pos.getZ() - radius, pos.getX(), pos.getY() + radius, pos.getZ() + radius);
                break;
            case UP:
            case DOWN:
                box = new AABB(pos.getX() - radius, pos.getY(), pos.getZ() - radius, pos.getX() + radius, pos.getY(), pos.getZ() + radius);
                break;
            case SOUTH:
            case NORTH:
                box = new AABB(pos.getX() - radius, pos.getY() - radius, pos.getZ(), pos.getX() + radius, pos.getY() + radius, pos.getZ());
                break;
            default:
                return BlockPos.betweenClosed(BlockPos.ZERO, BlockPos.ZERO);
        }
        return BlockPos.betweenClosed(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ));
    }
}