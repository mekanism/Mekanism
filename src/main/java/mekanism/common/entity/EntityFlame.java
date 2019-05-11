package mekanism.common.entity;

import io.netty.buffer.ByteBuf;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.Pos3D;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
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

    public EntityFlame(EntityPlayer player) {
        this(player.world);

        Pos3D playerPos = new Pos3D(player).translate(0, 1.6, 0);
        Pos3D flameVec = new Pos3D(1, 1, 1);

        flameVec = flameVec.multiply(new Pos3D(player.getLookVec())).rotateYaw(6);

        Pos3D mergedVec = playerPos.translate(flameVec);
        setPosition(mergedVec.x, mergedVec.y, mergedVec.z);

        Pos3D motion = new Pos3D(0.4, 0.4, 0.4).multiply(new Pos3D(player.getLookVec()));

        setHeading(motion);

        motionX = motion.x;
        motionY = motion.y;
        motionZ = motion.z;

        owner = player;
        mode = ((ItemFlamethrower) player.inventory.getCurrentItem().getItem())
              .getMode(player.inventory.getCurrentItem());
    }

    public void setHeading(Pos3D motion) {
        float d = MathHelper.sqrt((motion.x * motion.x) + (motion.z * motion.z));

        prevRotationYaw = rotationYaw = (float) (Math.atan2(motion.x, motion.z) * 180.0D / Math.PI);
        prevRotationPitch = rotationPitch = (float) (Math.atan2(motion.y, d) * 180.0D / Math.PI);
    }

    @Override
    public void onUpdate() {
        if (isDead) {
            return;
        }

        ticksExisted++;

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;

        posX += motionX;
        posY += motionY;
        posZ += motionZ;

        setPosition(posX, posY, posZ);

        calculateVector();

        if (ticksExisted > LIFESPAN) {
            setDead();
        }
    }

    private void calculateVector() {
        Vec3d localVec = new Vec3d(posX, posY, posZ);
        Vec3d motionVec = new Vec3d(posX + motionX * 2, posY + motionY * 2, posZ + motionZ * 2);
        RayTraceResult mop = world.rayTraceBlocks(localVec, motionVec, true, false, false);
        localVec = new Vec3d(posX, posY, posZ);
        motionVec = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);

        if (mop != null) {
            motionVec = new Vec3d(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
        }

        Entity entity = null;
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this,
              getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(1.0D, 1.0D, 1.0D));
        double entityDist = 0.0D;
        int i;

        for (Entity entity1 : list) {
            if ((entity1 instanceof EntityItem || entity1.canBeCollidedWith()) && entity1 != owner) {
                float boundsScale = 0.3F;
                AxisAlignedBB newBounds = entity1.getEntityBoundingBox()
                      .expand((double) boundsScale, (double) boundsScale, (double) boundsScale);
                RayTraceResult RayTraceResult1 = newBounds.calculateIntercept(localVec, motionVec);

                if (RayTraceResult1 != null) {
                    double dist = localVec.distanceTo(RayTraceResult1.hitVec);

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

        if (mop != null && mop.entityHit instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) mop.entityHit;

            if (entityplayer.capabilities.disableDamage || owner instanceof EntityPlayer && !((EntityPlayer) owner)
                  .canAttackPlayer(entityplayer)) {
                mop = null;
            }
        }

        if (mop != null) {
            if (mop.typeOfHit == Type.ENTITY && mop.entityHit != null && !mop.entityHit.isImmuneToFire()) {
                if (mop.entityHit instanceof EntityItem && mode != ItemFlamethrower.FlamethrowerMode.COMBAT) {
                    if (mop.entityHit.ticksExisted > 100) {
                        if (!smeltItem((EntityItem) mop.entityHit)) {
                            burn(mop.entityHit);
                        }
                    }
                } else {
                    burn(mop.entityHit);
                }
            } else if (mop.typeOfHit == Type.BLOCK) {
                IBlockState state = world.getBlockState(mop.getBlockPos());
                Block block = state.getBlock();
                boolean fluid = MekanismUtils.isFluid(world, new Coord4D(mop, world)) || MekanismUtils
                      .isDeadFluid(world, new Coord4D(mop, world));

                Coord4D sideCoord = new Coord4D(mop.getBlockPos().offset(mop.sideHit), world);

                if (MekanismConfig.current().general.aestheticWorldDamage.val() && !fluid && (
                      sideCoord.isAirBlock(world) || sideCoord.isReplaceable(world))) {
                    if (mode != ItemFlamethrower.FlamethrowerMode.COMBAT && !smeltBlock(new Coord4D(mop, world))) {
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

            setDead();
        }
    }

    private boolean smeltItem(EntityItem item) {
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
                IBlockState state = block.getBlockState(world);
                Block b = state.getBlock();
                Block newBlock = Block.getBlockFromItem(result.getItem());

                if (newBlock != Blocks.AIR) {
                    world.setBlockState(block.getPos(),
                          Block.getBlockFromItem(result.getItem()).getStateFromMeta(result.getItemDamage()), 3);
                } else {
                    world.setBlockToAir(block.getPos());

                    EntityItem item = new EntityItem(world, block.x + 0.5, block.y + 0.5, block.z + 0.5, result.copy());
                    item.motionX = 0;
                    item.motionY = 0;
                    item.motionZ = 0;
                    world.spawnEntity(item);
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
        } else {
            return DamageSource.causeThrownDamage(this, owner);
        }
    }

    private void spawnParticlesAt(Pos3D pos) {
        for (int i = 0; i < 10; i++) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.x + (rand.nextFloat() - 0.5),
                  pos.y + (rand.nextFloat() - 0.5), pos.z + (rand.nextFloat() - 0.5), 0, 0, 0);
        }
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(@Nonnull NBTTagCompound nbtTags) {
        mode = ItemFlamethrower.FlamethrowerMode.values()[nbtTags.getInteger("mode")];
    }

    @Override
    protected void writeEntityToNBT(@Nonnull NBTTagCompound nbtTags) {
        nbtTags.setInteger("mode", mode.ordinal());
    }

    @Override
    public void writeSpawnData(ByteBuf dataStream) {
        dataStream.writeInt(mode.ordinal());
    }

    @Override
    public void readSpawnData(ByteBuf dataStream) {
        mode = ItemFlamethrower.FlamethrowerMode.values()[dataStream.readInt()];
    }
}
