package codechicken.multipart.minecraft;

import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.IFaceRedstonePart;
import codechicken.multipart.TickScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class ButtonPart extends McSidedMetaPart implements IFaceRedstonePart
{
    public static BlockButton stoneButton = (BlockButton) Block.stoneButton;
    public static BlockButton woodenButton = (BlockButton) Block.woodenButton;
    public static int[] metaSideMap = new int[]{-1, 4, 5, 2, 3};
    public static int[] sideMetaMap = new int[]{-1, -1, 3, 4, 1, 2};
    
    public static BlockButton getButton(int meta)
    {
        return (meta&0x10) > 0 ? woodenButton : stoneButton;
    }
    
    public ButtonPart()
    {
    }
    
    public ButtonPart(int meta)
    {
        super(meta);
    }
    
    @Override
    public int sideForMeta(int meta)
    {
        return metaSideMap[meta&7];
    }

    @Override
    public Block getBlock()
    {
        return getButton(meta);
    }
    
    @Override
    public String getType()
    {
        return "mc_button";
    }
    
    public int delay()
    {
        return sensitive() ? 30 : 20;
    }
    
    public boolean sensitive()
    {
        return (meta&0x10) > 0;
    }
    
    @Override
    public Cuboid6 getBounds()
    {
        int m = meta & 7;
        double d = pressed() ? 0.0625 : 0.125;
        
        if (m == 1)
            return new Cuboid6(0.0, 0.375, 0.5 - 0.1875, d, 0.625, 0.5 + 0.1875);
        if (m == 2)
            return new Cuboid6(1.0 - d, 0.375, 0.5 - 0.1875, 1.0, 0.625, 0.5 + 0.1875);
        if (m == 3)
            return new Cuboid6(0.5 - 0.1875, 0.375, 0.0, 0.5 + 0.1875, 0.625, d);
        if (m == 4)
            return new Cuboid6(0.5 - 0.1875, 0.375, 1.0 - d, 0.5 + 0.1875, 0.625, 1.0);
        
        return null;//falloff
    }

    public static McBlockPart placement(World world, BlockCoord pos, int side, int type)
    {
        if(side == 0 || side == 1)
            return null;
        
        pos = pos.copy().offset(side^1);
        if(!world.isBlockSolidOnSide(pos.x, pos.y, pos.z, ForgeDirection.getOrientation(side)))
            return null;
        
        return new ButtonPart(sideMetaMap[side^1]|type<<4);
    }

    @Override
    public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
    {
        if(pressed())
            return false;
        
        if(!world().isRemote)
            toggle();
        
        return true;
    }
    
    @Override
    public void scheduledTick()
    {
        if(pressed())
            updateState();
    }
    
    public boolean pressed()
    {
        return (meta&8) > 0;
    }
    
    @Override
    public void onEntityCollision(Entity entity)
    {
        if(!pressed() && !world().isRemote && entity instanceof EntityArrow)
            updateState();
    }

    private void toggle()
    {
        boolean in = !pressed();
        meta^=8;
        world().playSoundEffect(x() + 0.5, y() + 0.5, z() + 0.5, "random.click", 0.3F, in ? 0.6F : 0.5F);
        if(in)
            scheduleTick(delay());
        
        sendDescUpdate();
        tile().notifyPartChange(this);
        tile().notifyNeighborChange(metaSideMap[meta&7]);
        tile().markDirty();
    }

    private void updateState()
    {
        boolean arrows = sensitive() && !world().getEntitiesWithinAABB(EntityArrow.class,
                getBounds().add(Vector3.fromTileEntity(tile())).toAABB()).isEmpty();
        boolean pressed = pressed();
        
        if(arrows != pressed)
            toggle();
        if(arrows && pressed)
            scheduleTick(delay());
    }
    
    @Override
    public void onRemoved()
    {
        if(pressed())
            tile().notifyNeighborChange(metaSideMap[meta&7]);
    }
    
    @Override
    public int weakPowerLevel(int side)
    {
        return pressed() ? 15 : 0;
    }

    @Override
    public int strongPowerLevel(int side)
    {
        return pressed() && side == metaSideMap[meta&7] ? 15 : 0;
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
