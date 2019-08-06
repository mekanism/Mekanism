package mekanism.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import mekanism.api.IClientTicker;
import mekanism.api.gas.GasStack;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.KeySync;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketItemStack.ItemStackMessage;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterPacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Hand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client-side tick handler for Mekanism. Used mainly for the update check upon startup.
 *
 * @author AidanBrady
 */
@SideOnly(Side.CLIENT)
public class ClientTickHandler {

    public static Minecraft mc = FMLClientHandler.instance().getClient();
    public static Random rand = new Random();
    public static Set<IClientTicker> tickingSet = new HashSet<>();
    public static Map<PlayerEntity, TeleportData> portableTeleports = new HashMap<>();
    public static int wheelStatus = 0;
    public boolean initHoliday = false;
    public boolean shouldReset = false;

    public static void killDeadNetworks() {
        tickingSet.removeIf(iClientTicker -> !iClientTicker.needsTicks());
    }

    public static boolean isJetpackActive(PlayerEntity player) {
        if (player != mc.player) {
            return Mekanism.playerState.isJetpackOn(player);
        }
        if (!player.isCreative() && !player.isSpectator()) {
            ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (!chest.isEmpty() && chest.getItem() instanceof ItemJetpack) {
                ItemJetpack jetpack = (ItemJetpack) chest.getItem();
                if (jetpack.getGas(chest) != null) {
                    JetpackMode mode = jetpack.getMode(chest);
                    if (mode == JetpackMode.NORMAL) {
                        return mc.currentScreen == null && mc.gameSettings.keyBindJump.isKeyDown();
                    } else if (mode == JetpackMode.HOVER) {
                        boolean ascending = mc.gameSettings.keyBindJump.isKeyDown();
                        boolean descending = mc.gameSettings.keyBindSneak.isKeyDown();
                        //if ((!ascending && !descending) || (ascending && descending) || mc.currentScreen != null || (descending && mc.currentScreen == null))
                        //Simplifies to
                        if (!ascending || descending || mc.currentScreen != null) {
                            return !CommonPlayerTickHandler.isOnGround(player);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isGasMaskOn(PlayerEntity player) {
        if (player != mc.player) {
            return Mekanism.playerState.isGasmaskOn(player);
        }
        return CommonPlayerTickHandler.isGasMaskOn(player);
    }

    public static boolean isFreeRunnerOn(PlayerEntity player) {
        if (player != mc.player) {
            return Mekanism.freeRunnerOn.contains(player.getUniqueID());
        }

        ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
        if (!stack.isEmpty() && stack.getItem() instanceof ItemFreeRunners) {
            ItemFreeRunners freeRunners = (ItemFreeRunners) stack.getItem();
            /*freeRunners.getEnergy(stack) > 0 && */
            return freeRunners.getMode(stack) == ItemFreeRunners.FreeRunnerMode.NORMAL;
        }
        return false;
    }

    public static boolean isFlamethrowerOn(PlayerEntity player) {
        if (player != mc.player) {
            return Mekanism.playerState.isFlamethrowerOn(player);
        }
        return hasFlamethrower(player) && mc.gameSettings.keyBindUseItem.isKeyDown();
    }

    public static boolean hasFlamethrower(PlayerEntity player) {
        ItemStack currentItem = player.inventory.getCurrentItem();
        if (!currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower) {
            return ((ItemFlamethrower) currentItem.getItem()).getGas(currentItem) != null;
        }
        return false;
    }

    public static void portableTeleport(PlayerEntity player, Hand hand, Frequency freq) {
        int delay = MekanismConfig.current().general.portableTeleporterDelay.val();
        if (delay == 0) {
            Mekanism.packetHandler.sendToServer(new PortableTeleporterMessage(PortableTeleporterPacketType.TELEPORT, hand, freq));
        } else {
            portableTeleports.put(player, new TeleportData(hand, freq, mc.world.getWorldTime() + delay));
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

        if (!Mekanism.proxy.isPaused()) {
            for (Iterator<IClientTicker> iter = tickingSet.iterator(); iter.hasNext(); ) {
                IClientTicker ticker = iter.next();

                if (ticker.needsTicks()) {
                    ticker.clientTick();
                } else {
                    iter.remove();
                }
            }
        }

        if (mc.world != null) {
            shouldReset = true;
        } else if (shouldReset) {
            MekanismClient.reset();
            shouldReset = false;
        }

        if (mc.world != null && mc.player != null && !Mekanism.proxy.isPaused()) {
            if (!initHoliday || MekanismClient.ticksPassed % 1200 == 0) {
                HolidayManager.check();
                initHoliday = true;
            }

            UUID playerUUID = mc.player.getUniqueID();
            boolean freeRunnerOn = isFreeRunnerOn(mc.player);
            if (Mekanism.freeRunnerOn.contains(playerUUID) != freeRunnerOn) {
                if (freeRunnerOn && mc.currentScreen == null) {
                    Mekanism.freeRunnerOn.add(playerUUID);
                } else {
                    Mekanism.freeRunnerOn.remove(playerUUID);
                }
                Mekanism.packetHandler.sendToServer(new PacketFreeRunnerData.FreeRunnerDataMessage(PacketFreeRunnerData.FreeRunnerPacket.UPDATE, playerUUID, freeRunnerOn));
            }

            ItemStack bootStack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            if (!bootStack.isEmpty() && bootStack.getItem() instanceof ItemFreeRunners && freeRunnerOn && !mc.player.isSneaking()) {
                mc.player.stepHeight = 1.002F;
            } else if (mc.player.stepHeight == 1.002F) {
                mc.player.stepHeight = 0.6F;
            }

            // Update player's state for various items; this also automatically notifies server if something changed and
            // kicks off sounds as necessary
            Mekanism.playerState.setJetpackState(playerUUID, isJetpackActive(mc.player), true);
            Mekanism.playerState.setGasmaskState(playerUUID, isGasMaskOn(mc.player), true);
            Mekanism.playerState.setFlamethrowerState(playerUUID, hasFlamethrower(mc.player), isFlamethrowerOn(mc.player), true);

            for (Iterator<Entry<PlayerEntity, TeleportData>> iter = portableTeleports.entrySet().iterator(); iter.hasNext(); ) {
                Entry<PlayerEntity, TeleportData> entry = iter.next();
                PlayerEntity player = entry.getKey();
                for (int i = 0; i < 100; i++) {
                    double x = player.posX + rand.nextDouble() - 0.5D;
                    double y = player.posY + rand.nextDouble() * 2 - 2D;
                    double z = player.posZ + rand.nextDouble() - 0.5D;
                    mc.world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, 0, 1, 0);
                }

                if (mc.world.getWorldTime() == entry.getValue().teleportTime) {
                    Mekanism.packetHandler.sendToServer(new PortableTeleporterMessage(PortableTeleporterPacketType.TELEPORT, entry.getValue().hand, entry.getValue().freq));
                    iter.remove();
                }
            }

            ItemStack chestStack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (!chestStack.isEmpty() && chestStack.getItem() instanceof ItemJetpack) {
                MekanismClient.updateKey(mc.gameSettings.keyBindJump, KeySync.ASCEND);
                MekanismClient.updateKey(mc.gameSettings.keyBindSneak, KeySync.DESCEND);
            }

            if (!mc.player.isCreative() && !mc.player.isSpectator()) {
                if (isFlamethrowerOn(mc.player)) {
                    ItemFlamethrower flamethrower = (ItemFlamethrower) mc.player.inventory.getCurrentItem().getItem();
                    flamethrower.useGas(mc.player.inventory.getCurrentItem());
                }
            }

            if (isJetpackActive(mc.player)) {
                ItemJetpack jetpack = (ItemJetpack) chestStack.getItem();
                JetpackMode mode = jetpack.getMode(chestStack);
                if (mode == JetpackMode.NORMAL) {
                    mc.player.motionY = Math.min(mc.player.motionY + 0.15D, 0.5D);
                    mc.player.fallDistance = 0.0F;
                } else if (mode == JetpackMode.HOVER) {
                    boolean ascending = mc.gameSettings.keyBindJump.isKeyDown();
                    boolean descending = mc.gameSettings.keyBindSneak.isKeyDown();
                    if ((!ascending && !descending) || (ascending && descending) || mc.currentScreen != null) {
                        if (mc.player.motionY > 0) {
                            mc.player.motionY = Math.max(mc.player.motionY - 0.15D, 0);
                        } else if (mc.player.motionY < 0) {
                            if (!CommonPlayerTickHandler.isOnGround(mc.player)) {
                                mc.player.motionY = Math.min(mc.player.motionY + 0.15D, 0);
                            }
                        }
                    } else if (ascending) {
                        mc.player.motionY = Math.min(mc.player.motionY + 0.15D, 0.2D);
                    } else if (!CommonPlayerTickHandler.isOnGround(mc.player)) {
                        mc.player.motionY = Math.max(mc.player.motionY - 0.15D, -0.2D);
                    }
                    mc.player.fallDistance = 0.0F;
                }
                jetpack.useGas(chestStack);
            }

            if (isGasMaskOn(mc.player)) {
                ItemScubaTank tank = (ItemScubaTank) chestStack.getItem();
                final int max = 300;
                tank.useGas(chestStack);
                GasStack received = tank.useGas(chestStack, max - mc.player.getAir());

                if (received != null) {
                    mc.player.setAir(mc.player.getAir() + received.amount);
                }
                if (mc.player.getAir() == max) {
                    for (PotionEffect effect : mc.player.getActivePotionEffects()) {
                        for (int i = 0; i < 9; i++) {
                            effect.onUpdate(mc.player);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        if (MekanismConfig.current().client.allowConfiguratorModeScroll.val() && mc.player != null && mc.player.isSneaking()) {
            ItemStack stack = mc.player.getHeldItemMainhand();
            int delta = event.getDwheel();

            if (stack.getItem() instanceof ItemConfigurator && delta != 0) {
                ItemConfigurator configurator = (ItemConfigurator) stack.getItem();
                RenderTickHandler.modeSwitchTimer = 100;

                wheelStatus += event.getDwheel();
                int scaledDelta = wheelStatus / 120;
                wheelStatus = wheelStatus % 120;
                int newVal = configurator.getState(stack).ordinal() + (scaledDelta % ConfiguratorMode.values().length);

                if (newVal > 0) {
                    newVal = newVal % ConfiguratorMode.values().length;
                } else if (newVal < 0) {
                    newVal = ConfiguratorMode.values().length + newVal;
                }
                configurator.setState(stack, ConfiguratorMode.values()[newVal]);
                Mekanism.packetHandler.sendToServer(new ItemStackMessage(Hand.MAIN_HAND, Collections.singletonList(newVal)));
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