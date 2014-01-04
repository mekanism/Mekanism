package codechicken.multipart

import codechicken.lib.vec.Cuboid6
import cpw.mods.fml.relauncher.SideOnly
import cpw.mods.fml.relauncher.Side
import net.minecraft.util.Icon
import net.minecraft.util.MovingObjectPosition
import net.minecraft.client.particle.EffectRenderer
import codechicken.lib.vec.Vector3
import codechicken.lib.raytracer.ExtendedMOP
import codechicken.lib.render.EntityDigIconFX

/**
 * This suite of 3 classes provides simple functions for standard minecraft style hit and break particles.
 * 
 * Scala|Java composition setup.
 * Due to the lack of mixin inheritance in Java, the classes are structured to suit both languages as follows.
 * IconHitEffects contains static implementations of the functions that would be overriden in TMultiPart
 * JIconHitEffects is the interface that should be implemented by a Java class, 
 * which can then override the functions in TMultipart and call the static methods in IconHitEffects with 'this' as the first parameter
 * TIconHitEffects is a trait for scala implementors that does includes the overrides/static calls that Java programmers need to include themselves.
 */
object IconHitEffects
{
    def addHitEffects(part:JIconHitEffects, hit:MovingObjectPosition, effectRenderer:EffectRenderer)
    {
        EntityDigIconFX.addBlockHitEffects(part.tile.worldObj, 
                part.getBounds.copy.add(Vector3.fromTileEntity(part.tile)), hit.sideHit, 
                part.getBreakingIcon(ExtendedMOP.getData(hit), hit.sideHit), effectRenderer)
    }
    
    def addDestroyEffects(part:JIconHitEffects, effectRenderer:EffectRenderer)
    {
        addDestroyEffects(part, effectRenderer, true)
    }
    
    def addDestroyEffects(part:JIconHitEffects, effectRenderer:EffectRenderer, scaleDensity:Boolean)
    {
        val icons = new Array[Icon](6)
        for(i <- 0 until 6)
            icons(i) = part.getBrokenIcon(i)
        val bounds = 
            if(scaleDensity) part.getBounds.copy
            else Cuboid6.full.copy
        EntityDigIconFX.addBlockDestroyEffects(part.tile.worldObj, 
                bounds.add(Vector3.fromTileEntity(part.tile)), icons, effectRenderer)
    }
}

/**
 * Java interface containing callbacks for particle rendering.
 * Make sure to override addHitEffects and addDestroyEffects as in TIconHitEffects
 */
trait JIconHitEffects extends TMultiPart
{
    def getBounds:Cuboid6
    
    @SideOnly(Side.CLIENT)
    def getBreakingIcon(subPart:Any, side:Int):Icon = getBrokenIcon(side)
    
    @SideOnly(Side.CLIENT)
    def getBrokenIcon(side:Int):Icon
}

/**
 * Trait for scala programmers
 */
trait TIconHitEffects extends JIconHitEffects
{
    @SideOnly(Side.CLIENT)
    override def addHitEffects(hit:MovingObjectPosition, effectRenderer:EffectRenderer)
    {
        IconHitEffects.addHitEffects(this, hit, effectRenderer)
    }
    
    @SideOnly(Side.CLIENT)
    override def addDestroyEffects(effectRenderer:EffectRenderer)
    {
        IconHitEffects.addDestroyEffects(this, effectRenderer)
    }
}