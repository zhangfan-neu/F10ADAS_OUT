package com.neusoft.oddc.ui.setting;


import com.neusoft.oddc.widget.expandablerecycler.common.models.ExpandableGroup;

import java.util.List;

public class EntitySettingGroup extends ExpandableGroup {

    private Class clazz;

    public EntitySettingGroup(String title, Class clazz, List<String> items) {
        super(title, items);
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }

}
