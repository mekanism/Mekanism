package mekanism.common.entity;

import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.Pos3D;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFlamethrower.FlamethrowerMode;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.WorldEvents;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityFlame extends Entity implements IEntityAdditionalSpawnData {

    public static final int LIFESPAN = 80;
    public static final int DAMAGE = 10;

    public Entity owner = null;
    public ItemFlamethrower.FlamethrowerMode mode = ItemFlamethrower.FlamethrowerMode.COMBAT;

    public EntityFlame(EntityType<EntityFlame> type, World world) {
        super(type, world);
    }

    public EntityFlame(PlayerEntity player) {
        this(MekanismEntityTypes.FLAME.getEntityType(), player.world);
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
        prevRotationYaw = rotationYaw = (float) Math.toDegrees(Math.atan2(motion.x, motion.z));
        prevRotationPitch = rotationPitch = (float) Math.toDegrees(Math.atan2(motion.y, d));
    }

    @Override
    public void tick() {
        if (!isAlive()) {
            return;
        }
        ticksExisted++;

        prevPosX = func_226277_ct_();
        prevPosY = func_226278_cu_();
        prevPosZ = func_226281_cx_();

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;

        Vec3d motion = getMotion();
        func_226288_n_(func_226277_ct_() + motion.getX(), func_226278_cu_() + motion.getY(), func_226281_cx_() + motion.getZ());

        setPosition(func_226277_ct_(), func_226278_cu_(), func_226281_cx_());

        calculateVector();
        if (ticksExisted > LIFESPAN) {
            remove();
        }
    }

    private void calculateVector() {
        //TODO: Reimplement this
        /*Vec3d localVec = new Vec3d(posX, posY, posZ);
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
                //TODO: Verify this is correct
                BlockRayTraceResult result = AxisAlignedBB.rayTrace(Collections.singleton(newBounds), localVec, motionVec, getPosition());

                if (result != null) {
                    double dist = localVec.distanceTo(result.getHitVec());
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
            PlayerEntity player = (PlayerEntity) mop.entityHit;
            if (player.capabilities.disableDamage || owner instanceof PlayerEntity && !((PlayerEntity) owner).canAttackPlayer(player)) {
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
                boolean fluid = MekanismUtils.isFluid(world, new Coord4D(blockResult, world)) || MekanismUtils.isDeadFluid(world, new Coord4D(blockResult, world));

                Coord4D sideCoord = new Coord4D(blockResult.getPos().offset(blockResult.getFace()), world);

                if (MekanismConfig.general.aestheticWorldDamage.get() && !fluid && MekanismUtils.isValidReplaceableBlock(world, sideCoord.getPos())) {
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
        }*/
    }

    private boolean smeltItem(ItemEntity item) {
        Optional<FurnaceRecipe> recipe = world.getRecipeManager().getRecipe(IRecipeType.SMELTING, new Inventory(item.getItem()), world);
        if (recipe.isPresent()) {
            ItemStack result = recipe.get().getRecipeOutput();
            item.setItem(StackUtils.size(result, item.getItem().getCount()));
            item.ticksExisted = 0;
            spawnParticlesAt(new Pos3D(item));
            playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1.0F, 1.0F);
            return true;
        }
        return false;
    }

    private boolean smeltBlock(Coord4D block) {
        if (world.isAirBlock(block.getPos())) {
            return false;
        }
        ItemStack stack = new ItemStack(world.getBlockState(block.getPos()).getBlock());
        if (stack.isEmpty()) {
            return false;
        }
        Optional<FurnaceRecipe> recipe;
        try {
            recipe = world.getRecipeManager().getRecipe(IRecipeType.SMELTING, new Inventory(stack), world);
        } catch (Exception e) {
            return false;
        }
        if (recipe.isPresent()) {
            if (!world.isRemote) {
                BlockState state = world.getBlockState(block.getPos());
                ItemStack result = recipe.get().getRecipeOutput();
                if (result.getItem() instanceof BlockItem) {
                    world.setBlockState(block.getPos(), Block.getBlockFromItem(result.getItem().getItem()).getDefaultState());
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
    protected void registerData() {
    }

    @Override
    protected void readAdditional(@Nonnull CompoundNBT nbtTags) {
        mode = FlamethrowerMode.byIndexStatic(nbtTags.getInt("mode"));
    }

    @Override
    protected void writeAdditional(@Nonnull CompoundNBT nbtTags) {
        nbtTags.putInt("mode", mode.ordinal());
    }

    @Nonnull
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer dataStream) {
        dataStream.writeEnumValue(mode);
    }

    @Override
    public void readSpawnData(PacketBuffer dataStream) {
        mode = dataStream.readEnumValue(FlamethrowerMode.class);
    }
}