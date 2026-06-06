package mekanism.common.tile.laser;

import java.util.Comparator;
import java.util.List;
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
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.item.LivingEntityEquipmentWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - V11: Make the laser "shrink" the further distance it goes, If above a certain energy level and in water makes it make a bubble stream
public abstract class TileEntityBasicLaser extends TileEntityMekanism {

    protected LaserEnergyContainer energyContainer;
    @SyntheticComputerMethod(getter = "getDiggingPos")
    private BlockPos digging;
    private int diggingProgress = 0;
    private int lastFired = 0;

    public TileEntityBasicLaser(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    protected abstract @Nullable IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener);

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        int energyFired = fireLaser();
        if (energyFired > 0) {
            if (energyFired != lastFired || !getActive()) {
                setActive(true);
                lastFired = energyFired;
                sendUpdatePacket = true;
            }
        } else if (getActive()) {
            setActive(false);
            diggingProgress = 0;
            if (lastFired != 0) {
                lastFired = 0;
                sendUpdatePacket = true;
            }
        }
        return sendUpdatePacket;
    }

    private int fireLaser() {
        int toFire = toFire();
        if (toFire == 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            if (energyContainer.extract(toFire, transaction, AutomationType.INTERNAL) == toFire) {
                fireLaser(toFire, transaction);
                transaction.commit();
                return toFire;
            }
            return 0;
        }
    }

    private void fireLaser(int firing, TransactionContext transaction) {
        Direction direction = getDirection();
        ServerLevel level = (ServerLevel) getWorldNN();
        Pos3D from = Pos3D.create(this).centre().translate(direction, 0.501);
        Pos3D to = from.translate(direction, MekanismConfig.general.laserRange.get() - 0.002);
        BlockHitResult result = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty()));
        if (result.getType() != Type.MISS) {
            to = new Pos3D(result.getLocation());
        }

        float laserEnergyScale = getEnergyScale(firing);
        int remainingEnergy = firing;
        List<Entity> hitEntities = level.getEntitiesOfClass(Entity.class, getLaserBox(direction, from, to, laserEnergyScale));
        if (hitEntities.isEmpty()) {
            setEmittingRedstone(false);
        } else {
            setEmittingRedstone(true);
            //Sort the entities in order of which one is closest to the laser
            Pos3D finalFrom = from;
            hitEntities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(finalFrom)));
            int energyPerDamage = MekanismConfig.general.laserEnergyPerDamage.get();
            AABB adjustedAABB = null;
            for (Entity entity : hitEntities) {
                if (adjustedAABB != null && !entity.getBoundingBox().intersects(adjustedAABB)) {
                    //If we have a smaller AABB than we started with, make sure the entity still is getting hit by the laser
                    // before we do any processing related to behavior when hit
                    continue;
                } else if (isInvulnerableToLaser(entity, level)) {
                    //The entity can absorb all the energy because they are immune to the damage
                    remainingEnergy = 0;
                    //Update the position that the laser is going to
                    to = from.adjustPosition(direction, entity);
                    break;
                } else if (entity instanceof ItemEntity item && handleHitItem(item, transaction)) {
                    //TODO: Allow the tractor beam to have an energy cost for pulling items?
                    continue;
                }
                boolean updateEnergyScale = false;
                double value = (double) remainingEnergy / energyPerDamage;
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
                        remainingEnergy -= MathUtils.clampToInt(energyPerDamage * damageBlocked);
                        if (remainingEnergy == 0) {
                            //If we absorbed it all then update the position the laser is going to and break
                            to = from.adjustPosition(direction, entity);
                            break;
                        }
                        updateDamage = true;
                    }
                    //After our shield checks see if the armor the entity is wearing can dissipate or refract lasers
                    float dissipationPercent = 0;
                    float refractionPercent = 0;
                    ResourceHandler<ItemResource> armorSlots = LivingEntityEquipmentWrapper.of(livingEntity, EquipmentSlot.Type.HUMANOID_ARMOR);
                    for (int slot = 0, size = armorSlots.size(); slot < size; slot++) {
                        ItemStack stack = ItemUtil.getStack(armorSlots, slot);
                        if (!stack.isEmpty()) {
                            ILaserDissipation laserDissipation = stack.getCapability(Capabilities.LASER_DISSIPATION);
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
                        remainingEnergy = (int) (remainingEnergy * (1 - dissipationPercent));
                        if (remainingEnergy == 0) {
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
                        float refractedEnergy = remainingEnergy * refractionPercent;
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
                    if (entity instanceof ServerPlayer player && level.getServer().isHardcore()) {
                        totemTimesUsed = player.getStats().getValue(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                    }
                    //Note: We add the laser damage type to bypass cooldown via tags so this will go off regardless of invulnerability timer
                    boolean damaged = entity.hurtServer(level, MekanismDamageTypes.LASER.source(level), damage);
                    if (damaged) {
                        //If we damaged it
                        if (entity instanceof LivingEntity livingEntity) {
                            //Update the damage to match how much health the entity lost
                            damage = Math.clamp(health - livingEntity.getHealth(), 0, damage);
                            if (entity instanceof ServerPlayer player) {
                                //If the damage actually went through fire the trigger
                                boolean hardcoreTotem = totemTimesUsed != -1 && totemTimesUsed < player.getStats().getValue(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                                MekanismCriteriaTriggers.DAMAGE.value().trigger(player, MekanismDamageTypes.LASER, hardcoreTotem);
                            }
                        }
                        remainingEnergy -= MathUtils.clampToInt(energyPerDamage * damage);
                        if (remainingEnergy == 0) {
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
                        sendLaserDataToPlayers(level, new LaserParticleData(direction, entityPos.distance(from), laserEnergyScale), from);
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
        sendLaserDataToPlayers(level, new LaserParticleData(direction, to.distance(from), laserEnergyScale), from);

        if (remainingEnergy == 0 || result.getType() == Type.MISS) {
            //If all the energy was spent on damaging entities or if we aren't actively digging a block,
            // then reset any digging progress we may have
            digging = null;
            diggingProgress = 0;
        } else {
            //Otherwise, we still have energy left that we can use
            BlockPos hitPos = result.getBlockPos();
            if (!hitPos.equals(digging)) {
                digging = result.getType() == Type.MISS ? null : hitPos;
                diggingProgress = 0;
            }
            ILaserReceptor laserReceptor = WorldUtils.getCapability(level, Capabilities.LASER_RECEPTOR, hitPos, result.getDirection());
            if (laserReceptor != null && !laserReceptor.canLasersDig()) {
                //Give the energy to the receptor
                remainingEnergy -= laserReceptor.receiveLaserEnergy(remainingEnergy, transaction);
            } else {
                //Otherwise, make progress on breaking the block
                BlockState hitState = level.getBlockState(hitPos);
                float hardness = hitState.getDestroySpeed(level, hitPos);
                if (hardness >= 0) {
                    diggingProgress += remainingEnergy;
                    if (diggingProgress >= hardness * MekanismConfig.general.laserEnergyPerHardness.get()) {
                        if (MekanismConfig.general.aestheticWorldDamage.get()) {
                            withFakePlayer(level, to.x(), to.y(), to.z(), hitPos, hitState, result.getDirection(), transaction);
                        }
                        diggingProgress = 0;
                    } else {
                        //Note: If this has a significant network performance, we could instead convert this to a start/stop packet
                        PacketUtils.sendToAllTracking(new PacketHitBlockEffect(result), this);
                    }
                }
            }
        }
    }

    private static boolean isInvulnerableToLaser(Entity entity, ServerLevel level) {
        DamageSource lasers = MekanismDamageTypes.LASER.source(level);
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.isInvulnerableTo(level, lasers);
        }
        //fall back to the normally protected method, which fires an event
        return entity.isInvulnerableToBase(lasers);
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

    private void withFakePlayer(ServerLevel level, double x, double y, double z, BlockPos hitPos, BlockState hitState, Direction hitSide, TransactionContext transaction) {
        MekFakePlayer dummy = MekFakePlayer.setupFakePlayer(level, x, y, z);
        dummy.setEmulatingData(this);//pretend to be the owner
        //TODO - 26.1: Check about if we need to fire this on the client as well, or maybe just default mark it as notifying the client?
        BreakBlockEvent event = new BreakBlockEvent(level, hitPos, hitState, dummy);
        if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {
            if (hitState.getBlock() instanceof TntBlock && hitState.isFlammable(level, hitPos, hitSide)) {
                //Convert TNT that can be lit on fire into a tnt entity
                //Note: We don't mark the fake player as the igniter as then when the tnt explodes if it hits a player
                // there will be a crash as our fake player's level will be null
                hitState.onCaughtFire(level, hitPos, hitSide, null);
                level.removeBlock(hitPos, false);
            } else {
                //Use the disassembler as the item to break the block with as that is marked as being the correct tool for drops
                handleBreakBlock(hitState, level, hitPos, dummy, ItemAtomicDisassembler.fullyChargedStack(transaction), transaction);
            }
        }
        dummy.cleanupFakePlayer(level);
    }

    /**
     * @param from   Where the laser is firing from
     * @param damage Damage to do
     *
     * @return The amount of damage that was blocked
     *
     * @implNote most logic copied from {@link net.minecraft.world.entity.LivingEntity#applyItemBlocking}
     */
    private float damageShield(Level level, LivingEntity livingEntity, Pos3D from, float damage) {
        DamageSource source = MekanismDamageTypes.LASER.source(level, from);
        //Absorb part of the damage based on the given absorption ratio
        DamageContainer damageContainer = new DamageContainer(source, damage);
        //Note: Even though we fire this even here manually, it doesn't cause issues with the damage pipeline
        // as if we do block damage, then we won't end up firing the normal pipeline
        ItemStack blockingWith = livingEntity.getItemBlockingWith();
        if (blockingWith == null) {
            return 0;
        }
        BlocksAttacks blocksAttacks = blockingWith.get(DataComponents.BLOCKS_ATTACKS);
        if (blocksAttacks == null) {
            return 0;
        }
        HolderSet<DamageType> bypassedBy = blocksAttacks.bypassedBy().orElse(null);
        boolean originallyBlocked = bypassedBy != null && !bypassedBy.contains(source.typeHolder());
        float damageBlocked = 0;
        //assuming if it was bypassed we don't need to calculate this?
        if (originallyBlocked) {
            double angle = getAngle(livingEntity, from);
            damageBlocked = blocksAttacks.resolveBlockedDamage(source, damage, angle);
        }
        LivingShieldBlockEvent event = CommonHooks.onDamageBlock(livingEntity, damageContainer, damageBlocked, originallyBlocked);
        if (!event.getBlocked()) {
            //Blocking was not allowed, return we didn't block any damage
            return 0;
        }
        int shieldDamage = event.shieldDamage();
        if (shieldDamage > 0) {
            //Only damage the shield if the shield isn't setup to block damage for free
            blocksAttacks.hurtBlockingItem(level, blockingWith, livingEntity, livingEntity.getUsedItemHand(), damageBlocked, shieldDamage);
        }
        damageBlocked = event.getBlockedDamage();
        if (livingEntity instanceof Player player && damageBlocked > 0 && damageBlocked < 3.4028235E37F) {
            player.awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(damageBlocked * 10F));
        }
        return damageBlocked;
    }

    private static double getAngle(LivingEntity livingEntity, Pos3D from) {
        Vec3 viewVector = livingEntity.calculateViewVector(0.0F, livingEntity.getYHeadRot());
        Vec3 vectorTo = from.subtract(livingEntity.position());
        vectorTo = new Vec3(vectorTo.x, 0.0, vectorTo.z).normalize();
        return Math.acos(vectorTo.dot(viewVector));
    }

    private float getEnergyScale(int energy) {
        //Returned energy scale is between [0.1, 0.6]
        return Math.min((float) energy / MekanismConfig.usage.laser.get() / 10, 0.6F);
    }

    private void sendLaserDataToPlayers(ServerLevel level, LaserParticleData data, Vec3 from) {
        if (!isRemote()) {
            for (ServerPlayer player : level.players()) {
                //Note: We render laser particles regardless of the particle limit to avoid players accidentally killing themselves on them
                level.sendParticles(player, data, true, true, from.x, from.y, from.z, 1, 0, 0, 0, 0);
            }
        }
    }

    protected void setEmittingRedstone(boolean foundEntity) {
    }

    protected boolean handleHitItem(ItemEntity entity, TransactionContext transaction) {
        return false;
    }

    protected void handleBreakBlock(BlockState state, ServerLevel level, BlockPos hitPos, Player player, ItemStack tool, TransactionContext transaction) {
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

    protected int toFire() {
        return energyContainer.getAmountAsInt();
    }

    @Override
    public void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        lastFired = input.getIntOr(SerializationConstants.LAST_FIRED, lastFired);
    }

    @Override
    public void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(SerializationConstants.LAST_FIRED, lastFired);
    }

    @Override
    @Deprecated
    public void removeComponentsFromTag(@NotNull ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard(SerializationConstants.LAST_FIRED);
    }

    @Override
    public void writeReducedUpdatedTag(@NotNull ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        output.putInt(SerializationConstants.LAST_FIRED, lastFired);
    }

    @Override
    public void handleUpdateTag(@NotNull ValueInput input) {
        super.handleUpdateTag(input);
        lastFired = input.getIntOr(SerializationConstants.LAST_FIRED, lastFired);
    }

    public LaserEnergyContainer energyContainer() {
        return energyContainer;
    }
}