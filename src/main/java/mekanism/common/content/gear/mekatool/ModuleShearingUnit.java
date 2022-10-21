package mekanism.common.content.gear.mekatool;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.registries.MekanismModules;
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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class ModuleShearingUnit implements ICustomModule<ModuleShearingUnit> {

    private static final Predicate<Entity> SHEARABLE = entity -> !entity.isSpectator() && entity instanceof IForgeShearable;

    @Override
    public boolean canPerformAction(IModule<ModuleShearingUnit> module, ToolAction action) {
        if (action == ToolActions.SHEARS_DISARM) {
            return hasEnergyForAction(module, () -> {
                ItemStack container = module.getContainer();
                if (container.getItem() instanceof ItemMekaTool mekaTool) {
                    //Only require energy if we are installed on a Meka-Tool and can thus calculate the energy required to break the block "safely"
                    // Note: We assume hardness is zero like the default is for tripwires as we don't have the target block in our current context
                    return mekaTool.getDestroyEnergy(container, 0, mekaTool.isModuleEnabled(container, MekanismModules.SILK_TOUCH_UNIT));
                }
                return FloatingLong.ZERO;
            });
        }
        return ToolActions.DEFAULT_SHEARS_ACTIONS.contains(action);
    }

    private boolean hasEnergyForAction(IModule<ModuleShearingUnit> module, FloatingLongSupplier costSupplier) {
        FloatingLong cost = costSupplier.get();
        if (cost.isZero()) {
            return true;
        }
        IEnergyContainer energyContainer = module.getEnergyContainer();
        return energyContainer == null || energyContainer.getEnergy().smallerThan(cost);
    }

    @NotNull
    @Override
    public InteractionResult onInteract(IModule<ModuleShearingUnit> module, Player player, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof IForgeShearable) {
            FloatingLong cost = MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get();
            IEnergyContainer energyContainer = module.getEnergyContainer();
            if (cost.isZero() || energyContainer != null && energyContainer.getEnergy().greaterOrEqual(cost) &&
                                 shearEntity(energyContainer, entity, player, module.getContainer(), entity.level, entity.blockPosition())) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public ModuleDispenseResult onDispense(IModule<ModuleShearingUnit> module, BlockSource source) {
        ServerLevel world = source.getLevel();
        Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
        BlockPos pos = source.getPos().relative(facing);
        if (tryShearBlock(world, pos, facing.getOpposite()) || tryShearLivingEntity(module.getEnergyContainer(), world, pos, module.getContainer())) {
            return ModuleDispenseResult.HANDLED;
        }
        return ModuleDispenseResult.FAIL_PREVENT_DROP;
    }

    //Slightly modified copy of ShearsDispenseItemBehavior#tryShearBeehive modified to not crash if the tag has a block that isn't a
    // beehive block instance in it, and also to support shearing pumpkins via the dispenser
    private boolean tryShearBlock(ServerLevel world, BlockPos pos, Direction sideClicked) {
        BlockState state = world.getBlockState(pos);
        if (state.is(BlockTags.BEEHIVES) && state.getBlock() instanceof BeehiveBlock beehive && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
            world.playSound(null, pos, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            BeehiveBlock.dropHoneycomb(world, pos);
            beehive.releaseBeesAndResetHoneyLevel(world, state, pos, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
            return true;
        } else if (state.is(Blocks.PUMPKIN)) {
            //Carve pumpkin - copy from Pumpkin Block's onBlockActivated
            Direction side = sideClicked.getAxis() == Direction.Axis.Y ? Direction.NORTH : sideClicked;
            world.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1, 1);
            world.setBlock(pos, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, side), Block.UPDATE_ALL_IMMEDIATE);
            Block.popResource(world, pos, new ItemStack(Items.PUMPKIN_SEEDS, 4));
            return true;
        }
        return false;
    }

    //Modified copy of ShearsDispenseItemBehavior#tryShearLivingEntity to work with IForgeShearable
    private boolean tryShearLivingEntity(@Nullable IEnergyContainer energyContainer, ServerLevel world, BlockPos pos, ItemStack stack) {
        FloatingLong cost = MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get();
        if (cost.isZero() || energyContainer != null && energyContainer.getEnergy().greaterOrEqual(MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get())) {
            for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, new AABB(pos), SHEARABLE)) {
                if (shearEntity(energyContainer, entity, null, stack, world, pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shearEntity(@Nullable IEnergyContainer energyContainer, LivingEntity entity, @Nullable Player player, ItemStack stack, Level world, BlockPos pos) {
        IForgeShearable target = (IForgeShearable) entity;
        if (target.isShearable(stack, world, pos)) {
            if (!world.isClientSide) {
                List<ItemStack> drops = target.onSheared(player, stack, world, pos, stack.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE));
                //Note: Shear game event is handled by the target in onSheared
                for (ItemStack drop : drops) {
                    ItemEntity ent = entity.spawnAtLocation(drop, 1.0F);
                    if (ent != null) {
                        ent.setDeltaMovement(ent.getDeltaMovement().add((world.random.nextFloat() - world.random.nextFloat()) * 0.1F,
                              world.random.nextFloat() * 0.05F, (world.random.nextFloat() - world.random.nextFloat()) * 0.1F));
                    }
                }
                if (energyContainer != null) {
                    energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get(), Action.EXECUTE, AutomationType.MANUAL);
                }
            }
            return true;
        }
        return false;
    }
}