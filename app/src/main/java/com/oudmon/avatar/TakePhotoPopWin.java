package com.oudmon.avatar;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2016/9/24.
 */

public class TakePhotoPopWin extends PopupWindow implements View.OnClickListener {
  private Context mContext;
  private View view;

  private Button mTakePhoto;
  private Button mPickPhoto;
  private Button mCancel;
  private OnItemClickListener mListener;

  public TakePhotoPopWin(Context context) {
    mContext = context;
    view = LayoutInflater.from(mContext).inflate(R.layout.take_photo_pop, null);

    mTakePhoto = (Button) view.findViewById(R.id.btn_take_photo);
    mPickPhoto = (Button) view.findViewById(R.id.btn_pick_photo);
    mCancel = (Button) view.findViewById(R.id.btn_cancel);

    mCancel.setOnClickListener(this);
    mTakePhoto.setOnClickListener(this);
    mPickPhoto.setOnClickListener(this);

    setOutsideTouchable(true);
    setContentView(view);
    setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
    setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
    setFocusable(true);
    ColorDrawable drawable = new ColorDrawable(0x60000000);
    setBackgroundDrawable(drawable);
    setAnimationStyle(R.style.take_photo_anim);

    view.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        int top = view.findViewById(R.id.pop_layout).getTop();
        int y = (int) event.getY();
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
          if (y < top) {
            dismiss();
          }
        }
        return true;
      }
    });
  }

  public void registerListener(OnItemClickListener listener) {
    mListener = listener;
  }

  public void unregisterListener() {
    if (mListener != null) {
      mListener = null;
    }
  }

  @Override public void onClick(View view) {
    if (mListener == null) {
      return;
    }
    int id = view.getId();
    switch (id) {
      case R.id.btn_take_photo:
        mListener.onTakePicture();
        break;
      case R.id.btn_pick_photo:
        mListener.onPickPicture();
        break;
      case R.id.btn_cancel:
        mListener.onCancel();
        break;
    }
  }

  interface OnItemClickListener {
    void onTakePicture();
    void onPickPicture();
    void onCancel();
  }

}
