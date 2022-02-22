package mekanism.common.content.gear.mekatool;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.registries.MekanismModules;
import net.minecraft.Util;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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

@ParametersAreNonnullByDefault
public class ModuleShearingUnit implements ICustomModule<ModuleShearingUnit> {

    private static final Predicate<Entity> SHEARABLE = entity -> !entity.isSpectator() && entity instanceof IForgeShearable;
    private static final Set<ToolAction> SHEAR_ACTIONS_NO_DISARM = Util.make(() -> {
        Set<ToolAction> actions = new HashSet<>(ToolActions.DEFAULT_SHEARS_ACTIONS);
        actions.remove(ToolActions.SHEARS_DISARM);
        return actions;
    });

    //TODO - 1.18: Re-evaluate how to make MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get()
    // get used or remove the energy cost (maybe with something like the below)
    //TODO - 1.18: Register on player join, and unregister on player leave
    /*public static class ShearingListener implements GameEventListener {

        private final Player player;

        public ShearingListener(Player player) {
            this.player = player;
        }

        @Nonnull
        @Override
        public PositionSource getListenerSource() {
            //TODO - 1.18: Custom instance that returns based on player (or use entity and figure out player int id)
            return null;
        }

        @Override
        public int getListenerRadius() {
            //TODO - 1.18: player reach distance
            return 0;
        }

        @Override
        public boolean handleGameEvent(Level level, GameEvent event, @Nullable Entity entity, BlockPos pos) {
            if (event == GameEvent.SHEAR && entity == player) {
                //TODO - 1.18: Validate holding a meka tool with shearing installed and enabled
                BlockState state = level.getBlockState(pos);
                if (!state.isAir()) {//TODO - 1.18: Validate it isn't a replaceable/block an entity could be standing in?
                    //TODO - 1.18: Use energy??
                    //TODO - 1.18: Figure out if we are supposed to return true or false
                }
            }
            return false;
        }
    }*/

    @Nonnull
    @Override
    public Collection<ToolAction> getProvidedToolActions(IModule<ModuleShearingUnit> module) {
        //TODO - 1.18: Switch this to canPerform similar to the base one, that way we can cut down on required
        // calculations regarding energy if that isn't even affecting the current query
        ItemStack container = module.getContainer();
        if (container.getItem() instanceof ItemMekaTool mekaTool) {
            //If we are installed on a Meka-Tool only provide the disarm action if we have enough energy to break the block
            // and not if we would only break it but in an unsafe manner
            // Note: We assume hardness is zero like the default is for tripwires as we don't have the target block in our current context
            FloatingLong cost = mekaTool.getDestroyEnergy(container, 0, mekaTool.isModuleEnabled(container, MekanismModules.SILK_TOUCH_UNIT));
            IEnergyContainer energyContainer = module.getEnergyContainer();
            if (energyContainer == null || energyContainer.getEnergy().smallerThan(cost)) {
                return SHEAR_ACTIONS_NO_DISARM;
            }
        }
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

    //Slightly modified copy of ShearsDispenseItemBehavior#tryShearBeehive modified to not crash if the tag has a block that isn't a
    // beehive block instance in it, and also to support shearing pumpkins via the dispenser
    private boolean tryShearBlock(IEnergyContainer energyContainer, ServerLevel world, BlockPos pos, Direction sideClicked) {
        if (energyContainer.getEnergy().greaterOrEqual(MekanismConfig.gear.mekaToolEnergyUsageShearBlock.get())) {
            BlockState state = world.getBlockState(pos);
            if (state.is(BlockTags.BEEHIVES) && state.getBlock() instanceof BeehiveBlock beehive && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
                world.playSound(null, pos, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                BeehiveBlock.dropHoneycomb(world, pos);
                beehive.releaseBeesAndResetHoneyLevel(world, state, pos, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
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

    //Modified copy of ShearsDispenseItemBehavior#tryShearLivingEntity to work with IForgeShearable
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
                List<ItemStack> drops = target.onSheared(player, stack, world, pos, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack));
                //Note: Shear game event is handled by the target in onSheared
                for (ItemStack drop : drops) {
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