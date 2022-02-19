package mekanism.client;

import com.mojang.blaze3d.platform.GlStateManager;
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
import net.minecraft.client.renderer.entity.model.ArmorStandModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
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
    public static final Map<PlayerEntity, TeleportData> portableTeleports = new Object2ObjectArrayMap<>(1);
    public boolean initHoliday = false;
    public boolean shouldReset = false;
    public static boolean firstTick = true;
    public static boolean visionEnhancement = false;

    private static long lastScrollTime = -1;
    private static double scrollDelta;

    public static boolean isJetpackActive(PlayerEntity player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isJetpackOn(player);
        }
        if (!player.isSpectator()) {
            ItemStack chest = player.getItemBySlot(EquipmentSlotType.CHEST);
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

    public static boolean isScubaMaskOn(PlayerEntity player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isScubaMaskOn(player);
        }
        return CommonPlayerTickHandler.isScubaMaskOn(player, player.getItemBySlot(EquipmentSlotType.CHEST));
    }

    public static boolean isGravitationalModulationOn(PlayerEntity player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isGravitationalModulationOn(player);
        }
        return CommonPlayerTickHandler.isGravitationalModulationOn(player);
    }

    public static boolean isVisionEnhancementOn(PlayerEntity player) {
        IModule<ModuleVisionEnhancementUnit> module = MekanismAPI.getModuleHelper().load(player.getItemBySlot(EquipmentSlotType.HEAD), MekanismModules.VISION_ENHANCEMENT_UNIT);
        return module != null && module.isEnabled() && module.getContainerEnergy().greaterThan(MekanismConfig.gear.mekaSuitEnergyUsageVisionEnhancement.get());
    }

    public static boolean isFlamethrowerOn(PlayerEntity player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isFlamethrowerOn(player);
        }
        return hasFlamethrower(player) && minecraft.options.keyUse.isDown();
    }

    public static boolean hasFlamethrower(PlayerEntity player) {
        ItemStack currentItem = player.inventory.getSelected();
        return !currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower && ChemicalUtil.hasGas(currentItem);
    }

    public static void portableTeleport(PlayerEntity player, Hand hand, FrequencyIdentity identity) {
        int delay = MekanismConfig.gear.portableTeleporterDelay.get();
        if (delay == 0) {
            Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterTeleport(hand, identity));
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

            for (Iterator<Entry<PlayerEntity, TeleportData>> iter = portableTeleports.entrySet().iterator(); iter.hasNext(); ) {
                Entry<PlayerEntity, TeleportData> entry = iter.next();
                PlayerEntity player = entry.getKey();
                for (int i = 0; i < 100; i++) {
                    double x = player.getX() + rand.nextDouble() - 0.5D;
                    double y = player.getY() + rand.nextDouble() * 2 - 2D;
                    double z = player.getZ() + rand.nextDouble() - 0.5D;
                    minecraft.level.addParticle(ParticleTypes.PORTAL, x, y, z, 0, 1, 0);
                }
                TeleportData data = entry.getValue();
                if (minecraft.level.getGameTime() == data.teleportTime) {
                    Mekanism.packetHandler.sendToServer(new PacketPortableTeleporterTeleport(data.hand, data.identity));
                    iter.remove();
                }
            }

            ItemStack chestStack = minecraft.player.getItemBySlot(EquipmentSlotType.CHEST);
            IModule<ModuleJetpackUnit> jetpackModule = MekanismAPI.getModuleHelper().load(chestStack, MekanismModules.JETPACK_UNIT);

            if (!chestStack.isEmpty() && (chestStack.getItem() instanceof ItemJetpack || jetpackModule != null)) {
                MekanismClient.updateKey(minecraft.player.input.jumping, KeySync.ASCEND);
            }

            if (isJetpackActive(minecraft.player)) {
                JetpackMode mode = CommonPlayerTickHandler.getJetpackMode(chestStack);
                if (CommonPlayerTickHandler.handleJetpackMotion(minecraft.player, mode, () -> minecraft.player.input.jumping)) {
                    minecraft.player.fallDistance = 0.0F;
                }
            }

            if (isScubaMaskOn(minecraft.player) && minecraft.player.getAirSupply() == minecraft.player.getMaxAirSupply()) {
                for (EffectInstance effect : minecraft.player.getActiveEffects()) {
                    for (int i = 0; i < 9; i++) {
                        MekanismUtils.speedUpEffectSafely(minecraft.player, effect);
                    }
                }
            }

            if (isVisionEnhancementOn(minecraft.player)) {
                visionEnhancement = true;
                // adds if it doesn't exist, otherwise tops off duration to 220. equal or less than 200 will make vision flickers
                minecraft.player.addEffect(new EffectInstance(Effects.NIGHT_VISION, 220, 0, false, true, false));
            } else if (visionEnhancement) {
                visionEnhancement = false;
                EffectInstance effect = minecraft.player.getEffect(Effects.NIGHT_VISION);
                if (effect != null && effect.getDuration() <= 220) {
                    //Only remove it if it is our effect and not one that has a longer remaining duration
                    minecraft.player.removeEffect(Effects.NIGHT_VISION);
                }
            }

            ItemStack stack = minecraft.player.getItemBySlot(EquipmentSlotType.MAINHAND);
            if (MekKeyHandler.isRadialPressed() && stack.getItem() instanceof IRadialModeItem) {
                if (minecraft.screen == null || minecraft.screen instanceof GuiRadialSelector) {
                    updateSelectorRenderer((IRadialModeItem<?>) stack.getItem());
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
        if (!(minecraft.screen instanceof GuiRadialSelector) || ((GuiRadialSelector<?>) minecraft.screen).getEnumClass() != modeClass) {
            minecraft.setScreen(new GuiRadialSelector<>(modeClass, () -> {
                if (minecraft.player != null) {
                    ItemStack s = minecraft.player.getItemBySlot(EquipmentSlotType.MAINHAND);
                    if (s.getItem() instanceof IRadialModeItem) {
                        return ((IRadialModeItem<TYPE>) s.getItem()).getMode(s);
                    }
                }
                return modeItem.getDefaultMode();
            }, type -> {
                if (minecraft.player != null) {
                    Mekanism.packetHandler.sendToServer(new PacketRadialModeChange(EquipmentSlotType.MAINHAND, type.ordinal()));
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
    public void onGuiMouseEvent(GuiScreenEvent.MouseScrollEvent.Pre event) {
        if (event.getGui() instanceof GuiRadialSelector) {
            handleModeScroll(event, event.getScrollDelta());
        }
    }

    private void handleModeScroll(Event event, double delta) {
        if (delta != 0 && IModeItem.isModeItem(minecraft.player, EquipmentSlotType.MAINHAND)) {
            lastScrollTime = minecraft.level.getGameTime();
            scrollDelta += delta;
            int shift = (int) scrollDelta;
            scrollDelta %= 1;
            if (shift != 0) {
                RenderTickHandler.modeSwitchTimer = 100;
                Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.MAINHAND, shift));
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
            float fog = 0.1F;
            IModule<ModuleVisionEnhancementUnit> module = MekanismAPI.getModuleHelper().load(minecraft.player.getItemBySlot(EquipmentSlotType.HEAD), MekanismModules.VISION_ENHANCEMENT_UNIT);
            if (module != null) {
                fog -= module.getInstalledCount() * 0.022F;
            }
            RenderSystem.fogDensity(fog);
            RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
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
        if (model instanceof BipedModel) {
            //If the entity has a biped model, then see if it is wearing a meka suit, in which case we want to hide various parts of the model
            setModelVisibility(evt.getEntity(), (BipedModel<?>) model, false);
        }
    }

    @SubscribeEvent
    public void renderEntityPost(RenderLivingEvent.Post<?, ?> evt) {
        EntityModel<?> model = evt.getRenderer().getModel();
        if (model instanceof BipedModel) {
            //Undo model visibility changes we made to ensure that other entities of the same type are properly visible
            setModelVisibility(evt.getEntity(), (BipedModel<?>) model, true);
        }
    }

    private static void setModelVisibility(LivingEntity entity, BipedModel<?> entityModel, boolean showModel) {
        if (entity.getItemBySlot(EquipmentSlotType.HEAD).getItem() instanceof ItemMekaSuitArmor) {
            entityModel.head.visible = showModel;
            entityModel.hat.visible = showModel;
            if (entityModel instanceof PlayerModel) {
                PlayerModel<?> playerModel = (PlayerModel<?>) entityModel;
                playerModel.ear.visible = showModel;
            }
        }
        ItemStack chest = entity.getItemBySlot(EquipmentSlotType.CHEST);
        if (chest.getItem() instanceof ItemMekaSuitArmor) {
            entityModel.body.visible = showModel;
            if (!(entity instanceof ArmorStandEntity)) {
                //Don't adjust arms for armor stands as the model will end up changing them anyway and then we may incorrectly activate them
                entityModel.leftArm.visible = showModel;
                entityModel.rightArm.visible = showModel;
            }
            if (entityModel instanceof PlayerModel) {
                PlayerModel<?> playerModel = (PlayerModel<?>) entityModel;
                playerModel.cloak.visible = showModel;
                playerModel.jacket.visible = showModel;
                playerModel.leftSleeve.visible = showModel;
                playerModel.rightSleeve.visible = showModel;
            } else if (entityModel instanceof ArmorStandModel) {
                ArmorStandModel armorStandModel = (ArmorStandModel) entityModel;
                armorStandModel.bodyStick1.visible = showModel;
                armorStandModel.bodyStick2.visible = showModel;
                armorStandModel.shoulderStick.visible = showModel;
            }
        } else if (chest.getItem() instanceof ItemHDPEElytra && entityModel instanceof PlayerModel) {
            //Hide the player's cape if they have an HDPE elytra as it will be part of the elytra's layer and shouldn't be rendered
            PlayerModel<?> playerModel = (PlayerModel<?>) entityModel;
            playerModel.cloak.visible = showModel;
        }
        if (entity.getItemBySlot(EquipmentSlotType.LEGS).getItem() instanceof ItemMekaSuitArmor) {
            entityModel.leftLeg.visible = showModel;
            entityModel.rightLeg.visible = showModel;
            if (entityModel instanceof PlayerModel) {
                PlayerModel<?> playerModel = (PlayerModel<?>) entityModel;
                playerModel.leftPants.visible = showModel;
                playerModel.rightPants.visible = showModel;
            }
        }
    }

    //TODO - 1.18: Convert this to a record
    private static class TeleportData {

        private final Hand hand;
        private final FrequencyIdentity identity;
        private final long teleportTime;

        public TeleportData(Hand hand, FrequencyIdentity identity, long teleportTime) {
            this.hand = hand;
            this.identity = identity;
            this.teleportTime = teleportTime;
        }
    }
}