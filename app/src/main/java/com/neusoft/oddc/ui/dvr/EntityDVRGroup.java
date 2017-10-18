package com.neusoft.oddc.ui.dvr;


import com.neusoft.oddc.widget.expandablerecycler.common.models.ExpandableGroup;

import java.util.List;

public class EntityDVRGroup extends ExpandableGroup {

    public EntityDVRGroup(String title, List<EntityDVRChild> items) {
        super(title, items);
    }


}
