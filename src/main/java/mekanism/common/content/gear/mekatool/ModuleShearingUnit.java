package mekanism.common.content.gear.mekatool;

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
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
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

//TODO - 26.1: Look at ShearsItem#createToolProperties and see if we need to or can somehow apply those overrides?
// Also double check the stuff we override as it looks like some of it might have changed in vanilla
@ParametersAreNotNullByDefault
public class ModuleShearingUnit implements ICustomModule<ModuleShearingUnit> {

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
    public InteractionResult onInteract(IModule<ModuleShearingUnit> module, Player player, LivingEntity entity, InteractionHand hand, ItemAccess itemAccess,
          TransactionContext transaction) {
        if (entity instanceof IShearable shearable) {
            try (Transaction subTransaction = Transaction.open(transaction)) {
                Level level = entity.level();
                int cost = MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get();
                if (module.useAllEnergy(player, itemAccess, cost, subTransaction) && shearEntity(shearable, player, ItemAccessUtils.asStack(itemAccess), level, entity.blockPosition())) {
                    //Fire the game event on both sides
                    entity.gameEvent(GameEvent.SHEAR, player);
                    if (!level.isClientSide()) {
                        subTransaction.commit();
                    }
                    return InteractionResult.SUCCESS.heldItemTransformedTo(ItemAccessUtils.asStack(itemAccess));
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
            if (module.useAllEnergy(player, ItemAccess.forStack(stack), cost, subTransaction)) {
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
                    if (!level.isClientSide()) {
                        subTransaction.commit();
                    }
                    return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
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
        ItemStack accessAsStack = ItemAccessUtils.asStack(itemAccess);
        if (CommonHooks.tryDispenseShearsHarvestBlock(source, accessAsStack, world, pos) || ShearsDispenseItemBehavior.tryShearBeehive(world, accessAsStack, pos)) {
            //Handle shearing via tool modified state or on a beehive as tool modified state doesn't get it
            return ModuleDispenseResult.HANDLED;
        }
        //Modified copy of ShearsDispenseItemBehavior#tryShearEntity to handle energy usage when shearing
        try (Transaction subTransaction = Transaction.open(transaction)) {
            //If we are able to use the energy we need to (or there is no cost) then try to see if any of the entities can be sheared
            boolean usedEnergy = module.useAllEnergy(null, itemAccess, MekanismConfig.gear.mekaToolEnergyUsageShearEntity.get(), subTransaction);
            for (Entity entity : world.getEntities(null, new AABB(pos))) {
                if (entity.shearOffAllLeashConnections(null)) {
                    //Note: We don't commit energy usage here (or even check for it), as shearing leashes is normally handled by
                    // the entity and the SHEARS_HARVEST item ability. We just need to implement it here for dispenser usage
                    return ModuleDispenseResult.HANDLED;
                } else if (usedEnergy && entity instanceof IShearable target && shearEntity(target, null, accessAsStack, world, pos)) {
                    if (!world.isClientSide()) {
                        subTransaction.commit();
                    }
                    entity.gameEvent(GameEvent.SHEAR, entity);
                    return ModuleDispenseResult.HANDLED;
                }
            }
        }
        return ModuleDispenseResult.FAIL_PREVENT_DROP;
    }

    private boolean shearEntity(IShearable target, @Nullable Player player, ItemStack stack, Level world, BlockPos pos) {
        if (target.isShearable(player, stack, world, pos)) {
            if (!world.isClientSide() && world instanceof ServerLevel level) {
                for (ItemStack drop : target.onSheared(player, stack, level, pos)) {
                    target.spawnShearedDrop(level, pos, drop);
                }
            }
            return true;
        }
        return false;
    }
}