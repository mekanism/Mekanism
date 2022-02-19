package mekanism.common.content.gear.mekatool;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.inventory.AutomationType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.Constants.BlockFlags;

//Note: Some parts of this module are directly implemented in the meka tool most notably the handling of disarming tripwire hooks,
// and also exposing the shears tool type on the meka tool when shears are installed
@ParametersAreNonnullByDefault
public class ModuleShearingUnit implements ICustomModule<ModuleShearingUnit> {

    public static final ToolType SHEARS_TOOL_TYPE = ToolType.get("shears");
    private static final Predicate<Entity> SHEARABLE = entity -> !entity.isSpectator() && entity instanceof IForgeShearable;

    @Nonnull
    @Override
    public ActionResultType onItemUse(IModule<ModuleShearingUnit> module, ItemUseContext context) {
        IEnergyContainer energyContainer = module.getEnergyContainer();
        if (energyContainer == null || energyContainer.getEnergy().smallerThan(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get())) {
            return ActionResultType.PASS;
        }
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        return MekanismUtils.performActions(
              carvePumpkin(energyContainer, context, state),
              () -> shearBeehive(energyContainer, context, state)
        );
    }

    @Nonnull
    @Override
    public ActionResultType onInteract(IModule<ModuleShearingUnit> module, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (entity instanceof IForgeShearable) {
            IEnergyContainer energyContainer = module.getEnergyContainer();
            if (energyContainer != null && energyContainer.getEnergy().greaterOrEqual(MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get()) &&
                shearEntity(energyContainer, entity, player, module.getContainer(), entity.level, entity.blockPosition())) {
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public ModuleDispenseResult onDispense(IModule<ModuleShearingUnit> module, IBlockSource source) {
        IEnergyContainer energyContainer = module.getEnergyContainer();
        if (energyContainer != null) {
            ServerWorld world = source.getLevel();
            Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos pos = source.getPos().relative(facing);
            if (tryShearBlock(energyContainer, world, pos, facing.getOpposite()) || tryShearLivingEntity(energyContainer, world, pos, module.getContainer())) {
                return ModuleDispenseResult.HANDLED;
            }
        }
        return ModuleDispenseResult.FAIL_PREVENT_DROP;
    }

    private ActionResultType carvePumpkin(IEnergyContainer energyContainer, ItemUseContext context, BlockState state) {
        if (state.is(Blocks.PUMPKIN)) {
            World world = context.getLevel();
            //Carve pumpkin - copy from Pumpkin Block's onBlockActivated
            if (!world.isClientSide) {
                BlockPos pos = context.getClickedPos();
                Direction direction = context.getClickedFace();
                Direction side = direction.getAxis() == Direction.Axis.Y ? context.getHorizontalDirection().getOpposite() : direction;
                world.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundCategory.BLOCKS, 1, 1);
                world.setBlock(pos, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, side), BlockFlags.DEFAULT_AND_RERENDER);
                ItemEntity itementity = new ItemEntity(world, pos.getX() + 0.5 + side.getStepX() * 0.65, pos.getY() + 0.1,
                      pos.getZ() + 0.5 + side.getStepZ() * 0.65, new ItemStack(Items.PUMPKIN_SEEDS, 4));
                itementity.setDeltaMovement(0.05 * side.getStepX() + world.random.nextDouble() * 0.02, 0.05,
                      0.05 * side.getStepZ() + world.random.nextDouble() * 0.02D);
                world.addFreshEntity(itementity);
                energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get(), Action.EXECUTE, AutomationType.MANUAL);
            }
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.PASS;
    }

    private ActionResultType shearBeehive(IEnergyContainer energyContainer, ItemUseContext context, BlockState state) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        if (state.is(BlockTags.BEEHIVES) && state.getBlock() instanceof BeehiveBlock && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
            //Act as shears on beehives
            World world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundCategory.NEUTRAL, 1, 1);
            BeehiveBlock.dropHoneycomb(world, pos);
            BeehiveBlock beehive = (BeehiveBlock) state.getBlock();
            if (CampfireBlock.isSmokeyPos(world, pos)) {
                beehive.resetHoneyLevel(world, state, pos);
            } else {
                if (beehive.hiveContainsBees(world, pos)) {
                    beehive.angerNearbyBees(world, pos);
                }
                beehive.releaseBeesAndResetHoneyLevel(world, state, pos, player, BeehiveTileEntity.State.EMERGENCY);
            }
            if (!world.isClientSide) {
                energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get(), Action.EXECUTE, AutomationType.MANUAL);
            }
            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.PASS;
    }

    //Slightly modified copy of BeehiveDispenseBehavior#tryShearBeehive modified to not crash if the tag has a block that isn't a
    // beehive block instance in it, and also to support shearing pumpkins via the dispenser
    private boolean tryShearBlock(IEnergyContainer energyContainer, ServerWorld world, BlockPos pos, Direction sideClicked) {
        if (energyContainer.getEnergy().greaterOrEqual(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get())) {
            BlockState state = world.getBlockState(pos);
            if (state.is(BlockTags.BEEHIVES) && state.getBlock() instanceof BeehiveBlock && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
                world.playSound(null, pos, SoundEvents.BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
                BeehiveBlock.dropHoneycomb(world, pos);
                ((BeehiveBlock) state.getBlock()).releaseBeesAndResetHoneyLevel(world, state, pos, null, BeehiveTileEntity.State.BEE_RELEASED);
                energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get(), Action.EXECUTE, AutomationType.MANUAL);
                return true;
            } else if (state.is(Blocks.PUMPKIN)) {
                //Carve pumpkin - copy from Pumpkin Block's onBlockActivated
                Direction side = sideClicked.getAxis() == Direction.Axis.Y ? Direction.NORTH : sideClicked;
                world.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundCategory.BLOCKS, 1, 1);
                world.setBlock(pos, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, side), BlockFlags.DEFAULT_AND_RERENDER);
                Block.popResource(world, pos, new ItemStack(Items.PUMPKIN_SEEDS, 4));
                energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get(), Action.EXECUTE, AutomationType.MANUAL);
                return true;
            }
        }
        return false;
    }

    //Modified copy of BeehiveDispenseBehavior#tryShearLivingEntity to work with IForgeShearable
    private boolean tryShearLivingEntity(IEnergyContainer energyContainer, ServerWorld world, BlockPos pos, ItemStack stack) {
        if (energyContainer.getEnergy().greaterOrEqual(MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get())) {
            for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(pos), SHEARABLE)) {
                if (shearEntity(energyContainer, entity, null, stack, world, pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shearEntity(IEnergyContainer energyContainer, LivingEntity entity, @Nullable PlayerEntity player, ItemStack stack, World world, BlockPos pos) {
        IForgeShearable target = (IForgeShearable) entity;
        if (target.isShearable(stack, world, pos)) {
            if (!world.isClientSide) {
                for (ItemStack drop : target.onSheared(player, stack, world, pos, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack))) {
                    ItemEntity ent = entity.spawnAtLocation(drop, 1.0F);
                    if (ent != null) {
                        ent.setDeltaMovement(ent.getDeltaMovement().add((world.random.nextFloat() - world.random.nextFloat()) * 0.1F,
                              world.random.nextFloat() * 0.05F, (world.random.nextFloat() - world.random.nextFloat()) * 0.1F));
                    }
                }
                energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get(), Action.EXECUTE, AutomationType.MANUAL);
            }
            return true;
        }
        return false;
    }
}