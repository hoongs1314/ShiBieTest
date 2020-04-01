package com.example.a27796.shibietest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a27796.shibietest.RGB_Graying.RGBLuminanceSource;
import com.example.a27796.shibietest.utils.BitmapUtils;
import com.example.a27796.shibietest.utils.ColorBlobDetector;
import com.example.a27796.shibietest.utils.ColorDector;
import com.example.a27796.shibietest.utils.Coordinates;
import com.example.a27796.shibietest.utils.ImgPretreatment;
import com.example.a27796.shibietest.utils.MyUtils;
import com.example.a27796.shibietest.utils.ShapeDector;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    static String TAG = "ggg";
    Button b1;
    Button b2;
    Button b3;
    Button b4;

    TextView show_news;
    String textResult = "";
    ImageView showImg;
    JavaCameraView mjcv;
    MainActivity mainActivity;
    MyUtils myUtils;
    private CameraBridgeViewBase cbvb;
    Bitmap map = null;
    Bitmap smallBitmap = null;
    Bitmap erzhiBitmap = null;
    Bitmap graymap = null;
    Bitmap hsvmap = null;
    Bitmap bymap = null;
    private final int TESSTWORESULT = 1001;
    private final int TESSTWORREADIMG = 1002;
    private final int QRCODEIMG = 1003;
    private final int COLORIMG = 1004;
    private final int GREENCOLOR = 101;
    private final int REDCOLOR = 102;
    private final int YELLOWCOLOR = 103;
    private final int HSVIMG = 104;
    private final int BIANYUAN = 105;

    private Mat mRgba;
    private Mat mTmp;

    private static String LANGUAGE = "num";
    private static String ZIKU_PATH = getSDPath() + java.io.File.separator
            + "tessdata";

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    cbvb.enableView();
                    break;
                default:
                    break;
            }
        }
    };


    // 搜索进度
    private ProgressDialog progressDialog = null;

    // 搜索摄像cameraIP进度条
    private void search() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在搜索摄像头");
        progressDialog.show();
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SearchService.class);
        startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
//            Log.d(TAG,"OpenCV library not found!");
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Request();
        }
        initView();
        CameraInitCamera();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void Request() {             //获取相机拍摄读写权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//版本判断
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
            }
        }
    }

//一个权限没有，就一次申请所有所需的权限，这样可以在打开应用的时候获得所有权限

    public void initView() {
        b1 = findViewById(R.id.qrCodeB);
        b1.setOnClickListener(new initClick());
        b2 = findViewById(R.id.colorB);
        b2.setOnClickListener(new initClick());
        b3 = findViewById(R.id.shapeB);
        b3.setOnClickListener(new initClick());
        b4 = findViewById(R.id.chepaiB);
        b4.setOnClickListener(new initClick());

        showImg = findViewById(R.id.showImg);
        show_news = findViewById(R.id.show_news);
        mjcv = findViewById(R.id.javaCV);
        cbvb = (CameraBridgeViewBase) findViewById(R.id.javaCV);
        cbvb.setCvCameraViewListener(this);
    }


    //初始化摄像头
    private void CameraInitCamera() {
        //设置为可见
        mjcv.setVisibility(SurfaceView.VISIBLE);
        // 0  前置  1  后置
        mjcv.setCameraIndex(0);    //  0 是前置     1 是后置
        mjcv.setCvCameraViewListener(this);    //set up frame listener
        mjcv.enableFpsMeter();
        mjcv.enableView();
        mjcv.enableFpsMeter();
        //用完就直接释放
        if (mjcv != null) {
            mjcv.disableView();
        }
        mjcv.enableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mTmp = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Mat frame = inputFrame.rgba();
        map = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(frame, map);
        return frame;
    }

    private class initClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.qrCodeB:
                    Toast.makeText(MainActivity.this, "正在加载二维码识别", Toast.LENGTH_SHORT).show();
                    Qr_recognition();
                    break;
                case R.id.colorB:
                    Toast.makeText(MainActivity.this, "正在加载颜色识别", Toast.LENGTH_SHORT).show();
                    newColor();
                    break;
                case R.id.shapeB:
                    Toast.makeText(MainActivity.this, "正在加载图形识别", Toast.LENGTH_SHORT).show();
                    my_shape();
                    break;
                case R.id.chepaiB:
                    Toast.makeText(MainActivity.this, "正在加载车牌识别", Toast.LENGTH_SHORT).show();
                    importTraineddata();
                    chePaiShib();
                    break;
            }
        }
    }

    // 沉睡
    public void yanchi(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 图片识别
     */
    //二维码识别
    private String result_qr;
    public void Qr_recognition() {
        smallBitmap = BitmapUtils.getSmallBitmap(map);
        graymap = ImgPretreatment.converyToGrayImg(smallBitmap);
        showImg.setImageBitmap(graymap);
        Bitmap graybmp = ImgPretreatment.converyToGrayImg(smallBitmap);
        Result result = null;
        RGBLuminanceSource rSource = new RGBLuminanceSource(
                graybmp);
        try {
            //将图片转化为二进制图片
            BinaryBitmap binaryBitmap = new BinaryBitmap(
                    new HybridBinarizer(rSource));

            Map<DecodeHintType, String> hint = new HashMap<DecodeHintType, String>();
            hint.put(DecodeHintType.CHARACTER_SET, "utf-8");
            //初始化解析对象
            QRCodeReader reader = new QRCodeReader();
            //开始解析
            result = reader.decode(binaryBitmap, hint);
            //输出解析结果
            result_qr = result.toString();
            Log.i("ggg", "qrCodeRe" + result_qr);
            while (result_qr == null) {
                yanchi(1);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        Message msg = new Message();
        msg.what = QRCODEIMG;
        myHandler.sendMessage(msg);
    }

    //颜色识别
    public void newColor() {
        smallBitmap = BitmapUtils.getSmallBitmap(map);
        final int i = newconvertToBlack(smallBitmap);
        Log.i("ggg", "" + i);
        Message msg = new Message();
        switch (i) {
            case 0:
                msg.what = GREENCOLOR;
                break;
            case 1:
                msg.what = REDCOLOR;
                break;
            case 2:
                msg.what = YELLOWCOLOR;
                break;
        }
        myHandler.sendMessage(msg);
    }

    private int color = 2;

    private int newconvertToBlack(Bitmap bitmap) {
        //处理杂色
        Bitmap result = mColor(bitmap);
        Bitmap bm2 = MyUtils.bfuShi(result);
        Bitmap finalmap = mColor(bm2);
        return color;
    }

    //颜色处理
    public Bitmap mColor(Bitmap smallBitmap) {
        int width = smallBitmap.getWidth();
        int height = smallBitmap.getHeight();
        int red = 0;
        int green = 0;
        int blue = 0;
        int yellow = 0;
        int[] pixels = new int[width * height];
        smallBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] pl = new int[smallBitmap.getWidth() * smallBitmap.getHeight()];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                int pixel = pixels[offset + x];
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                if (r > 200 && g < 180 && b < 180) {
                    if (g > 130) {
                        //黄色
                        pl[offset + x] = 0xFFFFa700;
                        yellow = yellow + 1;
                    } else {
                        // 红色
                        pl[offset + x] = 0xFFFF0000;
                        red = red + 1;
                    }
                } else if (r < 160 && g > 170 && b < g) {
                    // 绿色
                    pl[offset + x] = 0xFF00FF00;
                    green = green + 1;
                } else if (r < 180 && g < 150 && b > 160) {
                    // 蓝色
                    pl[offset + x] = 0xFF0000FF;
                    blue = blue + 1;
                } else
                    pl[offset + x] = 0xff000000;// 黑色
            }
        }
        if (green > red && green > blue && green > yellow) {
            color = 2;
        } else if (red > green && red > blue) {
            color = 1;
        } else {
            color = 3;
        }
        Bitmap result = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        showImg.setImageBitmap(result);
        result.setPixels(pl, 0, width, 0, 0, width, height);
        return result;
    }


    /**
     * TFT框比例
     * 8.5:14.5≈0.58
     * 0.54-0.59
     * <p>
     * 车牌比例
     * 3.3:11=3:10 ≈0.33
     * 0.29-0.36
     * <p>
     * 车牌识别
     */
    private void chePaiShib() {
        smallBitmap = BitmapUtils.getSmallBitmap(map);
//        erzhiBitmap =MyUtils.erzhiYu(smallBitmap);
        Mat yuan = MyUtils.bitmapToMat(smallBitmap);
        Mat bianmat = MyUtils.bianYuanJianCe(yuan);
        Bitmap bianmap = MyUtils.matToBitmap(bianmat);
        savebitmap(bianmap);

        final Bitmap bm = MyUtils.matToBitmap(bianmat);

        showImg.setImageBitmap(bm);
        Message msg = new Message();
        msg.what = TESSTWORREADIMG;
        myHandler.sendMessage(msg);
        textResult = doOcr(bm
                , LANGUAGE);
        Log.i("ggg", "textResult:" + textResult);

        String reg = "[^0-9a-zA-Z\u4e00-\u9fa5.，,。？“”]+";
        String regEx = "[^a-zA-Z0-9]";

        textResult = textResult.replaceAll(regEx, "");
        Log.i("ggg", "textResult:" + textResult);
        Message msg2 = new Message();
        msg2.what = TESSTWORESULT;
        myHandler.sendMessage(msg2);
    }
    int saveBp = 0;
    private void savebitmap(Bitmap bm)
    {
        saveBp=saveBp+1;
        //因为xml用的是背景，所以这里也是获得背景
        Bitmap bitmap=bm;
        //创建文件，因为不存在2级目录，所以不用判断exist，要保存png，这里后缀就是png，要保存jpg，后缀就用jpg
        File file=new File(Environment.getExternalStorageDirectory() +"/Pictures/pai"+saveBp+".png");
        try {
            //文件输出流
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
            //写入，这里会卡顿，因为图片较大
            fileOutputStream.flush();
            //记得要关闭写入流
            fileOutputStream.close();
            //成功的提示，写入成功后，请在对应目录中找保存的图片
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //失败的提示
        } catch (IOException e) {
            e.printStackTrace();
            //失败的提示
        }

    }
    @SuppressLint("HandlerLeak")
    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TESSTWORESULT:
                    if (textResult == null || textResult.equals(""))
                        show_news.setText("识别失败");
                    else
                        show_news.setText(textResult);
                    break;
                case TESSTWORREADIMG:
                    show_news.setText("识别中......");
                case QRCODEIMG:
                    if (result_qr == null || result_qr.equals(""))
                        show_news.setText("识别失败");
                    else
                        show_news.setText(result_qr);
                    break;
                case GREENCOLOR:
                    show_news.setText("绿色");
                    break;
                case REDCOLOR:
                    show_news.setText("红色");
                    break;
                case YELLOWCOLOR:
                    show_news.setText("黄色");
                    break;
            }
        }
    };

    public void importTraineddata() {
        importZiKu();
        CopyAssets(ZIKU_PATH);
    }

    private void CopyAssets(final String dir) {
        final String[] files;
        try {
            // 获得Assets一共有多少文件,无二级目录即填写""
            files = this.getResources().getAssets().list("");
        } catch (IOException e1) {
            return;
        }
        final File mWorkingPath = new File(dir);
        // 如果文件路径不存在
        if (!mWorkingPath.exists()) {
            // 创建文件夹
            if (!mWorkingPath.mkdirs()) {
                // 文件夹创建不成功时调用
                Toast.makeText(this, "字库文件夹创建失败！请检查文件夹是否创建", Toast.LENGTH_SHORT).show();
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < files.length; i++) {
                    try {
                        // 获得每个文件的名字
                        String fileName = files[i];
                        if (fileName.contains(".traineddata")) {
                            File outFile = new File(mWorkingPath, fileName);
                            if (outFile.exists())
                                outFile.delete();
                            InputStream in = null;
                            in = getAssets().open(fileName);// 读取字库

                            OutputStream out = new FileOutputStream(outFile);
                            // Transfer bytes from in to out
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);     // 开始写入
                            }
                            out.flush();
                            in.close();
                            out.close();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    // 一键导入字库
    private void importZiKu() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 若文件夹不存在 首先创建文件夹 并把字库文件导入
                File path = new File(ZIKU_PATH);
                if (path.exists()) {
                    Log.e("字库文件存在", path.getPath());
                    return;
                }
                Log.e("字库文件不存在", path.getPath());
                path.mkdirs();
                OutputStream os = null;
                InputStream is = null;
                try {
                    // 创建本地的字库文件
                    os = new FileOutputStream(new File(ZIKU_PATH, "num.traineddata"));
                    // 得到内部的字库文件
                    AssetManager manager = getAssets();
                    is = manager.open("num.traineddata");
                    byte[] b = new byte[1024];
                    while (is.read(b) != -1) {
                        os.write(b);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Toast.makeText(MainActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
                    try {
                        if (os != null)
                            os.close();
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //进行图片识别

    public String doOcr(Bitmap bitmap, String language) {
        TessBaseAPI baseApi = new TessBaseAPI();
        // 必须加此行，tess-two要求BMP必须为此配置
        baseApi.init(getSDPath(), language);
        System.gc();
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        baseApi.setImage(bitmap);

        String text = baseApi.getUTF8Text();
        baseApi.clear();
        baseApi.end();
        return text;
    }


    //获取sd卡的路径

    //@return 路径的字符串

    public static String getSDPath() {

        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取外存目录
            Log.e(TAG, "getSDPath: " + sdDir);
        } else {
            Log.i("测试", "SD卡不存在");
        }
        return sdDir.toString();
    }

    /**
     * 图形识别
     */

    private int rectNum = 0;//矩形
    private int triaNum = 0;//三角形
    private int circNum = 0;//圆形

    public void my_shape() {
        smallBitmap = BitmapUtils.getSmallBitmap(map);
        Bitmap bm = smallBitmap;
        Mat ymat = MyUtils.bitmapToMat(bm);

        Mat bianmat = MyUtils.bianYuanJianCe(ymat);
        Bitmap newbm = MyUtils.matToBitmap(bianmat);

        Bitmap toblack = convertToBlack(newbm);
        Mat tobmat = MyUtils.bitmapToMat(toblack);
        tobmat = MyUtils.mfuShi(tobmat);
        Bitmap tobmap = MyUtils.matToBitmap(tobmat);
        my_shapere(tobmap);
    }

    public void my_shapere(Bitmap bitmap) {
        Mat tobmat = MyUtils.bitmapToMat(bitmap);
        Mat erzhi = MyUtils.erzhiYu(tobmat);

        //定义2个
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(erzhi, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //计算轮廓距
        List<Moments> momList = new ArrayList<>(contours.size());

        Rect rect = new Rect();
        Mat image2 = Mat.zeros(tobmat.size(), CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(image2, contours, i, new Scalar(100), -1);
            rect = Imgproc.boundingRect(contours.get(i));
            int height = rect.height;
            int width = rect.width;
            if ((height / width > 0.8 && height / width < 1.2) && (height > 10)) {
                Imgproc.rectangle(image2, rect, new Scalar(255, 0, 0));
            }
        }
        shapeResult.put("triangle", 0);
        shapeResult.put("square", 0);
        shapeResult.put("rectangle", 0);
        shapeResult.put("pentagon", 0);
        shapeResult.put("circle", 0);

        String shape;
        String color;
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f newMatOfPoint2f = new MatOfPoint2f(contours.get(i).toArray());
            ShapeDector shapeDector = new ShapeDector();
            shape = shapeDector.detect(contours.get(i), newMatOfPoint2f);
            //根据形状分别计算数量
            switch (shape) {
                case "triangle":    //三角形
                    shapeResult.put("triangle", shapeResult.get("triangle") + 1);
                    break;
                case "square":  //正方形
                    shapeResult.put("square", shapeResult.get("rectangle") + 1);
                    break;
                case "rectangle":   //矩形
                    shapeResult.put("rectangle", shapeResult.get("rectangle") + 1);
                    break;
                case "pentagon":    //五角星
                    shapeResult.put("pentagon", shapeResult.get("pentagon") + 1);
                    break;
                case "circle":    //圆
                    shapeResult.put("circle", shapeResult.get("circle") + 1);
                    break;
            }
            /**
             * 颜色检测
             */
            ColorDector colorDector = new ColorDector();
            color = colorDector.detect(tobmat, contours, i);
            if (shape != "unidentified") {
                Log.i("ggg", "第" + i + "个图形是color:" + color + "---shape:" + shape);
            }
        }
        int triangle = shapeResult.get("triangle"), square = shapeResult.get("square"), rectangle = shapeResult.get("rectangle"),
                circle = shapeResult.get("circle"), pentagon = shapeResult.get("pentagon");
        Log.i("ggg", "三角形的数量为：" + triangle);
        Log.i("ggg", "正方形的数量为：" + square);
        Log.i("ggg", "矩形的数量为：" + rectangle);
        Log.i("ggg", "圆形的数量为：" + circle);
        Log.i("ggg", "五角星：" + pentagon);
    }

    // 显示图片
    @SuppressLint("HandlerLeak")
    public Handler mphHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                showImg.setImageBitmap(smallBitmap);
            }
            if (msg.what == 50) {
                show_news.setText("红色圆形");
            }
            if (msg.what == 51) {
                show_news.setText("绿色圆形");
            }
            if (msg.what == 52) {
                show_news.setText("蓝色圆形");
            }

            if (msg.what == 60) {
                show_news.setText("红色三角形");
            }
            if (msg.what == 61) {
                show_news.setText("绿色三角形");
            }
            if (msg.what == 62) {
                show_news.setText("蓝色三角形");
            }

            if (msg.what == 70) {
                show_news.setText("红色矩形");
            }
            if (msg.what == 71) {
                show_news.setText("绿色矩形");
            }
            if (msg.what == 72) {
                show_news.setText("蓝色矩形");
            }
            if (msg.what == 80) {
                Toast.makeText(MainActivity.this, "请纠正算法", Toast.LENGTH_LONG).show();
            }
        }
    };

    private int RGB_num = 0;

    private Bitmap convertToBlack(Bitmap bip) {
//        RGB_num = index;
        if (bip == null) {
            Log.i("测试", "图片对象为空");
        }
        int width = bip.getWidth();
        int height = bip.getHeight();
        int red = 0;
        int green = 0;
        int blue = 0;
        int[] pixels = new int[width * height];
        bip.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] pl = new int[bip.getWidth() * bip.getHeight()];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                int pixel = pixels[offset + x];
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
//                switch
                if (r > 130 && g < 70 && b < 70) {
                    // 红色
                    pl[offset + x] = 0xFFFF0000;
                    red = red + 1;
                } else if (r < 80 && g > 140 && b < 100) {
                    // 绿色
                    pl[offset + x] = 0xFF00FF00;
                    green = green + 1;
                } else if (r < 100 && g < 130 && b > 210) {
                    // 蓝色
                    pl[offset + x] = 0xFF0000FF;
                    blue = blue + 1;
                } else if (r < 30 && g < 30 && b < 30) {
                    // 黑色
                    pl[offset + x] = 0xff000000;
                } else if (r > 150 && g < 120 && b > 200) {
//                    //品色
                    pl[offset + x] = 0xFFFF00FF;
//                } else if (r >180 && g > 180 && b < 80) {
//                    //黄色
//                    pl[offset + x] = 0xFFFFFF00;
//                } else if (r > 200 && g > 200 && b > 200) {
//                    //白色
//                    pl[offset + x] = 0xFFFFFFFF;
//                } else if (r < 100 && g > 200 && b > 200) {
//                    //青色
//                    pl[offset + x] = 0xFF00FFFF;
//                }else if (r > 220 && g < 100 && b > 220) {
//                    //品色
//                    pl[offset + x] = 0xFFFF00FF;
                } else
                    //黄色
                    pl[offset + x] = 0xFFFFFF00;
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        result.setPixels(pl, 0, width, 0, 0, width, height);
        showImg.setImageBitmap(result);
        return result;
    }

    private int cnum = 2;
    private boolean flag_go = true;
    private static Map<String, Integer> shapeResult = new HashMap<String, Integer>();

    private void shape_re() {
        Mat yuan = new Mat();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/1/shape.png";
        File file = new File(path);
        if (file.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(path);
            yuan = MyUtils.bitmapToMat(bm);
        } else {
            Log.i("ggg", "没找到");
        }
        Mat erzhi = MyUtils.erzhiYu(yuan);
        Bitmap berzhi = MyUtils.matToBitmap(erzhi);
        Mat pzmat = erzhi.clone();
        //定义2个
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(pzmat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Rect rect = new Rect();
        Mat image2 = Mat.zeros(pzmat.size(), CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(image2, contours, i, new Scalar(100), -1);
            rect = Imgproc.boundingRect(contours.get(i));
            int height = rect.height;
            int width = rect.width;
            if ((height / width > 0.8 && height / width < 1.2) && (height > 1)) {
                Imgproc.rectangle(image2, rect, new Scalar(255, 0, 0));
            }
        }

        shapeResult.put("triangle", 0);
        shapeResult.put("square", 0);
        shapeResult.put("rectangle", 0);
        shapeResult.put("pentagon", 0);
        shapeResult.put("circle", 0);

        String shape;
        String color;
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f newMatOfPoint2f = new MatOfPoint2f(contours.get(i).toArray());
            ShapeDector shapeDector = new ShapeDector();
            shape = shapeDector.detect(contours.get(i), newMatOfPoint2f);
            //根据形状分别计算数量
            switch (shape) {
                case "triangle":
                    shapeResult.put("triangle", shapeResult.get("triangle") + 1);
                    break;
                case "square":
                    shapeResult.put("square", shapeResult.get("rectangle") + 1);
                    break;
                case "rectangle":
                    shapeResult.put("rectangle", shapeResult.get("rectangle") + 1);
                    break;
                case "pentagon":
                    shapeResult.put("pentagon", shapeResult.get("pentagon") + 1);
                    break;
                default:
                    shapeResult.put("circle", shapeResult.get("circle") + 1);
                    break;
            }
            /**
             * 颜色检测
             */
            ColorDector colorDector = new ColorDector();
            color = colorDector.detect(pzmat, contours, i);
            Log.i("ggg", "第" + i + "个图形是color:" + color + "---shape:" + shape);
        }
        Log.i("ggg", "三角形的数量为：" + shapeResult.get("triangle"));
        Log.i("ggg", "矩形的数量为：" + shapeResult.get("rectangle"));
        Log.i("ggg", "圆形的数量为：" + shapeResult.get("circle"));
        Log.i("ggg", "五角星：" + shapeResult.get("pentagon"));
        Log.i("ggg", "圆形的数量为：" + shapeResult.get("circle"));

        showImg.setImageBitmap(MyUtils.matToBitmap(yuan));
    }


    //对获取权限处理的结果
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //检验是否获取权限，如果获取权限，外部存储会处于开放状态，会弹出一个toast提示获得授权
                    String sdCard = Environment.getExternalStorageState();
                    if (sdCard.equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(this, "获得授权", Toast.LENGTH_LONG).show();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "授权失败,无法使用车牌识别", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
