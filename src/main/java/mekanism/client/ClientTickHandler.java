package mekanism.client;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.api.chemical.gas.GasStack;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.HolidayManager;
import mekanism.common.KeySync;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.mekasuit.ModuleJetpackUnit;
import mekanism.common.frequency.Frequency;
import mekanism.common.item.IModeItem;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.network.PacketModeChange;
import mekanism.common.network.PacketPortableTeleporter;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterPacketType;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Client-side tick handler for Mekanism. Used mainly for the update check upon startup.
 *
 * @author AidanBrady
 */
public class ClientTickHandler {

    public static Minecraft minecraft = Minecraft.getInstance();
    public static Random rand = new Random();
    public static Map<PlayerEntity, TeleportData> portableTeleports = new Object2ObjectOpenHashMap<>();
    public boolean initHoliday = false;
    public boolean shouldReset = false;
    public static boolean firstTick = true;

    public static boolean isJetpackActive(PlayerEntity player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isJetpackOn(player);
        }
        if (!player.isCreative() && !player.isSpectator()) {
            ItemStack chest = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (!chest.isEmpty()) {
                JetpackMode mode = getJetpackMode(chest);
                if (mode == JetpackMode.NORMAL) {
                    return minecraft.currentScreen == null && minecraft.gameSettings.keyBindJump.isKeyDown();
                } else if (mode == JetpackMode.HOVER) {
                    boolean ascending = minecraft.gameSettings.keyBindJump.isKeyDown();
                    boolean descending = minecraft.gameSettings.keyBindSneak.isKeyDown();
                    if (!ascending || descending || minecraft.currentScreen != null) {
                        return !CommonPlayerTickHandler.isOnGround(player);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /** Will return null if jetpack module is not active */
    private static JetpackMode getJetpackMode(ItemStack stack) {
        if (stack.getItem() instanceof ItemJetpack && GasUtils.hasGas(stack)) {
            return ((ItemJetpack) stack.getItem()).getMode(stack);
        } else if (stack.getItem() instanceof IModuleContainerItem && GasUtils.hasGas(stack, MekanismGases.HYDROGEN.get())) {
            ModuleJetpackUnit module = Modules.load(stack, Modules.JETPACK_UNIT);
            if (module != null && module.isEnabled()) {
                return module.getMode();
            }
        }
        return null;
    }

    public static boolean isGasMaskOn(PlayerEntity player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isGasmaskOn(player);
        }
        return CommonPlayerTickHandler.isGasMaskOn(player);
    }

    public static boolean isStepBoostOn(PlayerEntity player) {
        ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.FEET);
        if (!stack.isEmpty() && stack.getItem() instanceof ItemFreeRunners) {
            if (stack.getItem() instanceof ItemFreeRunners) {
                ItemFreeRunners freeRunners = (ItemFreeRunners) stack.getItem();
                return freeRunners.getMode(stack) == ItemFreeRunners.FreeRunnerMode.NORMAL;
            }
            Module module = Modules.load(stack, Modules.HYDRAULIC_PROPULSION_UNIT);
            if (module != null && module.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFlamethrowerOn(PlayerEntity player) {
        if (player != minecraft.player) {
            return Mekanism.playerState.isFlamethrowerOn(player);
        }
        return hasFlamethrower(player) && minecraft.gameSettings.keyBindUseItem.isKeyDown();
    }

    public static boolean hasFlamethrower(PlayerEntity player) {
        ItemStack currentItem = player.inventory.getCurrentItem();
        return !currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower && GasUtils.hasGas(currentItem);
    }

    public static void portableTeleport(PlayerEntity player, Hand hand, Frequency freq) {
        int delay = MekanismConfig.general.portableTeleporterDelay.get();
        if (delay == 0) {
            Mekanism.packetHandler.sendToServer(new PacketPortableTeleporter(PortableTeleporterPacketType.TELEPORT, hand, freq));
        } else {
            portableTeleports.put(player, new TeleportData(hand, freq, minecraft.world.getDayTime() + delay));
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

        if (minecraft.world != null && firstTick) {
            MekanismClient.launchClient();
            firstTick = false;
        }

        if (minecraft.world != null) {
            shouldReset = true;
        } else if (shouldReset) {
            MekanismClient.reset();
            shouldReset = false;
        }

        if (minecraft.world != null && minecraft.player != null && !Mekanism.proxy.isPaused()) {
            if (!initHoliday || MekanismClient.ticksPassed % 1200 == 0) {
                HolidayManager.check(Minecraft.getInstance().player);
                initHoliday = true;
            }

            Mekanism.radiationManager.tickClient(minecraft.player);

            UUID playerUUID = minecraft.player.getUniqueID();
            boolean stepBoostOn = isStepBoostOn(minecraft.player);

            if (stepBoostOn && !minecraft.player.isShiftKeyDown()) {
                minecraft.player.stepHeight = 1.002F;
            } else if (minecraft.player.stepHeight == 1.002F) {
                minecraft.player.stepHeight = 0.6F;
            }

            // Update player's state for various items; this also automatically notifies server if something changed and
            // kicks off sounds as necessary
            Mekanism.playerState.setJetpackState(playerUUID, isJetpackActive(minecraft.player), true);
            Mekanism.playerState.setGasmaskState(playerUUID, isGasMaskOn(minecraft.player), true);
            Mekanism.playerState.setFlamethrowerState(playerUUID, hasFlamethrower(minecraft.player), isFlamethrowerOn(minecraft.player), true);

            for (Iterator<Entry<PlayerEntity, TeleportData>> iter = portableTeleports.entrySet().iterator(); iter.hasNext(); ) {
                Entry<PlayerEntity, TeleportData> entry = iter.next();
                PlayerEntity player = entry.getKey();
                for (int i = 0; i < 100; i++) {
                    double x = player.getPosX() + rand.nextDouble() - 0.5D;
                    double y = player.getPosY() + rand.nextDouble() * 2 - 2D;
                    double z = player.getPosZ() + rand.nextDouble() - 0.5D;
                    minecraft.world.addParticle(ParticleTypes.PORTAL, x, y, z, 0, 1, 0);
                }

                if (minecraft.world.getDayTime() == entry.getValue().teleportTime) {
                    Mekanism.packetHandler.sendToServer(new PacketPortableTeleporter(PortableTeleporterPacketType.TELEPORT, entry.getValue().hand, entry.getValue().freq));
                    iter.remove();
                }
            }

            ItemStack chestStack = minecraft.player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            ModuleJetpackUnit jetpackModule = Modules.load(chestStack, Modules.JETPACK_UNIT);

            if (!chestStack.isEmpty() && (chestStack.getItem() instanceof ItemJetpack || jetpackModule != null)) {
                MekanismClient.updateKey(minecraft.gameSettings.keyBindJump, KeySync.ASCEND);
                MekanismClient.updateKey(minecraft.gameSettings.keyBindSneak, KeySync.DESCEND);
            }

            if (!minecraft.player.isCreative() && !minecraft.player.isSpectator()) {
                if (isFlamethrowerOn(minecraft.player)) {
                    ItemFlamethrower flamethrower = (ItemFlamethrower) minecraft.player.inventory.getCurrentItem().getItem();
                    flamethrower.useGas(minecraft.player.inventory.getCurrentItem(), 1);
                }
            }

            if (isJetpackActive(minecraft.player)) {
                JetpackMode mode = getJetpackMode(chestStack);
                Vec3d motion = minecraft.player.getMotion();
                if (mode == JetpackMode.NORMAL) {
                    minecraft.player.setMotion(motion.getX(), Math.min(motion.getY() + 0.15D, 0.5D), motion.getZ());
                    minecraft.player.fallDistance = 0.0F;
                } else if (mode == JetpackMode.HOVER) {
                    boolean ascending = minecraft.gameSettings.keyBindJump.isKeyDown();
                    boolean descending = minecraft.gameSettings.keyBindSneak.isKeyDown();
                    if ((!ascending && !descending) || (ascending && descending) || minecraft.currentScreen != null) {
                        if (motion.getY() > 0) {
                            minecraft.player.setMotion(motion.getX(), Math.max(motion.getY() - 0.15D, 0), motion.getZ());
                        } else if (motion.getY() < 0) {
                            if (!CommonPlayerTickHandler.isOnGround(minecraft.player)) {
                                minecraft.player.setMotion(motion.getX(), Math.min(motion.getY() + 0.15D, 0), motion.getZ());
                            }
                        }
                    } else if (ascending) {
                        minecraft.player.setMotion(motion.getX(), Math.min(motion.getY() + 0.15D, 0.2D), motion.getZ());
                    } else if (!CommonPlayerTickHandler.isOnGround(minecraft.player)) {
                        minecraft.player.setMotion(motion.getX(), Math.max(motion.getY() - 0.15D, -0.2D), motion.getZ());
                    }
                    minecraft.player.fallDistance = 0.0F;
                }
                // I don't actually know if we need to do this
                if (chestStack.getItem() instanceof ItemJetpack) {
                    ((ItemJetpack) chestStack.getItem()).useGas(chestStack, 1);
                }
            }

            if (isGasMaskOn(minecraft.player)) {
                ItemScubaTank tank = (ItemScubaTank) chestStack.getItem();
                final int max = 300;
                tank.useGas(chestStack, 1);
                GasStack received = tank.useGas(chestStack, max - minecraft.player.getAir());

                if (!received.isEmpty()) {
                    minecraft.player.setAir(minecraft.player.getAir() + received.getAmount());
                }
                if (minecraft.player.getAir() == max) {
                    for (EffectInstance effect : minecraft.player.getActivePotionEffects()) {
                        for (int i = 0; i < 9; i++) {
                            effect.tick(minecraft.player, () -> MekanismUtils.onChangedPotionEffect(minecraft.player, effect, true));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseEvent(MouseScrollEvent event) {
        if (MekanismConfig.client.allowModeScroll.get() && minecraft.player != null && minecraft.player.isShiftKeyDown()) {
            int shift = (int) event.getScrollDelta();
            if (shift != 0 && IModeItem.isModeItem(minecraft.player, EquipmentSlotType.MAINHAND)) {
                RenderTickHandler.modeSwitchTimer = 100;
                Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.MAINHAND, shift));
                event.setCanceled(true);
            }
        }
    }

    private static class TeleportData {

        private Hand hand;
        private Frequency freq;
        private long teleportTime;

        public TeleportData(Hand h, Frequency f, long t) {
            hand = h;
            freq = f;
            teleportTime = t;
        }
    }
}