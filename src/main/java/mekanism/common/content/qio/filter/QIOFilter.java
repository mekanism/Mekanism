package mekanism.common.content.qio.filter;

import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import io.netty.buffer.ByteBuf;
import java.util.function.Supplier;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;

public abstract class QIOFilter<FILTER extends QIOFilter<FILTER>> extends BaseFilter<FILTER> {

    //Note: This method exists so that if we add any qio filter specific stuff, we can easily add support for it here
    protected static <FILTER extends QIOFilter<FILTER>> P1<Mu<FILTER>, Boolean> baseQIOCodec(Instance<FILTER> instance) {
        return baseCodec(instance)
              ;
    }

    //Note: This method exists so that if we add any qio filter specific stuff, we can easily add support for it here
    protected static <FILTER extends QIOFilter<FILTER>> StreamCodec<ByteBuf, FILTER> baseQIOStreamCodec(Supplier<FILTER> constructor) {
        return baseStreamCodec(constructor);
    }

    protected QIOFilter() {
    }

    protected QIOFilter(boolean enabled) {
        super(enabled);
    }

    protected QIOFilter(FILTER filter) {
        super(filter);
    }

    public abstract boolean test(ItemResource toCheck);

    @Override
    @ComputerMethod(threadSafe = true)
    public abstract FILTER clone();
}
