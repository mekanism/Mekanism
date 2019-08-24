package mekanism.additions.client;

import java.util.Collections;
import mekanism.additions.common.item.ItemWalkieTalkie;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
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
public class AdditionsKeyHandler extends MekKeyHandler {

    public static KeyBinding voiceKey = new KeyBinding("mekanismadditions.key.voice", GLFW.GLFW_KEY_U, MekanismKeyHandler.keybindCategory);

    private static Builder BINDINGS = new Builder()
          .addBinding(voiceKey, true);

    public AdditionsKeyHandler() {
        super(BINDINGS);
        ClientRegistry.registerKeyBinding(voiceKey);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(InputEvent.KeyInputEvent event) {
        keyTick();
    }

    @Override
    public void keyDown(KeyBinding kb, boolean isRepeat) {
        if (kb == MekanismKeyHandler.modeSwitchKey) {
            PlayerEntity player = Minecraft.getInstance().player;
            ItemStack toolStack = player.inventory.getCurrentItem();
            Item item = toolStack.getItem();

            if (player.isSneaking() && item instanceof ItemWalkieTalkie) {
                ItemWalkieTalkie wt = (ItemWalkieTalkie) item;
                if (wt.getOn(toolStack)) {
                    int newChan = wt.getChannel(toolStack) + 1;
                    if (newChan == 9) {
                        newChan = 1;
                    }
                    wt.setChannel(toolStack, newChan);
                    Mekanism.packetHandler.sendToServer(new PacketItemStack(Hand.MAIN_HAND, Collections.singletonList(newChan)));
                }
            }
        }
    }

    @Override
    public void keyUp(KeyBinding kb) {

    }
}