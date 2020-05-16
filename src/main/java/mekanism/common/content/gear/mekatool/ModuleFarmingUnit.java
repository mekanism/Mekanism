package mekanism.common.content.gear.mekatool;

import java.util.Map;
import java.util.function.ToIntFunction;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;

public class ModuleFarmingUnit extends ModuleMekaTool {

    private ModuleConfigItem<FarmingRadius> farmingRadius;

    @Override
    public void init() {
        super.init();
        addConfigItem(farmingRadius = new ModuleConfigItem<FarmingRadius>(this, "farming_radius", MekanismLang.MODULE_FARMING_RADIUS, new EnumData<>(FarmingRadius.class, getInstalledCount() + 1), FarmingRadius.LOW));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return MekanismUtils.performActions(
              //First try to use the disassembler as an axe
              stripLogsAOE(context),
              //Then as a shovel
              //Fire a generic use event, if we are allowed to use the tool return zero otherwise return -1
              // This is to mirror how onHoeUse returns of 0 if allowed, -1 if not allowed, and 1 if processing happened in the event
              () -> tillAOE(context, ShovelItem.SHOVEL_LOOKUP, ctx -> onToolUse(ctx.getPlayer(), ctx.getHand(), ctx.getPos(), ctx.getFace()) ? 0 : -1,
                    SoundEvents.ITEM_SHOVEL_FLATTEN, MekanismConfig.gear.mekaToolEnergyUsageShovel.get()),
              //Finally as a hoe
              () -> tillAOE(context, HoeItem.HOE_LOOKUP, ForgeEventFactory::onHoeUse, SoundEvents.ITEM_HOE_TILL, MekanismConfig.gear.mekaToolEnergyUsageHoe.get())
        );
    }

    public static enum FarmingRadius implements IHasTextComponent {
        OFF(0),
        LOW(1),
        MED(3),
        HIGH(5),
        ULTRA(7);
        private int radius;
        private ITextComponent label;

        private FarmingRadius(int radius) {
            this.radius = radius;
            this.label = new StringTextComponent(Integer.toString(radius));
        }

        @Override
        public ITextComponent getTextComponent() {
            return label;
        }

        public int getRadius() {
            return radius;
        }
    }

    private ActionResultType tillAOE(ItemUseContext context, Map<Block, BlockState> lookup, ToIntFunction<ItemUseContext> onItemUse, SoundEvent sound, FloatingLong energyUsage) {
        PlayerEntity player = context.getPlayer();
        if (player == null || player.isSneaking()) {
            //Skip if we don't have a player or they are sneaking
            return ActionResultType.PASS;
        }
        Direction sideHit = context.getFace();
        if (sideHit == Direction.DOWN) {
            //Don't allow tilling a block from underneath
            return ActionResultType.PASS;
        }
        Hand hand = context.getHand();
        ItemStack stack = player.getHeldItem(hand);
        int diameter = farmingRadius.get().getRadius();
        if (diameter == 0) {
            //If we don't have any blocks we are going to want to do, then skip it
            return ActionResultType.PASS;
        }
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer == null) {
            return ActionResultType.FAIL;
        }
        FloatingLong energy = energyContainer.getEnergy();
        if (energy.smallerThan(energyUsage)) {
            //Fail if we don't have enough energy or using the item failed
            return ActionResultType.FAIL;
        }
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        BlockState tilledState = lookup.get(world.getBlockState(pos).getBlock());
        if (tilledState == null) {
            //Skip tilling the blocks if the one we clicked cannot be tilled
            return ActionResultType.PASS;
        }
        BlockPos abovePos = pos.up();
        BlockState aboveState = world.getBlockState(abovePos);
        //Check to make sure the block above is not opaque
        if (aboveState.isOpaqueCube(world, abovePos)) {
            //If the block above our source is opaque, just skip tiling in general
            return ActionResultType.PASS;
        }
        int useResult = onItemUse.applyAsInt(context);
        if (useResult < 0) {
            return ActionResultType.PASS;
        } else if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        if (useResult == 0) {
            //Processing did not happen in the hook so we need to process it
            world.setBlockState(pos, tilledState, BlockFlags.DEFAULT_AND_RERENDER);
            Material aboveMaterial = aboveState.getMaterial();
            if (aboveMaterial == Material.PLANTS || aboveMaterial == Material.TALL_PLANTS) {
                world.destroyBlock(abovePos, true);
            }
            world.playSound(player, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        FloatingLong energyUsed = energyUsage.copy();
        int radius = (diameter - 1) / 2;
        for (BlockPos newPos : BlockPos.getAllInBoxMutable(pos.add(-radius, 0, -radius), pos.add(radius, 0, radius))) {
            if (pos.equals(newPos)) {
                //Skip the source position as it is free and we manually handled it before the loop
                continue;
            }
            if (energyUsed.add(energyUsage).greaterThan(energy)) {
                break;
            }
            BlockState stateAbove = world.getBlockState(newPos.up());
            //Check to make sure the block above is not opaque and that the result we would get from tilling the other block is
            // the same as the one we got on the initial block we interacted with
            if (!stateAbove.isOpaqueCube(world, newPos.up()) && tilledState == lookup.get(world.getBlockState(newPos).getBlock())) {
                //Some of the below methods don't behave properly when the BlockPos is mutable, so now that we are onto ones where it may actually
                // matter we make sure to get an immutable instance of newPos
                newPos = newPos.toImmutable();
                useResult = onItemUse.applyAsInt(new ItemUseContext(player, hand, new BlockRayTraceResult(Vec3d.ZERO, Direction.UP, newPos, false)));
                if (useResult < 0) {
                    //We were denied from using the item so continue to the next block
                    continue;
                }
                //Add energy cost
                energyUsed = energyUsed.plusEqual(energyUsage);
                if (useResult > 0) {
                    //Processing happened in the hook so we use our desired fuel amount
                    continue;
                } //else we are allowed to use the item
                //Replace the block. Note it just directly sets it (in the same way that HoeItem/ShovelItem do)
                world.setBlockState(newPos, tilledState, BlockFlags.DEFAULT_AND_RERENDER);
                Material aboveMaterial = stateAbove.getMaterial();
                if (aboveMaterial == Material.PLANTS || aboveMaterial == Material.TALL_PLANTS) {
                    //If the block above the one we tilled is a plant, then we try to remove it
                    world.destroyBlock(newPos.up(), true);
                }
                world.playSound(player, newPos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
        energyContainer.extract(energyUsed, Action.EXECUTE, AutomationType.MANUAL);
        return ActionResultType.SUCCESS;
    }

    private ActionResultType stripLogsAOE(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || player.isSneaking()) {
            //Skip if we don't have a player or they are sneaking
            return ActionResultType.PASS;
        }
        Hand hand = context.getHand();
        ItemStack stack = player.getHeldItem(hand);
        int diameter = farmingRadius.get().getRadius();
        if (diameter == 0) {
            //If we don't have any blocks we are going to want to do, then skip it
            return ActionResultType.PASS;
        }
        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
        if (energyContainer == null) {
            return ActionResultType.FAIL;
        }
        FloatingLong energy = energyContainer.getEnergy();
        FloatingLong energyUsage = MekanismConfig.gear.mekaToolEnergyUsageAxe.get();
        if (energy.smallerThan(energyUsage)) {
            //Fail if we don't have enough energy or using the item failed
            return ActionResultType.FAIL;
        }
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Map<Block, Block> lookup = AxeItem.BLOCK_STRIPPING_MAP;
        BlockState clickedState = world.getBlockState(pos);
        Block strippedBlock = lookup.get(clickedState.getBlock());
        if (strippedBlock == null) {
            //Skip stripping the blocks if the one we clicked cannot be stripped
            return ActionResultType.PASS;
        }
        //Note: We don't need to fire a check for the tool being used here as we never would have had our current method get called
        // if a generic interact was not allowed
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        Axis axis = clickedState.get(RotatedPillarBlock.AXIS);
        BlockState strippedState = strippedBlock.getDefaultState().with(RotatedPillarBlock.AXIS, axis);
        //Process the block we interacted with initially and play the sound
        world.setBlockState(pos, strippedState, BlockFlags.DEFAULT_AND_RERENDER);
        world.playSound(player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
        Direction side = context.getFace();
        FloatingLong energyUsed = energyUsage.copy();
        for (BlockPos newPos : getStrippingArea(pos, side, (diameter - 1) / 2)) {
            if (pos.equals(newPos)) {
                //Skip the source position as it is free and we manually handled it before the loop
                continue;
            }
            if (energyUsed.add(energyUsage).greaterThan(energy)) {
                break;
            }
            //Check to make that the result we would get from stripping the other block is the same as the one we got on the initial block we interacted with
            // Also make sure that it is on the same axis as the block we initially clicked
            BlockState state = world.getBlockState(newPos);
            if (strippedBlock == lookup.get(state.getBlock()) && axis == state.get(RotatedPillarBlock.AXIS)) {
                //Some of the below methods don't behave properly when the BlockPos is mutable, so now that we are onto ones where it may actually
                // matter we make sure to get an immutable instance of newPos
                newPos = newPos.toImmutable();
                if (!onToolUse(player, hand, newPos, side)) {
                    //We were denied from using the item so continue to the next block
                    continue;
                } //else we are allowed to use the item
                //Add energy cost
                energyUsed = energyUsed.plusEqual(energyUsage);
                //Replace the block. Note it just directly sets it (in the same way that AxeItem does).
                world.setBlockState(newPos, strippedState, BlockFlags.DEFAULT_AND_RERENDER);
                world.playSound(player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
        energyContainer.extract(energyUsed, Action.EXECUTE, AutomationType.MANUAL);
        return ActionResultType.SUCCESS;
    }

    private static Iterable<BlockPos> getStrippingArea(BlockPos pos, Direction direction, int radius) {
        AxisAlignedBB box;
        switch (direction) {
            case EAST:
            case WEST:
                box = new AxisAlignedBB(pos.getX(), pos.getY() - radius, pos.getZ() - radius, pos.getX(), pos.getY() + radius, pos.getZ() + radius);
                break;
            case UP:
            case DOWN:
                box = new AxisAlignedBB(pos.getX() - radius, pos.getY(), pos.getZ() - radius, pos.getX() + radius, pos.getY(), pos.getZ() + radius);
                break;
            case SOUTH:
            case NORTH:
                box = new AxisAlignedBB(pos.getX() - radius, pos.getY() - radius, pos.getZ(), pos.getX() + radius, pos.getY() + radius, pos.getZ());
                break;
            default:
                return BlockPos.getAllInBoxMutable(BlockPos.ZERO, BlockPos.ZERO);
        }
        return BlockPos.getAllInBoxMutable(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ));
    }

    private static boolean onToolUse(PlayerEntity player, Hand hand, BlockPos pos, Direction face) {
        RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, pos, face);
        //Nothing happens if it is cancelled or we are not allowed to use the item so return false
        return !event.isCanceled() && event.getUseItem() != Result.DENY;
    }
}