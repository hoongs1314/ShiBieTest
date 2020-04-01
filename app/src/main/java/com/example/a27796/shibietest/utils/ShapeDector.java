package com.example.a27796.shibietest.utils;

import android.util.Log;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

/**
 * Created by hui on 2018/2/11.
 */
public class ShapeDector {

    public String detect(MatOfPoint mp,MatOfPoint2f mp2f){
        String shape = "unidentified" ;
        double peri;
        peri = Imgproc.arcLength(mp2f,true);
        //对图像轮廓点进行多边形拟合
        MatOfPoint2f polyShape = new MatOfPoint2f();
        Imgproc.approxPolyDP(mp2f,polyShape,0.04*peri,true);
        int shapeLen = polyShape.toArray().length;
        //根据轮廓凸点拟合结果，判断属于那个形状
        Rect rect = Imgproc.boundingRect(mp);
        float width = rect.width;
        float height = rect.height;
        float ar = width/height;
//        Log.i("ggg","width:"+width);
        if (width>10){
            switch (shapeLen){
                case 3:
                    shape ="triangle";
                    break;
                case 4:
                    //计算宽高比，判断是矩形还是正方形
                    if (ar>=0.95 && ar <=1.05) {
                        shape="square";
                    }else {
                        shape="rectangle";
                    }
                    break;
                case 5:
                    shape ="pentagon";
                    break;
                default:
                    if((ar>=0.8 && ar <=1.2)){
                        shape="circle";
                    }
                    break;
            }
        }
        return shape;
    }
}
