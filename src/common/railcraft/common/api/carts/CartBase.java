package railcraft.common.api.carts;

import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityMinecart;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;

/**
 * Generally minecarts should extend this class or there will be
 * oddities if a user links two carts with different max speeds.
 *
 * It also contains some generic code that most carts will find useful.
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public abstract class CartBase extends EntityMinecart implements IMinecart
{

    private float trainSpeed = 1.2f;

    public CartBase(World world)
    {
        super(world);
        CartTools.setCartOwner(this, "[Railcraft]");
    }

    public World getWorld()
    {
        return worldObj;
    }

    @Override
    public final float getMaxSpeedRail()
    {
        return Math.min(getCartMaxSpeed(), trainSpeed);
    }

    @Override
    public float getCartMaxSpeed()
    {
        return 1.2f;
    }

    @Override
    public final void setTrainSpeed(float speed)
    {
        this.trainSpeed = speed;
    }

    @Override
    public final boolean interact(EntityPlayer player)
    {
        if(MinecraftForge.EVENT_BUS.post(new MinecartInteractEvent(this, player))) {
            return true;
        }
        if(CartTools.getCartOwner(this).equals("[Railcraft]")) {
            CartTools.setCartOwner(this, player);
        }
        return doInteract(player);
    }

    public boolean doInteract(EntityPlayer player)
    {
        return super.interact(player);
    }

    @Override
    public boolean doesCartMatchFilter(ItemStack stack, EntityMinecart cart)
    {
        if(stack == null || cart == null) {
            return false;
        }
        ItemStack cartItem = cart.getCartItem();
        return cartItem != null && stack.isItemEqual(cartItem);
    }

    @Override
    public void setDead()
    {
        for(int var1 = 0; var1 < this.getSizeInventory(); ++var1) {
            ItemStack var2 = this.getStackInSlot(var1);
            this.setInventorySlotContents(var1, null);

            if(!worldObj.isRemote && var2 != null) {
                float var3 = this.rand.nextFloat() * 0.8F + 0.1F;
                float var4 = this.rand.nextFloat() * 0.8F + 0.1F;
                float var5 = this.rand.nextFloat() * 0.8F + 0.1F;

                while(var2.stackSize > 0) {
                    int var6 = this.rand.nextInt(21) + 10;

                    if(var6 > var2.stackSize) {
                        var6 = var2.stackSize;
                    }

                    var2.stackSize -= var6;
                    EntityItem var7 = new EntityItem(this.worldObj, this.posX + (double)var3, this.posY + (double)var4, this.posZ + (double)var5, new ItemStack(var2.itemID, var6, var2.getItemDamage()));

                    if(var2.hasTagCompound()) {
                        var7.item.setTagCompound((NBTTagCompound)var2.getTagCompound().copy());
                    }

                    float var8 = 0.05F;
                    var7.motionX = (double)((float)this.rand.nextGaussian() * var8);
                    var7.motionY = (double)((float)this.rand.nextGaussian() * var8 + 0.2F);
                    var7.motionZ = (double)((float)this.rand.nextGaussian() * var8);
                    this.worldObj.spawnEntityInWorld(var7);
                }
            }
        }

        super.setDead();
    }
}
