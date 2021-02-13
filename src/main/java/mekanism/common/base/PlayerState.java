package mekanism.common.base;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.api.math.FloatingLong;
import mekanism.client.sound.PlayerSound.SoundType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.mekasuit.ModuleMekaSuit.ModuleGravitationalModulatingUnit;
import mekanism.common.network.PacketFlyingSync;
import mekanism.common.network.PacketGearStateUpdate;
import mekanism.common.network.PacketGearStateUpdate.GearType;
import mekanism.common.network.PacketResetPlayerClient;
import mekanism.common.network.PacketStepHeightSync;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;

public class PlayerState {

    private final Set<UUID> activeJetpacks = new ObjectOpenHashSet<>();
    private final Set<UUID> activeScubaMasks = new ObjectOpenHashSet<>();
    private final Set<UUID> activeGravitationalModulators = new ObjectOpenHashSet<>();
    private final Set<UUID> activeFlamethrowers = new ObjectOpenHashSet<>();
    private final Object2FloatMap<UUID> stepAssistedPlayers = new Object2FloatOpenHashMap<>();
    private final Map<UUID, FlightInfo> flightInfoMap = new Object2ObjectOpenHashMap<>();

    private IWorld world;

    public void clear(boolean isRemote) {
        activeJetpacks.clear();
        activeScubaMasks.clear();
        activeGravitationalModulators.clear();
        activeFlamethrowers.clear();
        if (isRemote) {
            SoundHandler.clearPlayerSounds();
        } else {
            stepAssistedPlayers.clear();
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
            if (Minecraft.getInstance().player == null || Minecraft.getInstance().player.getUniqueID().equals(uuid)) {
                SoundHandler.radiationSoundMap.clear();
            }
        }
        Mekanism.radiationManager.resetPlayer(uuid);
        if (!isRemote) {
            Mekanism.packetHandler.sendToAll(new PacketResetPlayerClient(uuid));
        }
    }

    public void clearPlayerServerSideOnly(UUID uuid) {
        stepAssistedPlayers.removeFloat(uuid);
        flightInfoMap.remove(uuid);
    }

    public void reapplyServerSideOnly(PlayerEntity player) {
        //For when the dimension changes/we need to reapply the step assist/flight info values to the client
        UUID uuid = player.getUniqueID();
        if (stepAssistedPlayers.containsKey(uuid)) {
            updateClientServerStepHeight(player, stepAssistedPlayers.getFloat(uuid));
        }
        if (flightInfoMap.containsKey(uuid)) {
            FlightInfo flightInfo = flightInfoMap.get(uuid);
            if (flightInfo.wasFlyingAllowed || flightInfo.wasFlying) {
                updateClientServerFlight(player, flightInfo.wasFlyingAllowed, flightInfo.wasFlying);
            }
        }
    }

    public void init(IWorld world) {
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

        // If something changed and we're in a remote world, take appropriate action
        if (changed && world.isRemote()) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler.sendToServer(new PacketGearStateUpdate(GearType.JETPACK, uuid, isActive));
            }

            // Start a sound playing if the person is now flying
            if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(world, uuid, SoundType.JETPACK);
            }
        }
    }

    public boolean isJetpackOn(PlayerEntity p) {
        return activeJetpacks.contains(p.getUniqueID());
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

        // If something changed and we're in a remote world, take appropriate action
        if (changed && world.isRemote()) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler.sendToServer(new PacketGearStateUpdate(GearType.SCUBA_MASK, uuid, isActive));
            }

            // Start a sound playing if the person is now using a scuba mask
            if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(world, uuid, SoundType.SCUBA_MASK);
            }
        }
    }

    public boolean isScubaMaskOn(PlayerEntity p) {
        return activeScubaMasks.contains(p.getUniqueID());
    }

    public Set<UUID> getActiveScubaMasks() {
        return activeScubaMasks;
    }

    // ----------------------
    //
    // Step assist state tracking
    //
    // ----------------------

    public void updateStepAssist(PlayerEntity player) {
        UUID uuid = player.getUniqueID();
        float additionalHeight = CommonPlayerTickHandler.getStepBoost(player);
        if (additionalHeight == 0) {
            if (stepAssistedPlayers.containsKey(uuid)) {
                //If we don't have step assist but we previously did, remove it and lower the step height
                stepAssistedPlayers.removeFloat(uuid);
                updateClientServerStepHeight(player, 0.6F);
            }
        } else {
            float totalHeight = 0.6F + additionalHeight;
            if (!stepAssistedPlayers.containsKey(uuid) || stepAssistedPlayers.getFloat(uuid) != totalHeight) {
                //If we should be able to have auto step, but we don't have it set yet, or our stored amount is different, update
                stepAssistedPlayers.put(uuid, totalHeight);
                updateClientServerStepHeight(player, totalHeight);
            }
        }
    }

    private void updateClientServerStepHeight(PlayerEntity player, float value) {
        player.stepHeight = value;
        Mekanism.packetHandler.sendTo(new PacketStepHeightSync(value), (ServerPlayerEntity) player);
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

        // If something changed and we're in a remote world, take appropriate action
        if (changed && world.isRemote()) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler.sendToServer(new PacketGearStateUpdate(GearType.GRAVITATIONAL_MODULATOR, uuid, isActive));
            }

            // Start a sound playing if the person is now using a gravitational modulator
            if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(world, uuid, SoundType.GRAVITATIONAL_MODULATOR);
            }
        }
    }

    public boolean isGravitationalModulationOn(PlayerEntity p) {
        return activeGravitationalModulators.contains(p.getUniqueID());
    }

    public Set<UUID> getActiveGravitationalModulators() {
        return activeGravitationalModulators;
    }

    public void updateFlightInfo(PlayerEntity player) {
        boolean isFlyingGameMode = !MekanismUtils.isPlayingMode(player);
        boolean hasGravitationalModulator = CommonPlayerTickHandler.isGravitationalModulationReady(player);
        FlightInfo flightInfo = flightInfoMap.computeIfAbsent(player.getUniqueID(), uuid -> new FlightInfo());
        if (isFlyingGameMode || hasGravitationalModulator) {
            //The player can fly
            if (!flightInfo.hadFlightItem) {
                //If they did not have a flight item
                if (!player.abilities.allowFlying) {
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
            } else if (flightInfo.wasFlyingAllowed && !player.abilities.allowFlying) {
                //If we were allowed to fly but something changed that state (such as another mod)
                // Re-enable flying and set the player back into flying if they were flying
                updateClientServerFlight(player, true, flightInfo.wasFlying);
            }
            //Update flight info states
            flightInfo.wasFlyingGameMode = isFlyingGameMode;
            flightInfo.wasFlying = player.abilities.isFlying;
            flightInfo.wasFlyingAllowed = player.abilities.allowFlying;
            if (player.abilities.isFlying && hasGravitationalModulator) {
                //If the player is actively flying (not just allowed to), and has the gravitational modulator ready then apply movement boost if active, and use energy
                FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get();
                boolean boostKey = Mekanism.keyMap.has(player.getUniqueID(), KeySync.BOOST);
                ModuleGravitationalModulatingUnit module = Modules.load(player.getItemStackFromSlot(EquipmentSlotType.CHEST), Modules.GRAVITATIONAL_MODULATING_UNIT);
                player.setSprinting(false);
                if (boostKey) {
                    player.moveRelative(module.getBoost(), new Vector3d(0, 0, 1));
                }
                module.useEnergy(player, boostKey ? usage.multiply(4) : usage);
            }
        } else {
            if (flightInfo.hadFlightItem) {
                if (player.abilities.allowFlying) {
                    updateClientServerFlight(player, false);
                }
                flightInfo.hadFlightItem = false;
            }
            flightInfo.wasFlyingGameMode = false;
            flightInfo.wasFlying = player.abilities.isFlying;
            flightInfo.wasFlyingAllowed = player.abilities.allowFlying;
        }
    }

    private void updateClientServerFlight(PlayerEntity player, boolean allowFlying) {
        updateClientServerFlight(player, allowFlying, allowFlying && player.abilities.isFlying);
    }

    private void updateClientServerFlight(PlayerEntity player, boolean allowFlying, boolean isFlying) {
        Mekanism.packetHandler.sendTo(new PacketFlyingSync(allowFlying, isFlying), (ServerPlayerEntity) player);
        player.abilities.allowFlying = allowFlying;
        player.abilities.isFlying = isFlying;
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

        if (world.isRemote()) {
            boolean startSound;
            // If something changed and we're in a remote world, take appropriate action
            if (changed) {
                // If the player is the "local" player, we need to tell the server the state has changed
                if (isLocal) {
                    Mekanism.packetHandler.sendToServer(new PacketGearStateUpdate(GearType.FLAMETHROWER, uuid, isActive));
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
                //Note: If they just continue to hold (but not use) a flame thrower it "will" continue having this
                // attempt to start the sound. This is not a major deal as the uuid gets checked before attempting
                // to retrieve the player or actually creating a new sound object.
            }
            if (startSound && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(world, uuid, SoundType.FLAMETHROWER);
            }
        }
    }

    public boolean isFlamethrowerOn(PlayerEntity p) {
        return activeFlamethrowers.contains(p.getUniqueID());
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