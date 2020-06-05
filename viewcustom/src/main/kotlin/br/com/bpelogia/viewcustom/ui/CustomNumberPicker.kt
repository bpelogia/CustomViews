package br.com.bpelogia.viewcustom.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.InputType
import android.text.Spanned
import android.text.TextUtils
import android.text.method.NumberKeyListener
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Scroller
import androidx.annotation.*
import androidx.core.content.ContextCompat
import br.com.bpelogia.viewcustom.R
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * A widget that enables the user to select a number form a predefined range.
 */
class CustomNumberPicker
/**
 * Create a new number picker
 *
 * @param mContext the application environment.
 * @param attrs a collection of attributes.
 * @param defStyle The default style to apply to this view.
 */
@JvmOverloads constructor(
        /**
         * The context of this widget.
         */
        private val mContext: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(mContext, attrs) {

    /**
     * The text for showing the current value.
     */
    private val mSelectedText: EditText

    /**
     * The min height of this widget.
     */
    private var mMinHeight: Int = 0

    /**
     * The max height of this widget.
     */
    private var mMaxHeight: Int = 0

    /**
     * The max width of this widget.
     */
    private var mMinWidth: Int = 0

    /**
     * The max width of this widget.
     */
    private var mMaxWidth: Int = 0

    /**
     * Flag whether to compute the max width.
     */
    private val mComputeMaxWidth: Boolean


    /**
     * The color of the selected text.
     */
    private var mSelectedTextColor = DEFAULT_TEXT_COLOR

    /**
     * The color of the text.
     */
    private var mTextColor = DEFAULT_TEXT_COLOR

    /**
     * The color of the text.
     */
    private var mSecondTextColor = mTextColor

    /**
     * The color of the text.
     */
    private var mSecondTextColorIndex = -1

    /**
     * The size of the text.
     */
    private var mTextSize = DEFAULT_TEXT_SIZE

    /**
     * The typeface of the text.
     */
    private var mTypeface: Typeface? = null

    /**
     * The width of the gap between text elements if the selector wheel.
     */
    private var mSelectorTextGapWidth: Int = 0

    /**
     * The height of the gap between text elements if the selector wheel.
     */
    private var mSelectorTextGapHeight: Int = 0

    /**
     * The values to be displayed instead the indices.
     */
    /**
     * Gets the values to be displayed instead of string values.
     *
     * @return The displayed values.
     */
    /**
     * Sets the values to be displayed.
     *
     * displayedValues The displayed values.
     *
     * **Note:** The length of the displayed values array
     * must be equal to the range of selectable numbers which is equal to
     * [.getMaxValue] - [.getMinValue] + 1.
     */
    // Allow text entry rather than strictly numeric entry.
    var displayedValues: Array<String>? = null
        set(displayedValues) {
            if (this.displayedValues == displayedValues) {
                return
            }
            field = displayedValues
            if (this.displayedValues != null) {
                mSelectedText.setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
            } else {
                mSelectedText.setRawInputType(InputType.TYPE_CLASS_NUMBER)
            }
            updateInputTextView()
            initializeSelectorWheelIndices()
            tryComputeMaxWidth()
        }

    /**
     * Lower value of the range of numbers allowed for the CustomNumberPicker
     */
    private var mMinValue = DEFAULT_MIN_VALUE

    /**
     * Upper value of the range of numbers allowed for the CustomNumberPicker
     */
    private var mMaxValue = DEFAULT_MAX_VALUE

    /**
     * Current value of this CustomNumberPicker
     */
    private var mValue: Int = 0

    /**
     * Listener to be notified upon current value change.
     */
    private var mOnValueChangeListener: OnValueChangeListener? = null

    /**
     * Listener to be notified upon scroll state change.
     */
    private var mOnScrollListener: OnScrollListener? = null

    /**
     * Formatter for for displaying the current value.
     */
    private var mFormatter: Formatter? = null

    /**
     * The speed for updating the value form long press.
     */
    private var mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL

    /**
     * Cache for the string representation of selector indices.
     */
    private val mSelectorIndexToStringCache = SparseArray<String>()

    /**
     * The number of items show in the selector wheel.
     */
    private var mWheelItemCount = 3

    /**
     * The index of the middle selector item.
     */
    private var mWheelMiddleItemIndex = mWheelItemCount / 2

    /**
     * The selector indices whose value are show by the selector.
     */
    private var mSelectorIndices = IntArray(mWheelItemCount)

    /**
     * The [Paint] for drawing the selector.
     */
    private val mSelectorWheelPaint: Paint

    /**
     * The size of a selector element (text + gap).
     */
    private var mSelectorElementSize: Int = 0

    /**
     * The initial offset of the scroll selector.
     */
    private var mInitialScrollOffset = Integer.MIN_VALUE

    /**
     * The current offset of the scroll selector.
     */
    private var mCurrentScrollOffset: Int = 0

    /**
     * The [Scroller] responsible for flinging the selector.
     */
    private val mFlingScroller: Scroller

    /**
     * The [Scroller] responsible for adjusting the selector.
     */
    private val mAdjustScroller: Scroller

    /**
     * The previous X coordinate while scrolling the selector.
     */
    private var mPreviousScrollerX: Int = 0

    /**
     * The previous Y coordinate while scrolling the selector.
     */
    private var mPreviousScrollerY: Int = 0

    /**
     * Handle to the reusable command for setting the input text selection.
     */
    private var mSetSelectionCommand: SetSelectionCommand? = null

    /**
     * Handle to the reusable command for changing the current value from long press by one.
     */
    private var mChangeCurrentByOneFromLongPressCommand: ChangeCurrentByOneFromLongPressCommand? = null

    /**
     * The X position of the last down event.
     */
    private var mLastDownEventX: Float = 0.toFloat()

    /**
     * The Y position of the last down event.
     */
    private var mLastDownEventY: Float = 0.toFloat()

    /**
     * The X position of the last down or move event.
     */
    private var mLastDownOrMoveEventX: Float = 0.toFloat()

    /**
     * The Y position of the last down or move event.
     */
    private var mLastDownOrMoveEventY: Float = 0.toFloat()

    /**
     * Determines speed during touch scrolling.
     */
    private var mVelocityTracker: VelocityTracker? = null

    /**
     * @see ViewConfiguration.getScaledTouchSlop
     */
    private val mTouchSlop: Int

    /**
     * @see ViewConfiguration.getScaledMinimumFlingVelocity
     */
    private val mMinimumFlingVelocity: Int

    /**
     * @see ViewConfiguration.getScaledMaximumFlingVelocity
     */
    private val mMaximumFlingVelocity: Int

    /**
     * Flag whether the selector should wrap around.
     */
    private var mWrapSelectorWheel: Boolean = false

    /**
     * Divider for showing item to be selected while scrolling
     */
    private var mSelectionDivider: Drawable? = null

    /**
     * The color of the selection divider.
     */
    private var mSelectionDividerColor: Int = 0

    /**
     * The distance between the two selection dividers.
     */
    private var mSelectionDividersDistance: Int = 0

    /**
     * The thickness of the selection divider.
     */
    private var mSelectionDividerThickness: Int = 0

    /**
     * The current scroll state of the number picker.
     */
    private var mScrollState = OnScrollListener.SCROLL_STATE_IDLE

    /**
     * The top of the top selection divider.
     */
    private var mTopSelectionDividerTop: Int = 0

    /**
     * The bottom of the bottom selection divider.
     */
    private var mBottomSelectionDividerBottom: Int = 0

    /**
     * The left of the top selection divider.
     */
    private var mLeftOfSelectionDividerLeft: Int = 0

    /**
     * The right of the bottom selection divider.
     */
    private var mRightOfSelectionDividerRight: Int = 0

    /**
     * The keycode of the last handled DPAD down event.
     */
    private var mLastHandledDownDpadKeyCode = -1

    /**
     * The width of this widget.
     */
    private val mWidth: Float

    /**
     * The height of this widget.
     */
    private val mHeight: Float

    /**
     * The orientation of this widget.
     */
    private var mOrientation: Int = HORIZONTAL

    /**
     * Gets whether the selector wheel wraps when reaching the min/max value.
     *
     * @return True if the selector wheel wraps.
     *
     * @see .getMinValue
     * @see .getMaxValue
     */
    /**
     * Sets whether the selector wheel shown during flinging/scrolling should
     * wrap around the [CustomNumberPicker.minValue] and
     * [CustomNumberPicker.maxValue] values.
     *
     *
     * By default if the range (max - min) is more than the number of items shown
     * on the selector wheel the selector wheel wrapping is enabled.
     *
     *
     *
     * **Note:** If the number of items, i.e. the range (
     * [.getMaxValue] - [.getMinValue]) is less than
     * the number of items shown on the selector wheel, the selector wheel will
     * not wrap. Hence, in such a case calling this method is a NOP.
     *
     *
     * wrapSelectorWheel Whether to wrap.
     */
    var wrapSelectorWheel: Boolean
        get() = mWrapSelectorWheel
        set(wrapSelectorWheel) {
            val wrappingAllowed = mMaxValue - mMinValue >= mSelectorIndices.size
            if ((!wrapSelectorWheel || wrappingAllowed) && wrapSelectorWheel != mWrapSelectorWheel) {
                mWrapSelectorWheel = wrapSelectorWheel
            }
        }

    /**
     * Returns the value of the picker.
     *
     * @return The value.
     */
    /**
     * Set the current value for the number picker.
     *
     *
     * If the argument is less than the [CustomNumberPicker.minValue] and
     * [CustomNumberPicker.wrapSelectorWheel] is `false` the
     * current value is set to the [CustomNumberPicker.minValue] value.
     *
     *
     *
     * If the argument is less than the [CustomNumberPicker.minValue] and
     * [CustomNumberPicker.wrapSelectorWheel] is `true` the
     * current value is set to the [CustomNumberPicker.maxValue] value.
     *
     *
     *
     * If the argument is less than the [CustomNumberPicker.maxValue] and
     * [CustomNumberPicker.wrapSelectorWheel] is `false` the
     * current value is set to the [CustomNumberPicker.maxValue] value.
     *
     *
     *
     * If the argument is less than the [CustomNumberPicker.maxValue] and
     * [CustomNumberPicker.wrapSelectorWheel] is `true` the
     * current value is set to the [CustomNumberPicker.minValue] value.
     *
     *
     * value The current value.
     * @see .setWrapSelectorWheel
     * @see .setMinValue
     * @see .setMaxValue
     */
    var value: Int
        get() = mValue
        set(value) = setValueInternal(value, false)

    /**
     * Returns the min value of the picker.
     *
     * @return The min value
     */
    /**
     * Sets the min value of the picker.
     *
     * minValue The min value inclusive.
     *
     * **Note:** The length of the displayed values array
     * set via [.setDisplayedValues] must be equal to the
     * range of selectable numbers which is equal to
     * [.getMaxValue] - [.getMinValue] + 1.
     */
    //        if (minValue < 0) {
    //            throw new IllegalArgumentException("minValue must be >= 0");
    //        }
    var minValue: Int
        get() = mMinValue
        set(minValue) {
            mMinValue = minValue
            if (mMinValue > mValue) {
                mValue = mMinValue
            }
            val wrapSelectorWheel = mMaxValue - mMinValue > mSelectorIndices.size
            this.wrapSelectorWheel = wrapSelectorWheel
            initializeSelectorWheelIndices()
            updateInputTextView()
            tryComputeMaxWidth()
            invalidate()
        }

    /**
     * Returns the max value of the picker.
     *
     * @return The max value.
     */
    /**
     * Sets the max value of the picker.
     *
     * maxValue The max value inclusive.
     *
     * **Note:** The length of the displayed values array
     * set via [.setDisplayedValues] must be equal to the
     * range of selectable numbers which is equal to
     * [.getMaxValue] - [.getMinValue] + 1.
     */
    var maxValue: Int
        get() = mMaxValue
        set(maxValue) {
            if (maxValue < 0) {
                throw IllegalArgumentException("maxValue must be >= 0")
            }
            mMaxValue = maxValue
            if (mMaxValue < mValue) {
                mValue = mMaxValue
            }

            val wrapSelectorWheel = mMaxValue - mMinValue > mSelectorIndices.size
            this.wrapSelectorWheel = wrapSelectorWheel
            initializeSelectorWheelIndices()
            updateInputTextView()
            tryComputeMaxWidth()
            invalidate()
        }

    private val isHorizontalMode: Boolean
        get() = mOrientation == HORIZONTAL

    var dividerColor: Int
        get() = mSelectionDividerColor
        set(@ColorInt color) {
            mSelectionDividerColor = color
            mSelectionDivider = ColorDrawable(color)
        }

    val dividerDistance: Float
        get() = pxToDp(mSelectionDividersDistance.toFloat())

    val dividerThickness: Float
        get() = pxToDp(mSelectionDividerThickness.toFloat())

    private var wheelItemCount: Int
        get() = mWheelItemCount
        set(count) {
            mWheelItemCount = count
            mWheelMiddleItemIndex = mWheelItemCount / 2
            mSelectorIndices = IntArray(mWheelItemCount)
        }

    /**
     * Set the formatter to be used for formatting the current value.
     *
     *
     * Note: If you have provided alternative values for the values this
     * formatter is never invoked.
     *
     *
     * formatter The formatter object. If formatter is `null`,
     * [String.valueOf] will be used.
     * @see .setDisplayedValues
     */
    var formatter: Formatter?
        get() = mFormatter
        set(formatter) {
            if (formatter === mFormatter) {
                return
            }
            mFormatter = formatter
            initializeSelectorWheelIndices()
            updateInputTextView()
        }

    var selectedTextColor: Int
        get() = mSelectedTextColor
        set(@ColorInt color) {
            mSelectedTextColor = color
            mSelectedText.setTextColor(mSelectedTextColor)
        }

    var textColor: Int
        get() = mTextColor
        set(@ColorInt color) {
            mTextColor = color
            mSelectorWheelPaint.color = mTextColor
        }

    var textSize: Float
        get() = spToPx(mTextSize)
        set(textSize) {
            mTextSize = textSize
            mSelectedText.textSize = pxToSp(mTextSize)
            mSelectorWheelPaint.textSize = mTextSize
        }

    var typeface: Typeface?
        get() = mTypeface
        set(typeface) {
            mTypeface = typeface
            if (mTypeface != null) {
                mSelectedText.typeface = mTypeface
                mSelectorWheelPaint.typeface = mTypeface
            } else {
                mSelectedText.typeface = Typeface.MONOSPACE
                mSelectorWheelPaint.typeface = Typeface.MONOSPACE
            }
        }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(VERTICAL, HORIZONTAL)
    annotation class Orientation

    /**
     * Use a custom CustomNumberPicker formatting callback to use two-digit minutes
     * strings like "01". Keeping a static formatter etc. is the most efficient
     * way to do this; it avoids creating temporary objects on every call to
     * format().
     */
    private class TwoDigitFormatter internal constructor() : Formatter {
        internal val mBuilder = StringBuilder()

        internal var mZeroDigit: Char = ' '
        internal lateinit var mFmt: java.util.Formatter

        internal val mArgs = arrayOfNulls<Any>(1)

        init {
            val locale = Locale.getDefault()
            init(locale)
        }

        private fun init(locale: Locale) {
            mFmt = createFormatter(locale)
            mZeroDigit = getZeroDigit(locale)
        }

        override fun format(value: Int): String {
            val currentLocale = Locale.getDefault()
            if (mZeroDigit != getZeroDigit(currentLocale)) {
                init(currentLocale)
            }
            mArgs[0] = value
            mBuilder.delete(0, mBuilder.length)
            mFmt.format("%02d", *mArgs)
            return mFmt.toString()
        }

        private fun getZeroDigit(locale: Locale): Char {
            // return LocaleData.get(locale).zeroDigit;
            return DecimalFormatSymbols(locale).zeroDigit
        }

        private fun createFormatter(locale: Locale): java.util.Formatter {
            return java.util.Formatter(mBuilder, locale)
        }
    }

    /**
     * Interface to listen for changes of the current value.
     */
    interface OnValueChangeListener {

        /**
         * Called upon a change of the current value.
         *
         * @param picker The CustomNumberPicker associated with this listener.
         * @param oldVal The previous value.
         * @param newVal The new value.
         */
        fun onValueChange(picker: CustomNumberPicker, oldVal: Int, newVal: Int)
    }

    /**
     * Interface to listen for the picker scroll state.
     */
    interface OnScrollListener {

        /**
         * Callback invoked while the number picker scroll state has changed.
         *
         * @param view The view whose scroll state is being reported.
         * @param scrollState The current scroll state. One of
         * [.SCROLL_STATE_IDLE],
         * [.SCROLL_STATE_TOUCH_SCROLL] or
         * [.SCROLL_STATE_IDLE].
         */
        fun onScrollStateChange(view: CustomNumberPicker, scrollState: Int)

        companion object {

            /**
             * The view is not scrolling.
             */
            val SCROLL_STATE_IDLE = 0

            /**
             * The user is scrolling using touch, and his finger is still on the screen.
             */
            val SCROLL_STATE_TOUCH_SCROLL = 1

            /**
             * The user had previously been scrolling using touch and performed a fling.
             */
            val SCROLL_STATE_FLING = 2
        }
    }

    /**
     * Interface used to format current value into a string for presentation.
     */
    interface Formatter {

        /**
         * Formats a string representation of the current value.
         *
         * @param value The currently selected value.
         * @return A formatted string representation.
         */
        fun format(value: Int): String
    }

    init {

        val attributesArray = mContext.obtainStyledAttributes(attrs, R.styleable.CustomNumberPicker, defStyle, 0)

        mSelectionDivider = ContextCompat.getDrawable(mContext, R.drawable.np_numberpicker_selection_divider)

        mSelectionDividerColor = attributesArray.getColor(R.styleable.CustomNumberPicker_np_dividerColor, mSelectionDividerColor)

        if(background == null) {
            val backgroundRetangle = ContextCompat.getDrawable(mContext, R.drawable.retangle_layout_picker)
            backgroundRetangle?.colorFilter = PorterDuffColorFilter(mSelectionDividerColor, PorterDuff.Mode.SRC_IN)
            background = backgroundRetangle
        }

        val defSelectionDividerDistance = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, UNSCALED_DEFAULT_SELECTION_DIVIDERS_DISTANCE.toFloat(),
                resources.displayMetrics).toInt()
        mSelectionDividersDistance = attributesArray.getDimensionPixelSize(
                R.styleable.CustomNumberPicker_np_dividerDistance, defSelectionDividerDistance)

        val defSelectionDividerThickness = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, UNSCALED_DEFAULT_SELECTION_DIVIDER_THICKNESS.toFloat(),
                resources.displayMetrics).toInt()
        mSelectionDividerThickness = attributesArray.getDimensionPixelSize(
                R.styleable.CustomNumberPicker_np_dividerThickness, defSelectionDividerThickness)

        mOrientation = attributesArray.getInt(R.styleable.CustomNumberPicker_np_orientation, VERTICAL)

        mWidth = attributesArray.getDimensionPixelSize(R.styleable.CustomNumberPicker_np_width, SIZE_UNSPECIFIED).toFloat()
        mHeight = attributesArray.getDimensionPixelSize(R.styleable.CustomNumberPicker_np_height, SIZE_UNSPECIFIED).toFloat()

        setWidthAndHeight()

        mComputeMaxWidth = true

        mValue = attributesArray.getInt(R.styleable.CustomNumberPicker_np_value, mValue)
        mMaxValue = attributesArray.getInt(R.styleable.CustomNumberPicker_np_max, mMaxValue)
        mMinValue = attributesArray.getInt(R.styleable.CustomNumberPicker_np_min, mMinValue)

        mSelectedTextColor = attributesArray.getColor(R.styleable.CustomNumberPicker_np_selectedTextColor,
                if(mSelectedTextColor == DEFAULT_TEXT_COLOR) ContextCompat.getColor(mContext, R.color.colorAccent) else mSelectedTextColor)
        mTextColor = attributesArray.getColor(R.styleable.CustomNumberPicker_np_textColor, mTextColor)
        mTextSize = attributesArray.getDimension(R.styleable.CustomNumberPicker_np_textSize, spToPx(mTextSize))
        mTypeface = Typeface.create(attributesArray.getString(R.styleable.CustomNumberPicker_np_typeface), Typeface.NORMAL)
        mFormatter = stringToFormatter(attributesArray.getString(R.styleable.CustomNumberPicker_np_formatter))
        mWheelItemCount = attributesArray.getInt(R.styleable.CustomNumberPicker_np_wheelItemCount, mWheelItemCount)

        // By default Linearlayout that we extend is not drawn. This is
        // its draw() method is not called but dispatchDraw() is called
        // directly (see ViewGroup.drawChild()). However, this class uses
        // the fading edge effect implemented by View and we need our
        // draw() method to be called. Therefore, we declare we will draw.
        setWillNotDraw(false)

        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.number_picker_with_selector_wheel, this, true)

        // input text
        mSelectedText = findViewById(R.id.np_numberpicker_input)
        mSelectedText.isEnabled = false
        mSelectedText.isFocusable = false
        mSelectedText.imeOptions = EditorInfo.IME_ACTION_NONE

        // create the selector wheel paint
        val paint = Paint()
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        mSelectorWheelPaint = paint

        selectedTextColor = mSelectedTextColor
        textColor = mTextColor
        textSize = mTextSize
        typeface = mTypeface
        formatter = mFormatter
        updateInputTextView()

        value = mValue
        maxValue = mMaxValue
        minValue = mMinValue

        dividerColor = mSelectionDividerColor

        wheelItemCount = mWheelItemCount

        mWrapSelectorWheel = attributesArray.getBoolean(R.styleable.CustomNumberPicker_np_wrapSelectorWheel, mWrapSelectorWheel)
        wrapSelectorWheel = mWrapSelectorWheel

        if (mWidth != SIZE_UNSPECIFIED.toFloat() && mHeight != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = mWidth / mMinWidth
            scaleY = mHeight / mMaxHeight
        } else if (mWidth != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = mWidth / mMinWidth
            scaleY = mWidth / mMinWidth
        } else if (mHeight != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = mHeight / mMaxHeight
            scaleY = mHeight / mMaxHeight
        }

        // initialize constants
        val configuration = ViewConfiguration.get(mContext)
        mTouchSlop = configuration.scaledTouchSlop
        mMinimumFlingVelocity = configuration.scaledMinimumFlingVelocity
        mMaximumFlingVelocity = configuration.scaledMaximumFlingVelocity / SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT

        // create the fling and adjust scrollers
        mFlingScroller = Scroller(mContext, null, true)
        mAdjustScroller = Scroller(mContext, DecelerateInterpolator(2.5f))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // If not explicitly specified this view is important for accessibility.
            if (importantForAccessibility == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            }
        }

        attributesArray.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val msrdWdth = measuredWidth
        val msrdHght = measuredHeight

        // Input text centered horizontally.
        val inptTxtMsrdWdth = mSelectedText.measuredWidth
        val inptTxtMsrdHght = mSelectedText.measuredHeight
        val inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2
        val inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2
        val inptTxtRight = inptTxtLeft + inptTxtMsrdWdth
        val inptTxtBottom = inptTxtTop + inptTxtMsrdHght
        mSelectedText.layout(inptTxtLeft, inptTxtTop, inptTxtRight, inptTxtBottom)

        if (changed) {
            // need to do all this when we know our size
            initializeSelectorWheel()
            initializeFadingEdges()

            if (isHorizontalMode) {
                mLeftOfSelectionDividerLeft = (width - mSelectionDividersDistance) / 2 - mSelectionDividerThickness
                mRightOfSelectionDividerRight = mLeftOfSelectionDividerLeft + 2 * mSelectionDividerThickness + mSelectionDividersDistance
            } else {
                mTopSelectionDividerTop = (height - mSelectionDividersDistance) / 2 - mSelectionDividerThickness
                mBottomSelectionDividerBottom = mTopSelectionDividerTop + 2 * mSelectionDividerThickness + mSelectionDividersDistance
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Try greedily to fit the max width and height.
        val newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec, mMaxWidth)
        val newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec, mMaxHeight)
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
        // Flag if we are measured with width or height less than the respective min.
        val widthSize = resolveSizeAndStateRespectingMinSize(mMinWidth, measuredWidth, widthMeasureSpec)
        val heightSize = resolveSizeAndStateRespectingMinSize(mMinHeight, measuredHeight, heightMeasureSpec)
        setMeasuredDimension(widthSize, heightSize)
    }

    /**
     * Move to the final position of a scroller. Ensures to force finish the scroller
     * and if it is not at its final position a scroll of the selector wheel is
     * performed to fast forward to the final position.
     *
     * @param scroller The scroller to whose final position to get.
     * @return True of the a move was performed, i.e. the scroller was not in final position.
     */
    private fun moveToFinalScrollerPosition(scroller: Scroller): Boolean {
        scroller.forceFinished(true)
        if (isHorizontalMode) {
            var amountToScroll = scroller.finalX - scroller.currX
            val futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementSize
            var overshootAdjustment = mInitialScrollOffset - futureScrollOffset
            if (overshootAdjustment != 0) {
                if (Math.abs(overshootAdjustment) > mSelectorElementSize / 2) {
                    if (overshootAdjustment > 0) {
                        overshootAdjustment -= mSelectorElementSize
                    } else {
                        overshootAdjustment += mSelectorElementSize
                    }
                }
                amountToScroll += overshootAdjustment
                scrollBy(amountToScroll, 0)
                return true
            }
        } else {
            var amountToScroll = scroller.finalY - scroller.currY
            val futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementSize
            var overshootAdjustment = mInitialScrollOffset - futureScrollOffset
            if (overshootAdjustment != 0) {
                if (Math.abs(overshootAdjustment) > mSelectorElementSize / 2) {
                    if (overshootAdjustment > 0) {
                        overshootAdjustment -= mSelectorElementSize
                    } else {
                        overshootAdjustment += mSelectorElementSize
                    }
                }
                amountToScroll += overshootAdjustment
                scrollBy(0, amountToScroll)
                return true
            }
        }
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        val action = event.action and MotionEvent.ACTION_MASK
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                removeAllCallbacks()
                mSelectedText.visibility = View.INVISIBLE
                if (isHorizontalMode) {
                    mLastDownEventX = event.x
                    mLastDownOrMoveEventX = mLastDownEventX
                    // Make sure we support flinging inside scrollables.
                    parent.requestDisallowInterceptTouchEvent(true)
                    if (!mFlingScroller.isFinished) {
                        mFlingScroller.forceFinished(true)
                        mAdjustScroller.forceFinished(true)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                    } else if (!mAdjustScroller.isFinished) {
                        mFlingScroller.forceFinished(true)
                        mAdjustScroller.forceFinished(true)
                    } else if (mLastDownEventX < mLeftOfSelectionDividerLeft) {
                        postChangeCurrentByOneFromLongPress(false, ViewConfiguration.getLongPressTimeout().toLong())
                    } else if (mLastDownEventX > mRightOfSelectionDividerRight) {
                        postChangeCurrentByOneFromLongPress(true, ViewConfiguration.getLongPressTimeout().toLong())
                    }
                    return true
                } else {
                    mLastDownEventY = event.y
                    mLastDownOrMoveEventY = mLastDownEventY
                    // Make sure we support flinging inside scrollables.
                    parent.requestDisallowInterceptTouchEvent(true)
                    if (!mFlingScroller.isFinished) {
                        mFlingScroller.forceFinished(true)
                        mAdjustScroller.forceFinished(true)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                    } else if (!mAdjustScroller.isFinished) {
                        mFlingScroller.forceFinished(true)
                        mAdjustScroller.forceFinished(true)
                    } else if (mLastDownEventY < mTopSelectionDividerTop) {
                        postChangeCurrentByOneFromLongPress(false, ViewConfiguration.getLongPressTimeout().toLong())
                    } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
                        postChangeCurrentByOneFromLongPress(true, ViewConfiguration.getLongPressTimeout().toLong())
                    }
                    return true
                }
            }
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
        val action = event.action and MotionEvent.ACTION_MASK
        when (action) {
            MotionEvent.ACTION_MOVE -> {
                if (isHorizontalMode) {
                    val currentMoveX = event.x
                    if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        val deltaDownX = Math.abs(currentMoveX - mLastDownEventX).toInt()
                        if (deltaDownX > mTouchSlop) {
                            removeAllCallbacks()
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        }
                    } else {
                        val deltaMoveX = (currentMoveX - mLastDownOrMoveEventX).toInt()
                        scrollBy(deltaMoveX, 0)
                        invalidate()
                    }
                    mLastDownOrMoveEventX = currentMoveX
                } else {
                    val currentMoveY = event.y
                    if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        val deltaDownY = Math.abs(currentMoveY - mLastDownEventY).toInt()
                        if (deltaDownY > mTouchSlop) {
                            removeAllCallbacks()
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        }
                    } else {
                        val deltaMoveY = (currentMoveY - mLastDownOrMoveEventY).toInt()
                        scrollBy(0, deltaMoveY)
                        invalidate()
                    }
                    mLastDownOrMoveEventY = currentMoveY
                }
            }
            MotionEvent.ACTION_UP -> {
                removeChangeCurrentByOneFromLongPress()
                val velocityTracker = mVelocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, mMaximumFlingVelocity.toFloat())
                if (isHorizontalMode) {
                    val initialVelocity = velocityTracker.xVelocity.toInt()
                    if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
                        fling(initialVelocity)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                    } else {
                        val eventX = event.x.toInt()
                        val deltaMoveX = Math.abs(eventX - mLastDownEventX).toInt()
                        if (deltaMoveX <= mTouchSlop) { // && deltaTime < ViewConfiguration.getTapTimeout()) {
                            val selectorIndexOffset = eventX / mSelectorElementSize - mWheelMiddleItemIndex
                            when {
                                selectorIndexOffset > 0 -> changeValueByOne(true)
                                selectorIndexOffset < 0 -> changeValueByOne(false)
                                else -> ensureScrollWheelAdjusted()
                            }
                        } else {
                            ensureScrollWheelAdjusted()
                        }
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                    }
                } else {
                    val initialVelocity = velocityTracker.yVelocity.toInt()
                    if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
                        fling(initialVelocity)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                    } else {
                        val eventY = event.y.toInt()
                        val deltaMoveY = Math.abs(eventY - mLastDownEventY).toInt()
                        if (deltaMoveY <= mTouchSlop) { // && deltaTime < ViewConfiguration.getTapTimeout()) {
                            val selectorIndexOffset = eventY / mSelectorElementSize - mWheelMiddleItemIndex
                            when {
                                selectorIndexOffset > 0 -> changeValueByOne(true)
                                selectorIndexOffset < 0 -> changeValueByOne(false)
                                else -> ensureScrollWheelAdjusted()
                            }
                        } else {
                            ensureScrollWheelAdjusted()
                        }
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                    }
                }
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
            }
        }
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val action = event.action and MotionEvent.ACTION_MASK
        when (action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> removeAllCallbacks()
        }
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> removeAllCallbacks()
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_UP -> when (event.action) {
                KeyEvent.ACTION_DOWN -> if (if (mWrapSelectorWheel || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                    value < maxValue
                else
                    value > minValue) {
                    requestFocus()
                    mLastHandledDownDpadKeyCode = keyCode
                    removeAllCallbacks()
                    if (mFlingScroller.isFinished) {
                        changeValueByOne(keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                    }
                    return true
                }
                KeyEvent.ACTION_UP -> if (mLastHandledDownDpadKeyCode == keyCode) {
                    mLastHandledDownDpadKeyCode = -1
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchTrackballEvent(event: MotionEvent): Boolean {
        val action = event.action and MotionEvent.ACTION_MASK
        when (action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> removeAllCallbacks()
        }
        return super.dispatchTrackballEvent(event)
    }

    override fun computeScroll() {
        var scroller = mFlingScroller
        if (scroller.isFinished) {
            scroller = mAdjustScroller
            if (scroller.isFinished) {
                return
            }
        }
        scroller.computeScrollOffset()
        if (isHorizontalMode) {
            val currentScrollerX = scroller.currX
            if (mPreviousScrollerX == 0) {
                mPreviousScrollerX = scroller.startX
            }
            scrollBy(currentScrollerX - mPreviousScrollerX, 0)
            mPreviousScrollerX = currentScrollerX
        } else {
            val currentScrollerY = scroller.currY
            if (mPreviousScrollerY == 0) {
                mPreviousScrollerY = scroller.startY
            }
            scrollBy(0, currentScrollerY - mPreviousScrollerY)
            mPreviousScrollerY = currentScrollerY
        }
        if (scroller.isFinished) {
            onScrollerFinished(scroller)
        } else {
            invalidate()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mSelectedText.isEnabled = enabled
    }

    override fun scrollBy(x: Int, y: Int) {
        val selectorIndices = mSelectorIndices
        val gap: Int
        if (isHorizontalMode) {
            if (!mWrapSelectorWheel && x > 0
                    && selectorIndices[mWheelMiddleItemIndex] <= mMinValue) {
                mCurrentScrollOffset = mInitialScrollOffset
                return
            }
            if (!mWrapSelectorWheel && x < 0
                    && selectorIndices[mWheelMiddleItemIndex] >= mMaxValue) {
                mCurrentScrollOffset = mInitialScrollOffset
                return
            }

            mCurrentScrollOffset += x
            gap = mSelectorTextGapWidth
        } else {
            if (!mWrapSelectorWheel && y > 0
                    && selectorIndices[mWheelMiddleItemIndex] <= mMinValue) {
                mCurrentScrollOffset = mInitialScrollOffset
                return
            }
            if (!mWrapSelectorWheel && y < 0
                    && selectorIndices[mWheelMiddleItemIndex] >= mMaxValue) {
                mCurrentScrollOffset = mInitialScrollOffset
                return
            }

            mCurrentScrollOffset += y
            gap = mSelectorTextGapHeight
        }

        while (mCurrentScrollOffset - mInitialScrollOffset > gap) {
            mCurrentScrollOffset -= mSelectorElementSize
            decrementSelectorIndices(selectorIndices)
            setValueInternal(selectorIndices[mWheelMiddleItemIndex], true)
            if (!mWrapSelectorWheel && selectorIndices[mWheelMiddleItemIndex] < mMinValue) {
                mCurrentScrollOffset = mInitialScrollOffset
            }
        }
        while (mCurrentScrollOffset - mInitialScrollOffset < -gap) {
            mCurrentScrollOffset += mSelectorElementSize
            incrementSelectorIndices(selectorIndices)
            setValueInternal(selectorIndices[mWheelMiddleItemIndex], true)
            if (!mWrapSelectorWheel && selectorIndices[mWheelMiddleItemIndex] > mMaxValue) {
                mCurrentScrollOffset = mInitialScrollOffset
            }
        }
    }

    /**
     * Sets the listener to be notified on change of the current value.
     *
     * @param onValueChangedListener The listener.
     */
    fun setOnValueChangedListener(onValueChangedListener: OnValueChangeListener) {
        mOnValueChangeListener = onValueChangedListener
    }

    /**
     * Set listener to be notified for scroll state changes.
     *
     * @param onScrollListener The listener.
     */
    fun setOnScrollListener(onScrollListener: OnScrollListener) {
        mOnScrollListener = onScrollListener
    }

    /**
     * Computes the max width if no such specified as an attribute.
     */
    private fun tryComputeMaxWidth() {
        if (!mComputeMaxWidth) {
            return
        }
        var maxTextWidth = 0
        if (displayedValues == null) {
            val maxDigitWidth = (0..9)
                    .asSequence()
                    .map { mSelectorWheelPaint.measureText(formatNumberWithLocale(it)) }
                    .max()
                    ?: 0f
            var numberOfDigits = 0
            var current = mMaxValue
            while (current > 0) {
                numberOfDigits++
                current /= 10
            }
            maxTextWidth = (numberOfDigits * maxDigitWidth).toInt()
        } else {
            val valueCount = displayedValues!!.size
            (0 until valueCount)
                    .asSequence()
                    .map { mSelectorWheelPaint.measureText(displayedValues!![it]) }
                    .filter { it > maxTextWidth }
                    .forEach { maxTextWidth = it.toInt() }
        }
        maxTextWidth += mSelectedText.paddingLeft + mSelectedText.paddingRight
        if (mMaxWidth != maxTextWidth) {
            mMaxWidth = if (maxTextWidth > mMinWidth) {
                maxTextWidth
            } else {
                mMinWidth
            }
            invalidate()
        }
    }

    /**
     * Sets the speed at which the numbers be incremented and decremented when
     * the up and down buttons are long pressed respectively.
     *
     *
     * The default value is 300 ms.
     *
     *
     * @param intervalMillis The speed (in milliseconds) at which the numbers
     * will be incremented and decremented.
     */
    fun setOnLongPressUpdateInterval(intervalMillis: Long) {
        mLongPressUpdateInterval = intervalMillis
    }

    override fun getTopFadingEdgeStrength(): Float {
        return if (isHorizontalMode) 0F else FADING_EDGE_STRENGTH
    }

    override fun getBottomFadingEdgeStrength(): Float {
        return if (isHorizontalMode) 0F else FADING_EDGE_STRENGTH
    }

    override fun getLeftFadingEdgeStrength(): Float {
        return if (isHorizontalMode) FADING_EDGE_STRENGTH else 0F
    }

    override fun getRightFadingEdgeStrength(): Float {
        return if (isHorizontalMode) FADING_EDGE_STRENGTH else 0F
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeAllCallbacks()
    }

    override fun onDraw(canvas: Canvas) {
        var x: Float
        var y: Float
        if (isHorizontalMode) {
            x = mCurrentScrollOffset.toFloat()
            y = (mSelectedText.baseline + mSelectedText.top).toFloat()
        } else {
            x = ((right - left) / 2).toFloat()
            y = mCurrentScrollOffset.toFloat()
        }

        // draw the selector wheel
        val selectorIndices = mSelectorIndices
        for (i in selectorIndices.indices) {
            val selectorIndex = selectorIndices[i]
            val scrollSelectorValue = mSelectorIndexToStringCache.get(selectorIndex)
            if (scrollSelectorValue != null) {
                when {
                    selectorIndex <= mSecondTextColorIndex -> mSelectorWheelPaint.color = mSecondTextColor
                    i == mWheelMiddleItemIndex -> mSelectorWheelPaint.color = mSelectedTextColor
                    else -> mSelectorWheelPaint.color = mTextColor
                }

                // Do not draw the middle item if input is visible since the input
                // is shown only if the wheel is static and it covers the middle
                // item. Otherwise, if the user starts editing the text via the
                // IME he may see a dimmed version of the old value intermixed
                // with the new one.
                if (i != mWheelMiddleItemIndex || mSelectedText.visibility != View.VISIBLE) {
                    canvas.drawText(scrollSelectorValue, x, y, mSelectorWheelPaint)
                }

                if (isHorizontalMode) {
                    x += mSelectorElementSize.toFloat()
                } else {
                    y += mSelectorElementSize.toFloat()
                }
            }
        }

        // draw the selection dividers
        if (mSelectionDivider != null) {
            if (isHorizontalMode) {
                // draw the left divider
                val leftOfLeftDivider = mLeftOfSelectionDividerLeft
                val rightOfLeftDivider = leftOfLeftDivider + mSelectionDividerThickness
                mSelectionDivider!!.setBounds(leftOfLeftDivider, 0, rightOfLeftDivider, bottom)
                mSelectionDivider!!.draw(canvas)

                // draw the right divider
                val rightOfRightDivider = mRightOfSelectionDividerRight
                val leftOfRightDivider = rightOfRightDivider - mSelectionDividerThickness
                mSelectionDivider!!.setBounds(leftOfRightDivider, 0, rightOfRightDivider, bottom)
                mSelectionDivider!!.draw(canvas)
            } else {
                // draw the top divider
                val topOfTopDivider = mTopSelectionDividerTop
                val bottomOfTopDivider = topOfTopDivider + mSelectionDividerThickness
                mSelectionDivider!!.setBounds(0, topOfTopDivider, right, bottomOfTopDivider)
                mSelectionDivider!!.draw(canvas)

                // draw the bottom divider
                val bottomOfBottomDivider = mBottomSelectionDividerBottom
                val topOfBottomDivider = bottomOfBottomDivider - mSelectionDividerThickness
                mSelectionDivider!!.setBounds(0, topOfBottomDivider, right, bottomOfBottomDivider)
                mSelectionDivider!!.draw(canvas)
            }
        }
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = CustomNumberPicker::class.java.name
        event.isScrollable = true
        val scroll = (mMinValue + mValue) * mSelectorElementSize
        val maxScroll = (mMaxValue - mMinValue) * mSelectorElementSize
        if (isHorizontalMode) {
            event.scrollX = scroll
            event.maxScrollX = maxScroll
        } else {
            event.scrollY = scroll
            event.maxScrollY = maxScroll
        }
    }

    /**
     * Makes a measure spec that tries greedily to use the max value.
     *
     * @param measureSpec The measure spec.
     * @param maxSize The max value for the size.
     * @return A measure spec greedily imposing the max size.
     */
    private fun makeMeasureSpec(measureSpec: Int, maxSize: Int): Int {
        if (maxSize == SIZE_UNSPECIFIED) {
            return measureSpec
        }
        val size = View.MeasureSpec.getSize(measureSpec)
        val mode = View.MeasureSpec.getMode(measureSpec)
        return when (mode) {
            View.MeasureSpec.EXACTLY -> measureSpec
            View.MeasureSpec.AT_MOST -> View.MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), View.MeasureSpec.EXACTLY)
            View.MeasureSpec.UNSPECIFIED -> View.MeasureSpec.makeMeasureSpec(maxSize, View.MeasureSpec.EXACTLY)
            else -> throw IllegalArgumentException("Unknown measure mode: " + mode)
        }
    }

    /**
     * Utility to reconcile a desired size and state, with constraints imposed
     * by a MeasureSpec. Tries to respect the min size, unless a different size
     * is imposed by the constraints.
     *
     * @param minSize The minimal desired size.
     * @param measuredSize The currently measured size.
     * @param measureSpec The current measure spec.
     * @return The resolved size and state.
     */
    private fun resolveSizeAndStateRespectingMinSize(minSize: Int, measuredSize: Int, measureSpec: Int): Int {
        return if (minSize != SIZE_UNSPECIFIED) {
            val desiredWidth = Math.max(minSize, measuredSize)
            resolveSizeAndState(desiredWidth, measureSpec, 0)
        } else {
            measuredSize
        }
    }

    /**
     * Resets the selector indices and clear the cached string representation of
     * these indices.
     */
    private fun initializeSelectorWheelIndices() {
        mSelectorIndexToStringCache.clear()
        val selectorIndices = mSelectorIndices
        val current = value
        for (i in mSelectorIndices.indices) {
            var selectorIndex = current + (i - mWheelMiddleItemIndex)
            if (mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex)
            }
            selectorIndices[i] = selectorIndex
            ensureCachedScrollSelectorValue(selectorIndices[i])
        }
    }

    /**
     * Sets the current value of this CustomNumberPicker.
     *
     * @param current The new value of the CustomNumberPicker.
     * @param notifyChange Whether to notify if the current value changed.
     */
    private fun setValueInternal(current: Int, notifyChange: Boolean) {
        var currentLocal = current
        if (mValue == currentLocal) {
            return
        }
        // Wrap around the values if we go past the start or end
        if (mWrapSelectorWheel) {
            currentLocal = getWrappedSelectorIndex(currentLocal)
        } else {
            currentLocal = Math.max(currentLocal, mMinValue)
            currentLocal = Math.min(currentLocal, mMaxValue)
        }
        val previous = mValue
        mValue = currentLocal
        updateInputTextView()
        if (notifyChange) {
            notifyChange(previous, currentLocal)
        }
        initializeSelectorWheelIndices()
        invalidate()
    }

    /**
     * Changes the current value by one which is increment or
     * decrement based on the passes argument.
     * decrement the current value.
     *
     * @param increment True to increment, false to decrement.
     */
    fun changeValueByOne(increment: Boolean) {
        mSelectedText.visibility = View.INVISIBLE
        if (!moveToFinalScrollerPosition(mFlingScroller)) {
            moveToFinalScrollerPosition(mAdjustScroller)
        }
        if (isHorizontalMode) {
            mPreviousScrollerX = 0
            if (increment) {
                mFlingScroller.startScroll(0, 0, -mSelectorElementSize, 0, SNAP_SCROLL_DURATION)
            } else {
                mFlingScroller.startScroll(0, 0, mSelectorElementSize, 0, SNAP_SCROLL_DURATION)
            }
        } else {
            mPreviousScrollerY = 0
            if (increment) {
                mFlingScroller.startScroll(0, 0, 0, -mSelectorElementSize, SNAP_SCROLL_DURATION)
            } else {
                mFlingScroller.startScroll(0, 0, 0, mSelectorElementSize, SNAP_SCROLL_DURATION)
            }
        }
        invalidate()
    }

    private fun initializeSelectorWheel() {
        initializeSelectorWheelIndices()
        val selectorIndices = mSelectorIndices
        val totalTextSize = selectorIndices.size * mTextSize.toInt()
        val textGapCount = selectorIndices.size.toFloat()
        val editTextTextPosition: Int
        if (isHorizontalMode) {
            val totalTextGapWidth = (right - left - totalTextSize).toFloat()
            mSelectorTextGapWidth = (totalTextGapWidth / textGapCount + 0.5f).toInt()
            mSelectorElementSize = mTextSize.toInt() + mSelectorTextGapWidth
            // Ensure that the middle item is positioned the same as the text in mSelectedText
            editTextTextPosition = mSelectedText.right / 2
        } else {
            val totalTextGapHeight = (bottom - top - totalTextSize).toFloat()
            mSelectorTextGapHeight = (totalTextGapHeight / textGapCount + 0.5f).toInt()
            mSelectorElementSize = mTextSize.toInt() + mSelectorTextGapHeight
            // Ensure that the middle item is positioned the same as the text in mSelectedText
            editTextTextPosition = mSelectedText.baseline + mSelectedText.top
        }
        mInitialScrollOffset = editTextTextPosition - mSelectorElementSize * mWheelMiddleItemIndex
        mCurrentScrollOffset = mInitialScrollOffset
        updateInputTextView()
    }

    private fun initializeFadingEdges() {
        if (isHorizontalMode) {
            isHorizontalFadingEdgeEnabled = true
            setFadingEdgeLength((right - left - mTextSize.toInt()) / 2)
        } else {
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength((bottom - top - mTextSize.toInt()) / 2)
        }
    }

    /**
     * Callback invoked upon completion of a given `scroller`.
     */
    private fun onScrollerFinished(scroller: Scroller) {
        if (scroller === mFlingScroller) {
            if (!ensureScrollWheelAdjusted()) {
                updateInputTextView()
            }
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
        } else if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            updateInputTextView()
        }
    }

    /**
     * Handles transition to a given `scrollState`
     */
    private fun onScrollStateChange(scrollState: Int) {
        if (mScrollState == scrollState) {
            return
        }
        mScrollState = scrollState
        if (mOnScrollListener != null) {
            mOnScrollListener!!.onScrollStateChange(this, scrollState)
        }
    }

    /**
     * Flings the selector with the given `velocity`.
     */
    private fun fling(velocity: Int) {
        if (isHorizontalMode) {
            mPreviousScrollerX = 0
            if (velocity > 0) {
                mFlingScroller.fling(0, 0, velocity, 0, 0, Integer.MAX_VALUE, 0, 0)
            } else {
                mFlingScroller.fling(Integer.MAX_VALUE, 0, velocity, 0, 0, Integer.MAX_VALUE, 0, 0)
            }
        } else {
            mPreviousScrollerY = 0
            if (velocity > 0) {
                mFlingScroller.fling(0, 0, 0, velocity, 0, 0, 0, Integer.MAX_VALUE)
            } else {
                mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocity, 0, 0, 0, Integer.MAX_VALUE)
            }
        }

        invalidate()
    }

    /**
     * @return The wrapped index `selectorIndex` value.
     */
    private fun getWrappedSelectorIndex(selectorIndex: Int): Int {
        if (selectorIndex > mMaxValue) {
            return mMinValue + (selectorIndex - mMaxValue) % (mMaxValue - mMinValue) - 1
        } else if (selectorIndex < mMinValue) {
            return mMaxValue - (mMinValue - selectorIndex) % (mMaxValue - mMinValue) + 1
        }
        return selectorIndex
    }

    /**
     * Increments the `selectorIndices` whose string representations
     * will be displayed in the selector.
     */
    private fun incrementSelectorIndices(selectorIndices: IntArray) {
        for (i in 0 until selectorIndices.size - 1) {
            selectorIndices[i] = selectorIndices[i + 1]
        }
        var nextScrollSelectorIndex = selectorIndices[selectorIndices.size - 2] + 1
        if (mWrapSelectorWheel && nextScrollSelectorIndex > mMaxValue) {
            nextScrollSelectorIndex = mMinValue
        }
        selectorIndices[selectorIndices.size - 1] = nextScrollSelectorIndex
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex)
    }

    /**
     * Decrements the `selectorIndices` whose string representations
     * will be displayed in the selector.
     */
    private fun decrementSelectorIndices(selectorIndices: IntArray) {
        for (i in selectorIndices.size - 1 downTo 1) {
            selectorIndices[i] = selectorIndices[i - 1]
        }
        var nextScrollSelectorIndex = selectorIndices[1] - 1
        if (mWrapSelectorWheel && nextScrollSelectorIndex < mMinValue) {
            nextScrollSelectorIndex = mMaxValue
        }
        selectorIndices[0] = nextScrollSelectorIndex
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex)
    }

    /**
     * Ensures we have a cached string representation of the given `
     * selectorIndex` to avoid multiple instantiations of the same string.
     */
    private fun ensureCachedScrollSelectorValue(selectorIndex: Int) {
        val cache = mSelectorIndexToStringCache
        var scrollSelectorValue: String? = cache.get(selectorIndex)
        if (scrollSelectorValue != null) {
            return
        }
        scrollSelectorValue = if (selectorIndex < mMinValue || selectorIndex > mMaxValue) {
            ""
        } else {
            if (displayedValues != null) {
                val displayedValueIndex = selectorIndex - mMinValue
                displayedValues!![displayedValueIndex]
            } else {
                formatNumber(selectorIndex)
            }
        }
        cache.put(selectorIndex, scrollSelectorValue)
    }

    private fun formatNumber(value: Int): String {
        return if (mFormatter != null) mFormatter!!.format(value) else formatNumberWithLocale(value)
    }

    /**
     * Updates the view of this CustomNumberPicker. If displayValues were specified in
     * the string corresponding to the index specified by the current value will
     * be returned. Otherwise, the formatter specified in [.setFormatter]
     * will be used to format the number.
     *
     * @return Whether the text was updated.
     */
    private fun updateInputTextView(): Boolean {
        /*
         * If we don't have displayed values then use the current number else
         * find the correct value in the displayed values for the current
         * number.
         */
        val text = if (displayedValues == null)
            formatNumber(mValue)
        else
            displayedValues!![mValue - mMinValue]
        if (!TextUtils.isEmpty(text) && text != mSelectedText.text.toString()) {
            mSelectedText.setText(text)
            return true
        }

        return false
    }

    /**
     * Notifies the listener, if registered, of a change of the value of this
     * CustomNumberPicker.
     */
    private fun notifyChange(previous: Int, current: Int) {
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener!!.onValueChange(this, previous, mValue)
        }
    }

    /**
     * Posts a command for changing the current value by one.
     *
     * @param increment Whether to increment or decrement the value.
     */
    private fun postChangeCurrentByOneFromLongPress(increment: Boolean, delayMillis: Long) {
        if (mChangeCurrentByOneFromLongPressCommand == null) {
            mChangeCurrentByOneFromLongPressCommand = ChangeCurrentByOneFromLongPressCommand()
        } else {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
        mChangeCurrentByOneFromLongPressCommand?.setStep(increment)
        postDelayed(mChangeCurrentByOneFromLongPressCommand, delayMillis)
    }

    /**
     * Removes the command for changing the current value by one.
     */
    private fun removeChangeCurrentByOneFromLongPress() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
    }

    /**
     * Removes all pending callback from the message queue.
     */
    private fun removeAllCallbacks() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
        if (mSetSelectionCommand != null) {
            removeCallbacks(mSetSelectionCommand)
        }
    }

    /**
     * @return The selected index given its displayed `value`.
     */
    private fun getSelectedPos(value: String): Int {
        var valueLocal = value
        if (displayedValues == null) {
            try {
                return Integer.parseInt(valueLocal)
            } catch (e: NumberFormatException) {
                // Ignore as if it's not a number we don't care
            }

        } else {
            for (i in displayedValues!!.indices) {
                // Don't force the user to type in jan when ja will do
                valueLocal = valueLocal.toLowerCase()
                if (displayedValues!![i].toLowerCase().startsWith(valueLocal)) {
                    return mMinValue + i
                }
            }

            /*
             * The user might have typed in a number into the month field i.e.
             * 10 instead of OCT so support that too.
             */
            try {
                return Integer.parseInt(valueLocal)
            } catch (e: NumberFormatException) {
                // Ignore as if it's not a number we don't care
            }

        }
        return mMinValue
    }

    /**
     * Posts an [SetSelectionCommand] from the given `selectionStart
    ` *  to `selectionEnd`.
     */
    private fun postSetSelectionCommand(selectionStart: Int, selectionEnd: Int) {
        if (mSetSelectionCommand == null) {
            mSetSelectionCommand = SetSelectionCommand()
        } else {
            removeCallbacks(mSetSelectionCommand)
        }
        mSetSelectionCommand?.mSelectionStart = selectionStart
        mSetSelectionCommand?.mSelectionEnd = selectionEnd
        post(mSetSelectionCommand)
    }

    /**
     * Filter for accepting only valid indices or prefixes of the string
     * representation of valid indices.
     */
    internal inner class InputTextFilter : NumberKeyListener() {

        // XXX This doesn't allow for range limits when controlled by a soft input method!
        override fun getInputType(): Int {
            return InputType.TYPE_CLASS_TEXT
        }

        override fun getAcceptedChars(): CharArray {
            return DIGIT_CHARACTERS
        }

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence {
            if (displayedValues == null) {
                var filtered: CharSequence? = super.filter(source, start, end, dest, dstart, dend)
                if (filtered == null) {
                    filtered = source.subSequence(start, end)
                }

                val result = (dest.subSequence(0, dstart).toString() + filtered
                        + dest.subSequence(dend, dest.length))

                if ("" == result) {
                    return result
                }
                val `val` = getSelectedPos(result)

                /*
                 * Ensure the user can't type in a value greater than the max
                 * allowed. We have to allow less than min as the user might
                 * want to delete some numbers and then type a new number.
                 */
                return if (`val` > mMaxValue) {
                    ""
                } else {
                    filtered
                }
            } else {
                val filtered = source.subSequence(start, end).toString()
                if (TextUtils.isEmpty(filtered)) {
                    return ""
                }
                val result = (dest.subSequence(0, dstart).toString() + filtered
                        + dest.subSequence(dend, dest.length))
                val str = result.toString().toLowerCase()
                for (`val` in displayedValues!!) {
                    val valLowerCase = `val`.toLowerCase()
                    if (valLowerCase.startsWith(str)) {
                        postSetSelectionCommand(result.length, `val`.length)
                        return `val`.subSequence(dstart, `val`.length)
                    }
                }
                return ""
            }
        }
    }

    /**
     * Ensures that the scroll wheel is adjusted i.e. there is no offset and the
     * middle element is in the middle of the widget.
     *
     * @return Whether an adjustment has been made.
     */
    private fun ensureScrollWheelAdjusted(): Boolean {
        // adjust to the closest value
        var delta = mInitialScrollOffset - mCurrentScrollOffset
        if (delta != 0) {
            if (Math.abs(delta) > mSelectorElementSize / 2) {
                delta += if (delta > 0) -mSelectorElementSize else mSelectorElementSize
            }
            if (isHorizontalMode) {
                mPreviousScrollerX = 0
                mAdjustScroller.startScroll(0, 0, delta, 0, SELECTOR_ADJUSTMENT_DURATION_MILLIS)
            } else {
                mPreviousScrollerY = 0
                mAdjustScroller.startScroll(0, 0, 0, delta, SELECTOR_ADJUSTMENT_DURATION_MILLIS)
            }
            invalidate()
            return true
        }
        return false
    }

    /**
     * Command for setting the input text selection.
     */
    internal inner class SetSelectionCommand : Runnable {
        var mSelectionStart: Int = 0

        var mSelectionEnd: Int = 0

        override fun run() {
            mSelectedText.setSelection(mSelectionStart, mSelectionEnd)
        }
    }

    /**
     * Command for changing the current value from a long press by one.
     */
    internal inner class ChangeCurrentByOneFromLongPressCommand : Runnable {
        private var mIncrement: Boolean = false

       fun setStep(increment: Boolean) {
            mIncrement = increment
        }

        override fun run() {
            changeValueByOne(mIncrement)
            postDelayed(this, mLongPressUpdateInterval)
        }
    }

    private fun formatNumberWithLocale(value: Int): String {
        return String.format(Locale.getDefault(), "%d", value)
    }

    private fun setWidthAndHeight() {
        if (isHorizontalMode) {
            mMinHeight = SIZE_UNSPECIFIED
            mMaxHeight = dpToPx(DEFAULT_MIN_WIDTH.toFloat()).toInt()
            mMinWidth = dpToPx(DEFAULT_MAX_HEIGHT.toFloat()).toInt()
            mMaxWidth = SIZE_UNSPECIFIED
        } else {
            mMinHeight = SIZE_UNSPECIFIED
            mMaxHeight = dpToPx(DEFAULT_MAX_HEIGHT.toFloat()).toInt()
            mMinWidth = dpToPx(DEFAULT_MIN_WIDTH.toFloat()).toInt()
            mMaxWidth = SIZE_UNSPECIFIED
        }
    }

    fun setDividerColorResource(@ColorRes colorId: Int) {
        dividerColor = ContextCompat.getColor(mContext, colorId)
    }

    fun setDividerDistance(distance: Int) {
        mSelectionDividersDistance = dpToPx(distance.toFloat()).toInt()
    }

    fun setDividerThickness(thickness: Int) {
        mSelectionDividerThickness = dpToPx(thickness.toFloat()).toInt()
    }

    override fun setOrientation(@Orientation orientation: Int) {
        mOrientation = orientation
        setWidthAndHeight()
    }

    fun setFormatter(formatter: String) {
        if (TextUtils.isEmpty(formatter)) {
            return
        }

        this@CustomNumberPicker.formatter = stringToFormatter(formatter)
    }

    fun setFormatter(@StringRes stringId: Int) {
        setFormatter(resources.getString(stringId))
    }

    fun setSecondTextColor(@ColorInt color: Int) {
        mSecondTextColor = color
    }

    fun setSecondTextColorIndex(index: Int) {
        mSecondTextColorIndex = index
    }

    fun setSelectedTextColorResource(@ColorRes colorId: Int) {
        selectedTextColor = ContextCompat.getColor(mContext, colorId)
    }

    fun setTextColorResource(@ColorRes colorId: Int) {
        textColor = ContextCompat.getColor(mContext, colorId)
    }

    fun setTextSize(@DimenRes dimenId: Int) {
        textSize = resources.getDimension(dimenId)
    }

    @JvmOverloads
    fun setTypeface(string: String, style: Int = Typeface.NORMAL) {
        if (TextUtils.isEmpty(string)) {
            return
        }
        typeface = Typeface.create(string, style)
    }

    @JvmOverloads
    fun setTypeface(@StringRes stringId: Int, style: Int = Typeface.NORMAL) {
        setTypeface(resources.getString(stringId), style)
    }

    private fun stringToFormatter(formatter: String?): Formatter? {
        return if (TextUtils.isEmpty(formatter)) {
            null
        } else object : Formatter {
            override fun format(i: Int): String {
                return String.format(Locale.getDefault(), formatter!!, i)
            }
        }

    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun pxToDp(px: Float): Float {
        return px / resources.displayMetrics.density
    }

    private fun spToPx(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }

    private fun pxToSp(px: Float): Float {
        return px / resources.displayMetrics.scaledDensity
    }

    override fun getOrientation(): Int {
        return mOrientation
    }

    companion object {

        const val VERTICAL = LinearLayout.VERTICAL

        const val HORIZONTAL = LinearLayout.HORIZONTAL

        /**
         * The default update interval during long press.
         */
        private val DEFAULT_LONG_PRESS_UPDATE_INTERVAL: Long = 300

        /**
         * The coefficient by which to adjust (divide) the max fling velocity.
         */
        private val SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 8

        /**
         * The the duration for adjusting the selector wheel.
         */
        private val SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800

        /**
         * The duration of scrolling while snapping to a given position.
         */
        private val SNAP_SCROLL_DURATION = 300

        /**
         * The strength of fading in the top and bottom while drawing the selector.
         */
        private val FADING_EDGE_STRENGTH = 0.9f

        /**
         * The default unscaled height of the selection divider.
         */
        private val UNSCALED_DEFAULT_SELECTION_DIVIDER_THICKNESS = 2

        /**
         * The default unscaled distance between the selection dividers.
         */
        private val UNSCALED_DEFAULT_SELECTION_DIVIDERS_DISTANCE = 48

        /**
         * Constant for unspecified size.
         */
        private val SIZE_UNSPECIFIED = -1

        /**
         * The default max value of this widget.
         */
        private val DEFAULT_MAX_VALUE = 100

        /**
         * The default min value of this widget.
         */
        private val DEFAULT_MIN_VALUE = 1

        /**
         * The default max height of this widget.
         */
        private val DEFAULT_MAX_HEIGHT = 180

        /**
         * The default min width of this widget.
         */
        private val DEFAULT_MIN_WIDTH = 64

        /**
         * The default color of text.
         */
        private val DEFAULT_TEXT_COLOR = -0x1000000

        /**
         * The default size of text.
         */
        private val DEFAULT_TEXT_SIZE = 25f

        private val sTwoDigitFormatter = TwoDigitFormatter()

        val twoDigitFormatter: Formatter
            get() = sTwoDigitFormatter

        /**
         * Utility to reconcile a desired size and state, with constraints imposed
         * by a MeasureSpec.  Will take the desired size, unless a different size
         * is imposed by the constraints.  The returned value is a compound integer,
         * with the resolved size in the [.MEASURED_SIZE_MASK] bits and
         * optionally the bit [.MEASURED_STATE_TOO_SMALL] set if the resulting
         * size is smaller than the size the view wants to be.
         *
         * @param size How big the view wants to be
         * @param measureSpec Constraints imposed by the parent
         * @return Size information bit mask as defined by
         * [.MEASURED_SIZE_MASK] and [.MEASURED_STATE_TOO_SMALL].
         */
        fun resolveSizeAndState(size: Int, measureSpec: Int, childMeasuredState: Int): Int {
            var result = size
            val specMode = View.MeasureSpec.getMode(measureSpec)
            val specSize = View.MeasureSpec.getSize(measureSpec)
            when (specMode) {
                View.MeasureSpec.UNSPECIFIED -> result = size
                View.MeasureSpec.AT_MOST -> if (specSize < size) {
                    result = specSize or View.MEASURED_STATE_TOO_SMALL
                } else {
                    result = size
                }
                View.MeasureSpec.EXACTLY -> result = specSize
            }
            return result or (childMeasuredState and View.MEASURED_STATE_MASK)
        }

        /**
         * The numbers accepted by the input text's [LayoutInflater.Filter]
         */
        private val DIGIT_CHARACTERS = charArrayOf(
                // Latin digits are the common case
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                // Arabic-Indic
                '\u0660', '\u0661', '\u0662', '\u0663', '\u0664', '\u0665', '\u0666', '\u0667', '\u0668', '\u0669',
                // Extended Arabic-Indic
                '\u06f0', '\u06f1', '\u06f2', '\u06f3', '\u06f4', '\u06f5', '\u06f6', '\u06f7', '\u06f8', '\u06f9',
                // Negative
                '-')
    }

}