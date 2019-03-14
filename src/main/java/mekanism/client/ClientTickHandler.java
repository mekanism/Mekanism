package mekanism.client;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import mekanism.api.IClientTicker;
import mekanism.api.gas.GasStack;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.KeySync;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig.client;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.frequency.Frequency;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketItemStack.ItemStackMessage;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterPacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
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
    public static Map<EntityPlayer, TeleportData> portableTeleports = new HashMap<>();
    public static int wheelStatus = 0;
    public boolean initHoliday = false;
    public boolean shouldReset = false;

    public static void killDeadNetworks() {
        tickingSet.removeIf(iClientTicker -> !iClientTicker.needsTicks());
    }

    public static boolean isJetpackActive(EntityPlayer player) {
        if (player != mc.player) {
            return Mekanism.playerState.isJetpackOn(player);
        }

        ItemStack stack = player.inventory.armorInventory.get(2);

        if (!stack.isEmpty() && !(player.isCreative() || player.isSpectator())) {
            if (stack.getItem() instanceof ItemJetpack) {
                ItemJetpack jetpack = (ItemJetpack) stack.getItem();

                if (jetpack.getGas(stack) != null) {
                    if ((mc.gameSettings.keyBindJump.isKeyDown() && jetpack.getMode(stack) == JetpackMode.NORMAL)
                          && mc.currentScreen == null) {
                        return true;
                    } else if (jetpack.getMode(stack) == JetpackMode.HOVER) {
                        if ((!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) || (
                              mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown())
                              || mc.currentScreen != null) {
                            return !CommonPlayerTickHandler.isOnGround(player);
                        } else if (mc.gameSettings.keyBindSneak.isKeyDown() && mc.currentScreen == null) {
                            return !CommonPlayerTickHandler.isOnGround(player);
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isGasMaskOn(EntityPlayer player) {
        if (player != mc.player) {
            return Mekanism.playerState.isGasmaskOn(player);
        }

        ItemStack tank = player.inventory.armorInventory.get(2);
        ItemStack mask = player.inventory.armorInventory.get(3);

        if (!tank.isEmpty() && !mask.isEmpty()) {
            if (tank.getItem() instanceof ItemScubaTank && mask.getItem() instanceof ItemGasMask) {
                ItemScubaTank scubaTank = (ItemScubaTank) tank.getItem();

                if (scubaTank.getGas(tank) != null) {
                    return scubaTank.getFlowing(tank);
                }
            }
        }

        return false;
    }

    public static boolean isFreeRunnerOn(EntityPlayer player) {
        if (player != mc.player) {
            return Mekanism.freeRunnerOn.contains(player.getName());
        }

        ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);

        if (!stack.isEmpty() && stack.getItem() instanceof ItemFreeRunners) {
            ItemFreeRunners freeRunners = (ItemFreeRunners) stack.getItem();

            /*freeRunners.getEnergy(stack) > 0 && */
            return freeRunners.getMode(stack)
                  == ItemFreeRunners.FreeRunnerMode.NORMAL;
        }

        return false;
    }

    public static boolean isFlamethrowerOn(EntityPlayer player) {
        if (player != mc.player) {
            return Mekanism.playerState.isFlamethrowerOn(player);
        }

        if (hasFlamethrower(player)) {
            return mc.gameSettings.keyBindUseItem.isKeyDown();
        }

        return false;
    }

    public static boolean hasFlamethrower(EntityPlayer player) {
        if (!player.inventory.getCurrentItem().isEmpty() && player.inventory.getCurrentItem()
              .getItem() instanceof ItemFlamethrower) {
            ItemFlamethrower flamethrower = (ItemFlamethrower) player.inventory.getCurrentItem().getItem();

            return flamethrower.getGas(player.inventory.getCurrentItem()) != null;
        }

        return false;
    }

    public static void portableTeleport(EntityPlayer player, EnumHand hand, Frequency freq) {
        if (general.portableTeleporterDelay == 0) {
            Mekanism.packetHandler
                  .sendToServer(new PortableTeleporterMessage(PortableTeleporterPacketType.TELEPORT, hand, freq));
        } else {
            portableTeleports
                  .put(player, new TeleportData(hand, freq, mc.world.getWorldTime() + general.portableTeleporterDelay));
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
            if ((!initHoliday || MekanismClient.ticksPassed % 1200 == 0) && mc.player != null) {
                HolidayManager.check();
                initHoliday = true;
            }

            if (Mekanism.freeRunnerOn.contains(mc.player.getName()) != isFreeRunnerOn(mc.player)) {
                if (isFreeRunnerOn(mc.player) && mc.currentScreen == null) {
                    Mekanism.freeRunnerOn.add(mc.player.getName());
                } else {
                    Mekanism.freeRunnerOn.remove(mc.player.getName());
                }

                Mekanism.packetHandler.sendToServer(
                      new PacketFreeRunnerData.FreeRunnerDataMessage(PacketFreeRunnerData.FreeRunnerPacket.UPDATE,
                            mc.player.getName(), isFreeRunnerOn(mc.player)));
            }

            ItemStack bootStack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.FEET);

            if (!bootStack.isEmpty() && bootStack.getItem() instanceof ItemFreeRunners && isFreeRunnerOn(mc.player)) {
                mc.player.stepHeight = 1.002F;
            } else {
                if (mc.player.stepHeight == 1.002F) {
                    mc.player.stepHeight = 0.6F;
                }
            }

            // Update player's state for various items; this also automatically notifies server if something changed and
            // kicks off sounds as necessary
            Mekanism.playerState.setJetpackState(mc.player.getName(), isJetpackActive(mc.player), true);
            Mekanism.playerState.setGasmaskState(mc.player.getName(), isGasMaskOn(mc.player), true);
            Mekanism.playerState.setFlamethrowerState(mc.player.getName(), isFlamethrowerOn(mc.player), true);

            for (Iterator<Entry<EntityPlayer, TeleportData>> iter = portableTeleports.entrySet().iterator();
                  iter.hasNext(); ) {
                Entry<EntityPlayer, TeleportData> entry = iter.next();

                for (int i = 0; i < 100; i++) {
                    double x = entry.getKey().posX + rand.nextDouble() - 0.5D;
                    double y = entry.getKey().posY + rand.nextDouble() * 2 - 2D;
                    double z = entry.getKey().posZ + rand.nextDouble() - 0.5D;

                    mc.world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, 0, 1, 0);
                }

                if (mc.world.getWorldTime() == entry.getValue().teleportTime) {
                    Mekanism.packetHandler.sendToServer(
                          new PortableTeleporterMessage(PortableTeleporterPacketType.TELEPORT, entry.getValue().hand,
                                entry.getValue().freq));
                    iter.remove();
                }
            }

            ItemStack chestStack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (!chestStack.isEmpty() && chestStack.getItem() instanceof ItemJetpack) {
                MekanismClient.updateKey(mc.gameSettings.keyBindJump, KeySync.ASCEND);
                MekanismClient.updateKey(mc.gameSettings.keyBindSneak, KeySync.DESCEND);
            }

            if (isFlamethrowerOn(mc.player)) {
                ItemFlamethrower flamethrower = (ItemFlamethrower) mc.player.inventory.getCurrentItem().getItem();

                if (!(mc.player.isCreative() || mc.player.isSpectator())) {
                    flamethrower.useGas(mc.player.inventory.getCurrentItem());
                }
            }

            if (isJetpackActive(mc.player)) {
                ItemJetpack jetpack = (ItemJetpack) chestStack.getItem();

                if (jetpack.getMode(chestStack) == JetpackMode.NORMAL) {
                    mc.player.motionY = Math.min(mc.player.motionY + 0.15D, 0.5D);
                    mc.player.fallDistance = 0.0F;
                } else if (jetpack.getMode(chestStack) == JetpackMode.HOVER) {
                    if ((!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) || (
                          mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown())
                          || mc.currentScreen != null) {
                        if (mc.player.motionY > 0) {
                            mc.player.motionY = Math.max(mc.player.motionY - 0.15D, 0);
                        } else if (mc.player.motionY < 0) {
                            if (!CommonPlayerTickHandler.isOnGround(mc.player)) {
                                mc.player.motionY = Math.min(mc.player.motionY + 0.15D, 0);
                            }
                        }
                    } else {
                        if (mc.gameSettings.keyBindJump.isKeyDown() && mc.currentScreen == null) {
                            mc.player.motionY = Math.min(mc.player.motionY + 0.15D, 0.2D);
                        } else if (mc.gameSettings.keyBindSneak.isKeyDown() && mc.currentScreen == null) {
                            if (!CommonPlayerTickHandler.isOnGround(mc.player)) {
                                mc.player.motionY = Math.max(mc.player.motionY - 0.15D, -0.2D);
                            }
                        }
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
                    for (Object obj : mc.player.getActivePotionEffects()) {
                        if (obj instanceof PotionEffect) {
                            for (int i = 0; i < 9; i++) {
                                ((PotionEffect) obj).onUpdate(mc.player);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        if (client.allowConfiguratorModeScroll && mc.player != null && mc.player.isSneaking()) {
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
                Mekanism.packetHandler
                      .sendToServer(new ItemStackMessage(EnumHand.MAIN_HAND, Lists.newArrayList(newVal)));
                event.setCanceled(true);
            }
        }
    }

    private static class TeleportData {

        private EnumHand hand;
        private Frequency freq;
        private long teleportTime;

        public TeleportData(EnumHand h, Frequency f, long t) {
            hand = h;
            freq = f;
            teleportTime = t;
        }
    }
}
