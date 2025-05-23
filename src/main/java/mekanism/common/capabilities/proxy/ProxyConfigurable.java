package mekanism.common.capabilities.proxy;

import mekanism.api.IConfigurable;
import mekanism.api.WrenchResult;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyConfigurable extends ProxyHandler implements IConfigurable {

    private final IConfigurable configurable;

    public ProxyConfigurable(IConfigurable configurable, @Nullable Direction side) {
        super(side, null);
        this.configurable = configurable;
    }

    @Override
    public WrenchResult onConfigure(ConfigureContext context) {
        return readOnly || side != context.side() ? WrenchResult.PASS : configurable.onConfigure(context);
    }
}