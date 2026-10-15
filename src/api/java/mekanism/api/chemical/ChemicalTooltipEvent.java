package mekanism.api.chemical;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jspecify.annotations.Nullable;

/// Similar to Neo's [net.neoforged.neoforge.event.entity.player.FluidTooltipEvent] but for Chemicals
///
/// @since 10.8.0
public class ChemicalTooltipEvent extends PlayerEvent {

    private final TooltipFlag flags;
    private final ChemicalStack chemicalStack;
    private final List<Component> toolTip;
    private final TooltipContext context;

    /// This event is fired in [ChemicalStack#getTooltipLines(TooltipContext, Player, TooltipFlag)], which in turn is called from its respective modded GUI. Tooltips may
    /// also be gathered with a `null` player during startup by other mods.
    @SuppressWarnings("DataFlowIssue")//Mirrors FluidTooltipEvent
    public ChemicalTooltipEvent(ChemicalStack chemicalStack, @Nullable Player player, List<Component> list, TooltipFlag flags, TooltipContext context) {
        super(player);
        this.chemicalStack = chemicalStack;
        this.toolTip = list;
        this.flags = flags;
        this.context = context;
    }

    /// Use to determine if the advanced information on fluid tooltips is being shown, toggled by F3+H.
    public TooltipFlag getFlags() {
        return flags;
    }

    /// The [ChemicalStack] with the tooltip.
    public ChemicalStack getChemicalStack() {
        return chemicalStack;
    }

    /// The [ChemicalStack] tooltip.
    public List<Component> getToolTip() {
        return toolTip;
    }

    /// This event is fired with a null player during startup when populating search trees for tooltips.
    @Nullable
    @Override
    @SuppressWarnings("NullableProblems")//Mirrors FluidTooltipEvent
    public Player getEntity() {
        return super.getEntity();
    }

    /// The [`tooltip context`][TooltipContext].
    public TooltipContext getContext() {
        return context;
    }
}