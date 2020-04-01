package com.example.a27796.shibietest.RGB_Graying;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.zxing.LuminanceSource;

import java.io.FileNotFoundException;

//二维码图片处理
public class RGBLuminanceSource extends LuminanceSource {
	private final byte[] luminances;

	public RGBLuminanceSource(Bitmap bitmap) {
		super(bitmap.getWidth(), bitmap.getHeight());
		//取得图片像素数组内容
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		// In order to measure pure decoding speed, we convert the entire image
		// to a greyscale array
		// up front, which is the same as the Y channel of the
		// YUVLuminanceSource in the real app.
		luminances = new byte[width * height];

		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				if (r == g && g == b) {
					// Image is already greyscale, so pick any channel.
					luminances[offset + x] = (byte) r;
				} else {
					// Calculate luminance cheaply, favoring green.
					luminances[offset + x] = (byte) ((r + g + g + b) >> 2);
				}
			}
		}
	}

	public RGBLuminanceSource(String path) throws FileNotFoundException {
		this(loadBitmap(path));
	}

	//返回生成好的像素数据
	@Override
	public byte[] getMatrix() {
		return luminances;
	}

	//得到指定行的像素数据
	@Override
	public byte[] getRow(int arg0, byte[] arg1) {
		if (arg0 < 0 || arg0 >= getHeight()) {
			throw new IllegalArgumentException(
					"Requested row is outside the image: " + arg0);
		}
		int width = getWidth();
		if (arg1 == null || arg1.length < width) {
			arg1 = new byte[width];
		}
		System.arraycopy(luminances, arg0 * width, arg1, 0, width);
		return arg1;
	}

	//加载图片
	private static Bitmap loadBitmap(String path) throws FileNotFoundException {
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		Log.i("ggg","加载图片");
		if (bitmap == null) {
			throw new FileNotFoundException("Couldn't open " + path);
		}
		return bitmap;
	}
}
