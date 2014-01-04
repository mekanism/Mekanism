package codechicken.multipart.scalatraits

import scala.collection.mutable.ListBuffer
import net.minecraftforge.common.ForgeDirection
import codechicken.multipart.TMultiPart
import codechicken.multipart.TileMultipart
import net.minecraftforge.fluids.IFluidHandler
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidTankInfo
import net.minecraftforge.fluids.Fluid

/**
 * Mixin trait implementation for parts implementing IFluidHandler.
 * Distributes fluid manipulation among fluid handling parts.
 */
trait TFluidHandlerTile extends TileMultipart with IFluidHandler
{
    var tankList = ListBuffer[IFluidHandler]()
    
    override def copyFrom(that:TileMultipart)
    {
        super.copyFrom(that)
        if(that.isInstanceOf[TFluidHandlerTile])
            tankList = that.asInstanceOf[TFluidHandlerTile].tankList
    }
    
    override def bindPart(part:TMultiPart)
    {
        super.bindPart(part)
        if(part.isInstanceOf[IFluidHandler])
            tankList+=part.asInstanceOf[IFluidHandler]
    }
    
    override def partRemoved(part:TMultiPart, p:Int)
    {
        super.partRemoved(part, p)
        if(part.isInstanceOf[IFluidHandler])
            tankList-=part.asInstanceOf[IFluidHandler]
    }
    
    override def clearParts()
    {
        super.clearParts()
        tankList.clear()
    }
    
    override def getTankInfo(dir:ForgeDirection):Array[FluidTankInfo] =
    {
        var tankCount:Int = 0
        tankList.foreach(t => tankCount += t.getTankInfo(dir).length)
        val tanks = new Array[FluidTankInfo](tankCount)
        var i = 0
        tankList.foreach(p => p.getTankInfo(dir).foreach{t =>
            tanks(i) = t
            i+=1
        })
        return tanks
    }
    
    override def fill(dir:ForgeDirection, liquid:FluidStack, doFill:Boolean):Int = 
    {
        var filled = 0
        val initial = liquid.amount
        tankList.foreach(p => 
            filled+=p.fill(dir, copy(liquid, initial-filled), doFill)
        )
        return filled
    }
    
    override def canFill(dir:ForgeDirection, liquid:Fluid) = tankList.find(_.canFill(dir, liquid)).isDefined
    
    override def canDrain(dir:ForgeDirection, liquid:Fluid) = tankList.find(_.canDrain(dir, liquid)).isDefined
    
    private def copy(liquid:FluidStack, quantity:Int):FluidStack =
    {
        val copy = liquid.copy
        copy.amount = quantity
        return copy
    }

    override def drain(dir:ForgeDirection, amount:Int, doDrain:Boolean):FluidStack =
    {
        var drained:FluidStack = null
        var d_amount = 0
        tankList.foreach{p =>
            val drain = amount-d_amount
            val ret = p.drain(dir, drain, false)
            if(ret != null && ret.amount > 0 && (drained == null || drained.isFluidEqual(ret)))
            {
                if(doDrain)
                    p.drain(dir, drain, true)

                if(drained == null)
                    drained = ret

                d_amount+=ret.amount
            }
        }
        if(drained != null)
            drained.amount = d_amount

        return drained
    }

    override def drain(dir:ForgeDirection, fluid:FluidStack, doDrain:Boolean):FluidStack =
    {
        val amount = fluid.amount
        var drained:FluidStack = null
        var d_amount = 0
        tankList.foreach{p =>
            val drain = copy(fluid, amount-d_amount)
            val ret = p.drain(dir, drain, false)
            if(ret != null && ret.amount > 0 && (drained == null || drained.isFluidEqual(ret)))
            {
                if(doDrain)
                    p.drain(dir, drain, true)

                if(drained == null)
                    drained = ret

                d_amount+=ret.amount
            }
        }
        if(drained != null)
            drained.amount = d_amount

        return drained
    }
}