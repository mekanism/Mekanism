package mekanism.common.integration.multipart;

import static mekanism.common.block.states.BlockStateMachine.MachineBlock.MACHINE_BLOCK_1;
import static mekanism.common.block.states.BlockStateMachine.MachineBlock.MACHINE_BLOCK_2;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.MCMPAddon;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartRegistry;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartOcclusionHelper;
import mcmultipart.api.ref.MCMPCapabilities;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.BlockTransmitter;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType.Size;
import mekanism.common.tile.TileEntityGlowPanel;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
                        tile = new MultipartTile(e.getObject(), id);
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
	
	public static boolean hasConnectionWith(TileEntity tile, EnumFacing side)
	{
        if(tile != null && tile.hasCapability(MCMPCapabilities.MULTIPART_TILE, null)) 
        {
            IMultipartTile multipartTile = tile.getCapability(MCMPCapabilities.MULTIPART_TILE, null);

            if(multipartTile instanceof MultipartTile && ((MultipartTile)multipartTile).getID().equals("transmitter")) 
            {
            	IPartInfo partInfo = ((MultipartTile)multipartTile).getInfo();
            	
            	if(partInfo != null)
            	{
	                for(IPartInfo info : partInfo.getContainer().getParts().values()) 
	                {
	                    IMultipart multipart = info.getPart();
	                    Collection<AxisAlignedBB> origBounds = getTransmitterSideBounds(multipartTile, side);
	
	                    if(MultipartOcclusionHelper.testBoxIntersection(origBounds, multipart.getOcclusionBoxes(info))) 
	                    {
	                        return false;
	                    }
	                }
            	}
            }
        }

        return true;
    }
	
	public static Collection<AxisAlignedBB> getTransmitterSideBounds(IMultipartTile tile, EnumFacing side)
	{
		if(tile.getTileEntity() instanceof TileEntityTransmitter)
		{
			TileEntityTransmitter transmitter = ((TileEntityTransmitter)tile.getTileEntity());
			boolean large = transmitter.getTransmitterType().getSize() == Size.LARGE;
			AxisAlignedBB ret = large ? BlockTransmitter.largeSides[side.ordinal()] : BlockTransmitter.smallSides[side.ordinal()];
			return Collections.singletonList(ret);
		}
		
		return Collections.emptyList();
	}
}
