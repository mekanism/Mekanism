package mekanism.client;

import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.item.IModeItem;
import mekanism.common.network.PacketModeChange;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class MekanismKeyHandler extends MekKeyHandler {

    public static KeyBinding modeSwitchKey = new KeyBinding(MekanismLang.KEY_MODE.getTranslationKey(), KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM,
          GLFW.GLFW_KEY_N, MekanismLang.MEKANISM.getTranslationKey());
    public static KeyBinding armorModeSwitchKey = new KeyBinding(MekanismLang.KEY_ARMOR_MODE.getTranslationKey(), KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM,
          GLFW.GLFW_KEY_G, MekanismLang.MEKANISM.getTranslationKey());
    public static KeyBinding freeRunnerModeSwitchKey = new KeyBinding(MekanismLang.KEY_FEET_MODE.getTranslationKey(), KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM,
          GLFW.GLFW_KEY_H, MekanismLang.MEKANISM.getTranslationKey());
    public static KeyBinding detailsKey = new KeyBinding(MekanismLang.KEY_DETAILS_MODE.getTranslationKey(), KeyConflictContext.GUI, InputMappings.Type.KEYSYM,
          GLFW.GLFW_KEY_LEFT_SHIFT, MekanismLang.MEKANISM.getTranslationKey());
    public static KeyBinding descriptionKey = new KeyBinding(MekanismLang.KEY_DESCRIPTION_MODE.getTranslationKey(), KeyConflictContext.GUI,
          KeyModifier.SHIFT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_N, MekanismLang.MEKANISM.getTranslationKey());

    private static Builder BINDINGS = new Builder(5)
          .addBinding(modeSwitchKey, false)
          .addBinding(armorModeSwitchKey, false)
          .addBinding(freeRunnerModeSwitchKey, false)
          .addBinding(detailsKey, false)
          .addBinding(descriptionKey, false);

    public MekanismKeyHandler() {
        super(BINDINGS);
        ClientRegistry.registerKeyBinding(modeSwitchKey);
        ClientRegistry.registerKeyBinding(armorModeSwitchKey);
        ClientRegistry.registerKeyBinding(freeRunnerModeSwitchKey);
        ClientRegistry.registerKeyBinding(detailsKey);
        ClientRegistry.registerKeyBinding(descriptionKey);
        MinecraftForge.EVENT_BUS.addListener(this::onTick);
    }

    private void onTick(InputEvent.KeyInputEvent event) {
        keyTick();
    }

    @Override
    public void keyDown(KeyBinding kb, boolean isRepeat) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (kb == modeSwitchKey) {
            if (IModeItem.isModeItem(player, EquipmentSlotType.MAINHAND)) {
                Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.MAINHAND, player.isSneaking()));
            } else if (IModeItem.isModeItem(player, EquipmentSlotType.OFFHAND)) {
                //Otherwise try their offhand
                Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.OFFHAND, player.isSneaking()));
            }
        } else if (kb == armorModeSwitchKey) {
            if (IModeItem.isModeItem(player, EquipmentSlotType.CHEST)) {
                Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.CHEST, player.isSneaking()));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC.getSoundEvent());
            }
        } else if (kb == freeRunnerModeSwitchKey) {
            if (IModeItem.isModeItem(player, EquipmentSlotType.FEET)) {
                Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.FEET, player.isSneaking()));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC.getSoundEvent());
            }
        }
    }

    @Override
    public void keyUp(KeyBinding kb) {
    }
}