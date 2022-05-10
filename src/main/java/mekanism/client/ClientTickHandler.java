package mekanism.client;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.IModule;
import mekanism.client.gui.GuiRadialSelector;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.sound.GeigerSound;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.HolidayManager;
import mekanism.common.base.KeySync;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.content.gear.mekasuit.ModuleVisionEnhancementUnit;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemHDPEElytra;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.item.interfaces.IRadialModeItem;
import mekanism.common.item.interfaces.IRadialSelectorEnum;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.network.to_server.PacketModeChange;
import mekanism.common.network.to_server.PacketPortableTeleporterTeleport;
import mekanism.common.network.to_server.PacketRadialModeChange;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Client-side tick handler for Mekanism. Used mainly for the update check upon startup.
 *
 * @author AidanBrady
 */
public class ClientTickHandler {

    public static final Minecraft minecraft = Minecraft.getInstance();
    public static final Random rand = new Random();
    public static final Map<Player, TeleportData> portableTeleports = new Object2ObjectArrayMap<>(1);
    public boolean initHoliday = false;
    public boolean shouldReset = false;
    public static boolean firstTick = true;
    public static boolean visionEnhancement = false;

    private static long lastScrollTime = -1;
    private static double scrollDelta;

    public static boolean isJetpackActive(Player player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isJetpackOn(player);
        }
        if (!player.isSpectator()) {
            ItemStack chest = ItemJetpack.getJetpack(player);
            if (!chest.isEmpty()) {
                JetpackMode mode = CommonPlayerTickHandler.getJetpackMode(chest);
                if (mode == JetpackMode.NORMAL) {
                    return minecraft.screen == null && minecraft.player.input.jumping;
                } else if (mode == JetpackMode.HOVER) {
                    boolean ascending = minecraft.player.input.jumping;
                    boolean descending = minecraft.player.input.shiftKeyDown;
                    if (!ascending || descending || minecraft.screen != null) {
                        return !CommonPlayerTickHandler.isOnGroundOrSleeping(player);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isScubaMaskOn(Player player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isScubaMaskOn(player);
        }
        return CommonPlayerTickHandler.isScubaMaskOn(player, player.getItemBySlot(EquipmentSlot.CHEST));
    }

    public static boolean isGravitationalModulationOn(Player player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isGravitationalModulationOn(player);
        }
        return CommonPlayerTickHandler.isGravitationalModulationOn(player);
    }

    public static boolean isVisionEnhancementOn(Player player) {
        IModule<ModuleVisionEnhancementUnit> module = MekanismAPI.getModuleHelper().load(player.getItemBySlot(EquipmentSlot.HEAD), MekanismModules.VISION_ENHANCEMENT_UNIT);
        return module != null && module.isEnabled() && module.getContainerEnergy().greaterThan(MekanismConfig.gear.mekaSuitEnergyUsageVisionEnhancement.get());
    }

    public static boolean isFlamethrowerOn(Player player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isFlamethrowerOn(player);
        }
        return hasFlamethrower(player) && minecraft.options.keyUse.isDown();
    }

    public static boolean hasFlamethrower(Player player) {
        ItemStack currentItem = player.getInventory().getSelected();
        return !currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower && ChemicalUtil.hasGas(currentItem);
    }

    public static void portableTeleport(Player player, InteractionHand hand, FrequencyIdentity identity) {
        int delay = MekanismConfig.gear.portableTeleporterDelay.get();
        if (delay == 0) {
            Mekanism.packetHandler().sendToServer(new PacketPortableTeleporterTeleport(hand, identity));
        } else {
            portableTeleports.put(player, new TeleportData(hand, identity, minecraft.level.getGameTime() + delay));
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (event.phase == Phase.START) {
            tickStart();
        }
    }

    public void tickStart() {
        MekanismClient.ticksPassed++;

        if (firstTick && minecraft.level != null) {
            MekanismClient.launchClient();
            firstTick = false;
        }

        if (minecraft.level != null) {
            shouldReset = true;
        } else if (shouldReset) {
            MekanismClient.reset();
            shouldReset = false;
        }

        if (minecraft.level != null && minecraft.player != null && !minecraft.isPaused()) {
            if (!initHoliday || MekanismClient.ticksPassed % 1_200 == 0) {
                HolidayManager.notify(Minecraft.getInstance().player);
                initHoliday = true;
            }

            //Reboot player sounds if needed
            SoundHandler.restartSounds();

            if (minecraft.level.getGameTime() - lastScrollTime > 20) {
                scrollDelta = 0;
            }

            RadiationManager.INSTANCE.tickClient(minecraft.player);

            UUID playerUUID = minecraft.player.getUUID();
            // Update player's state for various items; this also automatically notifies server if something changed and
            // kicks off sounds as necessary
            Mekanism.playerState.setJetpackState(playerUUID, isJetpackActive(minecraft.player), true);
            Mekanism.playerState.setScubaMaskState(playerUUID, isScubaMaskOn(minecraft.player), true);
            Mekanism.playerState.setGravitationalModulationState(playerUUID, isGravitationalModulationOn(minecraft.player), true);
            Mekanism.playerState.setFlamethrowerState(playerUUID, hasFlamethrower(minecraft.player), isFlamethrowerOn(minecraft.player), true);

            for (Iterator<Entry<Player, TeleportData>> iter = portableTeleports.entrySet().iterator(); iter.hasNext(); ) {
                Entry<Player, TeleportData> entry = iter.next();
                Player player = entry.getKey();
                for (int i = 0; i < 100; i++) {
                    double x = player.getX() + rand.nextDouble() - 0.5D;
                    double y = player.getY() + rand.nextDouble() * 2 - 2D;
                    double z = player.getZ() + rand.nextDouble() - 0.5D;
                    minecraft.level.addParticle(ParticleTypes.PORTAL, x, y, z, 0, 1, 0);
                }
                TeleportData data = entry.getValue();
                if (minecraft.level.getGameTime() == data.teleportTime) {
                    Mekanism.packetHandler().sendToServer(new PacketPortableTeleporterTeleport(data.hand, data.identity));
                    iter.remove();
                }
            }

            ItemStack chestStack = minecraft.player.getItemBySlot(EquipmentSlot.CHEST);
            IModule<ModuleJetpackUnit> jetpackModule = MekanismAPI.getModuleHelper().load(chestStack, MekanismModules.JETPACK_UNIT);

            ItemStack jetpack = ItemJetpack.getJetpack(minecraft.player, chestStack);
            if (!jetpack.isEmpty() || jetpackModule != null) {
                MekanismClient.updateKey(minecraft.player.input.jumping, KeySync.ASCEND);
            }

            if (isJetpackActive(minecraft.player)) {
                JetpackMode mode = CommonPlayerTickHandler.getJetpackMode(jetpack);
                if (CommonPlayerTickHandler.handleJetpackMotion(minecraft.player, mode, () -> minecraft.player.input.jumping)) {
                    minecraft.player.fallDistance = 0.0F;
                }
            }

            if (isScubaMaskOn(minecraft.player) && minecraft.player.getAirSupply() == minecraft.player.getMaxAirSupply()) {
                for (MobEffectInstance effect : minecraft.player.getActiveEffects()) {
                    for (int i = 0; i < 9; i++) {
                        MekanismUtils.speedUpEffectSafely(minecraft.player, effect);
                    }
                }
            }

            if (isVisionEnhancementOn(minecraft.player)) {
                visionEnhancement = true;
                // adds if it doesn't exist, otherwise tops off duration to 220. equal or less than 200 will make vision flickers
                minecraft.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, true, false));
            } else if (visionEnhancement) {
                visionEnhancement = false;
                MobEffectInstance effect = minecraft.player.getEffect(MobEffects.NIGHT_VISION);
                if (effect != null && effect.getDuration() <= 220) {
                    //Only remove it if it is our effect and not one that has a longer remaining duration
                    minecraft.player.removeEffect(MobEffects.NIGHT_VISION);
                }
            }

            ItemStack stack = minecraft.player.getItemBySlot(EquipmentSlot.MAINHAND);
            if (MekKeyHandler.isRadialPressed() && stack.getItem() instanceof IRadialModeItem<?> item) {
                if (minecraft.screen == null || minecraft.screen instanceof GuiRadialSelector) {
                    updateSelectorRenderer(item);
                }
            } else if (minecraft.screen instanceof GuiRadialSelector) {
                minecraft.setScreen(null);
            }

            if (MekanismConfig.client.enablePlayerSounds.get()) {
                RadiationScale scale = RadiationManager.INSTANCE.getClientScale();
                if (scale != RadiationScale.NONE && !SoundHandler.radiationSoundMap.containsKey(scale)) {
                    GeigerSound sound = GeigerSound.create(minecraft.player, scale);
                    SoundHandler.radiationSoundMap.put(scale, sound);
                    SoundHandler.playSound(sound);
                }
            }
        }
    }

    private <TYPE extends Enum<TYPE> & IRadialSelectorEnum<TYPE>> void updateSelectorRenderer(IRadialModeItem<TYPE> modeItem) {
        Class<TYPE> modeClass = modeItem.getModeClass();
        if (!(minecraft.screen instanceof GuiRadialSelector<?> screen) || screen.getEnumClass() != modeClass) {
            minecraft.setScreen(new GuiRadialSelector<>(modeClass, () -> {
                if (minecraft.player != null) {
                    ItemStack s = minecraft.player.getItemBySlot(EquipmentSlot.MAINHAND);
                    if (s.getItem() instanceof IRadialModeItem) {
                        //noinspection unchecked
                        return ((IRadialModeItem<TYPE>) s.getItem()).getMode(s);
                    }
                }
                return modeItem.getDefaultMode();
            }, type -> {
                if (minecraft.player != null) {
                    Mekanism.packetHandler().sendToServer(new PacketRadialModeChange(EquipmentSlot.MAINHAND, type.ordinal()));
                }
            }));
        }
    }

    @SubscribeEvent
    public void onMouseEvent(MouseScrollEvent event) {
        if (MekanismConfig.client.allowModeScroll.get() && minecraft.player != null && minecraft.player.isShiftKeyDown()) {
            handleModeScroll(event, event.getScrollDelta());
        }
    }

    @SubscribeEvent
    public void onGuiMouseEvent(ScreenEvent.MouseScrollEvent.Pre event) {
        if (event.getScreen() instanceof GuiRadialSelector) {
            handleModeScroll(event, event.getScrollDelta());
        }
    }

    private void handleModeScroll(Event event, double delta) {
        if (delta != 0 && IModeItem.isModeItem(minecraft.player, EquipmentSlot.MAINHAND)) {
            lastScrollTime = minecraft.level.getGameTime();
            scrollDelta += delta;
            int shift = (int) scrollDelta;
            scrollDelta %= 1;
            if (shift != 0) {
                RenderTickHandler.modeSwitchTimer = 100;
                Mekanism.packetHandler().sendToServer(new PacketModeChange(EquipmentSlot.MAINHAND, shift));
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFogLighting(EntityViewRenderEvent.FogColors event) {
        if (visionEnhancement) {
            event.setBlue(0.4F);
            event.setRed(0.4F);
            event.setGreen(0.8F);
        }
    }

    @SubscribeEvent
    public void onFog(EntityViewRenderEvent.RenderFogEvent event) {
        if (visionEnhancement) {
            float fog = 384;
            IModule<ModuleVisionEnhancementUnit> module = MekanismAPI.getModuleHelper().load(minecraft.player.getItemBySlot(EquipmentSlot.HEAD), MekanismModules.VISION_ENHANCEMENT_UNIT);
            if (module != null) {
                fog *= Math.pow(module.getInstalledCount(), 1.25) / (float) MekanismModules.VISION_ENHANCEMENT_UNIT.getModuleData().getMaxStackSize();
            }
            RenderSystem.setShaderFogStart(-8.0F);
            RenderSystem.setShaderFogEnd(fog * 0.5F);
        }
    }

    @SubscribeEvent
    public void recipesUpdated(RecipesUpdatedEvent event) {
        //Note: Dedicated servers first connection the server sends recipes then tags, and on reload sends tags then recipes.
        // We ignore this fact and only clear the cache in the recipes updated event however, as the cache should already be
        // empty on our initial connection, and even if it isn't the client has no way to query the recipes and cause the
        // caches to be initialized before the tags are then received as we lazily initialize our recipe caches.
        MekanismRecipeType.clearCache();
    }

    @SubscribeEvent
    public void renderEntityPre(RenderLivingEvent.Pre<?, ?> evt) {
        EntityModel<?> model = evt.getRenderer().getModel();
        if (model instanceof HumanoidModel<?> humanoidModel) {
            //If the entity has a biped model, then see if it is wearing a meka suit, in which case we want to hide various parts of the model
            setModelVisibility(evt.getEntity(), humanoidModel, false);
        }
    }

    @SubscribeEvent
    public void renderEntityPost(RenderLivingEvent.Post<?, ?> evt) {
        EntityModel<?> model = evt.getRenderer().getModel();
        if (model instanceof HumanoidModel<?> humanoidModel) {
            //Undo model visibility changes we made to ensure that other entities of the same type are properly visible
            setModelVisibility(evt.getEntity(), humanoidModel, true);
        }
    }

    private static void setModelVisibility(LivingEntity entity, HumanoidModel<?> entityModel, boolean showModel) {
        if (entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ItemMekaSuitArmor) {
            entityModel.head.visible = showModel;
            entityModel.hat.visible = showModel;
            if (entityModel instanceof PlayerModel<?> playerModel) {
                playerModel.ear.visible = showModel;
            }
        }
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof ItemMekaSuitArmor) {
            entityModel.body.visible = showModel;
            if (!(entity instanceof ArmorStand)) {
                //Don't adjust arms for armor stands as the model will end up changing them anyway and then we may incorrectly activate them
                entityModel.leftArm.visible = showModel;
                entityModel.rightArm.visible = showModel;
            }
            if (entityModel instanceof PlayerModel<?> playerModel) {
                playerModel.cloak.visible = showModel;
                playerModel.jacket.visible = showModel;
                playerModel.leftSleeve.visible = showModel;
                playerModel.rightSleeve.visible = showModel;
            } else if (entityModel instanceof ArmorStandModel armorStandModel) {
                armorStandModel.rightBodyStick.visible = showModel;
                armorStandModel.leftBodyStick.visible = showModel;
                armorStandModel.shoulderStick.visible = showModel;
            }
        } else if (chest.getItem() instanceof ItemHDPEElytra && entityModel instanceof PlayerModel<?> playerModel) {
            //Hide the player's cape if they have an HDPE elytra as it will be part of the elytra's layer and shouldn't be rendered
            playerModel.cloak.visible = showModel;
        }
        if (entity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ItemMekaSuitArmor) {
            entityModel.leftLeg.visible = showModel;
            entityModel.rightLeg.visible = showModel;
            if (entityModel instanceof PlayerModel<?> playerModel) {
                playerModel.leftPants.visible = showModel;
                playerModel.rightPants.visible = showModel;
            }
        }
    }

    private record TeleportData(InteractionHand hand, FrequencyIdentity identity, long teleportTime) {
    }
}