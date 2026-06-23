package mekanism.common.base;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.api.functions.ToFloatFunction;
import mekanism.api.gear.IModuleHelper;
import mekanism.client.sound.PlayerSound.SoundType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.PlayerExposure;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.player_data.PacketResetPlayerClient;
import mekanism.common.network.to_server.PacketGearStateUpdate;
import mekanism.common.network.to_server.PacketGearStateUpdate.GearType;
import mekanism.common.registries.MekanismModules;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public class PlayerState {

    private static final Identifier STEP_ASSIST_MODIFIER_ID = Mekanism.rl("step_assist");
    private static final Identifier DISABLE_ELYTRA_MODIFIER_ID = Mekanism.rl("disable_elytra");
    private static final AttributeModifier DISABLE_ELYTRA_MODIFIER = new AttributeModifier(DISABLE_ELYTRA_MODIFIER_ID, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    //these are read from the render thread on client, so use a map which is more resilient to that (even if data is 'outdated')
    private final Set<UUID> activeJetpacks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<UUID> activeScubaMasks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<UUID> activeGravitationalModulators = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Nullable
    private LevelAccessor level;

    public void clear(boolean isClientSide) {
        activeJetpacks.clear();
        activeScubaMasks.clear();
        activeGravitationalModulators.clear();
        if (isClientSide) {
            SoundHandler.clearPlayerSounds();
        }
    }

    public void clearPlayer(UUID uuid, boolean isClientSide) {
        activeJetpacks.remove(uuid);
        activeScubaMasks.remove(uuid);
        activeGravitationalModulators.remove(uuid);
        if (isClientSide) {
            SoundHandler.clearPlayerSounds(uuid);
            if (Minecraft.getInstance().player == null || Minecraft.getInstance().player.getUUID().equals(uuid)) {
                SoundHandler.radiationSoundMap.clear();
            }
        }
        PlayerExposure.resetPlayer(uuid);
        if (!isClientSide) {
            PacketDistributor.sendToAllPlayers(new PacketResetPlayerClient(uuid));
        }
    }

    public void init(LevelAccessor world) {
        this.level = world;
    }

    private LevelAccessor level() {
        return Objects.requireNonNull(level, "Player state level has not been initialized");
    }

    // ----------------------
    //
    // Jetpack state tracking
    //
    // ----------------------

    public void setJetpackState(UUID uuid, boolean isActive, boolean isLocal) {
        boolean alreadyActive = isJetpackOn(uuid);
        boolean changed = alreadyActive != isActive;
        if (alreadyActive && !isActive) {
            // On -> off
            activeJetpacks.remove(uuid);
        } else if (!alreadyActive && isActive) {
            // Off -> on
            activeJetpacks.add(uuid);
        }

        LevelAccessor level = level();
        // If something changed, and we're in a remote world, take appropriate action
        if (changed && level.isClientSide()) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                PacketUtils.sendToServer(new PacketGearStateUpdate(GearType.JETPACK, uuid, isActive));
            }

            // Start a sound playing if the person is now flying
            if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(level, uuid, SoundType.JETPACK);
            }
        }
    }

    public boolean isJetpackOn(Player p) {
        return isJetpackOn(p.getUUID());
    }

    public boolean isJetpackOn(UUID uuid) {
        return activeJetpacks.contains(uuid);
    }

    // ----------------------
    //
    // Scuba Mask state tracking
    //
    // ----------------------

    public void setScubaMaskState(UUID uuid, boolean isActive, boolean isLocal) {
        boolean alreadyActive = isScubaMaskOn(uuid);
        boolean changed = alreadyActive != isActive;
        if (alreadyActive && !isActive) {
            activeScubaMasks.remove(uuid); // On -> off
        } else if (!alreadyActive && isActive) {
            activeScubaMasks.add(uuid); // Off -> on
        }

        LevelAccessor level = level();
        // If something changed, and we're in a remote world, take appropriate action
        if (changed && level.isClientSide()) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                PacketUtils.sendToServer(new PacketGearStateUpdate(GearType.SCUBA_MASK, uuid, isActive));
            }

            // Start a sound playing if the person is now using a scuba mask
            if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(level, uuid, SoundType.SCUBA_MASK);
            }
        }
    }

    public boolean isScubaMaskOn(Player p) {
        return isScubaMaskOn(p.getUUID());
    }

    public boolean isScubaMaskOn(UUID uuid) {
        return activeScubaMasks.contains(uuid);
    }

    // ----------------------
    //
    // Step assist state tracking
    //
    // ----------------------

    public void updateStepAssist(Player player) {
        updateAttribute(player, Attributes.STEP_HEIGHT, STEP_ASSIST_MODIFIER_ID,  CommonPlayerTickHandler::getStepBoost);
    }

    //Note: The attributes that currently use this cannot be converted to just being attributes on the items, as they can be disabled based on the player state
    private void updateAttribute(Player player, Holder<Attribute> attribute, Identifier id, ToFloatFunction<Player> additionalSupplier) {
        AttributeInstance attributeInstance = player.getAttribute(attribute);
        if (attributeInstance != null) {
            AttributeModifier existing = attributeInstance.getModifier(id);
            float additional = additionalSupplier.applyAsFloat(player);
            if (existing != null) {
                if (existing.amount() == additional) {
                    //If we already have it set to the correct value just exit
                    //Note: We don't need to check for if it is equal to zero as we should never have the attribute applied then
                    return;
                }
                //Otherwise, remove the no longer valid value, so we can add it again properly
                attributeInstance.removeModifier(id);
            }
            if (additional > 0) {
                //If we should have the attribute, but we don't have it set yet, or our stored amount was different, update
                attributeInstance.addTransientModifier(new AttributeModifier(id, additional, Operation.ADD_VALUE));
            }
        }
    }

    // ----------------------
    //
    // Elytra Unit holding shift to disengage flight handling
    //
    // ----------------------

    public void updateElytraFlight(Player player) {
        //TODO - 26.2: Elytra - https://github.com/neoforged/NeoForge/pull/3192
        AttributeInstance attributeInstance = null;//player.getAttribute(NeoForgeMod.GLIDING_FLIGHT);
        if (attributeInstance != null) {
            boolean shouldDisableElytra = false;
            if (player.isShiftKeyDown() && player.isFallFlying()) {
                ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
                if (IModuleHelper.INSTANCE.isEnabled(stack, MekanismModules.ELYTRA_UNIT) && LivingEntity.canGlideUsing(stack, EquipmentSlot.CHEST)) {
                    //If the elytra unit is providing flight, allow disabling the flight by pressing shift
                    shouldDisableElytra = true;
                }
            }
            AttributeModifier existing = attributeInstance.getModifier(DISABLE_ELYTRA_MODIFIER_ID);
            if (existing != null) {
                if (!shouldDisableElytra) {
                    attributeInstance.removeModifier(DISABLE_ELYTRA_MODIFIER);
                }
            } else if (shouldDisableElytra) {
                attributeInstance.addTransientModifier(DISABLE_ELYTRA_MODIFIER);
            }
        }
    }

    // ----------------------
    //
    // Gravitational Modulator state tracking
    //
    // ----------------------

    public void setGravitationalModulationState(UUID uuid, boolean isActive, boolean isLocal) {
        boolean alreadyActive = isGravitationalModulationOn(uuid);
        boolean changed = alreadyActive != isActive;
        if (alreadyActive && !isActive) {
            activeGravitationalModulators.remove(uuid); // On -> off
        } else if (!alreadyActive && isActive) {
            activeGravitationalModulators.add(uuid); // Off -> on
        }

        LevelAccessor level = level();
        // If something changed, and we're in a remote world, take appropriate action
        if (changed && level.isClientSide()) {
            // If the player is the "local" player, we need to tell the server the state has changed
            if (isLocal) {
                PacketUtils.sendToServer(new PacketGearStateUpdate(GearType.GRAVITATIONAL_MODULATOR, uuid, isActive));
            }

            // Start a sound playing if the person is now using a gravitational modulator
            if (isActive && MekanismConfig.client.enablePlayerSounds.get()) {
                SoundHandler.startSound(level, uuid, SoundType.GRAVITATIONAL_MODULATOR);
            }
        }
    }

    public boolean isGravitationalModulationOn(Player p) {
        return isGravitationalModulationOn(p.getUUID());
    }

    public boolean isGravitationalModulationOn(UUID uuid) {
        return activeGravitationalModulators.contains(uuid);
    }
}