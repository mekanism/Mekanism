package mekanism.common.entity;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.Pos3D;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.WorldEvents;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityFlame extends Entity implements IEntityAdditionalSpawnData {

    public static final int LIFESPAN = 80;
    public static final int DAMAGE = 10;

    public Entity owner = null;
    public ItemFlamethrower.FlamethrowerMode mode = ItemFlamethrower.FlamethrowerMode.COMBAT;

    public EntityFlame(World world) {
        super(world);
        setSize(0.5F, 0.5F);
    }

    public EntityFlame(PlayerEntity player) {
        this(player.world);
        Pos3D playerPos = new Pos3D(player).translate(0, 1.6, 0);
        Pos3D flameVec = new Pos3D(1, 1, 1);

        flameVec = flameVec.multiply(new Pos3D(player.getLookVec())).rotateYaw(6);

        Pos3D mergedVec = playerPos.translate(flameVec);
        setPosition(mergedVec.x, mergedVec.y, mergedVec.z);

        Pos3D motion = new Pos3D(0.4, 0.4, 0.4).multiply(new Pos3D(player.getLookVec()));

        setHeading(motion);

        setMotion(motion);

        owner = player;
        mode = ((ItemFlamethrower) player.inventory.getCurrentItem().getItem()).getMode(player.inventory.getCurrentItem());
    }

    public void setHeading(Pos3D motion) {
        float d = MathHelper.sqrt((motion.x * motion.x) + (motion.z * motion.z));
        prevRotationYaw = rotationYaw = (float) (Math.atan2(motion.x, motion.z) * 180.0D / Math.PI);
        prevRotationPitch = rotationPitch = (float) (Math.atan2(motion.y, d) * 180.0D / Math.PI);
    }

    @Override
    public void tick() {
        if (!isAlive()) {
            return;
        }
        ticksExisted++;

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;

        Vec3d motion = getMotion();
        posX += motion.getX();
        posY += motion.getY();
        posZ += motion.getZ();

        setPosition(posX, posY, posZ);

        calculateVector();
        if (ticksExisted > LIFESPAN) {
            remove();
        }
    }

    private void calculateVector() {
        Vec3d localVec = new Vec3d(posX, posY, posZ);
        Vec3d motion = getMotion();
        Vec3d motionVec = new Vec3d(posX + motion.getX() * 2, posY + motion.getY() * 2, posZ + motion.getZ() * 2);
        BlockRayTraceResult mop = world.rayTraceBlocks(localVec, motionVec, true, false, false);
        localVec = new Vec3d(posX, posY, posZ);
        motionVec = new Vec3d(posX + motion.getX(), posY + motion.getY(), posZ + motion.getZ());
        if (mop != null) {
            motionVec = mop.getHitVec();
        }

        Entity entity = null;
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, getBoundingBox().expand(getMotion()).grow(1.0D, 1.0D, 1.0D));
        double entityDist = 0.0D;

        for (Entity entity1 : list) {
            if ((entity1 instanceof ItemEntity || entity1.canBeCollidedWith()) && entity1 != owner) {
                float boundsScale = 0.3F;
                AxisAlignedBB newBounds = entity1.getBoundingBox().expand(boundsScale, boundsScale, boundsScale);
                RayTraceResult RayTraceResult1 = newBounds.calculateIntercept(localVec, motionVec);

                if (RayTraceResult1 != null) {
                    double dist = localVec.distanceTo(RayTraceResult1.getHitVec());
                    if (dist < entityDist || entityDist == 0) {
                        entity = entity1;
                        entityDist = dist;
                    }
                }
            }
        }

        if (entity != null) {
            mop = new RayTraceResult(entity);
        }

        if (mop != null && mop.entityHit instanceof PlayerEntity) {
            PlayerEntity entityplayer = (PlayerEntity) mop.entityHit;
            if (entityplayer.capabilities.disableDamage || owner instanceof PlayerEntity && !((PlayerEntity) owner).canAttackPlayer(entityplayer)) {
                mop = null;
            }
        }

        if (mop != null) {
            if (mop instanceof EntityRayTraceResult) {
                EntityRayTraceResult entityResult = (EntityRayTraceResult) mop;
                if (entityResult.getEntity() != null && !entityResult.getEntity().isImmuneToFire()) {
                    if (entityResult.getEntity() instanceof ItemEntity && mode != ItemFlamethrower.FlamethrowerMode.COMBAT) {
                        if (entityResult.getEntity().ticksExisted > 100) {
                            if (!smeltItem((ItemEntity) entityResult.getEntity())) {
                                burn(entityResult.getEntity());
                            }
                        }
                    } else {
                        burn(entityResult.getEntity());
                    }
                }
            } else if (mop instanceof BlockRayTraceResult) {
                BlockRayTraceResult blockResult = mop;
                BlockState state = world.getBlockState(blockResult.getPos());
                boolean fluid = MekanismUtils.isFluid(world, new Coord4D(blockResult, world)) || MekanismUtils.isDeadFluid(world, new Coord4D(blockResult, world));

                Coord4D sideCoord = new Coord4D(blockResult.getPos().offset(blockResult.getFace()), world);

                if (MekanismConfig.current().general.aestheticWorldDamage.val() && !fluid && (sideCoord.isAirBlock(world) || sideCoord.isReplaceable(world))) {
                    if (mode != ItemFlamethrower.FlamethrowerMode.COMBAT && !smeltBlock(new Coord4D(blockResult, world))) {
                        if (mode == ItemFlamethrower.FlamethrowerMode.INFERNO && !world.isRemote) {
                            world.setBlockState(sideCoord.getPos(), Blocks.FIRE.getDefaultState());
                        }
                    }
                }

                if (fluid) {
                    spawnParticlesAt(new Pos3D(this));
                    playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
                }
            }
            remove();
        }
    }

    private boolean smeltItem(ItemEntity item) {
        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(item.getItem());
        if (!result.isEmpty()) {
            item.setItem(StackUtils.size(result, item.getItem().getCount()));
            item.ticksExisted = 0;
            spawnParticlesAt(new Pos3D(item));
            playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    private boolean smeltBlock(Coord4D block) {
        ItemStack stack = block.getStack(world);
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack result;
        try {
            result = FurnaceRecipes.instance().getSmeltingResult(block.getStack(world));
        } catch (Exception e) {
            return false;
        }
        if (!result.isEmpty()) {
            if (!world.isRemote) {
                BlockState state = block.getBlockState(world);
                if (result.getItem() instanceof BlockItem) {
                    world.setBlockState(block.getPos(), Block.getBlockFromItem(result.getItem()).getStateFromMeta(result.getDamage()), 3);
                } else {
                    world.removeBlock(block.getPos(), false);
                    ItemEntity item = new ItemEntity(world, block.x + 0.5, block.y + 0.5, block.z + 0.5, result.copy());
                    item.setMotion(0, 0, 0);
                    world.addEntity(item);
                }

                world.playEvent(WorldEvents.BREAK_BLOCK_EFFECTS, block.getPos(), Block.getStateId(state));
            }
            spawnParticlesAt(new Pos3D(block).translate(0.5, 0.5, 0.5));
            return true;
        }
        return false;
    }

    private void burn(Entity entity) {
        entity.setFire(20);
        entity.attackEntityFrom(getFlamethrowerDamage(), DAMAGE);
    }

    private DamageSource getFlamethrowerDamage() {
        if (owner == null) {
            return DamageSource.causeThrownDamage(this, this);
        }
        return DamageSource.causeThrownDamage(this, owner);
    }

    private void spawnParticlesAt(Pos3D pos) {
        for (int i = 0; i < 10; i++) {
            world.addParticle(ParticleTypes.SMOKE, pos.x + (rand.nextFloat() - 0.5), pos.y + (rand.nextFloat() - 0.5),
                  pos.z + (rand.nextFloat() - 0.5), 0, 0, 0);
        }
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(@Nonnull CompoundNBT nbtTags) {
        mode = ItemFlamethrower.FlamethrowerMode.values()[nbtTags.getInt("mode")];
    }

    @Override
    protected void writeEntityToNBT(@Nonnull CompoundNBT nbtTags) {
        nbtTags.putInt("mode", mode.ordinal());
    }

    @Override
    public void writeSpawnData(PacketBuffer dataStream) {
        dataStream.writeInt(mode.ordinal());
    }

    @Override
    public void readSpawnData(PacketBuffer dataStream) {
        mode = ItemFlamethrower.FlamethrowerMode.values()[dataStream.readInt()];
    }
}