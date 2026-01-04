package mekanism.client.render.obj;

import java.util.Collection;

public class VisibleModelConfiguration extends WrapperModelConfiguration {

    private final Collection<String> visibleGroups;

    public VisibleModelConfiguration(IGeometryBakingContext internal, Collection<String> visibleGroups) {
        super(internal);
        this.visibleGroups = visibleGroups;
    }

    @Override
    public boolean isComponentVisible(String component, boolean fallback) {
        //Ignore fallback as we always have a true or false answer
        return visibleGroups.contains(component);
    }
}