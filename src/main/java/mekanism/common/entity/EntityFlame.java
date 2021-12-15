package mekanism.common.entity;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.TNTBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.Constants.WorldEvents;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityFlame extends ProjectileEntity implements IEntityAdditionalSpawnData {

    public static final int LIFESPAN = 80;
    private static final int DAMAGE = 10;

    private FlamethrowerMode mode = FlamethrowerMode.COMBAT;

    public EntityFlame(EntityType<EntityFlame> type, World world) {
        super(type, world);
    }

    @Nullable
    public static EntityFlame create(PlayerEntity player) {
        EntityFlame flame = MekanismEntityTypes.FLAME.get().create(player.level);
        if (flame == null) {
            return null;
        }
        Pos3D playerPos = new Pos3D(player.getX(), player.getEyeY() - 0.1, player.getZ());
        Pos3D flameVec = new Pos3D(1, 1, 1);

        Vector3d lookVec = player.getLookAngle();
        flameVec = flameVec.multiply(lookVec).yRot(6);

        Vector3d mergedVec = playerPos.add(flameVec);
        flame.setPos(mergedVec.x, mergedVec.y, mergedVec.z);
        flame.setOwner(player);
        ItemStack selected = player.inventory.getSelected();
        flame.mode = ((ItemFlamethrower) selected.getItem()).getMode(selected);
        flame.shootFromRotation(player, player.xRot, player.yRot, 0, 0.5F, 1);
        //Attempt to ray trace the area between the player and where the flame would actually start
        // if it hits a block instead just have the flame hit the block directly to avoid being able
        // to shoot a flamethrower through one thick walls.
        BlockRayTraceResult blockRayTrace = player.level.clip(new RayTraceContext(playerPos, mergedVec, BlockMode.OUTLINE, FluidMode.NONE, flame));
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

        xRotO = xRot;
        yRotO = yRot;

        Vector3d motion = getDeltaMovement();
        setPosRaw(getX() + motion.x(), getY() + motion.y(), getZ() + motion.z());

        setPos(getX(), getY(), getZ());

        calculateVector();
        if (tickCount > LIFESPAN) {
            remove();
        }
    }

    private void calculateVector() {
        Vector3d localVec = new Vector3d(getX(), getY(), getZ());
        Vector3d motion = getDeltaMovement();
        Vector3d motionVec = new Vector3d(getX() + motion.x() * 2, getY() + motion.y() * 2, getZ() + motion.z() * 2);
        BlockRayTraceResult blockRayTrace = level.clip(new RayTraceContext(localVec, motionVec, BlockMode.OUTLINE, FluidMode.ANY, this));
        localVec = new Vector3d(getX(), getY(), getZ());
        motionVec = new Vector3d(getX() + motion.x(), getY() + motion.y(), getZ() + motion.z());
        if (blockRayTrace.getType() != Type.MISS) {
            motionVec = blockRayTrace.getLocation();
        }
        EntityRayTraceResult entityResult = ProjectileHelper.getEntityHitResult(level, this, localVec, motionVec,
              getBoundingBox().expandTowards(getDeltaMovement()).inflate(1.0D, 1.0D, 1.0D), EntityPredicates.NO_SPECTATORS);
        onHit(entityResult == null ? blockRayTrace : entityResult);
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult entityResult) {
        Entity entity = entityResult.getEntity();
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            Entity owner = getOwner();
            if (player.abilities.invulnerable || owner instanceof PlayerEntity && !((PlayerEntity) owner).canHarmPlayer(player)) {
                return;
            }
        }
        if (!entity.fireImmune()) {
            if (entity instanceof ItemEntity && mode == FlamethrowerMode.HEAT) {
                if (entity.tickCount > 100 && !smeltItem((ItemEntity) entity)) {
                    burn(entity);
                }
            } else {
                burn(entity);
            }
        }
        remove();
    }

    @Override
    protected void onHitBlock(@Nonnull BlockRayTraceResult blockRayTrace) {
        super.onHitBlock(blockRayTrace);
        BlockPos hitPos = blockRayTrace.getBlockPos();
        Direction hitSide = blockRayTrace.getDirection();
        BlockState hitState = level.getBlockState(hitPos);
        boolean hitFluid = !hitState.getFluidState().isEmpty();
        if (!level.isClientSide && MekanismConfig.general.aestheticWorldDamage.get() && !hitFluid) {
            if (mode == FlamethrowerMode.HEAT) {
                Entity owner = getOwner();
                if (owner instanceof PlayerEntity) {
                    smeltBlock((PlayerEntity) owner, hitState, hitPos, hitSide);
                }
            } else if (mode == FlamethrowerMode.INFERNO) {
                Entity owner = getOwner();
                BlockPos sidePos = hitPos.relative(hitSide);
                if (CampfireBlock.canLight(hitState)) {
                    tryPlace(owner, hitPos, hitSide, hitState.setValue(BlockStateProperties.LIT, true));
                } else if (AbstractFireBlock.canBePlacedAt(level, sidePos, hitSide)) {
                    tryPlace(owner, sidePos, hitSide, AbstractFireBlock.getState(level, sidePos));
                } else if (hitState.isFlammable(level, hitPos, hitSide)) {
                    //TODO: Is there some event we should/can be firing here?
                    hitState.catchFire(level, hitPos, hitSide, owner instanceof LivingEntity ? (LivingEntity) owner : null);
                    if (hitState.getBlock() instanceof TNTBlock) {
                        level.removeBlock(hitPos, false);
                    }
                }
            }
        }
        if (hitFluid) {
            spawnParticlesAt(blockPosition());
            playSound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 1.0F);
        }
        remove();
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
        Optional<FurnaceRecipe> recipe = level.getRecipeManager().getRecipeFor(IRecipeType.SMELTING, new Inventory(item.getItem()), level);
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

    private void smeltBlock(PlayerEntity shooter, BlockState hitState, BlockPos blockPos, Direction hitSide) {
        if (hitState.isAir(level, blockPos)) {
            return;
        }
        ItemStack stack = new ItemStack(hitState.getBlock());
        if (stack.isEmpty()) {
            return;
        }
        Optional<FurnaceRecipe> recipe;
        try {
            recipe = level.getRecipeManager().getRecipeFor(IRecipeType.SMELTING, new Inventory(stack), level);
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
                level.levelEvent(WorldEvents.BREAK_BLOCK_EFFECTS, blockPos, Block.getId(hitState));
                spawnParticlesAt((ServerWorld) level, blockPos);
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

    private void spawnParticlesAt(ServerWorld world, BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            world.sendParticles(ParticleTypes.SMOKE, pos.getX() + (random.nextFloat() - 0.5), pos.getY() + (random.nextFloat() - 0.5),
                  pos.getZ() + (random.nextFloat() - 0.5), 3, 0, 0, 0, 0);
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundNBT nbtTags) {
        super.readAdditionalSaveData(nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.MODE, FlamethrowerMode::byIndexStatic, mode -> this.mode = mode);
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundNBT nbtTags) {
        super.addAdditionalSaveData(nbtTags);
        nbtTags.putInt(NBTConstants.MODE, mode.ordinal());
    }

    @Nonnull
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer dataStream) {
        dataStream.writeEnum(mode);
    }

    @Override
    public void readSpawnData(PacketBuffer dataStream) {
        mode = dataStream.readEnum(FlamethrowerMode.class);
    }
}