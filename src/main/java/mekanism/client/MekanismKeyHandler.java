package mekanism.client;

import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.KeySync;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.network.PacketModeChange;
import mekanism.common.network.PacketOpenGui;
import mekanism.common.network.PacketOpenGui.GuiType;
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

    public static final KeyBinding handModeSwitchKey = new KeyBinding(MekanismLang.KEY_HAND_MODE.getTranslationKey(), KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM,
          GLFW.GLFW_KEY_N, MekanismLang.MEKANISM.getTranslationKey());
    public static final KeyBinding headModeSwitchKey = new KeyBinding(MekanismLang.KEY_HEAD_MODE.getTranslationKey(), KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM,
          GLFW.GLFW_KEY_V, MekanismLang.MEKANISM.getTranslationKey());
    public static final KeyBinding chestModeSwitchKey = new KeyBinding(MekanismLang.KEY_CHEST_MODE.getTranslationKey(), KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM,
          GLFW.GLFW_KEY_G, MekanismLang.MEKANISM.getTranslationKey());
    public static final KeyBinding feetModeSwitchKey = new KeyBinding(MekanismLang.KEY_FEET_MODE.getTranslationKey(), KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM,
          GLFW.GLFW_KEY_B, MekanismLang.MEKANISM.getTranslationKey());
    public static final KeyBinding detailsKey = new KeyBinding(MekanismLang.KEY_DETAILS_MODE.getTranslationKey(), KeyConflictContext.GUI, InputMappings.Type.KEYSYM,
          GLFW.GLFW_KEY_LEFT_SHIFT, MekanismLang.MEKANISM.getTranslationKey());
    public static final KeyBinding descriptionKey = new KeyBinding(MekanismLang.KEY_DESCRIPTION_MODE.getTranslationKey(), KeyConflictContext.GUI,
          KeyModifier.SHIFT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_N, MekanismLang.MEKANISM.getTranslationKey());
    public static final KeyBinding moduleTweakerKey = new KeyBinding(MekanismLang.KEY_MODULE_TWEAKER.getTranslationKey(), KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM,
          GLFW.GLFW_KEY_BACKSLASH, MekanismLang.MEKANISM.getTranslationKey());
    public static final KeyBinding boostKey = new KeyBinding(MekanismLang.KEY_BOOST.getTranslationKey(), KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM,
          GLFW.GLFW_KEY_LEFT_CONTROL, MekanismLang.MEKANISM.getTranslationKey());
    public static final KeyBinding hudKey = new KeyBinding(MekanismLang.KEY_HUD.getTranslationKey(), KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM,
          GLFW.GLFW_KEY_H, MekanismLang.MEKANISM.getTranslationKey());

    private static final Builder BINDINGS = new Builder(9)
          .addBinding(handModeSwitchKey, false)
          .addBinding(headModeSwitchKey, false)
          .addBinding(chestModeSwitchKey, false)
          .addBinding(feetModeSwitchKey, false)
          .addBinding(detailsKey, false)
          .addBinding(descriptionKey, false)
          .addBinding(moduleTweakerKey, false)
          .addBinding(boostKey, false)
          .addBinding(hudKey, false);

    public MekanismKeyHandler() {
        super(BINDINGS);
        ClientRegistry.registerKeyBinding(handModeSwitchKey);
        ClientRegistry.registerKeyBinding(headModeSwitchKey);
        ClientRegistry.registerKeyBinding(chestModeSwitchKey);
        ClientRegistry.registerKeyBinding(feetModeSwitchKey);
        ClientRegistry.registerKeyBinding(detailsKey);
        ClientRegistry.registerKeyBinding(descriptionKey);
        ClientRegistry.registerKeyBinding(moduleTweakerKey);
        ClientRegistry.registerKeyBinding(boostKey);
        ClientRegistry.registerKeyBinding(hudKey);
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
        if (kb == handModeSwitchKey) {
            if (IModeItem.isModeItem(player, EquipmentSlotType.MAINHAND, false)) {
                Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.MAINHAND, player.isSneaking()));
            } else if (!IModeItem.isModeItem(player, EquipmentSlotType.MAINHAND) && IModeItem.isModeItem(player, EquipmentSlotType.OFFHAND)) {
                //Otherwise try their offhand
                Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.OFFHAND, player.isSneaking()));
            }
        } else if (kb == headModeSwitchKey) {
            if (IModeItem.isModeItem(player, EquipmentSlotType.HEAD)) {
                Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.HEAD, player.isSneaking()));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC.getSoundEvent());
            }
        } else if (kb == chestModeSwitchKey) {
            if (IModeItem.isModeItem(player, EquipmentSlotType.CHEST)) {
                Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.CHEST, player.isSneaking()));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC.getSoundEvent());
            }
        } else if (kb == feetModeSwitchKey) {
            if (IModeItem.isModeItem(player, EquipmentSlotType.FEET)) {
                Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.FEET, player.isSneaking()));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC.getSoundEvent());
            }
        } else if (kb == moduleTweakerKey) {
            if (ModuleTweakerContainer.hasTweakableItem(player)) {
                Mekanism.packetHandler.sendToServer(new PacketOpenGui(GuiType.MODULE_TWEAKER));
            }
        } else if (kb == boostKey) {
            MekanismClient.updateKey(kb, KeySync.BOOST);
        } else if (kb == hudKey) {
            MekanismClient.renderHUD = !MekanismClient.renderHUD;
        }
    }

    @Override
    public void keyUp(KeyBinding kb) {
        if (kb == boostKey) {
            MekanismClient.updateKey(kb, KeySync.BOOST);
        }
    }
}