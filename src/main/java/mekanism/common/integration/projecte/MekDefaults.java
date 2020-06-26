/*package mekanism.common.integration.projecte;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.Collections;
import java.util.List;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.common.Tags.Items;

@EMCMapper
public class MekDefaults implements IEMCMapper<NormalizedSimpleStack, Long> {

    @Override
    public String getName() {
        return "MekDefaults";
    }

    @Override
    public String getDescription() {
        return "Adds default values for some base resources that are added in Mekanism.";
    }

    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, CommentedFileConfig config, IResourceManager resourceManager) {
        //Set EMC values to what they were set in ProjectE in 1.12
        mapper.setValueBefore(NSSItem.createItem(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)), 512L);
        List<NormalizedSimpleStack> iron = Collections.singletonList(NSSItem.createTag(Items.INGOTS_IRON));
        mapper.setValueFromConversion(2, NSSItem.createItem(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.COPPER)), iron);
        mapper.setValueFromConversion(1, NSSItem.createItem(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN)), iron);
        //TODO: Default value for salt?
    }
}*/