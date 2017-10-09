package com.neusoft.oddc.widget.expandablerecycler.common.listeners;


import com.neusoft.oddc.widget.expandablerecycler.common.models.ExpandableGroup;

public interface GroupExpandCollapseListener {

    /**
     * Called when a group is expanded
     *
     * @param group the {@link ExpandableGroup} being expanded
     */
    void onGroupExpanded(ExpandableGroup group);

    /**
     * Called when a group is collapsed
     *
     * @param group the {@link ExpandableGroup} being collapsed
     */
    void onGroupCollapsed(ExpandableGroup group);
}
