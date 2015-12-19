package net.devwiki.util;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * 文件工具类
 * Created by Administrator on 2015/12/17 0017.
 */
public class FileUtil {

    public static final String ROOT_NAME = "/RecordPlayer/";

    public static void createRootDir(){
        if (TextUtils.isEmpty(getRootPath())){
            return;
        }
        File file = new File(getRootPath());
        if (file.exists()){
            return;
        }
        file.mkdirs();
    }

    public static String getRootPath(){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return Environment.getExternalStorageDirectory().getAbsolutePath() + ROOT_NAME;
        } else {
            return null;
        }
    }
}
