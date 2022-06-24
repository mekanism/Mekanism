package mekanism.common.base;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.functions.FloatSupplier;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import mekanism.client.sound.PlayerSound.SoundType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekasuit.ModuleGravitationalModulatingUnit;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.network.to_client.PacketFlyingSync;
import mekanism.common.network.to_client.PacketResetPlayerClient;
import mekanism.common.network.to_server.PacketGearStateUpdate;
import mekanism.common.network.to_server.PacketGearStateUpdate.GearType;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

public class PlayerState {

    private static final UUID STEP_ASSIST_MODIFIER_UUID = UUID.fromString("026E638A-570D-48F2-BA91-3E86BBB26576");
    private static final UUID SWIM_BOOST_MODIFIER_UUID = UUID.fromString("B8BEEC12-741C-47C3-A74D-AA00F0D2ACF0");

    private final Set<UUID> activeJetpacks = new ObjectOpenHashSet<>();
    private final Set<UUID> activeScubaMasks = new ObjectOpenHashSet<>();
    private final Set<UUID> activeGravitationalModulators = new ObjectOpenHashSet<>();
    private final Set<UUID> activeFlamethrowers = new ObjectOpenHashSet<>();
    private final Map<UUID, FlightInfo> flightInfoMap = new Object2ObjectOpenHashMap<>();

    private LevelAccessor world;

    public void clear(boolean isRemote) {
        activeJetpacks.clear();
        activeScubaMasks.clear();
        activeGravitationalModulators.clear();
        activeFlamethrowers.clear();
        if (isRemote) {
            SoundHandler.clearPlayerSounds();
        } else {
            flightInfoMap.clear();
        }
    }

    public void clearPlayer(UUID uuid, boolean isRemote) {
        activeJetpacks.remove(uuid);
        activeScubaMasks.remove(uuid);
        activeGravitationalModulators.remove(uuid);
        activeFlamethrowers.remove(uuid);
        if (isRemote) {
            SoundHandler.clearPlayerSounds(uuid);
            if (Minecraft.getInstance().player == null || Minecraft.getInstance().player.getUUID().equals(uuid)) {
                SoundHandler.radiationSoundMap.clear();
            }
        }
        RadiationManager.INSTANCE.resetPlayer(uuid);
        if (!isRemote) {
            Mekanism.packetHandler().sendToAll(new PacketResetPlayerClient(uuid));
        }
    }

    public void clearPlayerServerSideOnly(UUID uuid) {
        flightInfoMap.remove(uuid);
    }

    public void reapplyServerSideOnly(Player player) {
        //For when the dimension changes/we need to reapply the flight info values to the client
        UUID uuid = player.getUUID();
        FlightInfo flightInfo = flightInfoMap.get(uuid);
        if (flightInfo != null) {
            if (flightInfo.wasFlyingAllowed || flightInfo.wasFlying) {
                updateClientServerFlight(player, flightInfo.wasFlyingAllowed, flightInfo.wasFlying);
            }
        }
    }

    public void init(LevelAccessor world) {
        this.world = world;
    }

    // ----------------------
    //
    // Jetpack state tracking
    //
    // ----------------------

    public void setJetpackState(UUID uuid, boolean isActive, boolean isLocal) {
        boolean alreadyActive = activeJetpacks.contains(uuid);
        boolean changed = alreadyActive != isActive;
        if (alreadyActive && !isActive) {
            // On -> off
            activeJetpacks.remove(uuid);
        } else if (!alreadyActive && isActive) {
            // Off -> on
            activeJetpacks.add(uuid);
        }

        // If something changed, and we're in a remote world, take appropriate action
        if (changed && world.isClientSide()) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler().sendToServer(new PacketGearStateUpdate(GearType.JETPACK, uuid, isActive));
            }

            // Start a sound playing if the person is now flying
            if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(world, uuid, SoundType.JETPACK);
            }
        }
    }

    public boolean isJetpackOn(Player p) {
        return activeJetpacks.contains(p.getUUID());
    }

    public Set<UUID> getActiveJetpacks() {
        return activeJetpacks;
    }

    // ----------------------
    //
    // Scuba Mask state tracking
    //
    // ----------------------

    public void setScubaMaskState(UUID uuid, boolean isActive, boolean isLocal) {
        boolean alreadyActive = activeScubaMasks.contains(uuid);
        boolean changed = alreadyActive != isActive;
        if (alreadyActive && !isActive) {
            activeScubaMasks.remove(uuid); // On -> off
        } else if (!alreadyActive && isActive) {
            activeScubaMasks.add(uuid); // Off -> on
        }

        // If something changed, and we're in a remote world, take appropriate action
        if (changed && world.isClientSide()) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler().sendToServer(new PacketGearStateUpdate(GearType.SCUBA_MASK, uuid, isActive));
            }

            // Start a sound playing if the person is now using a scuba mask
            if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(world, uuid, SoundType.SCUBA_MASK);
            }
        }
    }

    public boolean isScubaMaskOn(Player p) {
        return activeScubaMasks.contains(p.getUUID());
    }

    public Set<UUID> getActiveScubaMasks() {
        return activeScubaMasks;
    }

    // ----------------------
    //
    // Step assist state tracking
    //
    // ----------------------

    public void updateStepAssist(Player player) {
        updateAttribute(player, ForgeMod.STEP_HEIGHT_ADDITION.get(), STEP_ASSIST_MODIFIER_UUID, "Step Assist", () -> CommonPlayerTickHandler.getStepBoost(player));
    }

    // ----------------------
    //
    // Swim boost state tracking
    //
    // ----------------------

    public void updateSwimBoost(Player player) {
        updateAttribute(player, ForgeMod.SWIM_SPEED.get(), SWIM_BOOST_MODIFIER_UUID, "Swim Boost", () -> CommonPlayerTickHandler.getSwimBoost(player));
    }

    private void updateAttribute(Player player, Attribute attribute, UUID uuid, String name, FloatSupplier additionalSupplier) {
        AttributeInstance attributeInstance = player.getAttribute(attribute);
        if (attributeInstance != null) {
            AttributeModifier existing = attributeInstance.getModifier(uuid);
            float additional = additionalSupplier.getAsFloat();
            if (existing != null) {
                if (existing.getAmount() == additional) {
                    //If we already have it set to the correct value just exit
                    //Note: We don't need to check for if it is equal to zero as we should never have the attribute applied then
                    return;
                }
                //Otherwise, remove the no longer valid value, so we can add it again properly
                attributeInstance.removeModifier(existing);
            }
            if (additional > 0) {
                //If we should have the attribute, but we don't have it set yet, or our stored amount was different, update
                attributeInstance.addTransientModifier(new AttributeModifier(uuid, name, additional, Operation.ADDITION));
            }
        }
    }

    // ----------------------
    //
    // Gravitational Modulator state tracking
    //
    // ----------------------

    public void setGravitationalModulationState(UUID uuid, boolean isActive, boolean isLocal) {
        boolean alreadyActive = activeGravitationalModulators.contains(uuid);
        boolean changed = alreadyActive != isActive;
        if (alreadyActive && !isActive) {
            activeGravitationalModulators.remove(uuid); // On -> off
        } else if (!alreadyActive && isActive) {
            activeGravitationalModulators.add(uuid); // Off -> on
        }

        // If something changed, and we're in a remote world, take appropriate action
        if (changed && world.isClientSide()) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler().sendToServer(new PacketGearStateUpdate(GearType.GRAVITATIONAL_MODULATOR, uuid, isActive));
            }

            // Start a sound playing if the person is now using a gravitational modulator
            if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(world, uuid, SoundType.GRAVITATIONAL_MODULATOR);
            }
        }
    }

    public boolean isGravitationalModulationOn(Player p) {
        return activeGravitationalModulators.contains(p.getUUID());
    }

    public Set<UUID> getActiveGravitationalModulators() {
        return activeGravitationalModulators;
    }

    public void updateFlightInfo(Player player) {
        boolean isFlyingGameMode = !MekanismUtils.isPlayingMode(player);
        boolean hasGravitationalModulator = CommonPlayerTickHandler.isGravitationalModulationReady(player);
        FlightInfo flightInfo = flightInfoMap.computeIfAbsent(player.getUUID(), uuid -> new FlightInfo());
        if (isFlyingGameMode || hasGravitationalModulator) {
            //The player can fly
            if (!flightInfo.hadFlightItem) {
                //If they did not have a flight item
                if (!player.getAbilities().mayfly) {
                    //and they are not already allowed to fly, then enable it
                    updateClientServerFlight(player, true);
                }
                //and mark that they had a flight item
                flightInfo.hadFlightItem = true;
            } else if (flightInfo.wasFlyingGameMode && !isFlyingGameMode) {
                //The player was in a game mode that allowed flight, but no longer is, though they still are allowed to fly
                //Sync the fact to the client. Also passes wasFlying so that if they were flying previously,
                //and are still allowed to the game mode change doesn't force them out of it
                updateClientServerFlight(player, true, flightInfo.wasFlying);
            } else if (flightInfo.wasFlyingAllowed && !player.getAbilities().mayfly) {
                //If we were allowed to fly but something changed that state (such as another mod)
                // Re-enable flying and set the player back into flying if they were flying
                updateClientServerFlight(player, true, flightInfo.wasFlying);
            }
            //Update flight info states
            flightInfo.wasFlyingGameMode = isFlyingGameMode;
            flightInfo.wasFlying = player.getAbilities().flying;
            flightInfo.wasFlyingAllowed = player.getAbilities().mayfly;
            if (player.getAbilities().flying && hasGravitationalModulator) {
                //If the player is actively flying (not just allowed to), and has the gravitational modulator ready then apply movement boost if active, and use energy
                IModule<ModuleGravitationalModulatingUnit> module = MekanismAPI.getModuleHelper().load(player.getItemBySlot(EquipmentSlot.CHEST), MekanismModules.GRAVITATIONAL_MODULATING_UNIT);
                if (module != null) {//Should not be null but double check
                    FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get();
                    if (Mekanism.keyMap.has(player.getUUID(), KeySync.BOOST)) {
                        FloatingLong boostUsage = usage.multiply(4);
                        if (module.canUseEnergy(player, boostUsage, false)) {
                            float boost = module.getCustomInstance().getBoost();
                            if (boost > 0) {
                                player.moveRelative(boost, new Vec3(0, 0, 1));
                                usage = boostUsage;
                            }
                        }
                    }
                    module.useEnergy(player, usage);
                }
            }
        } else {
            if (flightInfo.hadFlightItem) {
                if (player.getAbilities().mayfly) {
                    updateClientServerFlight(player, false);
                }
                flightInfo.hadFlightItem = false;
            }
            flightInfo.wasFlyingGameMode = false;
            flightInfo.wasFlying = player.getAbilities().flying;
            flightInfo.wasFlyingAllowed = player.getAbilities().mayfly;
        }
    }

    private void updateClientServerFlight(Player player, boolean allowFlying) {
        updateClientServerFlight(player, allowFlying, allowFlying && player.getAbilities().flying);
    }

    private void updateClientServerFlight(Player player, boolean allowFlying, boolean isFlying) {
        Mekanism.packetHandler().sendTo(new PacketFlyingSync(allowFlying, isFlying), (ServerPlayer) player);
        player.getAbilities().mayfly = allowFlying;
        player.getAbilities().flying = isFlying;
    }

    // ----------------------
    //
    // Flamethrower state tracking
    //
    // ----------------------

    public void setFlamethrowerState(UUID uuid, boolean isActive, boolean isLocal) {
        setFlamethrowerState(uuid, isActive, isActive, isLocal);
    }

    public void setFlamethrowerState(UUID uuid, boolean hasFlameThrower, boolean isActive, boolean isLocal) {
        boolean alreadyActive = activeFlamethrowers.contains(uuid);
        boolean changed = alreadyActive != isActive;
        if (alreadyActive && !isActive) {
            activeFlamethrowers.remove(uuid); // On -> off
        } else if (!alreadyActive && isActive) {
            activeFlamethrowers.add(uuid); // Off -> on
        }

        if (world == null) {
            //world is set from the OnWorldLoad event, a tick should never have happened before that.
            throw new NullPointerException("mekanism.common.base.PlayerState#world is null. This should not happen. Optifine is known to cause this on client side.");
        }

        if (world.isClientSide()) {
            boolean startSound;
            // If something changed, and we're in a remote world, take appropriate action
            if (changed) {
                // If the player is the "local" player, we need to tell the server the state has changed
                if (isLocal) {
                    Mekanism.packetHandler().sendToServer(new PacketGearStateUpdate(GearType.FLAMETHROWER, uuid, isActive));
                }

                // Start a sound playing if the person is now using a flamethrower
                startSound = isActive;
            } else {
                //Start the sound if it isn't already active, and still isn't, but has a flame thrower
                // This allows us to catch and start playing the idle sound
                //TODO: Currently this only happens for the local player as "having" a flame thrower is not
                // synced from server to client. This is not that big a deal, though may be something we want
                // to look into eventually
                startSound = !isActive && hasFlameThrower;
                //Note: If they just continue to hold (but not use) a flamethrower it "will" continue having this
                // attempt to start the sound. This is not a major deal as the uuid gets checked before attempting
                // to retrieve the player or actually creating a new sound object.
            }
            if (startSound && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(world, uuid, SoundType.FLAMETHROWER);
            }
        }
    }

    public boolean isFlamethrowerOn(Player p) {
        return activeFlamethrowers.contains(p.getUUID());
    }

    public Set<UUID> getActiveFlamethrowers() {
        return activeFlamethrowers;
    }

    private static class FlightInfo {

        public boolean hadFlightItem;
        public boolean wasFlyingGameMode;
        public boolean wasFlyingAllowed;
        public boolean wasFlying;
    }
}