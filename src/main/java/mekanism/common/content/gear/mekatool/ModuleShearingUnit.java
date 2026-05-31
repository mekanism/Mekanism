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
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 1.21: Look at ShearsItem#createToolProperties and see if we need to or can somehow apply those overrides?
// Also double check the stuff we override as it looks like some of it might have changed in vanilla
@ParametersAreNotNullByDefault
public class ModuleShearingUnit implements ICustomModule<ModuleShearingUnit> {

    private static final Predicate<Entity> SHEARABLE = entity -> !entity.isSpectator() && entity instanceof IShearable;

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean canPerformAction(IModule<ModuleShearingUnit> module, IModuleContainer container, ITEM instance,
          ItemAbility action) {
        if (action == ItemAbilities.SHEARS_DISARM) {
            if (instance.is(MekanismItems.MEKA_TOOL)) {
                //Only require energy if we are installed on a Meka-Tool and can thus calculate the energy required to break the block "safely"
                // Note: We assume hardness is zero like the default is for tripwires as we don't have the target block in our current context
                int cost = ItemMekaTool.getDestroyEnergy(container, 0, container.hasEnabled(MekanismModules.SILK_TOUCH_UNIT));
                return module.hasEnoughEnergy(ItemAccessUtils.queryOnlyAccess(instance), cost);
            }
            //Note: If for some reason we are installed on something that is not the Meka-Tool don't stop the action from being enabled
            // as it may not actually require energy
            return true;
        } else if (action == ItemAbilities.SHEARS_DIG) {
            //Note: If for some reason we are installed on something that is not the Meka-Tool don't stop the action from being enabled
            // as it may not actually require energy
            return !instance.is(MekanismItems.MEKA_TOOL) || ItemMekaTool.hasEnergyForDigAction(container, module.getEnergyHandler(ItemAccessUtils.queryOnlyAccess(instance)));
        } else if (action == ItemAbilities.SHEARS_TRIM) {
            return module.hasEnoughEnergy(ItemAccessUtils.queryOnlyAccess(instance), MekanismConfig.gear.mekaToolEnergyUsageShearTrim);
        }
        return ItemAbilities.DEFAULT_SHEARS_ACTIONS.contains(action);
    }

    @NotNull
    @Override
    public InteractionResult onInteract(IModule<ModuleShearingUnit> module, Player player, LivingEntity entity, InteractionHand hand,
          ItemAccess itemAccess, TransactionContext transaction) {
        if (entity instanceof IShearable) {
            try (Transaction subTransaction = Transaction.open(transaction)) {
                Level level = entity.level();
                int cost = MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get();
                if (module.useEnergy(player, itemAccess, cost, subTransaction) == cost && shearEntity(entity, player, itemAccess, level, entity.blockPosition())) {
                    if (!level.isClientSide()) {
                        subTransaction.commit();
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(IModule<ModuleShearingUnit> module, UseOnContext context, TransactionContext transaction) {
        int cost = MekanismConfig.gear.mekaToolEnergyUsageShearTrim.get();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        try (Transaction subTransaction = Transaction.open(transaction)) {
            if (module.useEnergy(player, ItemAccess.forStack(stack), cost, subTransaction) == cost) {
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
                    subTransaction.commit();
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public ModuleDispenseResult onDispense(IModule<ModuleShearingUnit> module, ItemAccess itemAccess, BlockSource source, TransactionContext transaction) {
        ServerLevel world = source.level();
        Direction facing = source.state().getValue(DispenserBlock.FACING);
        BlockPos pos = source.pos().relative(facing);
        if (CommonHooks.tryDispenseShearsHarvestBlock(source, itemAccess.getResource().toStack(itemAccess.getAmount()), world, pos)) {
            return ModuleDispenseResult.HANDLED;
        }
        //TODO - 26.1: Vanilla dispensers try shearing a beehive at the location before trying to shear any entities
        // Should we be doing so here? I think at one point we did, so figure out what happened to it
        //Modified copy of ShearsDispenseItemBehavior#tryShearLivingEntity to work with IForgeShearable
        try (Transaction subTransaction = Transaction.openRoot()) {
            int cost = MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get();
            //If we are able to use the energy we need to (or there is no cost) then try to see if any of the entities can be sheared
            if (module.useEnergy(null, itemAccess, cost, subTransaction) == cost) {
                for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, new AABB(pos), SHEARABLE)) {
                    if (shearEntity(entity, null, itemAccess, world, pos)) {
                        if (!world.isClientSide()) {
                            subTransaction.commit();
                        }
                        return ModuleDispenseResult.HANDLED;
                    }
                }
            }
        }
        return ModuleDispenseResult.FAIL_PREVENT_DROP;
    }

    private boolean shearEntity(LivingEntity entity, @Nullable Player player, ItemAccess itemAccess, Level world, BlockPos pos) {
        ItemStack stack = itemAccess.getResource().toStack(itemAccess.getAmount());
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