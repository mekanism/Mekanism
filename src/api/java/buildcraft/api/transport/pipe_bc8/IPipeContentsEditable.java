package buildcraft.api.transport.pipe_bc8;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.Fluid;

import buildcraft.api.core.INBTLoadable_BC8;
import buildcraft.api.core.INetworkLoadable_BC8;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipePropertyProviderEditable;

import io.netty.buffer.ByteBuf;

/** Provides a way to change the contents of a pipe. */
public interface IPipeContentsEditable extends IPipeContents, INetworkLoadable_BC8<IPipeContentsEditable>, INBTLoadable_BC8<IPipeContentsEditable> {
    IPipeContents asReadOnly();

    void setJourneyPart(EnumContentsJourneyPart journeyPart);

    void setDirection(EnumFacing direction);

    void setSpeed(double speed);

    public interface IPipeContentsEditableFluid extends IPipeContentsEditable, IPipeContentsFluid {
        int setAmount(int newAmount);

        void setFluid(Fluid fluid);

        void setNBT(NBTTagCompound compound);

        void setFluidStack();

        @Override
        IPipeContentsFluid asReadOnly();

        @Override
        IPipeContentsEditableFluid readFromNBT(NBTBase nbt);

        @Override
        IPipeContentsEditableFluid readFromByteBuf(ByteBuf buf);
    }

    public interface IPipeContentsEditableItem extends IPipeContentsEditable, IPipeContentsItem {
        void setStack(ItemStack newStack);

        @Override
        IPipePropertyProviderEditable getProperties();

        @Override
        IPipeContentsItem asReadOnly();

        @Override
        IPipeContentsEditableItem readFromNBT(NBTBase nbt);

        @Override
        IPipeContentsEditableItem readFromByteBuf(ByteBuf buf);
    }
}
