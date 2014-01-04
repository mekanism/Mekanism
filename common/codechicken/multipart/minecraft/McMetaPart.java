package codechicken.multipart.minecraft;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.lighting.LazyLightMatrix;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class McMetaPart extends McBlockPart implements IPartMeta
{
    public byte meta;
    
    public McMetaPart()
    {
    }
    
    public McMetaPart(int meta)
    {
        this.meta = (byte)meta;
    }
    
    @Override
    public void save(NBTTagCompound tag)
    {
        tag.setByte("meta", meta);
    }
    
    @Override
    public void load(NBTTagCompound tag)
    {
        meta = tag.getByte("meta");
    }
    
    @Override
    public void writeDesc(MCDataOutput packet)
    {
        packet.writeByte(meta);
    }
    
    @Override
    public void readDesc(MCDataInput packet)
    {
        meta = packet.readByte();
    }
    
    @Override
    public World getWorld()
    {
        return world();
    }
    
    @Override
    public int getMetadata()
    {
        return meta;
    }
    
    @Override
    public int getBlockId()
    {
        return getBlock().blockID;
    }
    
    @Override
    public BlockCoord getPos()
    {
        return new BlockCoord(tile());
    }
    
    @Override
    public boolean doesTick()
    {
        return false;
    }
    
    @Override
    public void renderStatic(Vector3 pos, LazyLightMatrix olm, int pass)
    {
        if(pass == 0)
            new RenderBlocks(new PartMetaAccess(this)).renderBlockByRenderType(getBlock(), x(), y(), z());
    }
}
