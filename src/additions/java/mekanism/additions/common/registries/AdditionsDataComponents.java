package mekanism.additions.common.registries;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.item.ItemWalkieTalkie.WalkieData;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.DataComponentDeferredRegister;
import net.minecraft.core.component.DataComponentType;

public class AdditionsDataComponents {

    private AdditionsDataComponents() {
    }

    public static final DataComponentDeferredRegister DATA_COMPONENTS = new DataComponentDeferredRegister(MekanismAdditions.MODID);

    public static final MekanismDeferredHolder<DataComponentType<?>, DataComponentType<WalkieData>> WALKIE_DATA = DATA_COMPONENTS.simple("walkie_data",
          builder -> builder.persistent(WalkieData.CODEC)
                .networkSynchronized(WalkieData.STREAM_CODEC)
    );
}