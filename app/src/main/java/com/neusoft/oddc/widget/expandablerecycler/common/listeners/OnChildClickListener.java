package com.neusoft.oddc.widget.expandablerecycler.common.listeners;

public interface OnChildClickListener {

    /**
     * @param flatPos the flat position (raw index within the list of visible items in the
     *                RecyclerView of a GroupViewHolder)
     * @return false if click expanded group, true if click collapsed group
     */
    boolean onChildClick(int flatPos);
}