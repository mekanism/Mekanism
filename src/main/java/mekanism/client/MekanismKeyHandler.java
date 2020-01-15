package mekanism.client;

import java.util.Collections;
import mekanism.api.text.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
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
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class MekanismKeyHandler extends MekKeyHandler {

    public static final String keybindCategory = Mekanism.MOD_NAME;
    public static KeyBinding modeSwitchKey = new KeyBinding(MekanismLang.KEY_MODE.getTranslationKey(), GLFW.GLFW_KEY_N, keybindCategory);
    public static KeyBinding armorModeSwitchKey = new KeyBinding(MekanismLang.KEY_ARMOR_MODE.getTranslationKey(), GLFW.GLFW_KEY_G, keybindCategory);
    public static KeyBinding freeRunnerModeSwitchKey = new KeyBinding(MekanismLang.KEY_FEET_MODE.getTranslationKey(), GLFW.GLFW_KEY_H, keybindCategory);

    public static KeyBinding sneakKey = Minecraft.getInstance().gameSettings.field_228046_af_;

    private static Builder BINDINGS = new Builder()
          .addBinding(modeSwitchKey, false)
          .addBinding(armorModeSwitchKey, false)
          .addBinding(freeRunnerModeSwitchKey, false);

    public MekanismKeyHandler() {
        super(BINDINGS);

        ClientRegistry.registerKeyBinding(modeSwitchKey);
        ClientRegistry.registerKeyBinding(armorModeSwitchKey);
        ClientRegistry.registerKeyBinding(freeRunnerModeSwitchKey);

        MinecraftForge.EVENT_BUS.addListener(this::onTick);
    }

    private void onTick(InputEvent.KeyInputEvent event) {
        keyTick();
    }

    @Override
    public void keyDown(KeyBinding kb, boolean isRepeat) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;
        if (kb == modeSwitchKey) {
            if (player.func_225608_bj_()) {
                ItemStack toolStack = player.inventory.getCurrentItem();
                Item item = toolStack.getItem();
                if (item instanceof ItemConfigurator) {
                    ItemConfigurator configurator = (ItemConfigurator) item;
                    ConfiguratorMode configuratorMode = configurator.getState(toolStack).getNext();
                    configurator.setState(toolStack, configuratorMode);
                    Mekanism.packetHandler.sendToServer(new PacketItemStack(Hand.MAIN_HAND, Collections.singletonList(configuratorMode)));
                    player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                          MekanismLang.CONFIGURE_STATE.translateColored(EnumColor.GRAY, configuratorMode)));
                } else if (item instanceof ItemElectricBow) {
                    ItemElectricBow bow = (ItemElectricBow) item;
                    boolean newBowState = !bow.getFireState(toolStack);
                    bow.setFireState(toolStack, newBowState);
                    Mekanism.packetHandler.sendToServer(new PacketItemStack(Hand.MAIN_HAND, Collections.singletonList(newBowState)));
                    player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                          MekanismLang.FIRE_MODE.translateColored(EnumColor.GRAY, OnOff.of(newBowState, true))));
                } else if (item instanceof ItemBlockFluidTank) {
                    ItemBlockFluidTank fluidTank = (ItemBlockFluidTank) item;
                    boolean newBucketMode = !fluidTank.getBucketMode(toolStack);
                    fluidTank.setBucketMode(toolStack, newBucketMode);
                    Mekanism.packetHandler.sendToServer(new PacketItemStack(Hand.MAIN_HAND, Collections.singletonList(fluidTank.getBucketMode(toolStack))));
                    player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                          MekanismLang.BUCKET_MODE.translateColored(EnumColor.GRAY, OnOff.of(newBucketMode, true))));
                } else if (item instanceof ItemFlamethrower) {
                    ItemFlamethrower flamethrower = (ItemFlamethrower) item;
                    flamethrower.incrementMode(toolStack);
                    Mekanism.packetHandler.sendToServer(PacketFlamethrowerData.MODE_CHANGE(Hand.MAIN_HAND));
                    player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                          MekanismLang.FLAMETHROWER_MODE_BUMP.translateColored(EnumColor.GRAY, flamethrower.getMode(toolStack))));
                }
            }
        } else if (kb == armorModeSwitchKey) {
            ItemStack chestStack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
            Item chestItem = chestStack.getItem();

            if (chestItem instanceof ItemJetpack) {
                ItemJetpack jetpack = (ItemJetpack) chestItem;
                if (player.func_225608_bj_()) {
                    jetpack.setMode(chestStack, JetpackMode.DISABLED);
                } else {
                    jetpack.incrementMode(chestStack);
                }

                Mekanism.packetHandler.sendToServer(PacketJetpackData.MODE_CHANGE(player.func_225608_bj_()));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC.getSoundEvent());
            } else if (chestItem instanceof ItemScubaTank) {
                ItemScubaTank scubaTank = (ItemScubaTank) chestItem;
                scubaTank.toggleFlowing(chestStack);
                Mekanism.packetHandler.sendToServer(PacketScubaTankData.MODE_CHANGE(false));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC.getSoundEvent());
            }
        } else if (kb == freeRunnerModeSwitchKey) {
            ItemStack feetStack = player.getItemStackFromSlot(EquipmentSlotType.FEET);
            Item feetItem = feetStack.getItem();

            if (feetItem instanceof ItemFreeRunners) {
                ItemFreeRunners freeRunners = (ItemFreeRunners) feetItem;
                if (player.func_225608_bj_()) {
                    freeRunners.setMode(feetStack, ItemFreeRunners.FreeRunnerMode.DISABLED);
                } else {
                    freeRunners.incrementMode(feetStack);
                }
                Mekanism.packetHandler.sendToServer(new PacketFreeRunnerData(PacketFreeRunnerData.FreeRunnerPacket.MODE, null, player.func_225608_bj_()));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC.getSoundEvent());
            }
        }
    }

    @Override
    public void keyUp(KeyBinding kb) {
    }
}