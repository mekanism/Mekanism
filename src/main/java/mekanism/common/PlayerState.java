package mekanism.common;

import java.util.HashSet;
import java.util.Set;
import mekanism.client.sound.SoundHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerDataMessage;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerPacket;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketScubaTankData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class PlayerState {

    private Set<String> activeJetpacks = new HashSet<>();
    private Set<String> activeGasmasks = new HashSet<>();
    private Set<String> activeFlamethrowers = new HashSet<>();

    private World world;

    public void clear() {
        activeJetpacks.clear();
        activeGasmasks.clear();
        activeFlamethrowers.clear();
    }

    public void clearPlayer(EntityPlayer p) {
        activeJetpacks.remove(p.getName());
        activeGasmasks.remove(p.getName());
        activeFlamethrowers.remove(p.getName());
    }

    public void init(World world) {
        this.world = world;
    }

    // ----------------------
    //
    // Jetpack state tracking
    //
    // ----------------------

    public void setJetpackState(String playerId, boolean isActive, boolean isLocal) {
        boolean alreadyActive = activeJetpacks.contains(playerId);
        boolean changed = (alreadyActive != isActive);

        if (alreadyActive && !isActive) {
            // On -> off
            activeJetpacks.remove(playerId);
        } else if (!alreadyActive && isActive) {
            // Off -> on
            activeJetpacks.add(playerId);
        }

        // If something changed and we're in a remote world, take appropriate action
        if (changed && world.isRemote) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler.sendToServer(PacketJetpackData.JetpackDataMessage.UPDATE(playerId, isActive));
            }

            // Start a sound playing if the person is now flying
            if (isActive && MekanismConfig.client.enablePlayerSounds) {
                SoundHandler.startSound(world.getPlayerEntityByName(playerId), "jetpack");
            }
        }
    }

    public boolean isJetpackOn(EntityPlayer p) {
        return activeJetpacks.contains(p.getName());
    }

    public Set<String> getActiveJetpacks() {
        return activeJetpacks;
    }

    public void setActiveJetpacks(Set<String> newActiveJetpacks) {
        for (String activeUser : newActiveJetpacks) {
            setJetpackState(activeUser, true, false);
        }
    }

    // ----------------------
    //
    // Gasmask state tracking
    //
    // ----------------------

    public void setGasmaskState(String playerId, boolean isActive, boolean isLocal) {
        boolean alreadyActive = activeGasmasks.contains(playerId);
        boolean changed = (alreadyActive != isActive);

        if (alreadyActive && !isActive) {
            activeGasmasks.remove(playerId); // On -> off
        } else if (!alreadyActive && isActive) {
            activeGasmasks.add(playerId); // Off -> on
        }

        // If something changed and we're in a remote world, take appropriate action
        if (changed && world.isRemote) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler
                      .sendToServer(PacketScubaTankData.ScubaTankDataMessage.UPDATE(playerId, isActive));
            }

            // Start a sound playing if the person is now using a gasmask
            if (isActive && MekanismConfig.client.enablePlayerSounds) {
                SoundHandler.startSound(world.getPlayerEntityByName(playerId), "gasmask");
            }
        }
    }

    public boolean isGasmaskOn(EntityPlayer p) {
        return activeGasmasks.contains(p.getName());
    }

    public Set<String> getActiveGasmasks() {
        return activeGasmasks;
    }

    public void setActiveGasmasks(Set<String> newActiveGasmasks) {
        for (String activeUser : newActiveGasmasks) {
            setGasmaskState(activeUser, true, false);
        }
    }

    // ----------------------
    //
    // Flamethrower state tracking
    //
    // ----------------------

    public void setFlamethrowerState(String playerId, boolean isActive, boolean isLocal) {
        boolean alreadyActive = activeFlamethrowers.contains(playerId);
        boolean changed = (alreadyActive != isActive);

        if (alreadyActive && !isActive) {
            activeFlamethrowers.remove(playerId); // On -> off
        } else if (!alreadyActive && isActive) {
            activeFlamethrowers.add(playerId); // Off -> on
        }

        // If something changed and we're in a remote world, take appropriate action
        if (changed && world.isRemote) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                Mekanism.packetHandler.sendToServer(new FlamethrowerDataMessage(FlamethrowerPacket.UPDATE, null,
                      playerId, isActive));
            }

            // Start a sound playing if the person is now using a flamethrower
            if (isActive && MekanismConfig.client.enablePlayerSounds) {
                SoundHandler.startSound(world.getPlayerEntityByName(playerId), "flamethrower");
            }
        }
    }

    public boolean isFlamethrowerOn(EntityPlayer p) {
        return activeFlamethrowers.contains(p.getName());
    }

}
