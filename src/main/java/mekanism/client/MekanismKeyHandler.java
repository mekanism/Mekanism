package mekanism.client;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import mekanism.api.EnumColor;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismSounds;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.ItemElectricBow;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.network.PacketFlamethrowerData;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerDataMessage;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerPacket;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketFreeRunnerData.FreeRunnerDataMessage;
import mekanism.common.network.PacketItemStack.ItemStackMessage;
import mekanism.common.network.PacketJetpackData.JetpackDataMessage;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.TextComponentGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class MekanismKeyHandler extends MekKeyHandler {

    public static final String keybindCategory = Mekanism.MOD_NAME;
    public static KeyBinding modeSwitchKey = new KeyBinding("key.mode", Keyboard.KEY_M, keybindCategory);
    public static KeyBinding armorModeSwitchKey = new KeyBinding("key.armorMode", Keyboard.KEY_G, keybindCategory);
    public static KeyBinding freeRunnerModeSwitchKey = new KeyBinding("key.feetMode", Keyboard.KEY_H, keybindCategory);
    public static KeyBinding voiceKey = new KeyBinding("key.voice", Keyboard.KEY_U, keybindCategory);

    public static KeyBinding sneakKey = Minecraft.getMinecraft().gameSettings.keyBindSneak;
    public static KeyBinding jumpKey = Minecraft.getMinecraft().gameSettings.keyBindJump;

    private static Builder BINDINGS = new Builder()
        .addBinding(modeSwitchKey, false)
        .addBinding(armorModeSwitchKey, false)
        .addBinding(freeRunnerModeSwitchKey, false)
        .addBinding(voiceKey, true)
          ;

    public MekanismKeyHandler() {
        super(BINDINGS);

        ClientRegistry.registerKeyBinding(modeSwitchKey);
        ClientRegistry.registerKeyBinding(armorModeSwitchKey);
        ClientRegistry.registerKeyBinding(freeRunnerModeSwitchKey);
        ClientRegistry.registerKeyBinding(voiceKey);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(InputEvent event) {
        keyTick();
    }

    @Override
    public void keyDown(KeyBinding kb, boolean isRepeat) {
        if (kb == modeSwitchKey) {
            EntityPlayer player = FMLClientHandler.instance().getClient().player;
            ItemStack toolStack = player.inventory.getCurrentItem();

            Item item = toolStack.getItem();

            if (player.isSneaking() && item instanceof ItemConfigurator) {
                ItemConfigurator configurator = (ItemConfigurator) item;

                ConfiguratorMode configuratorMode = configurator.getState(toolStack);
                int toSet = (configuratorMode.ordinal() + 1) % ConfiguratorMode.values().length;
                configuratorMode = ConfiguratorMode.values()[toSet];
                configurator.setState(toolStack, configuratorMode);

                Mekanism.packetHandler.sendToServer(new ItemStackMessage(EnumHand.MAIN_HAND, Collections.singletonList(toSet)));

                player.sendMessage(
                    new TextComponentGroup(TextFormatting.GRAY)
                    .string(Mekanism.LOG_TAG, TextFormatting.DARK_BLUE).string(" ")
                    .translation("tooltip.configureState")
                    .string(": ")
                    .component(configuratorMode.getNameComponent(), configuratorMode.getColor().textFormatting)
                );
            } else if (player.isSneaking() && item instanceof ItemElectricBow) {
                ItemElectricBow bow = (ItemElectricBow) item;

                boolean newBowState = !bow.getFireState(toolStack);
                bow.setFireState(toolStack, newBowState);
                Mekanism.packetHandler.sendToServer(new ItemStackMessage(EnumHand.MAIN_HAND, Collections.singletonList(newBowState)));
                player.sendMessage(
                    new TextComponentGroup(TextFormatting.GRAY)
                    .string(Mekanism.LOG_TAG, TextFormatting.DARK_BLUE).string(" ")
                    .translation("tooltip.fireMode")
                    .string(": ")
                    .translation(LangUtils.transOnOffKey(newBowState), newBowState ? TextFormatting.DARK_GREEN : TextFormatting.DARK_RED)
                );
            } else if (player.isSneaking() && item instanceof ItemBlockMachine) {
                ItemBlockMachine machine = (ItemBlockMachine) item;

                if (MachineType.get(toolStack) == MachineType.FLUID_TANK) {
                    boolean newBucketMode = !machine.getBucketMode(toolStack);
                    machine.setBucketMode(toolStack, newBucketMode);
                    Mekanism.packetHandler.sendToServer(new ItemStackMessage(EnumHand.MAIN_HAND, Collections.singletonList(machine.getBucketMode(toolStack))));
                    player.sendMessage(
                          new TextComponentGroup(TextFormatting.GRAY)
                          .string(Mekanism.LOG_TAG, TextFormatting.DARK_BLUE).string(" ")
                          .translation("tooltip.portableTank.bucketMode")
                          .string(": ")
                          .translation(LangUtils.transOnOffKey(newBucketMode), newBucketMode ? TextFormatting.DARK_GREEN : TextFormatting.DARK_RED)
                    );
                }
            } else if (player.isSneaking() && item instanceof ItemWalkieTalkie) {
                ItemWalkieTalkie wt = (ItemWalkieTalkie) item;

                if (wt.getOn(toolStack)) {
                    int newChan = wt.getChannel(toolStack) + 1;
                    if (newChan == 9) {
                        newChan = 1;
                    }
                    wt.setChannel(toolStack, newChan);
                    Mekanism.packetHandler.sendToServer(new ItemStackMessage(EnumHand.MAIN_HAND, Collections.singletonList(newChan)));
                }
            } else if (player.isSneaking() && item instanceof ItemFlamethrower) {
                ItemFlamethrower flamethrower = (ItemFlamethrower) item;

                flamethrower.incrementMode(toolStack);
                Mekanism.packetHandler.sendToServer(new FlamethrowerDataMessage(FlamethrowerPacket.MODE, EnumHand.MAIN_HAND, null, false));
                player.sendMessage(
                      new TextComponentGroup(TextFormatting.GRAY)
                      .string(Mekanism.LOG_TAG, TextFormatting.DARK_BLUE).string(" ")
                      .translation("tooltip.flamethrower.modeBump")
                      .string(": ")
                      .component(flamethrower.getMode(toolStack).getTextComponent())
                );
            }
        } else if (kb == armorModeSwitchKey) {
            EntityPlayer player = FMLClientHandler.instance().getClient().player;
            ItemStack chestStack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            Item chestItem = chestStack.getItem();

            if (chestItem instanceof ItemJetpack) {
                ItemJetpack jetpack = (ItemJetpack) chestItem;

                if (player.isSneaking()) {
                    jetpack.setMode(chestStack, JetpackMode.DISABLED);
                } else {
                    jetpack.incrementMode(chestStack);
                }

                Mekanism.packetHandler.sendToServer(JetpackDataMessage.MODE_CHANGE(player.isSneaking()));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC);
            } else if (chestItem instanceof ItemScubaTank) {
                ItemScubaTank scubaTank = (ItemScubaTank) chestItem;

                scubaTank.toggleFlowing(chestStack);
                Mekanism.packetHandler.sendToServer(ScubaTankDataMessage.MODE_CHANGE(false));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC);
            }
        } else if (kb == freeRunnerModeSwitchKey) {
            EntityPlayer player = FMLClientHandler.instance().getClient().player;
            ItemStack feetStack = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            Item feetItem = feetStack.getItem();

            if (feetItem instanceof ItemFreeRunners) {
                ItemFreeRunners freeRunners = (ItemFreeRunners) feetItem;

                if (player.isSneaking()) {
                    freeRunners.setMode(feetStack, ItemFreeRunners.FreeRunnerMode.DISABLED);
                } else {
                    freeRunners.incrementMode(feetStack);
                }

                Mekanism.packetHandler.sendToServer(new FreeRunnerDataMessage(PacketFreeRunnerData.FreeRunnerPacket.MODE, null, player.isSneaking()));
                SoundHandler.playSound(MekanismSounds.HYDRAULIC);
            }
        }
    }

    @Override
    public void keyUp(KeyBinding kb) {
    }
}
