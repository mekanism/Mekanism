package mekanism.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Optional;
import mekanism.client.ClientRegistrationUtil;
import mekanism.client.MekanismClient;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.KeySync;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.item.interfaces.IChemicalItem;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketModeChange;
import mekanism.common.network.to_server.PacketModeChangeCurios;
import mekanism.common.network.to_server.PacketOpenGui;
import mekanism.common.network.to_server.PacketOpenGui.GuiType;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;

public class MekanismKeyHandler {

    //TODO - 26.1: Re-evaluate key binding categories and also translating this
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Mekanism.rl("main"));
    public static final KeyMapping handModeSwitchKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_HAND_MODE).conflictInGame().keyCode(InputConstants.KEY_N)
          .onKeyDown((_, _) -> {
              Player player = Minecraft.getInstance().player;
              if (player != null) {
                  if (IModeItem.isModeItem(player, EquipmentSlot.MAINHAND, false)) {
                      PacketUtils.sendToServer(new PacketModeChange(EquipmentSlot.MAINHAND, player.isShiftKeyDown()));
                  } else if (!IModeItem.isModeItem(player, EquipmentSlot.MAINHAND) && IModeItem.isModeItem(player, EquipmentSlot.OFFHAND, false)) {
                      //Otherwise, try their offhand
                      PacketUtils.sendToServer(new PacketModeChange(EquipmentSlot.OFFHAND, player.isShiftKeyDown()));
                  }
              }
          }).build();
    public static final KeyMapping headModeSwitchKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_HEAD_MODE).conflictInGame().keyCode(InputConstants.KEY_V)
          .onKeyDown((_, _) -> handlePotentialModeItem(EquipmentSlot.HEAD)).build();
    public static final KeyMapping chestModeSwitchKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_CHEST_MODE).conflictInGame().keyCode(InputConstants.KEY_G)
          .onKeyDown((_, _) -> handlePotentialModeItem(EquipmentSlot.CHEST)).build();
    public static final KeyMapping legsModeSwitchKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_LEGS_MODE).conflictInGame().keyCode(InputConstants.KEY_J)
          .onKeyDown((_, _) -> handlePotentialModeItem(EquipmentSlot.LEGS)).build();
    public static final KeyMapping feetModeSwitchKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_FEET_MODE).conflictInGame().keyCode(InputConstants.KEY_B)
          .onKeyDown((_, _) -> handlePotentialModeItem(EquipmentSlot.FEET)).build();
    public static final KeyMapping detailsKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_DETAILS_MODE).conflictInGui().keyCode(InputConstants.KEY_LSHIFT).build();
    public static final KeyMapping descriptionKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_DESCRIPTION_MODE).conflictInGui().modifier(KeyModifier.SHIFT)
          .keyCode(InputConstants.KEY_N).build();
    public static final KeyMapping moduleTweakerKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_MODULE_TWEAKER).conflictInGame().keyCode(InputConstants.KEY_BACKSLASH)
          .onKeyDown((_, _) -> {
              Player player = Minecraft.getInstance().player;
              if (player != null && ModuleTweakerContainer.hasTweakableItem(player)) {
                  PacketUtils.sendToServer(new PacketOpenGui(GuiType.MODULE_TWEAKER));
              }
          }).build();
    //TODO - 26.1: Evaluate control vs command for macs
    public static final KeyMapping boostKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_BOOST).conflictInGame().keyCode(InputConstants.KEY_LCONTROL)
          .onKeyDown((kb, _) -> MekanismClient.updateKey(kb, KeySync.BOOST)).onKeyUp(kb -> MekanismClient.updateKey(kb, KeySync.BOOST)).build();
    public static final KeyMapping hudKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_HUD).conflictInGame().keyCode(InputConstants.KEY_H)
          .onKeyDown((_, _) -> {
              MekanismConfig.client.enableHUD.set(!MekanismConfig.client.enableHUD.get());
              // save the updated config info
              MekanismConfig.client.save();
          }).build();

    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        ClientRegistrationUtil.registerKeyBindings(event, handModeSwitchKey, headModeSwitchKey, chestModeSwitchKey, legsModeSwitchKey, feetModeSwitchKey,
              detailsKey, descriptionKey, moduleTweakerKey, boostKey, hudKey);
    }

    private static void handlePotentialModeItem(EquipmentSlot slot) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (IModeItem.isModeItem(player, slot)) {
                PacketUtils.sendToServer(new PacketModeChange(slot, player.isShiftKeyDown()));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC);
            } else if (Mekanism.hooks.curios.isLoaded()) {
                Optional<SlotResult> curiosResult = CuriosIntegration.findFirstCurioAsResult(player, stack -> {
                    if (stack.canEquip(slot, player) && IModeItem.isModeItem(stack, slot)) {
                        return !(stack.getItem() instanceof IChemicalItem item) || item.hasChemical(stack);
                    }
                    return false;
                });
                if (curiosResult.isPresent()) {
                    SlotContext slotContext = curiosResult.get().slotContext();
                    PacketUtils.sendToServer(new PacketModeChangeCurios(slotContext.identifier(), slotContext.index(), player.isShiftKeyDown()));
                    SoundHandler.playSound(MekanismSounds.HYDRAULIC);
                }
            }
        }
    }
}