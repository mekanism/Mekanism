package mekanism.client;

import java.util.Collections;
import mekanism.api.text.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismSounds;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.item.gear.ItemElectricBow;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemFreeRunners;
import mekanism.common.item.gear.ItemJetpack;
import mekanism.common.item.gear.ItemJetpack.JetpackMode;
import mekanism.common.item.gear.ItemScubaTank;
import mekanism.common.network.PacketFlamethrowerData;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketItemStack;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class MekanismKeyHandler extends MekKeyHandler {

    public static final String keybindCategory = Mekanism.MOD_NAME;
    public static KeyBinding modeSwitchKey = new KeyBinding("mekanism.key.mode", GLFW.GLFW_KEY_N, keybindCategory);
    public static KeyBinding armorModeSwitchKey = new KeyBinding("mekanism.key.armorMode", GLFW.GLFW_KEY_G, keybindCategory);
    public static KeyBinding freeRunnerModeSwitchKey = new KeyBinding("mekanism.key.feetMode", GLFW.GLFW_KEY_H, keybindCategory);
    public static KeyBinding voiceKey = new KeyBinding("mekanism.key.voice", GLFW.GLFW_KEY_U, keybindCategory);

    public static KeyBinding sneakKey = Minecraft.getInstance().gameSettings.keyBindSneak;
    public static KeyBinding jumpKey = Minecraft.getInstance().gameSettings.keyBindJump;

    private static Builder BINDINGS = new Builder()
          .addBinding(modeSwitchKey, false)
          .addBinding(armorModeSwitchKey, false)
          .addBinding(freeRunnerModeSwitchKey, false)
          .addBinding(voiceKey, true);

    public MekanismKeyHandler() {
        super(BINDINGS);

        ClientRegistry.registerKeyBinding(modeSwitchKey);
        ClientRegistry.registerKeyBinding(armorModeSwitchKey);
        ClientRegistry.registerKeyBinding(freeRunnerModeSwitchKey);
        ClientRegistry.registerKeyBinding(voiceKey);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(InputEvent.KeyInputEvent event) {
        keyTick();
    }

    @Override
    public void keyDown(KeyBinding kb, boolean isRepeat) {
        if (kb == modeSwitchKey) {
            PlayerEntity player = Minecraft.getInstance().player;
            ItemStack toolStack = player.inventory.getCurrentItem();
            Item item = toolStack.getItem();

            if (player.isSneaking() && item instanceof ItemConfigurator) {
                ItemConfigurator configurator = (ItemConfigurator) item;
                ConfiguratorMode configuratorMode = configurator.getState(toolStack);
                int toSet = (configuratorMode.ordinal() + 1) % ConfiguratorMode.values().length;
                configuratorMode = ConfiguratorMode.values()[toSet];
                configurator.setState(toolStack, configuratorMode);
                Mekanism.packetHandler.sendToServer(new PacketItemStack(Hand.MAIN_HAND, Collections.singletonList(configuratorMode)));
                player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY,
                      Translation.of("mekanism.tooltip.configureState"), configuratorMode));
            } else if (player.isSneaking() && item instanceof ItemElectricBow) {
                ItemElectricBow bow = (ItemElectricBow) item;
                boolean newBowState = !bow.getFireState(toolStack);
                bow.setFireState(toolStack, newBowState);
                Mekanism.packetHandler.sendToServer(new PacketItemStack(Hand.MAIN_HAND, Collections.singletonList(newBowState)));
                player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY,
                      Translation.of("mekanism.tooltip.fireMode"), BooleanStateDisplay.OnOff.of(newBowState, true)));
            } else if (player.isSneaking() && item instanceof ItemBlockFluidTank) {
                ItemBlockFluidTank fluidTank = (ItemBlockFluidTank) item;
                boolean newBucketMode = !fluidTank.getBucketMode(toolStack);
                fluidTank.setBucketMode(toolStack, newBucketMode);
                Mekanism.packetHandler.sendToServer(new PacketItemStack(Hand.MAIN_HAND, Collections.singletonList(fluidTank.getBucketMode(toolStack))));
                player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY,
                      Translation.of("mekanism.tooltip.portableTank.bucketMode"), BooleanStateDisplay.OnOff.of(newBucketMode, true)));
            } else if (player.isSneaking() && item instanceof ItemWalkieTalkie) {
                ItemWalkieTalkie wt = (ItemWalkieTalkie) item;
                if (wt.getOn(toolStack)) {
                    int newChan = wt.getChannel(toolStack) + 1;
                    if (newChan == 9) {
                        newChan = 1;
                    }
                    wt.setChannel(toolStack, newChan);
                    Mekanism.packetHandler.sendToServer(new PacketItemStack(Hand.MAIN_HAND, Collections.singletonList(newChan)));
                }
            } else if (player.isSneaking() && item instanceof ItemFlamethrower) {
                ItemFlamethrower flamethrower = (ItemFlamethrower) item;
                flamethrower.incrementMode(toolStack);
                Mekanism.packetHandler.sendToServer(PacketFlamethrowerData.MODE_CHANGE(Hand.MAIN_HAND));
                player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY,
                      Translation.of("mekanism.tooltip.flamethrower.modeBump"), flamethrower.getMode(toolStack).getTextComponent()));
            }
        } else if (kb == armorModeSwitchKey) {
            PlayerEntity player = Minecraft.getInstance().player;
            ItemStack chestStack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            Item chestItem = chestStack.getItem();

            if (chestItem instanceof ItemJetpack) {
                ItemJetpack jetpack = (ItemJetpack) chestItem;
                if (player.isSneaking()) {
                    jetpack.setMode(chestStack, JetpackMode.DISABLED);
                } else {
                    jetpack.incrementMode(chestStack);
                }

                Mekanism.packetHandler.sendToServer(PacketJetpackData.MODE_CHANGE(player.isSneaking()));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC);
            } else if (chestItem instanceof ItemScubaTank) {
                ItemScubaTank scubaTank = (ItemScubaTank) chestItem;
                scubaTank.toggleFlowing(chestStack);
                Mekanism.packetHandler.sendToServer(PacketScubaTankData.MODE_CHANGE(false));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC);
            }
        } else if (kb == freeRunnerModeSwitchKey) {
            PlayerEntity player = Minecraft.getInstance().player;
            ItemStack feetStack = player.getItemStackFromSlot(EquipmentSlotType.FEET);
            Item feetItem = feetStack.getItem();

            if (feetItem instanceof ItemFreeRunners) {
                ItemFreeRunners freeRunners = (ItemFreeRunners) feetItem;
                if (player.isSneaking()) {
                    freeRunners.setMode(feetStack, ItemFreeRunners.FreeRunnerMode.DISABLED);
                } else {
                    freeRunners.incrementMode(feetStack);
                }
                Mekanism.packetHandler.sendToServer(new PacketFreeRunnerData(PacketFreeRunnerData.FreeRunnerPacket.MODE, null, player.isSneaking()));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC);
            }
        }
    }

    @Override
    public void keyUp(KeyBinding kb) {
    }
}