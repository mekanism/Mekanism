package mekanism.common.content.gear.mekatool;

import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.registries.MekanismModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 1.20.5: Look at ShearsItem#createToolProperties and see if we need to or can somehow apply those overrides?
// Also double check the stuff we override as it looks like some of it might have changed in vanilla
@ParametersAreNotNullByDefault
public class ModuleShearingUnit implements ICustomModule<ModuleShearingUnit> {

    private static final Predicate<Entity> SHEARABLE = entity -> !entity.isSpectator() && entity instanceof IShearable;

    @Override
    public boolean canPerformAction(IModule<ModuleShearingUnit> module, IModuleContainer container, ItemStack stack, ToolAction action) {
        if (action == ToolActions.SHEARS_DISARM) {
            if (stack.getItem() instanceof ItemMekaTool) {
                //Only require energy if we are installed on a Meka-Tool and can thus calculate the energy required to break the block "safely"
                // Note: We assume hardness is zero like the default is for tripwires as we don't have the target block in our current context
                FloatingLong cost = ItemMekaTool.getDestroyEnergy(container, 0, container.hasEnabled(MekanismModules.SILK_TOUCH_UNIT));
                return module.hasEnoughEnergy(stack, cost);
            }
            //Note: If for some reason we are installed on something that is not the Meka-Tool don't stop the action from being enabled
            // as it may not actually require energy
            return true;
        } else if (action == ToolActions.SHEARS_DIG) {
            //Note: If for some reason we are installed on something that is not the Meka-Tool don't stop the action from being enabled
            // as it may not actually require energy
            return !(stack.getItem() instanceof ItemMekaTool) || ItemMekaTool.hasEnergyForDigAction(container, module.getEnergyContainer(stack));
        }
        return ToolActions.DEFAULT_SHEARS_ACTIONS.contains(action);
    }

    @NotNull
    @Override
    public InteractionResult onInteract(IModule<ModuleShearingUnit> module, Player player, LivingEntity entity, InteractionHand hand, IModuleContainer moduleContainer, ItemStack stack) {
        if (entity instanceof IShearable) {
            FloatingLong cost = MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get();
            IEnergyContainer energyContainer = module.getEnergyContainer(stack);
            if (cost.isZero() || energyContainer != null && energyContainer.getEnergy().greaterOrEqual(cost) &&
                                 shearEntity(energyContainer, entity, player, stack, entity.level(), entity.blockPosition())) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public ModuleDispenseResult onDispense(IModule<ModuleShearingUnit> module, IModuleContainer moduleContainer, ItemStack stack, BlockSource source) {
        ServerLevel world = source.level();
        Direction facing = source.state().getValue(DispenserBlock.FACING);
        BlockPos pos = source.pos().relative(facing);
        if (tryShearBlock(world, pos, facing.getOpposite()) || tryShearLivingEntity(module.getEnergyContainer(stack), world, pos, stack)) {
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
        IShearable target = (IShearable) entity;
        if (target.isShearable(stack, world, pos)) {
            if (!world.isClientSide) {
                for (ItemStack drop : target.onSheared(player, stack, world, pos, stack.getEnchantmentLevel(Enchantments.FORTUNE))) {
                    target.spawnShearedDrop(world, pos, drop);
                }
                entity.gameEvent(GameEvent.SHEAR, player);
                if (energyContainer != null) {
                    energyContainer.extract(MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get(), Action.EXECUTE, AutomationType.MANUAL);
                }
            }
            return true;
        }
        return false;
    }
}