package com.oudmon.avatar;

import android.os.Environment;
import java.io.File;

/**
 * Created by Administrator on 2016/12/7.
 */

public class BaseAlbumDirFactory extends AlbumStorageDirFactory {

  // Standard storage location for digital camera files
  public static final String CAMERA_DIR = "/dcim/";

  @Override public File getAlbumStorageDir(String albumName) {
    return new File(Environment.getExternalStorageDirectory() + CAMERA_DIR + albumName);
  }
}
