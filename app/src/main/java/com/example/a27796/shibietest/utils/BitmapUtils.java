package com.example.a27796.shibietest.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.File;

public class BitmapUtils {

	public static Bitmap getSmallBitmap(Bitmap srcbitmap) {
		float scale = calculateInSampleSize(srcbitmap.getHeight(), srcbitmap.getWidth());
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap bitmap = null;
		if (scale != 1.0f) {
			bitmap = Bitmap.createBitmap(srcbitmap, 0, 0, srcbitmap.getWidth(), srcbitmap.getHeight(), matrix, true);
			srcbitmap.recycle();
			srcbitmap = null;
		} else {
			return srcbitmap;
		}

		return bitmap;
	}

	// 根据路径获得图片并压缩，返回bitmap用于显示
	public static Bitmap getSmallBitmap(File f) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(f.getAbsolutePath(), options);
		float scale = calculateInSampleSize(options.outHeight, options.outWidth);
		options.inJustDecodeBounds = false;
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap bitmap = null;
		try {
			Bitmap srcbitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
			if (scale != 1.0f) {
				bitmap = Bitmap.createBitmap(srcbitmap, 0, 0, options.outWidth, options.outHeight, matrix, true);
				srcbitmap.recycle();
				srcbitmap = null;
			} else {
				return srcbitmap;
			}
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
			return null;
		}

		return bitmap;
	}

	// 计算图片的缩放�?
	public static float calculateInSampleSize(int outHeight, int outWidth) {
		final int height = outHeight;
		final int width = outWidth;
		float scale = 1.0f;
		if (width < height) {
			if (width > 640) {
				scale = 640.0f / width;
			}
		} else {
			if (height > 640) {
				scale = 640.0f / height;
			}
		}
		return scale;
	}
}

