package com.neusoft.oddc.widget.expandablerecycler.checker.models;

import android.os.Parcel;

import java.util.List;

/**
 * A subclass of {@link CheckedExpandableGroup} that allows for only *one* child to be checked at
 * one time
 */
public class SingleCheckExpandableGroup extends CheckedExpandableGroup {

    @SuppressWarnings("unused")
    public static final Creator<SingleCheckExpandableGroup> CREATOR =
            new Creator<SingleCheckExpandableGroup>() {
                @Override
                public SingleCheckExpandableGroup createFromParcel(Parcel in) {
                    return new SingleCheckExpandableGroup(in);
                }

                @Override
                public SingleCheckExpandableGroup[] newArray(int size) {
                    return new SingleCheckExpandableGroup[size];
                }
            };

    public SingleCheckExpandableGroup(String title, List items) {
        super(title, items);
    }

    protected SingleCheckExpandableGroup(Parcel in) {
        super(in);
    }

    @Override
    public void onChildClicked(int childIndex, boolean checked) {
        if (checked) {
            for (int i = 0; i < getItemCount(); i++) {
                unCheckChild(i);
            }
            checkChild(childIndex);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
