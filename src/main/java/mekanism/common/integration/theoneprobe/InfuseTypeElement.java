package mekanism.common.integration.theoneprobe;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import net.minecraft.network.PacketBuffer;

public class InfuseTypeElement extends ChemicalElement<InfuseType, InfusionStack> {

    public static int ID;

    public InfuseTypeElement(@Nonnull InfusionStack stored, int capacity) {
        super(stored, capacity);
    }

    public InfuseTypeElement(ByteBuf buf) {
        this(ChemicalUtils.readInfusionStack(new PacketBuffer(buf)), buf.readInt());
    }

    @Override
    protected ILangEntry getStoredFormat() {
        return MekanismLang.GENERIC_STORED;
    }

    @Override
    public int getID() {
        return ID;
    }
}