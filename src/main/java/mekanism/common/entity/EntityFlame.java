package mekanism.common.entity;

import java.util.Optional;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityFlame extends Projectile implements IEntityWithComplexSpawn {

    public static final int LIFESPAN = 4 * SharedConstants.TICKS_PER_SECOND;
    private static final int DAMAGE = 10;

    public EntityFlame(EntityType<EntityFlame> type, Level world) {
        super(type, world);
    }

    @Nullable
    public static EntityFlame create(Level level, LivingEntity owner, InteractionHand hand, FlamethrowerMode mode) {
        EntityFlame flame = MekanismEntityTypes.FLAME.get().create(level);
        if (flame == null) {
            return null;
        }
        Pos3D ownerPos = new Pos3D(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        Pos3D flameVec = new Pos3D(1, 1, 1);

        boolean rightHanded = MekanismUtils.isRightArm(owner, hand);

        Vec3 lookVec = owner.getLookAngle();
        flameVec = flameVec.multiply(lookVec)
              .yRot(rightHanded ? 10 : -10);

        Vec3 mergedVec = ownerPos.add(flameVec);
        flame.setPos(mergedVec.x, mergedVec.y, mergedVec.z);
        flame.setOwner(owner);
        flame.setData(MekanismAttachmentTypes.FLAMETHROWER_MODE, mode);
        flame.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0, 0.5F, 1);
        //Attempt to ray trace the area between the owner and where the flame would actually start
        // if it hits a block instead just have the flame hit the block directly to avoid being able
        // to shoot a flamethrower through one thick walls.
        BlockHitResult blockRayTrace = level.clip(new ClipContext(ownerPos, mergedVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, flame));
        if (blockRayTrace.getType() != Type.MISS) {
            flame.onHit(blockRayTrace);
        }
        return flame;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > LIFESPAN) {
            discard();
        } else {
            Vec3 localVec = position();
            Vec3 motion = getDeltaMovement();
            Vec3 motionVec = localVec.add(motion);
            HitResult hitResult = level().clip(new ClipContext(localVec, motionVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));
            if (hitResult.getType() != Type.MISS) {
                motionVec = hitResult.getLocation();
            }
            EntityHitResult entityResult = ProjectileUtil.getEntityHitResult(level(), this, localVec, motionVec,
                  getBoundingBox().expandTowards(getDeltaMovement()).inflate(1.0), this::canHitEntity);
            if (entityResult != null && entityResult.getType() == Type.ENTITY) {
                if (entityResult.getEntity() instanceof Player target && getOwner() instanceof Player owner && !owner.canHarmPlayer(target)) {
                    hitResult = null;
                } else {
                    hitResult = entityResult;
                }
            }
            if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
                if (!EventHooks.onProjectileImpact(this, hitResult)) {
                    onHit(hitResult);
                }
            }

            setPos(motionVec.x, motionVec.y, motionVec.z);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityResult) {
        Entity entity = entityResult.getEntity();
        if (entity instanceof Player player) {
            Entity owner = getOwner();
            if (player.getAbilities().invulnerable || owner instanceof Player o && !o.canHarmPlayer(player)) {
                return;
            }
        }
        if (!entity.fireImmune()) {
            if (entity instanceof ItemEntity item && getData(MekanismAttachmentTypes.FLAMETHROWER_MODE) == FlamethrowerMode.HEAT) {
                if (entity.tickCount > 5 * SharedConstants.TICKS_PER_SECOND && !smeltItem(item)) {
                    burn(entity);
                }
            } else {
                burn(entity);
            }
        }
        discard();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult blockRayTrace) {
        super.onHitBlock(blockRayTrace);
        BlockPos hitPos = blockRayTrace.getBlockPos();
        Direction hitSide = blockRayTrace.getDirection();
        BlockState hitState = level().getBlockState(hitPos);
        boolean hitFluid = !hitState.getFluidState().isEmpty();
        if (!level().isClientSide && MekanismConfig.general.aestheticWorldDamage.get() && !hitFluid) {
            FlamethrowerMode mode = getData(MekanismAttachmentTypes.FLAMETHROWER_MODE);
            if (mode == FlamethrowerMode.HEAT) {
                Entity owner = getOwner();
                if (owner instanceof Player player) {
                    smeltBlock(player, hitState, hitPos, hitSide);
                }
            } else if (mode == FlamethrowerMode.INFERNO) {
                Entity owner = getOwner();
                BlockPos sidePos = hitPos.relative(hitSide);
                UseOnContext igniterContext = new UseOnContext(level(), owner instanceof Player player ? player : null,
                      InteractionHand.MAIN_HAND, new ItemStack(Items.FIRE_CHARGE), blockRayTrace);
                if (!tryModify(owner, hitPos, hitSide, hitState, igniterContext, ItemAbilities.FIRESTARTER_LIGHT)) {
                    if (BaseFireBlock.canBePlacedAt(level(), sidePos, hitSide)) {
                        tryPlace(owner, sidePos, hitSide, BaseFireBlock.getState(level(), sidePos));
                    } else if (hitState.isFlammable(level(), hitPos, hitSide)) {
                        //TODO: Is there some event we should/can be firing here?
                        hitState.onCaughtFire(level(), hitPos, hitSide, owner instanceof LivingEntity livingEntity ? livingEntity : null);
                        if (hitState.getBlock() instanceof TntBlock) {
                            level().removeBlock(hitPos, false);
                        }
                    }
                }
            }
        }
        if (hitFluid) {
            spawnParticlesAt(blockPosition());
            playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
        }
        discard();
    }

    private boolean tryModify(@Nullable Entity shooter, BlockPos pos, Direction hitSide, BlockState state, UseOnContext context, ItemAbility ability) {
        BlockSnapshot blockSnapshot = BlockSnapshot.create(level().dimension(), level(), pos);
        BlockState modifiedState = state.getToolModifiedState(context, ability, false);
        if (modifiedState != null) {
            level().setBlockAndUpdate(pos, modifiedState);
        }
        if (modifiedState == null || EventHooks.onBlockPlace(shooter, blockSnapshot, hitSide)) {
            level().restoringBlockSnapshots = true;
            blockSnapshot.restore(blockSnapshot.getFlags() | Block.UPDATE_CLIENTS);
            level().restoringBlockSnapshots = false;
            return false;
        }
        return true;
    }

    private boolean tryPlace(@Nullable Entity shooter, BlockPos pos, Direction hitSide, BlockState newState) {
        BlockSnapshot blockSnapshot = BlockSnapshot.create(level().dimension(), level(), pos);
        level().setBlockAndUpdate(pos, newState);
        if (EventHooks.onBlockPlace(shooter, blockSnapshot, hitSide)) {
            level().restoringBlockSnapshots = true;
            blockSnapshot.restore(blockSnapshot.getFlags() | Block.UPDATE_CLIENTS);
            level().restoringBlockSnapshots = false;
            return false;
        }
        return true;
    }

    private boolean smeltItem(ItemEntity item) {
        ItemStack stack = item.getItem();
        if (!stack.isEmpty()) {//This probably should never be empty but validate it in case
            Level level = level();
            Optional<RecipeHolder<SmeltingRecipe>> recipe = MekanismRecipeType.getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
            if (recipe.isPresent()) {
                ItemStack result = recipe.get().value().getResultItem(level.registryAccess());
                item.setItem(result.copyWithCount(result.getCount() * stack.getCount()));
                item.tickCount = 0;
                spawnParticlesAt(item.blockPosition());
                playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }

    private void smeltBlock(Player shooter, BlockState hitState, BlockPos blockPos, Direction hitSide) {
        if (hitState.isAir()) {
            return;
        }
        ItemStack stack = new ItemStack(hitState.getBlock());
        if (stack.isEmpty()) {
            return;
        }
        Optional<RecipeHolder<SmeltingRecipe>> recipe;
        try {
            recipe = MekanismRecipeType.getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level());
        } catch (Exception e) {
            return;
        }
        if (recipe.isPresent()) {
            if (!level().isClientSide) {
                if (NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level(), blockPos, hitState, shooter)).isCanceled()) {
                    //We can't break the block exit
                    return;
                }
                ItemStack result = recipe.get().value().getResultItem(level().registryAccess());
                if (!(result.getItem() instanceof BlockItem) || !tryPlace(shooter, blockPos, hitSide, Block.byItem(result.getItem()).defaultBlockState())) {
                    level().removeBlock(blockPos, false);
                    ItemEntity item = new ItemEntity(level(), blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, result.copy());
                    item.setDeltaMovement(0, 0, 0);
                    level().addFreshEntity(item);
                }
                level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, blockPos, Block.getId(hitState));
                spawnParticlesAt((ServerLevel) level(), blockPos);
            }
        }
    }

    private void burn(Entity entity) {
        if (!(entity instanceof ItemEntity) || MekanismConfig.gear.flamethrowerDestroyItems.get()) {
            //Only actually burn the entity if it is not an item, or we allow destroying items
            if (entity.hurt(MekanismDamageTypes.FLAMETHROWER.source(registryAccess(), this, getOwner()), DAMAGE)) {
                //Mirror flame enchant of igniting for 5 seconds
                entity.igniteForSeconds(5);
            }
        }
    }

    private void spawnParticlesAt(BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            level().addParticle(ParticleTypes.SMOKE, pos.getX() + (random.nextFloat() - 0.5), pos.getY() + (random.nextFloat() - 0.5),
                  pos.getZ() + (random.nextFloat() - 0.5), 0, 0, 0);
        }
    }

    private void spawnParticlesAt(ServerLevel world, BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            world.sendParticles(ParticleTypes.SMOKE, pos.getX() + (random.nextFloat() - 0.5), pos.getY() + (random.nextFloat() - 0.5),
                  pos.getZ() + (random.nextFloat() - 0.5), 3, 0, 0, 0, 0);
        }
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(getData(MekanismAttachmentTypes.FLAMETHROWER_MODE));
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        setData(MekanismAttachmentTypes.FLAMETHROWER_MODE, buffer.readEnum(FlamethrowerMode.class));
    }
}