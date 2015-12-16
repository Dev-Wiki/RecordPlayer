package net.devwiki.util;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * AMR文件的工具类
 * Created by Administrator on 2015/12/16 0016.
 */
public class AmrUtil {

    private static final byte[] AMR_HEAD = new byte[]{
            (byte) 0x23, (byte) 0x21, (byte) 0x41,
            (byte) 0x4D, (byte) 0x52, (byte) 0x0A};

    /**
     * 获取amr文件的时长
     *
     * @param filePath 文件路径
     * @return 时长, 单位:秒
     */
    public static int getAmrDuration(String filePath) {
        int length = getAmrDurationMsec(filePath);
        if (length%1000 == 0){
            return length/1000;
        }
        return length/1000 + 1;
    }

    /**
     * 获取amr文件的时长
     *
     * @param filePath 文件路径
     * @return 时长, 单位:毫秒
     */
    public static int getAmrDurationMsec(String filePath) {
        if (TextUtils.isEmpty(filePath)){
            return 0;
        }
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()){
            return 0;
        }
        return (int) Math.ceil(getAmrDuration(file));
    }

    /**
     * 获取Amr文件的时长
     *
     * @param file amr文件
     * @return 时长, 单位:毫秒
     */
    public static long getAmrDuration(File file) {
        long duration = -1;
        int[] packedSize = {12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0, 0, 0};
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            long length = file.length();                                                                        // 文件的长度
            int pos = 6;                                                                                        // 设置初始位置
            int frameCount = 0;                                                                                    // 初始帧数
            int packedPos = -1;

            byte[] datas = new byte[1];                                                                            // 初始数据值
            while (pos <= length) {
                randomAccessFile.seek(pos);
                if (randomAccessFile.read(datas, 0, 1) != 1) {
                    duration = length > 0 ? ((length - 6) / 650) : 0;
                    break;
                }
                packedPos = (datas[0] >> 3) & 0x0F;
                pos += packedSize[packedPos] + 1;
                frameCount++;
            }
            duration += frameCount * 20;                                                                        // 帧数*20
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return duration;
    }

    public static void saveAmrFile(String path, byte[] amrData, boolean isFirst, boolean isLast){
        if (isFirst){
            byte[] data = new byte[AMR_HEAD.length + amrData.length];
            System.arraycopy(AMR_HEAD, 0, data, 0, AMR_HEAD.length);
            System.arraycopy(amrData, 0, data, AMR_HEAD.length, amrData.length);
        }
    }

    private static void saveToFile(byte[] data, String path){
        if(data == null || data.length == 0){
            return;
        }
        if (TextUtils.isEmpty(path)){
            return;
        }
    }
}
