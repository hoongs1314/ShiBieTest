package com.example.a27796.shibietest.utils;

import android.graphics.Bitmap;
import android.util.Log;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MyUtils {



    private static int power(int i, int j) {
        // TODO Auto-generated method stub
        int y=1;
        String ab = Integer.toBinaryString(j);
        for(int a=0;a<ab.length();a++)
        {
            //	System.out.println(ab.length());
            int s=Integer.parseInt(String.valueOf(ab.charAt(a)));//char类型转化为int

            //		System.out.println(s);
            y=y*y;
            if(s==1){
                y=y*i;
            }
        }
        System.out.println(ab);
        return y;
    }

    public  String nremoveMethod(String s) {
        StringBuffer bu = new StringBuffer();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (s.indexOf(c) == s.lastIndexOf(c)) {// 此字符第一次位置和最后位置一致
                bu.append(c); // 即肯定没有重复的直接添加
            } else {// 同理 次字符出现过多次
                int number = s.indexOf(c);// 次字符第一次出现的位置
                if (number == i) {// 第一次出现的位置和当前位置一致 即第一次出现添加
                    bu.append(c);
                }
            }
        }
        return bu.toString();
    }
    public static String numremoveMethod(String s){
        String[] array = s.split(",");
        for (int i = 0; i <array.length ; i++) {
            for (int j = i+1; j < array.length ; j++) {
                if (array[j] == array[i]){
                    array[j].replace(array[j],"");
                }
            }
        }
        StringBuffer str5 = new StringBuffer();
        for (String ss : array) {
            str5.append(s);
        }
        String newStr = String.valueOf(str5);
        return newStr;
    }

    //只留数字
//    String regEx ="[^0-9^$]";
//    String ss = s.replaceAll(regEx,"");
    //实现数组由小到大进行重新排序
    public void paiXuDx(int[] ints){
        int temp;
        //每轮执行交换后就有一位相对较小数往前进一位,比如1，每次交换后都会向前进一位，所以称为冒泡
        // 交换（length-1）次后结束。
        for (int i = 0; i <ints.length ; i++) {
            //执行一轮交换
            for (int j = 0; j <ints.length-1 ; j++) {
                if(ints[i]>ints[i+1]){//如果当前位比下一位大，则交换。
                    temp=ints[i];
                    ints[i]=ints[i+1];
                    ints[i+1]=temp;
                }
            }
        }
        for(int n:ints){
            Log.i("ggg", String.valueOf(n));
        }
    }

    //Mat转Bitmap
    public static Bitmap matToBitmap(Mat mat) {
        Bitmap resultBitmap = null;
        if (mat != null) {
            resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            if (resultBitmap != null)
                Utils.matToBitmap(mat, resultBitmap);
        }
        return resultBitmap;
    }

    //Bitmap转Mat
    public static Mat bitmapToMat(Bitmap bm) {
        Bitmap bmp32 = bm.copy(Bitmap.Config.RGB_565, true);
        Mat imgMat = new Mat ( bm.getHeight(), bm.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bmp32, imgMat);
        return imgMat;
    }
    public static Mat boke(Mat inputmat){
        Mat outputmat = new Mat();
        Imgproc.cvtColor(inputmat,outputmat,Imgproc.COLOR_BGRA2GRAY);
        return outputmat;
    }
    /**
     * 灰度化处理
     * Imgproc.cvtColor(mat,mat, Imgproc.COLOR_GRAY2RGBA,4);
     */
    //二值预处理
    public static Mat erzhiYu(Mat mat){
        Mat shuchu1 = new Mat(mat.size(),mat.type());
        Mat erzhimat = new Mat(shuchu1.size(),mat.type());
        Imgproc.cvtColor(mat, shuchu1, Imgproc.COLOR_BGR2GRAY);
        Bitmap b1 = MyUtils.matToBitmap(shuchu1);
        Imgproc.threshold(shuchu1,erzhimat,150,255,CvType.CV_8SC1);
        Bitmap b2 = MyUtils.matToBitmap(erzhimat);
        return erzhimat;
    }
    public static Mat suoFang(Mat mat){
        //缩放之后的图片
        Mat imageResized = mat.clone();
        float width=mat.width();
        float height=mat.height();
        //缩放图片
        Imgproc.resize(mat, imageResized, new Size(300,height*(300/width)));
        return imageResized;
    }
    //高斯滤波
    public static Mat removeNoiseGaussianBlur(Mat srcMat){
        Mat blurredImage=new Mat();
        Size size=new Size(7,7);
        Imgproc.GaussianBlur(srcMat,blurredImage,size,10,0);
        return blurredImage;
    }
    public static Rect findTFTcont(Mat mat){
        Rect rect = new Rect();
        Rect TFTrect = new Rect();
        Mat TFTmat = new Mat();

        List<MatOfPoint> contours = new ArrayList<>();
        Mat mat1 = new Mat();
        Imgproc.findContours(mat,contours,mat1,Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint2f curve = new MatOfPoint2f(contours.get(0).toArray());
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        double epsilon = 15;
        boolean closed = true;
        ArrayList<MatOfPoint> list = new ArrayList<>();
        Imgproc.approxPolyDP(curve, approxCurve, epsilon, closed );
        list.add(new MatOfPoint(approxCurve.toArray()));

        Mat image = Mat.zeros(mat.size(), CvType.CV_8UC3);
        int tft = -1;
        for (int i = 0; i <contours.size() ; i++) {
//            Imgproc.drawContours(matimg,contours,i,new Scalar(100),-1);
            Imgproc.drawContours(image,contours,i,new Scalar(100),-1);
            rect =Imgproc.boundingRect(contours.get(i));
            double mh = rect.height;
            double mw = rect.width;
            float bizhi = (float) (mw/mh);
            if (bizhi>1.5 && bizhi<2.1 && mh>100){
                tft = i;
                Log.i("ggg","TFT矩形为===>"+tft);
                Imgproc.rectangle(image,rect,new Scalar(0,255,0));         //将识别出的TFT显示框框出来
                TFTmat = new Mat(image, rect);      //将识别出的TFT显示框给裁剪出来
                Bitmap bm = matToBitmap(TFTmat);
                TFTrect =rect;
            }
            if (tft < 0){
                Imgproc.rectangle(image,rect,new Scalar(255,0,0));
                TFTrect =rect;
            }
        }
        return TFTrect;
    }
    //寻找形状边框
    public static Mat drawShapeCont(Mat mat){
        Rect rect = new Rect();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat findcmat = new Mat();
        Imgproc.findContours(mat,contours,findcmat,Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint2f curve = new MatOfPoint2f(contours.get(0).toArray());
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        double epsilon = 15;
        boolean closed = true;
        ArrayList<MatOfPoint> list = new ArrayList<>();
        Imgproc.approxPolyDP(curve, approxCurve, epsilon, closed );
        list.add(new MatOfPoint(approxCurve.toArray()));

        Mat image = Mat.zeros(mat.size(), CvType.CV_8UC3);

        Bitmap im = MyUtils.matToBitmap(image);

        for (int i = 0; i <contours.size() ; i++) {
            Imgproc.drawContours(image,contours,i,new Scalar(100),-1);
            rect =Imgproc.boundingRect(contours.get(i));
            Imgproc.rectangle(image,rect,new Scalar(255,0,0));
        }
        Bitmap im2 = MyUtils.matToBitmap(image);
        return image;
    }
    public static Mat duobianx(Mat mat,List<MatOfPoint> contours){

        MatOfPoint2f curve = new MatOfPoint2f(contours.get(0).toArray());
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        double epsilon = 15;
        boolean closed = true;
        ArrayList<MatOfPoint> list = new ArrayList<>();
        Imgproc.approxPolyDP(curve, approxCurve, epsilon, closed );
        list.add(new MatOfPoint(approxCurve.toArray()));
        return mat;
    }
    //边缘处理
    public static Mat bianYuanJianCe(Mat mat){
        Mat bianmat = mat.clone();
        Mat erzhimat = erzhiYu(mat);

        Mat fumat = mfuShi(erzhimat);
        Bitmap bfu = MyUtils.matToBitmap(fumat);
        Rect recttft = findTFTcont(fumat);
        Mat newmat = new Mat(bianmat,recttft);
        Bitmap newb = matToBitmap(newmat);

        return newmat;
    }

    public static Mat guoLv(Mat image){
        Mat mat = new Mat(image.size(),image.type());
        Imgproc.threshold(image,mat,0,255,CvType.CV_8SC1);
        return mat;
    }
    //提取轮廓
    public static Mat bianKuang(Mat image){
        List<MatOfPoint> contours = new ArrayList<>();
        Mat graym = new Mat(image.size(),image.type());
        Log.i("ggg", String.valueOf(graym.type()));
        Bitmap grabmp;
//        Mat erzmat = new Mat(graym.size(),graym.type());

        Imgproc.cvtColor(image,graym,Imgproc.COLOR_BGRA2GRAY);
        graym.convertTo(graym,CvType.CV_8UC1);
        grabmp = MyUtils.matToBitmap(graym);

        Bitmap erbmp = ImgPretreatment.doPretreatment(grabmp);
        Mat erzmat = bitmapToMat(erbmp);

//        Imgproc.threshold(graym,erzmat,0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        erzmat.convertTo(erzmat,CvType.CV_8UC1);
        Imgproc.findContours(erzmat,contours,new Mat(),Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        Scalar scalar =new Scalar(255,150,120);
        for (int i = 0; i <contours.size() ; i++) {
            Log.i("ggg","contours"+contours.toString());
            Imgproc.drawContours(erzmat,contours,i,scalar,-1);
        }
        return erzmat;
    }

    public static List<Point> getCornersByContour(Mat imgsource){
        List<MatOfPoint> contours=new ArrayList<>();
        //轮廓检测
        Imgproc.findContours(imgsource,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea=-1;
        int maxAreaIdx=-1;
        MatOfPoint temp_contour=contours.get(0);//假设最大的轮廓在index=0处
        MatOfPoint2f approxCurve=new MatOfPoint2f();
        for (int idx=0;idx<contours.size();idx++){
            temp_contour=contours.get(idx);
            double contourarea=Imgproc.contourArea(temp_contour);
            //当前轮廓面积比最大的区域面积大就检测是否为四边形
            if (contourarea>maxArea){
                //检测contour是否是四边形
                MatOfPoint2f new_mat=new MatOfPoint2f(temp_contour.toArray());
                int contourSize= (int) temp_contour.total();
                MatOfPoint2f approxCurve_temp=new MatOfPoint2f();
                //对图像轮廓点进行多边形拟合
                Imgproc.approxPolyDP(new_mat,approxCurve_temp,contourSize*0.05,true);
                if (approxCurve_temp.total()==4){
                    maxArea=contourarea;
                    maxAreaIdx=idx;
                    approxCurve=approxCurve_temp;
                }
            }
        }
        double[] temp_double=approxCurve.get(0,0);
        Point point1=new Point(temp_double[0],temp_double[1]);

        temp_double=approxCurve.get(1,0);
        Point point2=new Point(temp_double[0],temp_double[1]);

        temp_double=approxCurve.get(2,0);
        Point point3=new Point(temp_double[0],temp_double[1]);
        temp_double=approxCurve.get(3,0);

        Point point4=new Point(temp_double[0],temp_double[1]);

        List<Point> source=new ArrayList<>();
        source.add(point1);
        source.add(point2);
        source.add(point3);
        source.add(point4);
        //对4个点进行排序
        Point centerPoint=new Point(0,0);//质心
        for (Point corner:source){
            centerPoint.x+=corner.x;
            centerPoint.y+=corner.y;
        }
        centerPoint.x=centerPoint.x/source.size();
        centerPoint.y=centerPoint.y/source.size();
        Point lefttop=new Point();
        Point righttop=new Point();
        Point leftbottom=new Point();
        Point rightbottom=new Point();
        for (int i=0;i<source.size();i++){
            if (source.get(i).x<centerPoint.x&&source.get(i).y<centerPoint.y){
                lefttop=source.get(i);
            }else if (source.get(i).x>centerPoint.x&&source.get(i).y<centerPoint.y){
                righttop=source.get(i);
            }else if (source.get(i).x<centerPoint.x&& source.get(i).y>centerPoint.y){
                leftbottom=source.get(i);
            }else if (source.get(i).x>centerPoint.x&&source.get(i).y>centerPoint.y){
                rightbottom=source.get(i);
            }
        }
        source.clear();
        source.add(lefttop);
        source.add(righttop);
        source.add(leftbottom);
        source.add(rightbottom);
        return source;
    }
//    private static double[] redRow = new double [] {136,208, 195,};//red
//    private static double[] greenRow = new double [] {224,42, 211,};//green
//    private static double[] blueRow = new double [] {82,207, 20,};//blue
    private static double[] redRow = new double [] {255,50, 50,};//red
    private static double[] greenRow = new double [] {50,255, 50,};//green
    private static double[] blueRow = new double [] {50,50, 255,};//blue
    private static String color ="";
    public static void getPixs(Mat image){

        //缩放之后的图片
        Mat imageResized = image.clone();
        float width=image.width();
        float height=image.height();
        //缩放图片
        Imgproc.resize(image, imageResized, new Size(300,height*(300/width)));
        //Imgcodecs.imwrite("/../imgs/resize.jpg",imageResized);
        float ratio = image.width() / imageResized.width();//计算比例
        //模糊图像
        Mat blurredImg = imageResized.clone();
        Mat labImg =imageResized.clone();
        Imgproc.GaussianBlur(imageResized,blurredImg,new Size(5,5),0);
        Imgproc.cvtColor(blurredImg, labImg, Imgproc.COLOR_BGR2Lab);
        Mat mask = Mat.zeros(image.height(),image.width(), CvType.CV_8UC1);
        Mat erodeImg = new Mat(mask.size(),mask.type());
        Imgproc.erode(mask,erodeImg,new Mat(),new Point(),2);
        Bitmap bm1= MyUtils.matToBitmap(blurredImg);

        Bitmap bm2= MyUtils.matToBitmap(mask);
        Bitmap bm3= MyUtils.matToBitmap(labImg);

        //Imgcodecs.imwrite("F:\\ideawork\\ShapeColorDetector\\imgs\\ercode.jpg",erodeImg);
        Scalar scalar = Core.mean(mask,erodeImg);
        //申明计算标准差后的结果数组
        double[] meanScalar = new double [] {
                scalar.val[0],
                scalar.val[1],
                scalar.val[2]
        };
        EuclideanDistance euclideanDistance = new EuclideanDistance();
        /**
         *1.分别计算红色、绿色、蓝色和标准差的距离
         */
        double redDistance = euclideanDistance.compute(redRow,meanScalar);
        double greenDistance = euclideanDistance.compute(greenRow,meanScalar);
        double blueDistance = euclideanDistance.compute(blueRow,meanScalar);
        if (redDistance < greenDistance && redDistance < blueDistance){//红色距离最小
            color = "red";
            Log.i("ggg","red");
        }else if (greenDistance < blueDistance){ //绿色距离最小
            color = "green";
            Log.i("ggg","green");
        }else{ //黄色最小
            color = "yellow";
            Log.i("ggg","yellow");
        }

    }
    //Mat图像转hsv
    public static Mat colorToHSV(Mat mat){
        Bitmap map =matToBitmap(mat);
        Mat lastmat = bitmapToMat(map);
        Mat nowRegionHsv = new Mat();
        Imgproc.cvtColor(lastmat, nowRegionHsv, Imgproc.COLOR_RGB2HSV);
        return nowRegionHsv;
    }
    //bitmap图像转hsv
    public static Bitmap colorToHSV(Bitmap bitmap){
        Mat lastmat = bitmapToMat(bitmap);
        Mat nowRegionHsv = new Mat();
        Imgproc.cvtColor(lastmat, nowRegionHsv, Imgproc.COLOR_RGB2HSV);
        return matToBitmap(nowRegionHsv);
    }
    //图片腐蚀，放大暗像素
    public static Bitmap bfuShi(Bitmap bitmap){
        Mat ermat=new Mat();
        Mat mat = bitmapToMat(bitmap);
        Mat elemat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(3, 3), new Point(-1, -1));
        Imgproc.erode(mat,ermat,elemat,new Point(-1,-1),3);
        return matToBitmap(ermat);
    }
    public static Mat mfuShi(Mat mat){
        Mat ermat=mat.clone();
        Mat elemat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(3, 3), new Point(-1, -1));
        Imgproc.erode(mat,ermat,elemat,new Point(-1,-1),2);
        return ermat;
    }
    //图片膨胀，放大亮像素
    public static Bitmap bpengZhang(Bitmap bitmap){
        Mat ermat=new Mat();
        Mat mat = bitmapToMat(bitmap);
        Mat elemat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(5, 2));
        Imgproc.dilate(mat,ermat,elemat,new Point(-1,-1),1);
        return matToBitmap(elemat);
    }
    public static Mat mpengZhang(Mat mat){
        Mat ermat=new Mat();
        Mat elemat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(3,3), new Point(-1, -1));
        Imgproc.dilate(mat,ermat,elemat,new Point(-1,-1),1);
        return elemat;
    }

    public static String removeMethod(String s) {
        System.out.println("去重前----:" + s);
        StringBuffer bu = new StringBuffer();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (s.indexOf(c) == s.lastIndexOf(c)) {// 此字符第一次位置和最后位置一致
                bu.append(c); // 即肯定没有重复的直接添加
            } else {// 同理 次字符出现过多次
                int number = s.indexOf(c);// 次字符第一次出现的位置
                if (number == i) {// 第一次出现的位置和当前位置一致 即第一次出现添加
                    bu.append(c);
                }
            }
        }
        System.out.println("去重后----:" + bu.toString());
        return bu.toString();
    }
}
