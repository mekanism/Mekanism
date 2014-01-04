package codechicken.multipart.minecraft;

import java.util.Arrays;
import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.IconHitEffects;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JIconHitEffects;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.NormalOcclusionTest;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class McBlockPart extends JCuboidPart implements JNormalOcclusion, JIconHitEffects
{
    public abstract Block getBlock();
    
    @Override
    public boolean occlusionTest(TMultiPart npart)
    {
        return NormalOcclusionTest.apply(this, npart);
    }

    @Override
    public Iterable<Cuboid6> getOcclusionBoxes()
    {
        return Arrays.asList(getBounds());
    }
    
    @Override
    public Iterable<Cuboid6> getCollisionBoxes()
    {
        return Collections.emptyList();
    }
    
    @Override
    public Iterable<ItemStack> getDrops()
    {
        return Arrays.asList(new ItemStack(getBlock()));
    }
    
    @Override
    public ItemStack pickItem(MovingObjectPosition hit)
    {
        return new ItemStack(getBlock());
    }
    
    @Override
    public float getStrength(MovingObjectPosition hit, EntityPlayer player)
    {
        return getBlock().getPlayerRelativeBlockHardness(player, player.worldObj, hit.blockX, hit.blockY, hit.blockZ)*30;
    }
    
    @Override
    public Icon getBreakingIcon(Object subPart, int side)
    {
        return getBlock().getIcon(0, 0);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public Icon getBrokenIcon(int side)
    {
        return getBlock().getIcon(0, 0);
    }
    
    @Override
    public void addHitEffects(MovingObjectPosition hit, EffectRenderer effectRenderer)
    {
        IconHitEffects.addHitEffects(this, hit, effectRenderer);
    }
    
    @Override
    public void addDestroyEffects(MovingObjectPosition hit, EffectRenderer effectRenderer)
    {
        IconHitEffects.addDestroyEffects(this, effectRenderer, false);
    }
    
    @Override
    public int getLightValue()
    {
        return Block.lightValue[getBlock().blockID];
    }
}
