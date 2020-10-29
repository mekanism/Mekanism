package mekanism.common.tile.laser;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.inventory.AutomationType;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.base.MekFakePlayer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.network.PacketLaserHitBlock;
import mekanism.common.particle.LaserParticleData;
import mekanism.common.registries.MekanismDamageSource;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.WorldEvents;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

//TODO - V11: Make the laser "shrink" the further distance it goes, If above a certain energy level and in water makes it make a bubble stream
public abstract class TileEntityBasicLaser extends TileEntityMekanism {

    protected LaserEnergyContainer energyContainer;
    private BlockPos digging;
    private FloatingLong diggingProgress = FloatingLong.ZERO;
    private FloatingLong lastFired = FloatingLong.ZERO;

    public TileEntityBasicLaser(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        addInitialEnergyContainers(builder);
        return builder.build();
    }

    protected abstract void addInitialEnergyContainers(EnergyContainerHelper builder);

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        FloatingLong firing = energyContainer.extract(toFire(), Action.SIMULATE, AutomationType.INTERNAL);
        if (!firing.isZero()) {
            if (!firing.equals(lastFired) || !getActive()) {
                setActive(true);
                lastFired = firing;
                sendUpdatePacket();
            }

            Direction direction = getDirection();
            Pos3D from = Pos3D.create(this).centre().translate(direction, 0.501);
            Pos3D to = from.translate(direction, MekanismConfig.general.laserRange.get() - 0.002);
            BlockRayTraceResult result = getWorldNN().rayTraceBlocks(new RayTraceContext(from, to, BlockMode.OUTLINE, FluidMode.NONE, null));
            if (result.getType() != Type.MISS) {
                to = new Pos3D(result.getHitVec());
            }

            float laserEnergyScale = getEnergyScale(firing);
            FloatingLong remainingEnergy = firing.copy();
            //TODO: Make the dimensions scale with laser size
            // (so that the tractor beam can actually pickup items that are on the ground underneath it)
            List<Entity> hitEntities = getWorldNN().getEntitiesWithinAABB(Entity.class, Pos3D.getAABB(from, to));
            if (hitEntities.isEmpty()) {
                setEmittingRedstone(false);
            } else {
                setEmittingRedstone(true);
                //Sort the entities in order of which one is closest to the laser
                Pos3D finalFrom = from;
                hitEntities.sort(Comparator.comparing(entity -> entity.getDistanceSq(finalFrom)));
                FloatingLong energyPerDamage = MekanismConfig.general.laserEnergyPerDamage.get();
                for (Entity entity : hitEntities) {
                    if (entity.isInvulnerableTo(MekanismDamageSource.LASER)) {
                        //The entity can absorb all the energy because they are immune to the damage
                        remainingEnergy = FloatingLong.ZERO;
                        //Update the position that the laser is going to
                        to = from.adjustPosition(direction, entity);
                        break;
                    }
                    if (entity instanceof ItemEntity && handleHitItem((ItemEntity) entity)) {
                        //TODO: Allow the tractor beam to have an energy cost for pulling items?
                        continue;
                    }
                    FloatingLong value = remainingEnergy.divide(energyPerDamage);
                    float damage = value.floatValue();
                    float health = 0;
                    if (entity instanceof LivingEntity) {
                        //If the entity is a living entity check if they are blocking with a shield and then allow
                        // the shield to cause some of the damage to be dissipated in exchange for durability
                        LivingEntity livingEntity = (LivingEntity) entity;
                        //TODO - V11: Add a system for dissipating lasers in armor/the meka-suit
                        if (livingEntity.isActiveItemStackBlocking() && livingEntity.getActiveItemStack().isShield(livingEntity)) {
                            float damageBlocked = damageShield(livingEntity, livingEntity.getActiveItemStack(), damage, 2);
                            //Remove how ever much energy we were able to block
                            remainingEnergy = remainingEnergy.minusEqual(energyPerDamage.multiply(damageBlocked));
                            if (remainingEnergy.isZero()) {
                                //If we absorbed it all then update the position the laser is going to and break
                                to = from.adjustPosition(direction, entity);
                                break;
                            }
                            //Otherwise update the damage we are actually going to try and do to the entity
                            value = remainingEnergy.divide(energyPerDamage);
                            damage = value.floatValue();
                        }
                        health = livingEntity.getHealth();
                    }
                    if (!entity.isImmuneToFire()) {
                        entity.setFire(value.intValue());
                    }
                    int lastHurtResistTime = entity.hurtResistantTime;
                    //Set the hurt resistance time to zero to ensure we get a chance to do damage
                    entity.hurtResistantTime = 0;
                    boolean damaged = entity.attackEntityFrom(MekanismDamageSource.LASER, damage);
                    //Set the hurt resistance time to whatever it was before the laser hit as lasers should not have a downtime in damage frequency
                    entity.hurtResistantTime = lastHurtResistTime;
                    if (damaged) {
                        if (entity instanceof LivingEntity) {
                            //Update the damage to match how much health the entity lost
                            damage = Math.min(damage, Math.max(0, health - ((LivingEntity) entity).getHealth()));
                        }
                        remainingEnergy = remainingEnergy.minusEqual(energyPerDamage.multiply(damage));
                        if (remainingEnergy.isZero()) {
                            //Update the position that the laser is going to
                            to = from.adjustPosition(direction, entity);
                            break;
                        }
                        float energyScale = getEnergyScale(remainingEnergy);
                        if (laserEnergyScale - energyScale > 0.01) {
                            //Otherwise send the laser between the two positions and update the energy scale
                            Pos3D entityPos = from.adjustPosition(direction, entity);
                            sendLaserDataToPlayers(new LaserParticleData(direction, entityPos.distance(from), laserEnergyScale), from);
                            laserEnergyScale = energyScale;
                            //Update the from position to be where the entity is
                            from = entityPos;
                        }
                    }
                }
            }
            //Tell the clients to render the laser
            sendLaserDataToPlayers(new LaserParticleData(direction, to.distance(from), laserEnergyScale), from);

            if (remainingEnergy.isZero() || result.getType() == Type.MISS) {
                //If all the energy was spent on damaging entities or if we aren't actively digging a block,
                // then reset any digging progress we may have
                digging = null;
                diggingProgress = FloatingLong.ZERO;
            } else {
                //Otherwise we still have energy left that we can use
                BlockPos hitPos = result.getPos();
                if (!hitPos.equals(digging)) {
                    digging = result.getType() == Type.MISS ? null : hitPos;
                    diggingProgress = FloatingLong.ZERO;
                }
                Optional<ILaserReceptor> capability = CapabilityUtils.getCapability(WorldUtils.getTileEntity(world, hitPos), Capabilities.LASER_RECEPTOR_CAPABILITY,
                      result.getFace()).resolve();
                if (capability.isPresent() && !capability.get().canLasersDig()) {
                    //Give the energy to the receptor
                    capability.get().receiveLaserEnergy(remainingEnergy, result.getFace());
                } else {
                    //Otherwise make progress on breaking the block
                    BlockState hitState = world.getBlockState(hitPos);
                    float hardness = hitState.getBlockHardness(world, hitPos);
                    if (hardness >= 0) {
                        diggingProgress = diggingProgress.plusEqual(remainingEnergy);
                        if (diggingProgress.compareTo(MekanismConfig.general.laserEnergyNeededPerHardness.get().multiply(hardness)) >= 0) {
                            if (MekanismConfig.general.aestheticWorldDamage.get()) {
                                MekFakePlayer.withFakePlayer((ServerWorld) world, to.getX(), to.getY(), to.getZ(), dummy -> {
                                    dummy.setEmulatingUUID(getOwnerUUID());//pretend to be the owner
                                    BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, hitPos, hitState, dummy);
                                    if (!MinecraftForge.EVENT_BUS.post(event)) {
                                        handleBreakBlock(hitState, hitPos);
                                        hitState.onReplaced(world, hitPos, Blocks.AIR.getDefaultState(), false);
                                        world.removeBlock(hitPos, false);
                                        world.playEvent(WorldEvents.BREAK_BLOCK_EFFECTS, hitPos, Block.getStateId(hitState));
                                    }
                                    return null;
                                });
                            }
                            diggingProgress = FloatingLong.ZERO;
                        } else {
                            //Note: If this has a significant network performance, we could instead convert this to a start/stop packet
                            Mekanism.packetHandler.sendToAllTracking(new PacketLaserHitBlock(result), this);
                        }
                    }
                }
            }
            energyContainer.extract(firing, Action.EXECUTE, AutomationType.INTERNAL);
        } else if (getActive()) {
            setActive(false);
            if (!diggingProgress.isZero()) {
                diggingProgress = FloatingLong.ZERO;
            }
            if (!lastFired.isZero()) {
                lastFired = FloatingLong.ZERO;
                sendUpdatePacket();
            }
        }
    }

    /**
     * Based off of PlayerEntity#damageShield
     */
    private float damageShield(LivingEntity livingEntity, ItemStack activeStack, float damage, int absorptionRation) {
        //Absorb part of the damage based on the given absorption ratio
        float damageBlocked = damage;
        float effectiveDamage = damage / absorptionRation;
        if (effectiveDamage >= 1) {
            //Allow the shield to absorb sub single unit damage values for free
            int durabilityNeeded = 1 + MathHelper.floor(effectiveDamage);
            int activeDurability = activeStack.getMaxDamage() - activeStack.getDamage();
            Hand hand = livingEntity.getActiveHand();
            activeStack.damageItem(durabilityNeeded, livingEntity, entity -> {
                entity.sendBreakAnimation(hand);
                if (livingEntity instanceof PlayerEntity) {
                    ForgeEventFactory.onPlayerDestroyItem((PlayerEntity) livingEntity, activeStack, hand);
                }
            });
            if (activeStack.isEmpty()) {
                if (hand == Hand.MAIN_HAND) {
                    livingEntity.setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
                } else {
                    livingEntity.setItemStackToSlot(EquipmentSlotType.OFFHAND, ItemStack.EMPTY);
                }
                livingEntity.resetActiveHand();
                livingEntity.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + 0.4F * world.rand.nextFloat());
                //Durability needed to block damage - durability we had is the left over durability that would have been needed to block it all
                int unblockedDamage = (durabilityNeeded - activeDurability) * absorptionRation;
                damageBlocked = Math.max(0, damage - unblockedDamage);
            }
        }
        if (livingEntity instanceof ServerPlayerEntity && damageBlocked > 0 && damageBlocked < 3.4028235E37F) {
            ((ServerPlayerEntity) livingEntity).addStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(damageBlocked * 10F));
        }
        return damageBlocked;
    }

    private float getEnergyScale(FloatingLong energy) {
        return Math.min(energy.divide(MekanismConfig.usage.laser.get()).divide(10).floatValue(), 0.6F);
    }

    private void sendLaserDataToPlayers(LaserParticleData data, Pos3D from) {
        if (!isRemote() && world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                serverWorld.spawnParticle(player, data, true, from.x, from.y, from.z, 1, 0, 0, 0, 0);
            }
        }
    }

    protected void setEmittingRedstone(boolean foundEntity) {
    }

    protected boolean handleHitItem(ItemEntity entity) {
        return false;
    }

    protected void handleBreakBlock(BlockState state, BlockPos hitPos) {
        Block.spawnDrops(state, world, hitPos, WorldUtils.getTileEntity(world, hitPos));
    }

    protected FloatingLong toFire() {
        return FloatingLong.MAX_VALUE;
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        NBTUtils.setFloatingLongIfPresent(nbtTags, NBTConstants.LAST_FIRED, value -> lastFired = value);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putString(NBTConstants.LAST_FIRED, lastFired.toString());
        return nbtTags;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putString(NBTConstants.LAST_FIRED, lastFired.toString());
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        NBTUtils.setFloatingLongIfPresent(tag, NBTConstants.LAST_FIRED, fired -> lastFired = fired);
    }

    public LaserEnergyContainer getEnergyContainer() {
        return energyContainer;
    }
}