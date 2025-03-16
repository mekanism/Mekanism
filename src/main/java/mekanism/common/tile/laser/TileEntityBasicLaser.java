package mekanism.common.tile.laser;

import java.util.Comparator;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.lasers.ILaserDissipation;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.math.MathUtils;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.base.MekFakePlayer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.item.gear.ItemAtomicDisassembler;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.PacketHitBlockEffect;
import mekanism.common.particle.LaserParticleData;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

//TODO - V11: Make the laser "shrink" the further distance it goes, If above a certain energy level and in water makes it make a bubble stream
public abstract class TileEntityBasicLaser extends TileEntityMekanism {

    protected LaserEnergyContainer energyContainer;
    @SyntheticComputerMethod(getter = "getDiggingPos")
    private BlockPos digging;
    private long diggingProgress = 0;
    private long lastFired = 0;

    public TileEntityBasicLaser(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(facingSupplier);
        addInitialEnergyContainers(builder, listener);
        return builder.build();
    }

    protected abstract void addInitialEnergyContainers(EnergyContainerHelper builder, IContentsListener listener);

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        long firing = energyContainer.extract(toFire(), Action.SIMULATE, AutomationType.INTERNAL);
        if (firing > 0L) {
            if (firing != lastFired || !getActive()) {
                setActive(true);
                lastFired = firing;
                sendUpdatePacket = true;
            }

            Direction direction = getDirection();
            Level level = getWorldNN();
            Pos3D from = Pos3D.create(this).centre().translate(direction, 0.501);
            Pos3D to = from.translate(direction, MekanismConfig.general.laserRange.get() - 0.002);
            BlockHitResult result = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty()));
            if (result.getType() != Type.MISS) {
                to = new Pos3D(result.getLocation());
            }

            float laserEnergyScale = getEnergyScale(firing);
            long remainingEnergy = firing;
            List<Entity> hitEntities = level.getEntitiesOfClass(Entity.class, getLaserBox(direction, from, to, laserEnergyScale));
            if (hitEntities.isEmpty()) {
                setEmittingRedstone(false);
            } else {
                setEmittingRedstone(true);
                //Sort the entities in order of which one is closest to the laser
                Pos3D finalFrom = from;
                hitEntities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(finalFrom)));
                long energyPerDamage = MekanismConfig.general.laserEnergyPerDamage.get();
                AABB adjustedAABB = null;
                for (Entity entity : hitEntities) {
                    if (adjustedAABB != null && !entity.getBoundingBox().intersects(adjustedAABB)) {
                        //If we have a smaller AABB than we started with, make sure the entity still is getting hit by the laser
                        // before we do any processing related to behavior when hit
                        continue;
                    } else if (entity.isInvulnerableTo(MekanismDamageTypes.LASER.source(level))) {
                        //The entity can absorb all the energy because they are immune to the damage
                        remainingEnergy = 0L;
                        //Update the position that the laser is going to
                        to = from.adjustPosition(direction, entity);
                        break;
                    } else if (entity instanceof ItemEntity item && handleHitItem(item)) {
                        //TODO: Allow the tractor beam to have an energy cost for pulling items?
                        continue;
                    }
                    boolean updateEnergyScale = false;
                    double value = ((double) remainingEnergy / energyPerDamage);
                    float damage = (float) value;
                    float health = 0;
                    if (entity instanceof LivingEntity livingEntity) {
                        //If the entity is a living entity check if they are blocking with a shield and then allow
                        // the shield to cause some damage to be dissipated in exchange for durability
                        boolean updateDamage = false;
                        //TODO - V11: Add a laser reflector capability that shields can implement to cause the laser beam to be reflected
                        // maybe even implement this ability but don't add it to any of our things yet?
                        float damageBlocked = damageShield(level, livingEntity, from, damage);
                        if (damageBlocked > 0) {
                            if (livingEntity instanceof ServerPlayer player) {
                                //If the entity is a player trigger the advancement criteria for blocking a laser with a shield
                                MekanismCriteriaTriggers.BLOCK_LASER.value().trigger(player);
                            }
                            //Remove however much energy we were able to block
                            remainingEnergy -= MathUtils.clampToLong(energyPerDamage * damageBlocked);
                            if (remainingEnergy == 0L) {
                                //If we absorbed it all then update the position the laser is going to and break
                                to = from.adjustPosition(direction, entity);
                                break;
                            }
                            updateDamage = true;
                        }
                        //After our shield checks see if the armor the entity is wearing can dissipate or refract lasers
                        double dissipationPercent = 0;
                        double refractionPercent = 0;
                        for (ItemStack armor : livingEntity.getArmorSlots()) {
                            if (!armor.isEmpty()) {
                                ILaserDissipation laserDissipation = armor.getCapability(Capabilities.LASER_DISSIPATION);
                                if (laserDissipation != null) {
                                    dissipationPercent += laserDissipation.getDissipationPercent();
                                    refractionPercent += laserDissipation.getRefractionPercent();
                                    if (dissipationPercent >= 1) {
                                        //If we will fully dissipate it, don't bother checking the rest of the armor slots
                                        break;
                                    }
                                }
                            }
                        }
                        //We start by dissipating energy across the armor after it is blocked by the shield
                        // we check this after blocking by the shield as the shield is in front of the entity and their armor
                        if (dissipationPercent > 0) {
                            //If we will dissipate any energy, cap the dissipation amount at one
                            dissipationPercent = Math.min(dissipationPercent, 1);
                            remainingEnergy = (long) (remainingEnergy * (1D - dissipationPercent));
                            if (remainingEnergy == 0L) {
                                //If we dissipated it all then update the position the laser is going to and break
                                to = from.adjustPosition(direction, entity);
                                break;
                            }
                            updateDamage = true;
                        }
                        //After dissipating any energy across the armor we try to refract some energy through the armor this
                        // will further reduce the damage the entity would take and allow the laser to continue through onto
                        // the other side
                        if (refractionPercent > 0) {
                            //If we will refract any energy, cap the refraction amount at one
                            refractionPercent = Math.min(refractionPercent, 1);
                            double refractedEnergy = remainingEnergy * refractionPercent;
                            //Don't actually use the refracted energy from our remaining energy
                            // but lower the damage values to not include the energy that is being refracted
                            // and mark that we don't actually need to update the damage values (as we just did so here)
                            value = (remainingEnergy - refractedEnergy) / energyPerDamage;
                            damage = (float) value;
                            updateDamage = false;
                            //Mark the energy scale should be checked for updates as if some energy got dissipated above, and
                            // we end up refracting all the remaining energy we won't do any damage and not get through the
                            // normal code path that checks if the energy scale changed
                            updateEnergyScale = true;
                        }
                        if (updateDamage) {
                            //Update the damage we are actually going to try and do to the entity as the amount of energy being used changed
                            value = ((double) remainingEnergy / energyPerDamage);
                            damage = (float) value;
                        }
                        health = livingEntity.getHealth();
                    }
                    if (damage > 0) {
                        //If the damage is more than zero, which should be all cases except for when we are refracting all the energy past the entity
                        // set the entity on fire if it is not damage immune and try to damage it
                        if (!entity.fireImmune()) {
                            entity.igniteForTicks(MathUtils.clampToInt(value));
                        }
                        int totemTimesUsed = -1;
                        if (entity instanceof ServerPlayer player) {
                            MinecraftServer server = entity.getServer();
                            if (server != null && server.isHardcore()) {
                                totemTimesUsed = player.getStats().getValue(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                            }
                        }
                        //Note: We add the laser damage type to bypass cooldown via tags so this will go off regardless of invulnerability timer
                        boolean damaged = entity.hurt(MekanismDamageTypes.LASER.source(level), damage);
                        if (damaged) {
                            //If we damaged it
                            if (entity instanceof LivingEntity livingEntity) {
                                //Update the damage to match how much health the entity lost
                                damage = Math.min(damage, Math.max(0, health - livingEntity.getHealth()));
                                if (entity instanceof ServerPlayer player) {
                                    //If the damage actually went through fire the trigger
                                    boolean hardcoreTotem = totemTimesUsed != -1 && totemTimesUsed < player.getStats().getValue(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                                    MekanismCriteriaTriggers.DAMAGE.value().trigger(player, MekanismDamageTypes.LASER, hardcoreTotem);
                                }
                            }
                            remainingEnergy -= MathUtils.clampToLong(energyPerDamage * damage);
                            if (remainingEnergy == 0L) {
                                //Update the position that the laser is going to
                                to = from.adjustPosition(direction, entity);
                                break;
                            }
                            //If we have any energy left over after damaging the entity, mark that we are going to need to update the energy scale
                            updateEnergyScale = true;
                        }
                    }
                    if (updateEnergyScale) {
                        float energyScale = getEnergyScale(remainingEnergy);
                        if (laserEnergyScale - energyScale > 0.01) {
                            //Otherwise, send the laser between the two positions and update the energy scale
                            Pos3D entityPos = from.adjustPosition(direction, entity);
                            sendLaserDataToPlayers(new LaserParticleData(direction, entityPos.distance(from), laserEnergyScale), from);
                            laserEnergyScale = energyScale;
                            //Update the from position to be where the entity is
                            from = entityPos;
                            //Mark we have a new AABB we have to check against, as the beam isn't as large anymore,
                            // so it is possible some things no longer should be getting hit by it
                            adjustedAABB = getLaserBox(direction, from, to, laserEnergyScale);
                        }
                    }
                }
            }
            //Tell the clients to render the laser
            sendLaserDataToPlayers(new LaserParticleData(direction, to.distance(from), laserEnergyScale), from);

            if (remainingEnergy == 0L || result.getType() == Type.MISS) {
                //If all the energy was spent on damaging entities or if we aren't actively digging a block,
                // then reset any digging progress we may have
                digging = null;
                diggingProgress = 0L;
            } else {
                //Otherwise, we still have energy left that we can use
                BlockPos hitPos = result.getBlockPos();
                if (!hitPos.equals(digging)) {
                    digging = result.getType() == Type.MISS ? null : hitPos;
                    diggingProgress = 0L;
                }
                ILaserReceptor laserReceptor = WorldUtils.getCapability(level, Capabilities.LASER_RECEPTOR, hitPos, result.getDirection());
                if (laserReceptor != null && !laserReceptor.canLasersDig()) {
                    //Give the energy to the receptor
                    laserReceptor.receiveLaserEnergy(remainingEnergy);
                } else {
                    //Otherwise, make progress on breaking the block
                    BlockState hitState = level.getBlockState(hitPos);
                    float hardness = hitState.getDestroySpeed(level, hitPos);
                    if (hardness >= 0) {
                        diggingProgress += remainingEnergy;
                        if (diggingProgress >= hardness * MekanismConfig.general.laserEnergyPerHardness.get()) {
                            if (MekanismConfig.general.aestheticWorldDamage.get()) {
                                withFakePlayer((ServerLevel) level, to.x(), to.y(), to.z(), hitPos, hitState, result.getDirection());
                            }
                            diggingProgress = 0L;
                        } else {
                            //Note: If this has a significant network performance, we could instead convert this to a start/stop packet
                            PacketUtils.sendToAllTracking(new PacketHitBlockEffect(result), this);
                        }
                    }
                }
            }
            energyContainer.extract(firing, Action.EXECUTE, AutomationType.INTERNAL);
        } else if (getActive()) {
            setActive(false);
            if (diggingProgress != 0L) {
                diggingProgress = 0L;
            }
            if (lastFired != 0L) {
                lastFired = 0L;
                sendUpdatePacket = true;
            }
        }
        return sendUpdatePacket;
    }

    private AABB getLaserBox(Direction direction, Vec3 from, Vec3 to, float energyScale) {
        AABB aabb = new AABB(from, to);
        double halfDiameter = energyScale / 2;
        return switch (direction) {
            case DOWN, UP -> aabb.inflate(halfDiameter, 0, halfDiameter);
            case NORTH, SOUTH -> aabb.inflate(halfDiameter, halfDiameter, 0);
            case WEST, EAST -> aabb.inflate(0, halfDiameter, halfDiameter);
        };
    }

    private void withFakePlayer(ServerLevel level, double x, double y, double z, BlockPos hitPos, BlockState hitState, Direction hitSide) {
        MekFakePlayer dummy = MekFakePlayer.setupFakePlayer(level, x, y, z);
        dummy.setEmulatingUUID(getOwnerUUID());//pretend to be the owner
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, hitPos, hitState, dummy);
        if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {
            if (hitState.getBlock() instanceof TntBlock && hitState.isFlammable(level, hitPos, hitSide)) {
                //Convert TNT that can be lit on fire into a tnt entity
                //Note: We don't mark the fake player as the igniter as then when the tnt explodes if it hits a player
                // there will be a crash as our fake player's level will be null
                hitState.onCaughtFire(level, hitPos, hitSide, null);
                level.removeBlock(hitPos, false);
            } else {
                //Use the disassembler as the item to break the block with as that is marked as being the correct tool for drops
                handleBreakBlock(hitState, level, hitPos, dummy, ItemAtomicDisassembler.fullyChargedStack());
            }
        }
        dummy.cleanupFakePlayer(level);
    }

    /**
     * @param from   Where the laser is firing from
     * @param damage Damage to do
     *
     * @return The amount of damage that was blocked
     */
    private float damageShield(Level level, LivingEntity livingEntity, Pos3D from, float damage) {
        DamageSource source = MekanismDamageTypes.LASER.source(level, from);
        //Absorb part of the damage based on the given absorption ratio
        DamageContainer damageContainer = new DamageContainer(source, damage);
        //Note: Even though we fire this even here manually, it doesn't cause issues with the damage pipeline
        // as if we do block damage, then we won't end up firing the normal pipeline
        LivingShieldBlockEvent event = CommonHooks.onDamageBlock(livingEntity, damageContainer, livingEntity.isDamageSourceBlocked(source));
        if (event.isCanceled() || !event.getBlocked()) {
            //Blocking was not allowed, return we didn't block any damage
            return 0;
        }
        float shieldDamage = event.shieldDamage();
        if (shieldDamage > 0) {
            //Only damage the shield if the shield isn't setup to block damage for free
            livingEntity.hurtCurrentlyUsedShield(shieldDamage);
        }
        float damageBlocked = event.getBlockedDamage();
        if (livingEntity instanceof ServerPlayer player && damageBlocked > 0 && damageBlocked < 3.4028235E37F) {
            player.awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(damageBlocked * 10F));
        }
        return damageBlocked;
    }

    private float getEnergyScale(long energy) {
        //Returned energy scale is between [0.1, 0.6]
        return (float) Math.min(((double) energy / MekanismConfig.usage.laser.get()) / 10D, 0.6D);
    }

    private void sendLaserDataToPlayers(LaserParticleData data, Vec3 from) {
        if (!isRemote() && level instanceof ServerLevel serverWorld) {
            for (ServerPlayer player : serverWorld.players()) {
                serverWorld.sendParticles(player, data, true, from.x, from.y, from.z, 1, 0, 0, 0, 0);
            }
        }
    }

    protected void setEmittingRedstone(boolean foundEntity) {
    }

    protected boolean handleHitItem(ItemEntity entity) {
        return false;
    }

    protected void handleBreakBlock(BlockState state, ServerLevel level, BlockPos hitPos, Player player, ItemStack tool) {
        for (ItemEntity drop : WorldUtils.getDrops(state, level, hitPos, WorldUtils.getTileEntity(level, hitPos), player, tool, true)) {
            if (!drop.getItem().isEmpty()) {
                level.addFreshEntity(drop);
            }
        }
        breakBlock(state, level, hitPos, tool);
    }

    protected final void breakBlock(BlockState state, ServerLevel level, BlockPos hitPos, ItemStack tool) {
        state.spawnAfterBreak(level, hitPos, tool, false);
        level.removeBlock(hitPos, false);
        //TODO: We may want to evaluate at some point doing this with our fake player so that it is fired as the "cause"?
        level.gameEvent(GameEvent.BLOCK_DESTROY, hitPos, GameEvent.Context.of(null, state));
        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, hitPos, Block.getId(state));
    }

    protected long toFire() {
        return Long.MAX_VALUE;
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        NBTUtils.setLegacyEnergyIfPresent(nbt, SerializationConstants.LAST_FIRED, value -> lastFired = value);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        nbtTags.putLong(SerializationConstants.LAST_FIRED, lastFired);
    }

    @Override
    @Deprecated
    public void removeComponentsFromTag(@NotNull CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(SerializationConstants.LAST_FIRED);
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.putLong(SerializationConstants.LAST_FIRED, lastFired);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        NBTUtils.setLongIfPresent(tag, SerializationConstants.LAST_FIRED, fired -> lastFired = fired);
    }

    public LaserEnergyContainer getEnergyContainer() {
        return energyContainer;
    }
}