package mekanism.common.integration.crafttweaker.tag;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.actions.tags.ActionTagAdd;
import com.blamejared.crafttweaker.impl.actions.tags.ActionTagCreate;
import com.blamejared.crafttweaker.impl.actions.tags.ActionTagRemove;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.manager.TagManager;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.providers.IChemicalProvider;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.Tag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_CHEMICAL_TAG_MANAGER)
public abstract class CrTChemicalTagManager<CHEMICAL extends Chemical<CHEMICAL>, CRT_CHEMICAL extends ICrTChemical<CHEMICAL, ?, CRT_CHEMICAL, ?>>
      implements TagManager<CRT_CHEMICAL> {

    private final ChemicalTags<CHEMICAL> chemicalTags;

    protected CrTChemicalTagManager(ChemicalTags<CHEMICAL> chemicalTags) {
        this.chemicalTags = chemicalTags;
    }

    @Override
    public void addElements(MCTag<CRT_CHEMICAL> to, List<CRT_CHEMICAL> toAdd) {
        ITag<CHEMICAL> internal = getInternal(to);
        List<CHEMICAL> itemsFromDefinitions = getChemicals(toAdd);
        if (internal == null) {
            Tag<CHEMICAL> tagFromContents = Tag.getTagFromContents(Sets.newHashSet(itemsFromDefinitions));
            CraftTweakerAPI.apply(new ActionTagCreate<>(getTagCollection(), tagFromContents, to));
        } else {
            CraftTweakerAPI.apply(new ActionTagAdd<>(internal, itemsFromDefinitions, to));
        }
    }

    @Override
    public void removeElements(MCTag<CRT_CHEMICAL> from, List<CRT_CHEMICAL> toRemove) {
        ITag<CHEMICAL> internal = getInternal(from);
        List<CHEMICAL> chemicals = getChemicals(toRemove);
        CraftTweakerAPI.apply(new ActionTagRemove<>(internal, chemicals, from));
    }

    private List<CHEMICAL> getChemicals(List<CRT_CHEMICAL> toConvert) {
        return toConvert.stream().map(IChemicalProvider::getChemical).collect(Collectors.toList());
    }

    @Override
    public List<CRT_CHEMICAL> getElementsInTag(MCTag<CRT_CHEMICAL> theTag) {
        ITag<CHEMICAL> internal = getInternal(theTag);
        if (internal == null) {
            return Collections.emptyList();
        }
        return internal.getAllElements().stream().map(this::fromChemical).collect(Collectors.toList());
    }

    protected abstract CRT_CHEMICAL fromChemical(CHEMICAL chemical);

    @Override
    public ITagCollection<CHEMICAL> getTagCollection() {
        return chemicalTags.getCollection();
    }

    @Nullable
    @Override
    public ITag<CHEMICAL> getInternal(MCTag<CRT_CHEMICAL> theTag) {
        return getTagCollection().get(theTag.getIdInternal());
    }
}