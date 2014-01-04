package codechicken.multipart.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.IFaceRedstonePart;

public class LeverPart extends McSidedMetaPart implements IFaceRedstonePart
{
    public static BlockLever lever = (BlockLever) Block.lever;
    public static int[] metaSideMap = new int[]{1, 4, 5, 2, 3, 0, 0, 1};
    public static int[] sideMetaMap = new int[]{6, 0, 3, 4, 1, 2};
    public static int[] metaSwapMap = new int[]{5, 7};
    
    public LeverPart()
    {
    }
    
    public LeverPart(int meta)
    {
        super(meta);
    }
    
    @Override
    public Block getBlock()
    {
        return lever;
    }
    
    @Override
    public String getType()
    {
        return "mc_lever";
    }
    
    public boolean active()
    {
        return (meta&8) > 0;
    }
    
    @Override
    public Cuboid6 getBounds()
    {
        int m = meta & 7;
        double d = 0.1875;

        if (m == 1)
            return new Cuboid6(0, 0.2, 0.5 - d, d * 2, 0.8, 0.5 + d);
        if (m == 2)
            return new Cuboid6(1 - d * 2, 0.2, 0.5 - d, 1, 0.8, 0.5 + d);
        if (m == 3)
            return new Cuboid6(0.5 - d, 0.2, 0, 0.5 + d, 0.8, d * 2);
        if (m == 4)
            return new Cuboid6(0.5 - d, 0.2, 1 - d * 2, 0.5 + d, 0.8, 1);

        d = 0.25;
        if (m == 0 || m == 7)
            return new Cuboid6(0.5 - d, 0.4, 0.5 - d, 0.5 + d, 1, 0.5 + d);
        
        return new Cuboid6(0.5 - d, 0, 0.5 - d, 0.5 + d, 0.6, 0.5 + d);
    }
    
    @Override
    public int sideForMeta(int meta)
    {
        return metaSideMap[meta&7];
    }

    public static McBlockPart placement(World world, BlockCoord pos, EntityPlayer player, int side)
    {
        pos = pos.copy().offset(side^1);
        if(!world.isBlockSolidOnSide(pos.x, pos.y, pos.z, ForgeDirection.getOrientation(side)))
            return null;
        
        int meta = sideMetaMap[side^1];
        if(side < 2 && ((int)(player.rotationYaw / 90 + 0.5) & 1) == 0)
            meta = metaSwapMap[side^1];
        
        return new LeverPart(meta);
    }

    @Override
    public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
    {
        World world = world();
        if(world.isRemote)
            return true;

        world.playSoundEffect(x() + 0.5, y() + 0.5, z() + 0.5, "random.click", 0.3F, !active() ? 0.6F : 0.5F);
        meta ^= 8;
        sendDescUpdate();
        tile().notifyPartChange(this);
        tile().notifyNeighborChange(metaSideMap[meta&7]);
        tile().markDirty();
        return true;
    }

    @Override
    public void drawBreaking(RenderBlocks renderBlocks)
    {
        IBlockAccess actual = renderBlocks.blockAccess;
        renderBlocks.blockAccess = new PartMetaAccess(this);
        renderBlocks.renderBlockLever(lever, x(), y(), z());
        renderBlocks.blockAccess = actual;
    }

    @Override
    public void onRemoved()
    {
        if(active())
            tile().notifyNeighborChange(metaSideMap[meta&7]);
    }
    
    @Override
    public void onConverted()
    {
        if(active())
            tile().notifyNeighborChange(metaSideMap[meta&7]);
    }

    @Override
    public int weakPowerLevel(int side)
    {
        return active() ? 15 : 0;
    }

    @Override
    public int strongPowerLevel(int side)
    {
        return active() && side == metaSideMap[meta&7] ? 15 : 0;
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
