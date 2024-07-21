package mekanism.common.content.gear.mekatool;

import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.registries.MekanismModules;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 1.21: Look at ShearsItem#createToolProperties and see if we need to or can somehow apply those overrides?
// Also double check the stuff we override as it looks like some of it might have changed in vanilla
@ParametersAreNotNullByDefault
public class ModuleShearingUnit implements ICustomModule<ModuleShearingUnit> {

    private static final Predicate<Entity> SHEARABLE = entity -> !entity.isSpectator() && entity instanceof IShearable;

    @Override
    public boolean canPerformAction(IModule<ModuleShearingUnit> module, IModuleContainer container, ItemStack stack, ItemAbility action) {
        if (action == ItemAbilities.SHEARS_DISARM) {
            if (stack.getItem() instanceof ItemMekaTool) {
                //Only require energy if we are installed on a Meka-Tool and can thus calculate the energy required to break the block "safely"
                // Note: We assume hardness is zero like the default is for tripwires as we don't have the target block in our current context
                long cost = ItemMekaTool.getDestroyEnergy(container, 0, container.hasEnabled(MekanismModules.SILK_TOUCH_UNIT));
                return module.hasEnoughEnergy(stack, cost);
            }
            //Note: If for some reason we are installed on something that is not the Meka-Tool don't stop the action from being enabled
            // as it may not actually require energy
            return true;
        } else if (action == ItemAbilities.SHEARS_DIG) {
            //Note: If for some reason we are installed on something that is not the Meka-Tool don't stop the action from being enabled
            // as it may not actually require energy
            return !(stack.getItem() instanceof ItemMekaTool) || ItemMekaTool.hasEnergyForDigAction(container, module.getEnergyContainer(stack));
        } else if (action == ItemAbilities.SHEARS_TRIM) {
            return module.hasEnoughEnergy(stack, MekanismConfig.gear.mekaToolEnergyUsageShearTrim);
        }
        return ItemAbilities.DEFAULT_SHEARS_ACTIONS.contains(action);
    }

    @NotNull
    @Override
    public InteractionResult onInteract(IModule<ModuleShearingUnit> module, Player player, LivingEntity entity, InteractionHand hand, IModuleContainer moduleContainer, ItemStack stack) {
        if (entity instanceof IShearable) {
            long cost = MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get();
            IEnergyContainer energyContainer = module.getEnergyContainer(stack);
            if (cost == 0L || energyContainer != null && energyContainer.getEnergy() >= cost &&
                              shearEntity(energyContainer, entity, player, stack, entity.level(), entity.blockPosition())) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(IModule<ModuleShearingUnit> module, UseOnContext context) {
        long cost = MekanismConfig.gear.mekaToolEnergyUsageShearTrim.get();
        ItemStack stack = context.getItemInHand();
        IEnergyContainer energyContainer = module.getEnergyContainer(stack);
        if (cost == 0L || energyContainer != null && energyContainer.getEnergy() >= cost) {
            //Copy of ShearsItem#useOn
            Level level = context.getLevel();
            BlockPos blockpos = context.getClickedPos();
            BlockState state = level.getBlockState(blockpos);
            BlockState trimmedState = state.getToolModifiedState(context, ItemAbilities.SHEARS_TRIM, false);
            if (trimmedState != null) {
                if (context.getPlayer() instanceof ServerPlayer player) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, blockpos, stack);
                }
                level.setBlockAndUpdate(blockpos, trimmedState);
                level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(context.getPlayer(), trimmedState));
                if (cost > 0) {
                    energyContainer.extract(cost, Action.EXECUTE, AutomationType.MANUAL);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
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
        if (CommonHooks.tryDispenseShearsHarvestBlock(source, stack, world, pos) || tryShearLivingEntity(module.getEnergyContainer(stack), world, pos, stack)) {
            return ModuleDispenseResult.HANDLED;
        }
        return ModuleDispenseResult.FAIL_PREVENT_DROP;
    }

    //Modified copy of ShearsDispenseItemBehavior#tryShearLivingEntity to work with IForgeShearable
    private boolean tryShearLivingEntity(@Nullable IEnergyContainer energyContainer, ServerLevel world, BlockPos pos, ItemStack stack) {
        long cost = MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get();
        if (cost == 0L || energyContainer != null && energyContainer.getEnergy() >= MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get()) {
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
        if (target.isShearable(player, stack, world, pos)) {
            if (!world.isClientSide) {
                for (ItemStack drop : target.onSheared(player, stack, world, pos)) {
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