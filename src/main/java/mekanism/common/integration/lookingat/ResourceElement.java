package mekanism.common.integration.lookingat;

import mekanism.api.math.MathUtils;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.NotNull;

public abstract sealed class ResourceElement<RESOURCE extends Resource> extends LookingAtElement permits ChemicalElement, FluidElement {

    @NotNull
    protected final LargeResourceStack<RESOURCE> stored;
    protected final long capacity;

    public ResourceElement(@NotNull LargeResourceStack<RESOURCE> stored, long capacity) {
        super(CommonColors.BLACK, CommonColors.WHITE);
        this.stored = stored;
        this.capacity = capacity;
    }

    @Override
    public int getScaledLevel(int level) {
        if (capacity == 0 || stored.amount() == Long.MAX_VALUE) {
            return level;
        }
        return MathUtils.clampToInt(level * MathUtils.divideToLevel(stored.amount(), capacity));
    }

    @NotNull
    public LargeResourceStack<RESOURCE> getStored() {
        return stored;
    }

    public long getCapacity() {
        return capacity;
    }

    @Override
    public Component getText() {
        if (stored.isEmpty()) {
            return MekanismLang.EMPTY.translate();
        } else if (stored.amount() == Long.MAX_VALUE) {
            return MekanismLang.GENERIC_STORED.translate(stored.resource(), MekanismLang.INFINITE);
        }
        return MekanismLang.GENERIC_STORED_MB.translate(stored.resource(), TextUtils.format(stored.amount()));
    }
}