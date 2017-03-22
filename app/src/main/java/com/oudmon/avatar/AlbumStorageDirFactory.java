package com.oudmon.avatar;

import java.io.File;

/**
 * Created by Administrator on 2016/12/7.
 */

abstract public class AlbumStorageDirFactory {
  public abstract File getAlbumStorageDir(String albumName);
}
