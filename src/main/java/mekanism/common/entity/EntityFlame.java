package mekanism.common.entity;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.network.NetworkHooks;

public class EntityFlame extends Projectile implements IEntityAdditionalSpawnData {

    public static final int LIFESPAN = 80;
    private static final int DAMAGE = 10;

    private FlamethrowerMode mode = FlamethrowerMode.COMBAT;

    public EntityFlame(EntityType<EntityFlame> type, Level world) {
        super(type, world);
    }

    @Nullable
    public static EntityFlame create(Player player) {
        EntityFlame flame = MekanismEntityTypes.FLAME.get().create(player.level);
        if (flame == null) {
            return null;
        }
        Pos3D playerPos = new Pos3D(player.getX(), player.getEyeY() - 0.1, player.getZ());
        Pos3D flameVec = new Pos3D(1, 1, 1);

        Vec3 lookVec = player.getLookAngle();
        flameVec = flameVec.multiply(lookVec).yRot(6);

        Vec3 mergedVec = playerPos.add(flameVec);
        flame.setPos(mergedVec.x, mergedVec.y, mergedVec.z);
        flame.setOwner(player);
        ItemStack selected = player.getInventory().getSelected();
        flame.mode = ((ItemFlamethrower) selected.getItem()).getMode(selected);
        flame.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, 0.5F, 1);
        //Attempt to ray trace the area between the player and where the flame would actually start
        // if it hits a block instead just have the flame hit the block directly to avoid being able
        // to shoot a flamethrower through one thick walls.
        BlockHitResult blockRayTrace = player.level.clip(new ClipContext(playerPos, mergedVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, flame));
        if (blockRayTrace.getType() != Type.MISS) {
            flame.onHit(blockRayTrace);
        }
        return flame;
    }

    @Override
    public void baseTick() {
        if (!isAlive()) {
            return;
        }
        tickCount++;

        xo = getX();
        yo = getY();
        zo = getZ();

        xRotO = getXRot();
        yRotO = getYRot();

        Vec3 motion = getDeltaMovement();
        setPosRaw(getX() + motion.x(), getY() + motion.y(), getZ() + motion.z());

        setPos(getX(), getY(), getZ());

        calculateVector();
        if (tickCount > LIFESPAN) {
            discard();
        }
    }

    private void calculateVector() {
        Vec3 localVec = new Vec3(getX(), getY(), getZ());
        Vec3 motion = getDeltaMovement();
        Vec3 motionVec = new Vec3(getX() + motion.x() * 2, getY() + motion.y() * 2, getZ() + motion.z() * 2);
        BlockHitResult blockRayTrace = level.clip(new ClipContext(localVec, motionVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, this));
        localVec = new Vec3(getX(), getY(), getZ());
        motionVec = new Vec3(getX() + motion.x(), getY() + motion.y(), getZ() + motion.z());
        if (blockRayTrace.getType() != Type.MISS) {
            motionVec = blockRayTrace.getLocation();
        }
        EntityHitResult entityResult = ProjectileUtil.getEntityHitResult(level, this, localVec, motionVec,
              getBoundingBox().expandTowards(getDeltaMovement()).inflate(1.0D, 1.0D, 1.0D), EntitySelector.NO_SPECTATORS);
        onHit(entityResult == null ? blockRayTrace : entityResult);
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
            if (entity instanceof ItemEntity item && mode == FlamethrowerMode.HEAT) {
                if (entity.tickCount > 100 && !smeltItem(item)) {
                    burn(entity);
                }
            } else {
                burn(entity);
            }
        }
        discard();
    }

    @Override
    protected void onHitBlock(@Nonnull BlockHitResult blockRayTrace) {
        super.onHitBlock(blockRayTrace);
        BlockPos hitPos = blockRayTrace.getBlockPos();
        Direction hitSide = blockRayTrace.getDirection();
        BlockState hitState = level.getBlockState(hitPos);
        boolean hitFluid = !hitState.getFluidState().isEmpty();
        if (!level.isClientSide && MekanismConfig.general.aestheticWorldDamage.get() && !hitFluid) {
            if (mode == FlamethrowerMode.HEAT) {
                Entity owner = getOwner();
                if (owner instanceof Player player) {
                    smeltBlock(player, hitState, hitPos, hitSide);
                }
            } else if (mode == FlamethrowerMode.INFERNO) {
                Entity owner = getOwner();
                BlockPos sidePos = hitPos.relative(hitSide);
                if (CampfireBlock.canLight(hitState)) {
                    tryPlace(owner, hitPos, hitSide, hitState.setValue(BlockStateProperties.LIT, true));
                } else if (BaseFireBlock.canBePlacedAt(level, sidePos, hitSide)) {
                    tryPlace(owner, sidePos, hitSide, BaseFireBlock.getState(level, sidePos));
                } else if (hitState.isFlammable(level, hitPos, hitSide)) {
                    //TODO: Is there some event we should/can be firing here?
                    hitState.onCaughtFire(level, hitPos, hitSide, owner instanceof LivingEntity livingEntity ? livingEntity : null);
                    if (hitState.getBlock() instanceof TntBlock) {
                        level.removeBlock(hitPos, false);
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

    private boolean tryPlace(@Nullable Entity shooter, BlockPos pos, Direction hitSide, BlockState newState) {
        BlockSnapshot blockSnapshot = BlockSnapshot.create(level.dimension(), level, pos);
        level.setBlockAndUpdate(pos, newState);
        if (ForgeEventFactory.onBlockPlace(shooter, blockSnapshot, hitSide)) {
            level.restoringBlockSnapshots = true;
            blockSnapshot.restore(true, false);
            level.restoringBlockSnapshots = false;
            return false;
        }
        return true;
    }

    private boolean smeltItem(ItemEntity item) {
        Optional<SmeltingRecipe> recipe = MekanismRecipeType.getRecipeFor(RecipeType.SMELTING, new SimpleContainer(item.getItem()), level);
        if (recipe.isPresent()) {
            ItemStack result = recipe.get().getResultItem();
            item.setItem(StackUtils.size(result, item.getItem().getCount()));
            item.tickCount = 0;
            spawnParticlesAt(item.blockPosition());
            playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
            return true;
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
        Optional<SmeltingRecipe> recipe;
        try {
            recipe = MekanismRecipeType.getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), level);
        } catch (Exception e) {
            return;
        }
        if (recipe.isPresent()) {
            if (!level.isClientSide) {
                if (MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, blockPos, hitState, shooter))) {
                    //We can't break the block exit
                    return;
                }
                ItemStack result = recipe.get().getResultItem();
                if (!(result.getItem() instanceof BlockItem) || !tryPlace(shooter, blockPos, hitSide, Block.byItem(result.getItem()).defaultBlockState())) {
                    level.removeBlock(blockPos, false);
                    ItemEntity item = new ItemEntity(level, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, result.copy());
                    item.setDeltaMovement(0, 0, 0);
                    level.addFreshEntity(item);
                }
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, blockPos, Block.getId(hitState));
                spawnParticlesAt((ServerLevel) level, blockPos);
            }
        }
    }

    private void burn(Entity entity) {
        if (!(entity instanceof ItemEntity) || MekanismConfig.gear.flamethrowerDestroyItems.get()) {
            //Only actually burn the entity if it is not an item, or we allow destroying items
            entity.setSecondsOnFire(20);
            entity.hurt(DamageSource.thrown(this, getOwner()), DAMAGE);
        }
    }

    private void spawnParticlesAt(BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + (random.nextFloat() - 0.5), pos.getY() + (random.nextFloat() - 0.5),
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
    protected void defineSynchedData() {
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag nbtTags) {
        super.readAdditionalSaveData(nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.MODE, FlamethrowerMode::byIndexStatic, mode -> this.mode = mode);
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag nbtTags) {
        super.addAdditionalSaveData(nbtTags);
        NBTUtils.writeEnum(nbtTags, NBTConstants.MODE, mode);
    }

    @Nonnull
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf dataStream) {
        dataStream.writeEnum(mode);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf dataStream) {
        mode = dataStream.readEnum(FlamethrowerMode.class);
    }
}