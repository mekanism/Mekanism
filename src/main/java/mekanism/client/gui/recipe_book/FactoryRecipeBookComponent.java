package mekanism.client.gui.recipe_book;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.common.inventory.container.tile.FactoryContainer;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.HolderGetter.Provider;

public class FactoryRecipeBookComponent<MENU extends FactoryContainer> extends MekanismRecipeBookComponent<MENU> {

    private final Int2ObjectMap<List<IGhostRecipeComponent>> ghostInputs;
    private final Int2ObjectMap<List<IGhostRecipeComponent>> ghostOutputs;
    private int currentIndex;
    private boolean seenOutput;

    public FactoryRecipeBookComponent(MENU menu, TileEntityFactory<?> tile, Provider holderLookup, Supplier<Font> fontSupplier) {
        ghostInputs = new Int2ObjectArrayMap<>(tile.tier.processes);
        ghostOutputs = new Int2ObjectArrayMap<>(tile.tier.processes);
        super(menu, tile, holderLookup, fontSupplier);
    }

    @Override
    public void addComponent(IGhostRecipeComponent component, BookComponentType type) {
        if (type.isResult()) {
            seenOutput = true;
            List<IGhostRecipeComponent> outputs = this.ghostOutputs.computeIfAbsent(currentIndex, _ -> new ArrayList<>());
            outputs.add(component);
        } else if (type == BookComponentType.INPUT) {
            if (seenOutput) {
                seenOutput = false;
                currentIndex++;
            }
            List<IGhostRecipeComponent> inputs = this.ghostInputs.computeIfAbsent(currentIndex, _ -> new ArrayList<>());
            inputs.add(component);
        } else {
            //Add the extra one to all inputs
            for (List<IGhostRecipeComponent> inputs : ghostInputs.values()) {
                inputs.add(component);
            }
        }
    }

    @Override
    protected List<IGhostRecipeComponent> ghostInputs(int cacheIndex) {
        return ghostInputs.getOrDefault(cacheIndex, Collections.emptyList());
    }

    @Override
    protected List<IGhostRecipeComponent> ghostOutputs(int cacheIndex) {
        return ghostOutputs.getOrDefault(cacheIndex, Collections.emptyList());
    }

    @Override
    public void init(Minecraft minecraft) {
        super.init(minecraft);
        this.ghostInputs.clear();
        this.ghostOutputs.clear();
        this.currentIndex = 0;
        this.seenOutput = false;
    }
}