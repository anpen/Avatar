package com.oudmon.avatar;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import java.io.File;

/**
 * Created by Administrator on 2016/12/7.
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class FroyoAlbumDirFactory extends AlbumStorageDirFactory {
  @Override public File getAlbumStorageDir(String albumName) {
    return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
  }
}
