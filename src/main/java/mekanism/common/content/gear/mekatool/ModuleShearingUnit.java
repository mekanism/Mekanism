package mekanism.common.content.gear.mekatool;

import java.util.function.Predicate;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.registries.MekanismItems;
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
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

//TODO - 1.21: Look at ShearsItem#createToolProperties and see if we need to or can somehow apply those overrides?
// Also double check the stuff we override as it looks like some of it might have changed in vanilla
@ParametersAreNotNullByDefault
public class ModuleShearingUnit implements ICustomModule<ModuleShearingUnit> {

    private static final Predicate<Entity> SHEARABLE = entity -> !entity.isSpectator() && entity instanceof IShearable;

    @Override
    public boolean canPerformAction(IModule<ModuleShearingUnit> module, IModuleContainer container, @UnknownNullability ItemStack stack, ItemAbility action) {
        if (action == ItemAbilities.SHEARS_DISARM) {
            if (stack.is(MekanismItems.MEKA_TOOL)) {
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
            return !stack.is(MekanismItems.MEKA_TOOL) || ItemMekaTool.hasEnergyForDigAction(container, module.getEnergyContainer(stack));
        } else if (action == ItemAbilities.SHEARS_TRIM) {
            return module.hasEnoughEnergy(stack, MekanismConfig.gear.mekaToolEnergyUsageShearTrim);
        }
        return ItemAbilities.DEFAULT_SHEARS_ACTIONS.contains(action);
    }

    @NotNull
    @Override
    public InteractionResult onInteract(IModule<ModuleShearingUnit> module, Player player, LivingEntity entity, InteractionHand hand, IModuleContainer moduleContainer, ItemStack stack) {
        if (entity instanceof IShearable) {
            //TODO - 26.1: is there a risk that this is in a transactional context? Such as if an auto clicker is using energy,
            // and wraps the entire hitting the entity within their transaction?
            try (Transaction transaction = Transaction.openRoot()) {
                Level level = entity.level();
                long cost = MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get();
                if (module.useEnergy(player, stack, cost, transaction) == cost && shearEntity(entity, player, stack, level, entity.blockPosition())) {
                    if (!level.isClientSide()) {
                        transaction.commit();
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(IModule<ModuleShearingUnit> module, UseOnContext context) {
        long cost = MekanismConfig.gear.mekaToolEnergyUsageShearTrim.get();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        //TODO - 26.1: is there a risk that this is in a transactional context? Such as if an auto clicker is using energy,
        // and wraps the entire hitting the entity within their transaction?
        try (Transaction transaction = Transaction.openRoot()) {
            if (module.useEnergy(player, stack, cost, transaction) == cost) {
                //Copy of ShearsItem#useOn
                Level level = context.getLevel();
                BlockPos blockpos = context.getClickedPos();
                BlockState state = level.getBlockState(blockpos);
                BlockState trimmedState = state.getToolModifiedState(context, ItemAbilities.SHEARS_TRIM, false);
                if (trimmedState != null) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockpos, stack);
                    }
                    level.setBlockAndUpdate(blockpos, trimmedState);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, trimmedState));
                    //TODO - 26.1: Should we only commit on the server?
                    transaction.commit();
                    return InteractionResult.SUCCESS;
                }
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
        if (CommonHooks.tryDispenseShearsHarvestBlock(source, stack, world, pos)) {
            return ModuleDispenseResult.HANDLED;
        }
        //TODO - 26.1: Vanilla dispensers try shearing a beehive at the location before trying to shear any entities
        // Should we be doing so here? I think at one point we did, so figure out what happened to it
        //Modified copy of ShearsDispenseItemBehavior#tryShearLivingEntity to work with IForgeShearable
        try (Transaction transaction = Transaction.openRoot()) {
            long cost = MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get();
            //If we are able to use the energy we need to (or there is no cost) then try to see if any of the entities can be sheared
            if (module.useEnergy(null, stack, cost, transaction) == cost) {
                for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, new AABB(pos), SHEARABLE)) {
                    if (shearEntity(entity, null, stack, world, pos)) {
                        if (!world.isClientSide()) {
                            transaction.commit();
                        }
                        return ModuleDispenseResult.HANDLED;
                    }
                }
            }
        }
        return ModuleDispenseResult.FAIL_PREVENT_DROP;
    }

    private boolean shearEntity(LivingEntity entity, @Nullable Player player, ItemStack stack, Level world, BlockPos pos) {
        IShearable target = (IShearable) entity;
        if (target.isShearable(player, stack, world, pos)) {
            if (!world.isClientSide() && world instanceof ServerLevel level) {
                for (ItemStack drop : target.onSheared(player, stack, level, pos)) {
                    target.spawnShearedDrop(level, pos, drop);
                }
                entity.gameEvent(GameEvent.SHEAR, player);
            }
            return true;
        }
        return false;
    }
}