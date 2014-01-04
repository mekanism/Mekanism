package codechicken.multipart.minecraft;

import codechicken.lib.packet.PacketCustom;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TileMultipart;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet15Place;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

public class EventHandler
{
    private ThreadLocal<Object> placing = new ThreadLocal<Object>();
    
    @ForgeSubscribe
    public void playerInteract(PlayerInteractEvent event)
    {
        if(event.action == Action.RIGHT_CLICK_BLOCK && event.entityPlayer.worldObj.isRemote)
        {
            if(placing.get() != null)
                return;//for mods that do dumb stuff and call this event like MFR
            placing.set(event);
            if(place(event.entityPlayer, event.entityPlayer.worldObj))
                event.setCanceled(true);
            placing.set(null);
        }
    }
    
    public static boolean place(EntityPlayer player, World world)
    {
        MovingObjectPosition hit = RayTracer.reTrace(world, player);
        if(hit == null)
            return false;
        
        BlockCoord pos = new BlockCoord(hit.blockX, hit.blockY, hit.blockZ).offset(hit.sideHit);
        ItemStack held = player.getHeldItem();
        McBlockPart part = null;
        if(held == null)
            return false;
        
        if(held.itemID == Block.torchWood.blockID)
            part = TorchPart.placement(world, pos, hit.sideHit);
        else if(held.itemID == Block.lever.blockID)
            part = LeverPart.placement(world, pos, player, hit.sideHit);
        else if(held.itemID == Block.stoneButton.blockID)
            part = ButtonPart.placement(world, pos, hit.sideHit, 0);
        else if(held.itemID == Block.woodenButton.blockID)
            part = ButtonPart.placement(world, pos, hit.sideHit, 1);
        else if(held.itemID == Block.torchRedstoneActive.blockID)
            part = RedstoneTorchPart.placement(world, pos, hit.sideHit);
        
        if(part == null)
            return false;

        if(world.isRemote && !player.isSneaking())//attempt to use block activated like normal and tell the server the right stuff
        {
            Vector3 f = new Vector3(hit.hitVec).add(-hit.blockX, -hit.blockY, -hit.blockZ);
            Block block = Block.blocksList[world.getBlockId(hit.blockX, hit.blockY, hit.blockZ)];
            if(block != null && !ignoreActivate(block) && block.onBlockActivated(world, hit.blockX, hit.blockY, hit.blockZ, player, hit.sideHit, (float)f.x, (float)f.y, (float)f.z))
            {
                player.swingItem();
                PacketCustom.sendToServer(new Packet15Place(
                        hit.blockX, hit.blockY, hit.blockZ, hit.sideHit, 
                        player.inventory.getCurrentItem(), 
                        (float)f.x, (float)f.y, (float)f.z));
                return false;
            }
        }
        
        TileMultipart tile = TileMultipart.getOrConvertTile(world, pos);
        if(tile == null || !tile.canAddPart(part))
            return false;
        
        if(!world.isRemote)
        {
            TileMultipart.addPart(world, pos, part);
            world.playSoundEffect(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, 
                    part.getBlock().stepSound.getPlaceSound(), 
                    (part.getBlock().stepSound.getVolume() + 1.0F) / 2.0F, 
                    part.getBlock().stepSound.getPitch() * 0.8F);
            if(!player.capabilities.isCreativeMode)
            {
                held.stackSize--;
                if (held.stackSize == 0)
                {
                    player.inventory.mainInventory[player.inventory.currentItem] = null;
                    MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, held));
                }
            }
        }
        else
        {
            player.swingItem();
            new PacketCustom(McMultipartSPH.channel, 1).sendToServer();
        }
        return true;
    }

    /**
     * Because vanilla is weird.
     */
    private static boolean ignoreActivate(Block block)
    {
        if(block instanceof BlockFence)
            return true;
        return false;
    }
}
