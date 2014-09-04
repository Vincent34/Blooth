package com.example.blooth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.AbsSeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class VerticalSeekBar extends AbsSeekBar{
	private Drawable mThumb;
	private int height;
	private int width;
	public interface OnSeekBarChangeListener {
		void onProgressChanged(VerticalSeekBar verticalSeekBar, int progress, boolean fromUser);
		void onStartTrackingTouch(VerticalSeekBar verticalSeekBar);
		void onStopTrackingTouch(VerticalSeekBar verticalSeekBar);
	}
	private OnSeekBarChangeListener mOnSeekBarChangeListener;
	public VerticalSeekBar(Context context, AttributeSet attributeset, int defStyle) {
		super(context,attributeset,defStyle);
	}
	public VerticalSeekBar(Context context, AttributeSet attributeSet) {
		super(context,attributeSet);
	}
	public VerticalSeekBar(Context context) {
		super(context);
	}
	public void setOnSeekBarListener(OnSeekBarChangeListener l) {
		mOnSeekBarChangeListener = l;
	}
	void onStartTrackingTouch() {
		if (mOnSeekBarChangeListener != null) {
			mOnSeekBarChangeListener.onStartTrackingTouch(this);
		}
	}
	void onStopTrackingTouch() {
		if (mOnSeekBarChangeListener != null) {
			mOnSeekBarChangeListener.onStopTrackingTouch(this);
		}
	}
	void onProgressRefresh(float scale, boolean fromUser) {
		Drawable thumb = mThumb;
		if (thumb != null) {
			setThumbPos(getHeight(), thumb, scale, Integer.MIN_VALUE);
			invalidate();
		}
		if (mOnSeekBarChangeListener != null) {
			mOnSeekBarChangeListener.onProgressChanged(this,getProgress(),fromUser);
		}
	}
	private void setThumbPos(int w, Drawable thumb, float scale, int gap) {
		int available = w + getPaddingLeft() - getPaddingRight();
		int thumbWidth = thumb.getIntrinsicWidth();
		int thumbHeight = thumb.getIntrinsicHeight();
		available -= thumbWidth;
		available += getThumbOffset() / 2;
		int thumbPos = (int) (scale * available);
		int topBound, bottomBound;
		if (gap == Integer.MIN_VALUE) {
			Rect oldBound = thumb.getBounds();
			topBound = oldBound.top;
			bottomBound = oldBound.bottom;
		} else {
			topBound = gap;
			bottomBound = gap + thumbHeight;
		}
		thumb.setBounds(thumbPos, topBound, thumbPos + thumbWidth, bottomBound);
	}
	protected void onSizeChange(int ow, int oh, int oldw, int oldh) {
		super.onSizeChanged(ow, oh, oldw, oldh);
	}
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		height = View.MeasureSpec.getSize(heightMeasureSpec) / 2;
		width = 50;
		this.setMeasuredDimension(width, height);
	}
	public void setThumb(Drawable thumb) {
		mThumb = thumb;
		super.setThumb(thumb);
	}
	protected synchronized void onDraw(Canvas canvas) {
		canvas.rotate(-90);
		canvas.translate(-getHeight(),0);
		super.onDraw(canvas);
	}
	public boolean onTouchEvent(MotionEvent e) {
		if (!isEnabled()) {
			return false;
		}
		switch(e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setPressed(true);
			onStartTrackingTouch();
			trackTouchEvent(e);
			break;
		case MotionEvent.ACTION_MOVE:
			trackTouchEvent(e);
			attemptClaimDrag();
			break;
		case MotionEvent.ACTION_UP:
			trackTouchEvent(e);
			onStopTrackingTouch();
			setPressed(false);
			break;
		case MotionEvent.ACTION_CANCEL:
			onStopTrackingTouch();
			setPressed(false);
			break;
		}
		return true;
	}
	private void trackTouchEvent(MotionEvent event) {
	    final int Height = getHeight();
	    final int available = Height - getPaddingBottom() - getPaddingTop();
	    int Y = (int) event.getY();
	    float scale;
	    float progress = 0;
	    if (Y > Height - getPaddingBottom()) {
	        scale = 0.0f;
	    } else if (Y < getPaddingTop()) {
	        scale = 1.0f;
	    } else {
	        scale = (float) (Height - getPaddingBottom() - Y)
	                / (float) available;
	    }
	
	    final int max = getMax();
	    progress = scale * max;
	
	    setProgress((int) progress);
	}
	private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }
	public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            KeyEvent newEvent = null;
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP:
                newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DPAD_RIGHT);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DPAD_LEFT);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DPAD_DOWN);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DPAD_UP);
                break;
            default:
                newEvent = new KeyEvent(KeyEvent.ACTION_DOWN,
                        event.getKeyCode());
                break;
            }
            return newEvent.dispatch(this);
        }
        return false;
    }
}
