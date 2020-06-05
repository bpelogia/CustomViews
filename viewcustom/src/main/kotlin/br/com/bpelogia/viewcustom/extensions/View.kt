package br.com.bpelogia.viewcustom.extensions

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView

/**
 * @author Bruno Pelogia
 * @since 09/01/2018
 */
fun View.expandMore(vararg views: View) {
    views.filter { it.visibility != View.VISIBLE && (it.animation == null || it.animation.hasEnded()) }
            .forEach { it.expand() }
}

fun View.collapseMore(vararg views: View) {
    views.filter { it.visibility != View.GONE && (it.animation == null || it.animation.hasEnded()) }
            .forEach { it.collapse() }
}

fun View.expand(showView: View? = null) {
    this.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    val targetHeight = this.measuredHeight

    // cancel runnning animations and remove and listeners
    this.animate().cancel()
    this.animate().setListener(null)

    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    this.layoutParams.height = 1
    this.visibility = View.VISIBLE
    showView?.visibility = View.GONE

    val a = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            this@expand.layoutParams.height = if (interpolatedTime == 1f)
                LinearLayout.LayoutParams.WRAP_CONTENT
            else
                (targetHeight * interpolatedTime).toInt()
            this@expand.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }
    // 1dp/ms
    a.duration = 500
    this.startAnimation(a)
}


fun View.collapse(showView: View? = null) {
    val initialHeight = this.measuredHeight
    val a = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            if (interpolatedTime == 1f) {
                this@collapse.visibility = View.GONE
            } else {
                this@collapse.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                this@collapse.requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }
    // 1dp/ms
    a.duration = 500
    a.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {}

        override fun onAnimationEnd(animation: Animation) {
                showView?.expand()
        }

        override fun onAnimationRepeat(animation: Animation) {}
    })
    this.startAnimation(a)
}


fun View.moveViewDown(initialState: Boolean) {
    if (this.animation == null || this.animation.hasEnded()) {
        if (initialState) {
            this.animate()
                    .translationY(0f).duration = 500
        } else {
            this.animate()
                    .translationY(this.height.toFloat()).duration = 500
        }
    }
}

fun View.moveViewRight(initialState: Boolean) {
    if (this.animation == null || this.animation.hasEnded()) {
        if (initialState) {
            this.animate()
                    .translationX(0f).duration = 500
        } else {
            this.animate()
                    .translationX(this.width.toFloat()).duration = 500
        }
    }
}

fun View.moveViewLeft(initialState: Boolean) {
    if (this.animation == null || this.animation.hasEnded()) {
        if (initialState) {
            this.animate()
                    .translationX(0f).duration = 500
        } else {
            this.animate()
                    .translationX((-this.width).toFloat()).duration = 500
        }
    }
}

fun View.moveViewTop(initialState: Boolean) {
    if (this.animation == null || this.animation.hasEnded()) {
        if (initialState) {
            this.animate()
                    .translationY(0f).duration = 500
        } else {
            this.animate()
                    .translationY((-this.height).toFloat()).duration = 500
        }
    }
}

fun NestedScrollView.moveDownViewOnScrolling(view: View, showOrHideViews: ((scaleX: Float, scaleY: Float) -> Unit)? = null) {
    this.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, horizontalScrollPosition, verticalScrollPosition, previousHorizontalScrollPosition, previousVerticalScrollPosition ->
        val scrollTotalHeight = v.getChildAt(0).height - v.height
        if (verticalScrollPosition < previousVerticalScrollPosition && verticalScrollPosition < scrollTotalHeight) {
            view.moveViewDown(true)
            showOrHideViews?.invoke(1f,1f)
        } else if (verticalScrollPosition >= previousVerticalScrollPosition && verticalScrollPosition >= scrollTotalHeight - 650) {
            view.moveViewDown(false)
            showOrHideViews?.invoke(0f,0f)
        }
    })
}

fun NestedScrollView.moveDownViewOnScrolling(view: View, vararg showOrHideViews: View) {
    this.moveDownViewOnScrolling(view, { scaleX, scaleY ->
        for (v in showOrHideViews)
            v.animate()
                    .scaleX(scaleX)
                    .scaleY(scaleY)
    })
}

/*
fun showOrHideViewOnScrolling(scrollView: NestedScrollView, view: View, vararg showOrHideViews: View) {
    scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, horizontalScrollPosition, verticalScrollPosition, previousHorizontalScrollPosition, previousVerticalScrollPosition ->
        val scrollTotalHeight = v.getChildAt(0).height - v.height
        if (verticalScrollPosition < previousVerticalScrollPosition && verticalScrollPosition < scrollTotalHeight) {
            view.moveViewDown(true)
            for (view in showOrHideViews)
                view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
        } else if (verticalScrollPosition >= previousVerticalScrollPosition && verticalScrollPosition >= scrollTotalHeight - 550) {
            view.moveViewDown(false)
            for (view in showOrHideViews)
                view.animate()
                        .scaleX(0f)
                        .scaleY(0f)
        }
    })
}*/
