package net.devwiki.util;

public class AmrEncoder {
	static {
		try {
			System.loadLibrary("Amr_nb");
		} catch (UnsatisfiedLinkError error) {
			error.printStackTrace();
		}
	}

	/**
	 * 初始化
	 * 
	 * @param dtx
	 *            默认填0
	 * @return 句柄
	 */
	public static native int init(int dtx);

	/**
	 * 退出
	 * 
	 * @param handle
	 *            句柄
	 */
	public static native void exit(int handle);

	/**
	 * 转换
	 * 
	 * @param handle
	 *            句柄
	 * @param mode
	 *            MR475填0
	 * @param speech
	 *            传入数据
	 * @param serial
	 *            传出数据
	 * @param forceSpeech
	 *            默认填0
	 * @return
	 */
	public static native int encode(int handle, int mode, byte[] speech,
			byte[] serial, int forceSpeech);
}
