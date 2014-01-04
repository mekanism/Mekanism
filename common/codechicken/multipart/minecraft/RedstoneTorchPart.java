package codechicken.multipart.minecraft;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.IFaceRedstonePart;
import codechicken.multipart.IRandomUpdateTick;
import codechicken.multipart.RedstoneInteractions;

public class RedstoneTorchPart extends TorchPart implements IFaceRedstonePart, IRandomUpdateTick
{
    public static BlockRedstoneTorch torchActive = (BlockRedstoneTorch) Block.torchRedstoneActive;
    public static BlockRedstoneTorch torchIdle = (BlockRedstoneTorch) Block.torchRedstoneIdle;
    
    public class BurnoutEntry
    {
        public BurnoutEntry(long l)
        {
            timeout = l;
        }
        
        long timeout;
        BurnoutEntry next;
    }
    
    private BurnoutEntry burnout;
    
    public RedstoneTorchPart()
    {
    }
    
    public RedstoneTorchPart(int meta)
    {
        super(meta);
    }
    
    @Override
    public Block getBlock()
    {
        return active() ? torchActive : torchIdle;
    }
    
    public boolean active()
    {
        return (meta&0x10) > 0;
    }
    
    @Override
    public String getType()
    {
        return "mc_redtorch";
    }
    
    @Override
    public int sideForMeta(int meta)
    {
        return super.sideForMeta(meta&7);
    }
    
    @Override
    public Cuboid6 getBounds()
    {
        return getBounds(meta&7);
    }
    
    public static McBlockPart placement(World world, BlockCoord pos, int side)
    {
        if(side == 0)
            return null;
        pos = pos.copy().offset(side^1);
        if(!world.isBlockSolidOnSide(pos.x, pos.y, pos.z, ForgeDirection.getOrientation(side)))
            return null;
        
        return new RedstoneTorchPart(sideMetaMap[side^1]|0x10);
    }
    
    @Override
    public void randomDisplayTick(Random random)
    {
        if(!active())
            return;
        
        double d0 = x() + 0.5 + (random.nextFloat() - 0.5) * 0.2;
        double d1 = y() + 0.7 + (random.nextFloat() - 0.5) * 0.2;
        double d2 = z() + 0.5 + (random.nextFloat() - 0.5) * 0.2;
        double d3 = 0.22D;
        double d4 = 0.27D;
        
        World world = world();
        int m = meta&7;
        if (m == 1)
            world.spawnParticle("reddust", d0 - d4, d1 + d3, d2, 0, 0, 0);
        else if (m == 2)
            world.spawnParticle("reddust", d0 + d4, d1 + d3, d2, 0, 0, 0);
        else if (m == 3)
            world.spawnParticle("reddust", d0, d1 + d3, d2 - d4, 0, 0, 0);
        else if (m == 4)
            world.spawnParticle("reddust", d0, d1 + d3, d2 + d4, 0, 0, 0);
        else
            world.spawnParticle("reddust", d0, d1, d2, 0, 0, 0);
    }
    
    @Override
    public ItemStack pickItem(MovingObjectPosition hit)
    {
        return new ItemStack(torchActive);
    }
    
    @Override
    public void onNeighborChanged()
    {
        if(!world().isRemote)
        {
            if(!dropIfCantStay() && isBeingPowered() == active())
                scheduleTick(2);
        }
    }
    
    public boolean isBeingPowered()
    {
        int side = metaSideMap[meta&7];
        return RedstoneInteractions.getPowerTo(this, side) > 0;
    }
    
    @Override
    public void scheduledTick()
    {
        if(!world().isRemote && isBeingPowered() == active())
            toggle();
    }

    @Override
    public void randomUpdate()
    {
        scheduledTick();
    }
    
    private boolean burnedOut(boolean add)
    {
        long time = world().getTotalWorldTime();
        while(burnout != null && burnout.timeout <= time)
            burnout = burnout.next;
        
        if(add)
        {
            BurnoutEntry e = new BurnoutEntry(world().getTotalWorldTime()+60);
            if(burnout == null)
                burnout = e;
            else
            {
                BurnoutEntry b = burnout;
                while(b.next != null)
                    b = b.next;
                b.next = e;
            }
        }

        if(burnout == null)
            return false;
        
        int i = 0;
        BurnoutEntry b = burnout;
        while(b != null)
        {
            i++;
            b = b.next;
        }
        return i >= 8;
    }
    
    private void toggle()
    {
        if(active())//deactivating
        {
            if(burnedOut(true))
            {
                World world = world();
                Random rand = world.rand;
                world.playSoundEffect(x()+0.5, y()+0.5, z()+0.5, "random.fizz", 0.5F, 2.6F + (rand.nextFloat() - rand.nextFloat()) * 0.8F);
                McMultipartSPH.spawnBurnoutSmoke(world, x(), y(), z());
            }
        }
        else if(burnedOut(false))
        {
            return;
        }
        
        meta ^= 0x10;
        sendDescUpdate();
        tile().markDirty();
        tile().notifyPartChange(this);
        tile().notifyNeighborChange(1);
    }
    
    @Override
    public void drop() {
        meta|=0x10;//set state to on for drop
        super.drop();
    }

    @Override
    public void onRemoved()
    {
        if(active())
            tile().notifyNeighborChange(1);
    }
    
    @Override
    public void onAdded()
    {
        if(active())
            tile().notifyNeighborChange(1);
        onNeighborChanged();
    }

    @Override
    public int strongPowerLevel(int side)
    {
        return side == 1 && active() ? 15 : 0;
    }

    @Override
    public int weakPowerLevel(int side)
    {
        return active() && side != metaSideMap[meta&7] ? 15 : 0;
    }
    
    @Override
    public boolean canConnectRedstone(int side)
    {
        return true;
    }
    
    @Override
    public int getFace() {
        return metaSideMap[meta&7];
    }
}
