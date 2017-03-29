package mekanism.common.integration.multipart;

import static mekanism.common.block.states.BlockStateMachine.MachineBlock.MACHINE_BLOCK_1;
import static mekanism.common.block.states.BlockStateMachine.MachineBlock.MACHINE_BLOCK_2;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.MCMPAddon;
import mcmultipart.api.capability.MCMPCapabilities;
import mcmultipart.api.multipart.IMultipartRegistry;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.tile.TileEntityGlowPanel;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.commons.lang3.tuple.Pair;

@MCMPAddon
public class MultipartMekanism implements IMCMPAddon
{
	@SubscribeEvent
	public void onAttachTile(AttachCapabilitiesEvent<TileEntity> event)
	{
		TileEntity tile = event.getObject();
		
		if(tile instanceof TileEntityTransmitter)
		{
			register(event, "transmitter");
		}
		else if(tile instanceof TileEntityGlowPanel)
		{
			register(event, "glow_panel");
		}
	}
	
	@Override
	public void registerParts(IMultipartRegistry registry) 
	{
		MinecraftForge.EVENT_BUS.register(this);
		
		registry.registerPartWrapper(MekanismBlocks.Transmitter, new MultipartTransmitter());
		registry.registerStackWrapper(Item.getItemFromBlock(MekanismBlocks.Transmitter), s -> true, MekanismBlocks.Transmitter);
		registry.registerPartWrapper(MekanismBlocks.GlowPanel, new MultipartGlowPanel());
		registry.registerStackWrapper(Item.getItemFromBlock(MekanismBlocks.GlowPanel), s -> true, MekanismBlocks.GlowPanel);
    }
	
	private void register(AttachCapabilitiesEvent<TileEntity> e, String id)
	{
        e.addCapability(new ResourceLocation("mekanism:" + id), new ICapabilityProvider() {
            private MultipartTile tile;

            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) 
            {
                return capability == MCMPCapabilities.MULTIPART_TILE;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) 
            {
                if(capability == MCMPCapabilities.MULTIPART_TILE)
                {
                    if(tile == null)
                    {
                        tile = new MultipartTile(e.getObject());
                    }

                    return MCMPCapabilities.MULTIPART_TILE.cast(tile);
                }

                return null;
            }
        });
    }

	public void init()
	{
		registerMicroMaterials();
	}

	public void registerMicroMaterials()
	{
		for(int i = 0; i < 16; i++)
		{
			FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.BasicBlock, 1, i));
			
			if(!MachineType.get(MACHINE_BLOCK_1, i).hasModel)
			{
				FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.MachineBlock, 1, i));
			}
			
			if(!MachineType.get(MACHINE_BLOCK_2, i).hasModel)
			{
				FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.MachineBlock2, 1, i));
			}
		}
		
		FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.BasicBlock2, 1, 0));
		FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.CardboardBox));
	}
	
	public static AxisAlignedBB rotate(AxisAlignedBB aabb, EnumFacing side) 
	{
        Vec3d v1 = rotate(new Vec3d(aabb.minX, aabb.minY, aabb.minZ), side);
        Vec3d v2 = rotate(new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ), side);
        
        return new AxisAlignedBB(v1.xCoord, v1.yCoord, v1.zCoord, v2.xCoord, v2.yCoord, v2.zCoord);
    }

    public static Vec3d rotate(Vec3d vec, EnumFacing side)
    {
        switch(side) 
        {
	        case DOWN:
	            return new Vec3d(vec.xCoord, vec.yCoord, vec.zCoord);
	        case UP:
	            return new Vec3d(vec.xCoord, -vec.yCoord, -vec.zCoord);
	        case NORTH:
	            return new Vec3d(vec.xCoord, -vec.zCoord, vec.yCoord);
	        case SOUTH:
	            return new Vec3d(vec.xCoord, vec.zCoord, -vec.yCoord);
	        case WEST:
	            return new Vec3d(vec.yCoord, -vec.xCoord, vec.zCoord);
	        case EAST:
	            return new Vec3d(-vec.yCoord, vec.xCoord, vec.zCoord);
        }
        
        return null;
    }
    
    /* taken from MCMP */
    public static Pair<Vec3d, Vec3d> getRayTraceVectors(EntityPlayer player) 
    {
        float pitch = player.rotationPitch;
        float yaw = player.rotationYaw;
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        float f1 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f3 = -MathHelper.cos(-pitch * 0.017453292F);
        float f4 = MathHelper.sin(-pitch * 0.017453292F);
        float f5 = f2 * f3;
        float f6 = f1 * f3;
        double d3 = 5.0D;
        
        if(player instanceof EntityPlayerMP) 
        {
            d3 = ((EntityPlayerMP)player).interactionManager.getBlockReachDistance();
        }
        
        Vec3d end = start.addVector(f5 * d3, f4 * d3, f6 * d3);
        return Pair.of(start, end);
    }
    
    public static AdvancedRayTraceResult collisionRayTrace(BlockPos pos, Vec3d start, Vec3d end, Collection<AxisAlignedBB> boxes)
    {
        double minDistance = Double.POSITIVE_INFINITY;
        AdvancedRayTraceResult hit = null;
        int i = -1;

        for(AxisAlignedBB aabb : boxes) 
        {
            AdvancedRayTraceResult result = aabb == null ? null : collisionRayTrace(pos, start, end, aabb, i, null);

            if(result != null)
            {
                double d = result.squareDistanceTo(start);
                
                if(d < minDistance) 
                {
                    minDistance = d;
                    hit = result;
                }
            }

            i++;
        }

        return hit;
    }
    
    public static AdvancedRayTraceResult collisionRayTrace(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB bounds, int subHit, Object hitInfo)
    {
        RayTraceResult result = bounds.offset(pos).calculateIntercept(start, end);

        if(result == null) 
        {
            return null;
        }

        result = new RayTraceResult(RayTraceResult.Type.BLOCK, result.hitVec, result.sideHit, pos);
        result.subHit = subHit;
        result.hitInfo = hitInfo;

        return new AdvancedRayTraceResult(result, bounds);
    }
    
    private static class AdvancedRayTraceResultBase<T extends RayTraceResult> 
    {
        public final AxisAlignedBB bounds;
        public final T hit;

        public AdvancedRayTraceResultBase(T mop, AxisAlignedBB aabb)
        {
            hit = mop;
            bounds = aabb;
        }

        public boolean valid()
        {
            return hit != null && bounds != null;
        }

        public double squareDistanceTo(Vec3d vec) 
        {
            return hit.hitVec.squareDistanceTo(vec);
        }
    }

    public static class AdvancedRayTraceResult extends AdvancedRayTraceResultBase<RayTraceResult> 
    {
        public AdvancedRayTraceResult(RayTraceResult mop, AxisAlignedBB bounds) 
        {
            super(mop, bounds);
        }
    }
}
