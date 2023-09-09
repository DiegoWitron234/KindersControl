package com.miraimx.kinderscontrol

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.lang.Integer.max
import java.lang.Integer.min

class AdapterObserver(private val recyclerView: RecyclerView): RecyclerView.AdapterDataObserver() {
    override fun onChanged() {
        super.onChanged()
        adjustRecyclerViewHeight()
    }

    private fun adjustRecyclerViewHeight() {
        val desiredHeight = 200.dpToPx()
        val contentHeight = calculateContentHeight(recyclerView)

        val layoutParams = recyclerView.layoutParams
        layoutParams.height = max(desiredHeight, contentHeight)
        recyclerView.layoutParams = layoutParams
    }

    private fun Int.dpToPx(): Int {
        val scale = recyclerView.resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
    private fun calculateContentHeight(recyclerView: RecyclerView): Int {
        val layoutManager = recyclerView.layoutManager
        val adapter = recyclerView.adapter

        if (layoutManager != null && adapter != null) {
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                recyclerView.width,
                View.MeasureSpec.EXACTLY
            )

            var totalHeight = 0
            for (i in 0 until adapter.itemCount) {
                val view = layoutManager.findViewByPosition(i)
                view?.measure(widthMeasureSpec, heightMeasureSpec)
                totalHeight += view?.measuredHeight ?: 0
            }

            return totalHeight
        }

        return 0
    }
}