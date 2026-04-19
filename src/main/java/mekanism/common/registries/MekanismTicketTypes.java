package mekanism.common.registries;

import mekanism.common.Mekanism;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.TicketType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismTicketTypes {

    private MekanismTicketTypes() {
    }

    public static final DeferredRegister<TicketType> TICKET_TYPES = DeferredRegister.create(BuiltInRegistries.TICKET_TYPE, Mekanism.MODID);

    //TODO - 26.1: Figure out what flags we should be passing to this ticket type
    public static final Holder<TicketType> ROBIT_CHUNK_UNLOAD = TICKET_TYPES.register("robit_chunk_unload", () -> new TicketType(SharedConstants.TICKS_PER_SECOND, TicketType.FLAG_KEEP_DIMENSION_ACTIVE));
}