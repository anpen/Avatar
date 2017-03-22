package com.oudmon.avatar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.functions.Consumer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements TakePhotoPopWin.OnItemClickListener {

  private ImageView mAvatarView;
  private TakePhotoPopWin mPopupWindow;

  private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
  private File mCurrentPhotoFile;
  private Uri mUriCropped;

  private static final String TAG = "MainActivity";
  private static final String JPEG_FILE_PREFIX = "IMG_";
  private static final String JPEG_FILE_SUFFIX = ".jpg";

  private static final int REQUEST_IMAGE_CAPTURE = 1;
  private static final int REQUEST_IMAGE_PICK = 2;
  private static final int REQUEST_IMAGE_CROP = 3;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    init();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode != RESULT_OK) {
      return;
    }
    switch(requestCode) {
      case REQUEST_IMAGE_CAPTURE:
        dispatchCropPictureIntent(Uri.fromFile(mCurrentPhotoFile), mUriCropped);
        break;
      case REQUEST_IMAGE_PICK:
        dispatchCropPictureIntent(data.getData(), mUriCropped);
        break;
      case REQUEST_IMAGE_CROP:
        handlePicture();
        break;
    }
  }

  /* Photo album for this application */
  private String getAlbumName() {
    return getString(R.string.album_name);
  }

  private File getAlbumDir() {
    File storageDir = null;
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
      if (storageDir != null) {
        if (! storageDir.mkdirs()) {
          if (! storageDir.exists()){
            Log.d("CameraSample", "failed to create directory");
            return null;
          }
        }
      }

    } else {
      Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
    }
    return storageDir;
  }

  private File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
    File albumF = getAlbumDir();
    File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
    return imageF;
  }

  private Bitmap decodeUriAsBitmap(@NonNull Uri uri) {
    Bitmap bitmap = null;
    try {
      bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return bitmap;
  }

  private void handlePicture() {
    final Bitmap bitmap = decodeUriAsBitmap(mUriCropped);
    mAvatarView.setImageBitmap(bitmap);
  }

  private void init() {
    mAvatarView = (ImageView) findViewById(R.id.iv_avatar);
    mAvatarView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        popup();
      }
    });
    mPopupWindow = new TakePhotoPopWin(this);
    mPopupWindow.registerListener(this);

    // Must be done during an initialization phase like onCreate
    RxPermissions.getInstance(this).request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .subscribe(new Consumer<Boolean>() {
          @Override public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean) {
              Log.d(TAG, "permission permitted");
            }
          }
        });


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
      mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
    } else {
      mAlbumStorageDirFactory = new BaseAlbumDirFactory();
    }

    mUriCropped = Uri.parse("file:///sdcard/Pictures/temp.jpg");
  }

  private void popup() {
    mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    mPopupWindow.showAtLocation(findViewById(R.id.activity_main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
  }

  private boolean hasCamera() {
    return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
  }

  @Override public void onTakePicture() {
    mPopupWindow.dismiss();
    dispatchTakePictureIntent();
  }

  @Override public void onPickPicture() {
    mPopupWindow.dismiss();
    dispatchPickPictureIntent();
  }

  @Override public void onCancel() {
    mPopupWindow.dismiss();
  }

  private void dispatchTakePictureIntent() {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      try {
        mCurrentPhotoFile = createImageFile();
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCurrentPhotoFile));
      } catch (IOException e) {
        e.printStackTrace();
        mCurrentPhotoFile = null;
      }
      startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }
  }

  private void dispatchPickPictureIntent() {
    Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
    pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
    startActivityForResult(pickIntent, REQUEST_IMAGE_PICK);
  }

  private void dispatchCropPictureIntent(Uri source, Uri dest) {
    // 裁剪图片意图
    Intent intent = new Intent("com.android.camera.action.CROP");
    intent.setDataAndType(source, "image/*");
    intent.putExtra("crop", "true");
    // 裁剪框的比例，1：1
    intent.putExtra("aspectX", 1);
    intent.putExtra("aspectY", 1);
    // 裁剪后输出图片的尺寸大小
    intent.putExtra("outputX", 200);
    intent.putExtra("outputY", 200);
    intent.putExtra("output", dest);
    // 图片格式
    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
    intent.putExtra("noFaceDetection", true);// 取消人脸识别
    intent.putExtra("return-data", false);// true:不返回uri，false：返回uri
    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivityForResult(intent, REQUEST_IMAGE_CROP);
    }
  }
}
