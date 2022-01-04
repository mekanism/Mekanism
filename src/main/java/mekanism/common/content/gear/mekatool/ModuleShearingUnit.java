package mekanism.common.content.gear.mekatool;

import java.util.Collection;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

//Note: Some parts of this module are directly implemented in the meka tool most notably the handling of disarming tripwire hooks,
// and also exposing the shears tool type on the meka tool when shears are installed
@ParametersAreNonnullByDefault
public class ModuleShearingUnit implements ICustomModule<ModuleShearingUnit> {

    private static final Predicate<Entity> SHEARABLE = entity -> !entity.isSpectator() && entity instanceof IForgeShearable;

    @Nonnull
    @Override
    public InteractionResult onItemUse(IModule<ModuleShearingUnit> module, UseOnContext context) {
        IEnergyContainer energyContainer = module.getEnergyContainer();
        if (energyContainer == null || energyContainer.getEnergy().smallerThan(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get())) {
            return InteractionResult.PASS;
        }
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        //TODO - 1.18: Re-evaluate this, it isn't needed here anymore due to the tool action
        return MekanismUtils.performActions(
              carvePumpkin(energyContainer, context, state),
              () -> shearBeehive(energyContainer, context, state)
        );
    }

    @Nonnull
    @Override
    public Collection<ToolAction> getProvidedToolActions(IModule<ModuleShearingUnit> module) {
        return ToolActions.DEFAULT_SHEARS_ACTIONS;
    }

    @Nonnull
    @Override
    public InteractionResult onInteract(IModule<ModuleShearingUnit> module, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof IForgeShearable) {
            IEnergyContainer energyContainer = module.getEnergyContainer();
            if (energyContainer != null && energyContainer.getEnergy().greaterOrEqual(MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get()) &&
                shearEntity(energyContainer, entity, player, module.getContainer(), entity.level, entity.blockPosition())) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public ModuleDispenseResult onDispense(IModule<ModuleShearingUnit> module, BlockSource source) {
        IEnergyContainer energyContainer = module.getEnergyContainer();
        if (energyContainer != null) {
            ServerLevel world = source.getLevel();
            Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos pos = source.getPos().relative(facing);
            if (tryShearBlock(energyContainer, world, pos, facing.getOpposite()) || tryShearLivingEntity(energyContainer, world, pos, module.getContainer())) {
                return ModuleDispenseResult.HANDLED;
            }
        }
        return ModuleDispenseResult.FAIL_PREVENT_DROP;
    }

    private InteractionResult carvePumpkin(IEnergyContainer energyContainer, UseOnContext context, BlockState state) {
        if (state.is(Blocks.PUMPKIN)) {
            Level world = context.getLevel();
            //Carve pumpkin - copy from Pumpkin Block's onBlockActivated
            if (!world.isClientSide) {
                BlockPos pos = context.getClickedPos();
                Direction direction = context.getClickedFace();
                Direction side = direction.getAxis() == Direction.Axis.Y ? context.getHorizontalDirection().getOpposite() : direction;
                world.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1, 1);
                world.setBlock(pos, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, side), Block.UPDATE_ALL_IMMEDIATE);
                ItemEntity itementity = new ItemEntity(world, pos.getX() + 0.5 + side.getStepX() * 0.65, pos.getY() + 0.1,
                      pos.getZ() + 0.5 + side.getStepZ() * 0.65, new ItemStack(Items.PUMPKIN_SEEDS, 4));
                itementity.setDeltaMovement(0.05 * side.getStepX() + world.random.nextDouble() * 0.02, 0.05,
                      0.05 * side.getStepZ() + world.random.nextDouble() * 0.02D);
                world.addFreshEntity(itementity);
                energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get(), Action.EXECUTE, AutomationType.MANUAL);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult shearBeehive(IEnergyContainer energyContainer, UseOnContext context, BlockState state) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (state.is(BlockTags.BEEHIVES) && state.getBlock() instanceof BeehiveBlock && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
            //Act as shears on beehives
            Level world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundSource.NEUTRAL, 1, 1);
            BeehiveBlock.dropHoneycomb(world, pos);
            BeehiveBlock beehive = (BeehiveBlock) state.getBlock();
            if (CampfireBlock.isSmokeyPos(world, pos)) {
                beehive.resetHoneyLevel(world, state, pos);
            } else {
                if (beehive.hiveContainsBees(world, pos)) {
                    beehive.angerNearbyBees(world, pos);
                }
                beehive.releaseBeesAndResetHoneyLevel(world, state, pos, player, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
            }
            if (!world.isClientSide) {
                energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get(), Action.EXECUTE, AutomationType.MANUAL);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    //Slightly modified copy of BeehiveDispenseBehavior#tryShearBeehive modified to not crash if the tag has a block that isn't a
    // beehive block instance in it, and also to support shearing pumpkins via the dispenser
    private boolean tryShearBlock(IEnergyContainer energyContainer, ServerLevel world, BlockPos pos, Direction sideClicked) {
        if (energyContainer.getEnergy().greaterOrEqual(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get())) {
            BlockState state = world.getBlockState(pos);
            if (state.is(BlockTags.BEEHIVES) && state.getBlock() instanceof BeehiveBlock && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
                world.playSound(null, pos, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                BeehiveBlock.dropHoneycomb(world, pos);
                ((BeehiveBlock) state.getBlock()).releaseBeesAndResetHoneyLevel(world, state, pos, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
                energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get(), Action.EXECUTE, AutomationType.MANUAL);
                return true;
            } else if (state.is(Blocks.PUMPKIN)) {
                //Carve pumpkin - copy from Pumpkin Block's onBlockActivated
                Direction side = sideClicked.getAxis() == Direction.Axis.Y ? Direction.NORTH : sideClicked;
                world.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1, 1);
                world.setBlock(pos, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, side), Block.UPDATE_ALL_IMMEDIATE);
                Block.popResource(world, pos, new ItemStack(Items.PUMPKIN_SEEDS, 4));
                energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get(), Action.EXECUTE, AutomationType.MANUAL);
                return true;
            }
        }
        return false;
    }

    //Modified copy of BeehiveDispenseBehavior#tryShearLivingEntity to work with IForgeShearable
    private boolean tryShearLivingEntity(IEnergyContainer energyContainer, ServerLevel world, BlockPos pos, ItemStack stack) {
        if (energyContainer.getEnergy().greaterOrEqual(MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get())) {
            for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, new AABB(pos), SHEARABLE)) {
                if (shearEntity(energyContainer, entity, null, stack, world, pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shearEntity(IEnergyContainer energyContainer, LivingEntity entity, @Nullable Player player, ItemStack stack, Level world, BlockPos pos) {
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