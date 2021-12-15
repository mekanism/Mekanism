package mekanism.client.key;

import mekanism.client.ClientRegistrationUtil;
import mekanism.client.MekanismClient;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.KeySync;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.network.to_server.PacketModeChange;
import mekanism.common.network.to_server.PacketOpenGui;
import mekanism.common.network.to_server.PacketOpenGui.GuiType;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class MekanismKeyHandler {

    public static final KeyBinding handModeSwitchKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_HAND_MODE).conflictInGame().keyCode(GLFW.GLFW_KEY_N)
          .onKeyDown((kb, isRepeat) -> {
              PlayerEntity player = Minecraft.getInstance().player;
              if (player != null) {
                  if (IModeItem.isModeItem(player, EquipmentSlotType.MAINHAND, false)) {
                      Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.MAINHAND, player.isShiftKeyDown()));
                  } else if (!IModeItem.isModeItem(player, EquipmentSlotType.MAINHAND) && IModeItem.isModeItem(player, EquipmentSlotType.OFFHAND)) {
                      //Otherwise, try their offhand
                      Mekanism.packetHandler.sendToServer(new PacketModeChange(EquipmentSlotType.OFFHAND, player.isShiftKeyDown()));
                  }
              }
          }).build();
    public static final KeyBinding headModeSwitchKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_HEAD_MODE).conflictInGame().keyCode(GLFW.GLFW_KEY_V)
          .onKeyDown((kb, isRepeat) -> handlePotentialModeItem(EquipmentSlotType.HEAD)).build();
    public static final KeyBinding chestModeSwitchKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_CHEST_MODE).conflictInGame().keyCode(GLFW.GLFW_KEY_G)
          .onKeyDown((kb, isRepeat) -> handlePotentialModeItem(EquipmentSlotType.CHEST)).build();
    public static final KeyBinding legsModeSwitchKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_LEGS_MODE).conflictInGame().keyCode(GLFW.GLFW_KEY_J)
          .onKeyDown((kb, isRepeat) -> handlePotentialModeItem(EquipmentSlotType.LEGS)).build();
    public static final KeyBinding feetModeSwitchKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_FEET_MODE).conflictInGame().keyCode(GLFW.GLFW_KEY_B)
          .onKeyDown((kb, isRepeat) -> handlePotentialModeItem(EquipmentSlotType.FEET)).build();
    public static final KeyBinding detailsKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_DETAILS_MODE).conflictInGui().keyCode(GLFW.GLFW_KEY_LEFT_SHIFT).build();
    public static final KeyBinding descriptionKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_DESCRIPTION_MODE).conflictInGui().modifier(KeyModifier.SHIFT)
          .keyCode(GLFW.GLFW_KEY_N).build();
    public static final KeyBinding moduleTweakerKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_MODULE_TWEAKER).conflictInGame().keyCode(GLFW.GLFW_KEY_BACKSLASH)
          .onKeyDown((kb, isRepeat) -> {
              PlayerEntity player = Minecraft.getInstance().player;
              if (player != null && ModuleTweakerContainer.hasTweakableItem(player)) {
                  Mekanism.packetHandler.sendToServer(new PacketOpenGui(GuiType.MODULE_TWEAKER));
              }
          }).build();
    public static final KeyBinding boostKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_BOOST).conflictInGame().keyCode(GLFW.GLFW_KEY_LEFT_CONTROL)
          .onKeyDown((kb, isRepeat) -> MekanismClient.updateKey(kb, KeySync.BOOST)).onKeyUp(kb -> MekanismClient.updateKey(kb, KeySync.BOOST)).build();
    public static final KeyBinding hudKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_HUD).conflictInGame().keyCode(GLFW.GLFW_KEY_H)
          .onKeyDown((kb, isRepeat) -> MekanismClient.renderHUD = !MekanismClient.renderHUD).build();

    public static void registerKeybindings() {
        ClientRegistrationUtil.registerKeyBindings(handModeSwitchKey, headModeSwitchKey, chestModeSwitchKey, legsModeSwitchKey, feetModeSwitchKey,
              detailsKey, descriptionKey, moduleTweakerKey, boostKey, hudKey);
    }

    private static void handlePotentialModeItem(EquipmentSlotType slot) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null && IModeItem.isModeItem(player, slot)) {
            Mekanism.packetHandler.sendToServer(new PacketModeChange(slot, player.isShiftKeyDown()));
            SoundHandler.playSound(MekanismSounds.HYDRAULIC);
        }
    }
}