package com.neusoft.oddc.widget.recycler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.neusoft.oddc.R;


public class DefaultDividerDecoration extends RecyclerView.ItemDecoration {

    private Drawable divider;

    public DefaultDividerDecoration(Context context) {
        divider = context.getResources().getDrawable(R.drawable.material_recyclerview_divider);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        drawVertical(c, parent);
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int recyclerViewTop = parent.getPaddingTop();
        final int recyclerViewBottom = parent.getHeight() - parent.getPaddingBottom();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            // Draw divider
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = Math.max(recyclerViewTop, child.getBottom() + params.bottomMargin);
            final int bottom = Math.min(recyclerViewBottom, top + divider.getIntrinsicHeight());
            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0, 0, 0, divider.getIntrinsicHeight());
    }

}
