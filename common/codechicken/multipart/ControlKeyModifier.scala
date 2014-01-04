package codechicken.multipart

import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Keyboard
import cpw.mods.fml.common.registry.LanguageRegistry
import java.util.EnumSet
import cpw.mods.fml.common.TickType
import net.minecraft.client.Minecraft
import codechicken.lib.packet.PacketCustom
import codechicken.multipart.handler.MultipartCPH
import scala.collection.mutable.HashMap
import net.minecraft.entity.player.EntityPlayer

/**
 * A class that maintains a map server<->client of which players are holding the control (or placement modifier key) much like sneaking.
 */
object ControlKeyModifer
{
    implicit def playerControlValue(p:EntityPlayer) = new ControlKeyValue(p)
    
    class ControlKeyValue(p:EntityPlayer)
    {
        def isControlDown = map(p)
    }
    
    val map = HashMap[EntityPlayer, Boolean]().withDefaultValue(false)
    
    /**
     * Implicit static for Java users.
     */
    def isControlDown(p:EntityPlayer) = p.isControlDown
}

/**
 * Key Handler implementation
 */
object ControlKeyHandler extends KeyHandler (
        Array(new KeyBinding("key.control", Keyboard.KEY_LCONTROL)), 
        Array(false))
{
    import ControlKeyModifer._
    
    LanguageRegistry.instance.addStringLocalization("key.control", "Placement Modifier")
    
    def keyDown(types:EnumSet[TickType], kb:KeyBinding, tickEnd:Boolean, isRepeat:Boolean)
    {
        if(!tickEnd && Minecraft.getMinecraft.getNetHandler != null)
        {
            map.put(Minecraft.getMinecraft.thePlayer, true)
            val packet = new PacketCustom(MultipartCPH.channel, 1)
            packet.writeBoolean(true)
            packet.sendToServer()
        }
    }
    
    def keyUp(types:EnumSet[TickType], kb:KeyBinding, tickEnd:Boolean)
    {
        if(!tickEnd && Minecraft.getMinecraft.getNetHandler != null)
        {
            map.put(Minecraft.getMinecraft.thePlayer, false)
            val packet = new PacketCustom(MultipartCPH.channel, 1)
            packet.writeBoolean(false)
            packet.sendToServer()
        }
    }
    
    def getLabel = "Control Key Modifer"

    def ticks = EnumSet.of(TickType.CLIENT)
}