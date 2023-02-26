package com.example.senner.Helper;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecondStageProcess {

    private float histGray = 0.38F;
    private final double targetSize;
    private boolean isDebug = false;

    //构造函数中初始化加载OpenCV库
    SecondStageProcess(float histGray, float targetSize, boolean isDebug){
        LoadOpenCV();
        this.histGray = histGray;
        this.targetSize = targetSize;
        this.isDebug = isDebug;
    }

    private void LoadOpenCV() {
       OpenCVLoader.initDebug();   //对OpenCV库进行初始化加载，bool返回值可以判断是否加载成功。

    }

    /**
     * 输入 两点坐标
     * 输出 计算两点距离
     */
    private double CalculateDistanceTwoPoints(Point point1, Point point2){
        return Math.sqrt((point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y));
    }

    /**
     *输入 一个角点坐标和5个中心圆坐标
     * 调用函数1计算5个中心坐标距离 角点的距离
     * 输出5个坐标距离中距离角点最近的那一个坐标
     */
    private int FindNearestPoint(Point cornerPoint, List<Point> centerPoints){
        // 定义一个容器保存计算出的距离
        List<Double> Distances = new ArrayList<>();
        for(int i = 0; i < centerPoints.size(); i ++){
            Distances.add(CalculateDistanceTwoPoints(cornerPoint, centerPoints.get(i)));
        }
        Log.e("Distance", String.valueOf(Distances));
        Log.e("Distance", String.valueOf(centerPoints));

        double temp = Distances.get(0);
        // 创建一个始终指向最小元素的指针
        int index = 0;
        for(int i = 0; i < Distances.size(); i ++){
            if(temp > Distances.get(i)){
                temp = Distances.get(i);
                index = i;
            }
        }
        // 注意到循环完一次，Distance重置
        Distances.clear();
        return index;
    }

    /**
     * 输入5个中心点坐标和symbol图像的4个角点坐标
     * 4个角点输入顺序为[left_up, right_up, left_down, right_down]
     * 调用函数2 分别得出离4个角点最近的中心坐标
     * 输出排序后的5个中心坐标，排列后顺序为[left_up, right_up, left_down, right_down, core]
     */
    private List<Point> RankCenterPoints(List<Point> cornerPoints, List<Point> centerPoints){
        // 创建容器存放排序后的中心点
        List<Point> RankedCenterPoints = new ArrayList<>();
        for(int i = 0; i < cornerPoints.size(); i ++){
            // 按最小距离保存与角点对应的中心点
            // 存储完最近点后，将原来得位置赋值为空
            // 这样最后不为空的是core点
            // 找到距离最近点的下标
            int index = FindNearestPoint(cornerPoints.get(i), centerPoints);
            // 将找到的点存放在排列好的容器中
            RankedCenterPoints.add(centerPoints.get(index));
            // 原容器移除找到的点
            centerPoints.remove(index);
        }
        // 最后一个位置存放中心点
        for (Point centerPoint : centerPoints) {
            if (centerPoint != null) {
                RankedCenterPoints.add(centerPoint);
            }
        }

        return RankedCenterPoints;
    }

    /**
     * 输入排序后5个中心坐标及中心坐标权重
     * 计算4个边缘小圆中心的对角线交点坐标
     * 输出标志物的中心点坐标 中心点坐标 = 权重 * 中心大圆坐标+（1-权重）*对角线交点坐标
     */
    private Point CalculateCenterPointCoordinate(double weight, List<Point> centerPoints){

        double x1 = centerPoints.get(0).x;
        double y1 = centerPoints.get(0).y;
        double x2 = centerPoints.get(3).x;
        double y2 = centerPoints.get(3).y;
        double x3 = centerPoints.get(1).x;
        double y3 = centerPoints.get(1).y;
        double x4 = centerPoints.get(2).x;
        double y4 = centerPoints.get(2).y;

        Point crossPoint = new Point();
        crossPoint.x = (x1*x3*y2 - x1*x3*y4 - x1*x4*y2 + x1*x4*y3 - x2*x3*y1 + x2*x3*y4 + x2*x4*y1 - x2*x4*y3)/ (x1*y3 - x1*y4 - x2*y3 + x2*y4 - x3*y1 + x3*y2 + x4*y1 - x4*y2);
        crossPoint.y = (x1*y2*y3 - x1*y2*y4 - x2*y1*y3 + x2*y1*y4 - x3*y1*y4 + x3*y2*y4 + x4*y1*y3 - x4*y2*y3)/(x1*y3 - x1*y4 - x2*y3 + x2*y4 - x3*y1 + x3*y2 + x4*y1 - x4*y2);

        // 输出标志物的中心点坐标 中心点坐标 = 权重 * 中心大圆坐标+（1-权重）*对角线交点坐标
        crossPoint.x = weight * centerPoints.get(4).x + (1 - weight) * crossPoint.x;
        crossPoint.y = weight * centerPoints.get(4).y + (1 - weight) * crossPoint.y;

        return crossPoint;
    }

    /**
     * 输入排序后的坐标点集，和标志板尺寸
     *
     * length = 130*标志板尺寸/20
     * length1 = left_up和right_up两点距离
     * length2 = left_down和right_down两点距离
     * length3 = left_up和left_down两点距离
     * length4 = right_up和right_down两点距离
     * 输出比例因子kx= length*2/(length1+length2)  ky= length*2/(length3+length4)
     */
    private Size CalculateRatio(List<Point> centerPoints){
        double[] Distances = new double[5];
        Distances[0] = 130 * targetSize / 20;
        Distances[1] = CalculateDistanceTwoPoints(centerPoints.get(0), centerPoints.get(1));
        Distances[2] = CalculateDistanceTwoPoints(centerPoints.get(2), centerPoints.get(3));
        Distances[3] = CalculateDistanceTwoPoints(centerPoints.get(0), centerPoints.get(2));
        Distances[4] = CalculateDistanceTwoPoints(centerPoints.get(1), centerPoints.get(3));
        Size Ratio = new Size();
        Ratio.width = Distances[0] * 2 / (Distances[1] + Distances[2]);
        Ratio.height = Distances[0] * 2 / (Distances[3] + Distances[4]);

        return Ratio;
    }

    // 创建一个适合保存结果的数据结构
    public static class SecondStageResult{
        Bitmap ProcessedImage;
        Point centerPoint;
        Size Ratio;

        public SecondStageResult(Bitmap ProcessedImg, Point centerPoint, Size Ratio){
            this.ProcessedImage =ProcessedImg;
            this.centerPoint = centerPoint;
            this.Ratio = Ratio;

        }

    }

    /**
     * 输入视频流
     * While TRUE：
     * 	获取视频流的一帧 frame1
     * 	对frame1进行目标检测获取目标坐标其中左上角坐标为x_min，y_min
     * 	使用坐标对frame1进行图像截取，获取目标图像symbol
     *     对symbol图像进行直方图均值化 %cv2.createCLAHE(clipLimit=2, tileGridSize=(3, 3))
     * 	获取symbol图像灰度直方图数据
     * 	对灰度直方图像素按灰度值大小进行排列，取总个数的38% 像素对应的灰度值作为阈值thres
     * 	对symbol进行二值化得到图像thresh %cv2.threshold(symbol,thres,255,cv2.THRESH_BINARY)
     * 	对二值化图像thresh取反得到mask_inv %cv2.bitwise_not(thresh)
     * 	得到thresh外轮廓位置 % cv2.findContours(thresh,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)
     *
     * 	%对外轮廓进行填充
     * 	for i in 外轮廓像素个数
     * 		填充mask_inv中的外轮廓像素位置为黑色%cv2.fillPoly(mask_inv,[外轮廓[i]],0)
     * 	End
     *
     * 	叠加thresh和mask_inv图像%cv2.add(thresh,mask_inv)
     * 	对thresh图像进行先进行开运算再进行闭运算%cv2.morphologyEx()卷积核尺寸为3
     * 	对thresh图像进行高斯滤波%cv2.GaussianBlur卷积核尺寸为3
     * 	对thresh图像进行边缘检测%cv2.Canny(thresh,75,200)
     * 	提取轮廓数据% cv2.findContours(thresh,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE)
     *
     * 	创建一个空数组core_xy
     * 	For i in 轮廓数据
     * 		取一个轮廓拟合椭圆%cv2.fitEllipse
     * 		将椭圆中心坐标存储到core_xy
     * 	End
     *
     * 	If core_xy存储的坐标个数不为5
     * 		Continue
     * 	End
     *
     * 	调用函数3对core_xy排序
     * 	调用函数4得到标志物中心点坐标picture_core_x,picture_core_y
     * 	调用函数5计算比例因子kx，ky  %此函数中的标志物尺寸需在进行视觉监测任务前输入若无输入可默认为20
     *
     * 	symbol_x = x_min + picture_core_x %计算中心坐标在第一帧中的像素坐标
     *     symbol_y = y_min + picture_core_y
     *
     * 	if 是第一帧图像：
     * 		symbol_x_first = symbol_x
     *         symbol_y_first = symbol_y
     * 	end
     *
     *
     * 	distance_x = (symbol_x - symbol_x_first)*kx
     *     distance_y = (symbol_y_first - symbol_y)*ky
     *
     *
     * 	存储distance_x和distance_y
     * @param bitmap 阶段一获得的图像
     * @return
     */
    public SecondStageResult Process(Bitmap bitmap, List<Point> cornerPoints) {

        Mat image = new Mat();
        // 默认转化成的Mat为RGBA
        Utils.bitmapToMat(bitmap, image);
        // 灰度化
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGBA2GRAY);
        // 直方图均衡化
        CLAHE clahe = Imgproc.createCLAHE(2, new Size(3, 3));
        clahe.apply(image, image);

        // 同态滤波
        Mat grayArray = image.reshape(1, 1);
        // 将数组转为List并排序
        List<Double> grayList = new ArrayList<>();
        for (int i = 0; i < grayArray.cols(); i++) {
            double[] temp = grayArray.get(0, i);
            grayList.add(temp[0]);
        }
        Collections.sort(grayList);

        // 计算阈值
        int grayNumber = (int) Math.round(grayList.size() * histGray);
        double threshold = grayList.get(grayNumber) + 1;

        // 二值化
        Imgproc.threshold(image, image, threshold, 255, Imgproc.THRESH_BINARY);
        // 对二值化图像thresh取反
        Mat mask = new Mat();
        Core.bitwise_not(image, mask);

        // 监测外轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        // 填充外轮廓为黑色面
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.fillPoly(mask, contours, new Scalar(0));
        }

        // 利用掩模得到中间感兴趣
        Core.add(image, mask, image);
        // 闭运算先膨胀后腐蚀
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(3, 3));
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_CLOSE, kernel, new org.opencv.core.Point(-1, -1), 1);
        // 开运算先腐蚀后膨胀
        Imgproc.morphologyEx(image, image, Imgproc.MORPH_OPEN, kernel, new org.opencv.core.Point(-1, -1), 1);
        // 高斯模糊
        Imgproc.GaussianBlur(image, image, new org.opencv.core.Size(3, 3), 0, 0);
        // Canny边缘检测
        Imgproc.Canny(image, image, 75, 200);

        // 找到边缘轮廓
        contours.clear();
        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 对于每个轮廓进行椭圆拟合
        List<Point> centerPoints = new ArrayList<>();

        if(contours.size() == 5){// 恰好保存了5个轮廓
            for (int i = 0; i < contours.size(); i++) {
                // 要获取MatOfPoint中的点的数量，可以使用MatOfPoint.rows()方法。
                // MatOfPoint是一个矩阵，其中每一行表示一个点，因此rows()方法返回矩阵的行数，也就是点的数量。
                if(contours.get(i).rows() > 5){
                    MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
                    RotatedRect ellipse = Imgproc.fitEllipse(contour2f);
                    Imgproc.ellipse(image, ellipse, new Scalar(255, 255, 0, 0), 2);
                    // 再重新添加中心点，保证至少五个点
                    centerPoints.add(ellipse.center);
                    }
                }
            // 如果存放了5个椭圆中心坐标
            if(centerPoints.size() == 5){
                // 中心排序
                List<Point> rankedCenterPoints = RankCenterPoints(cornerPoints, centerPoints);
                if(isDebug){
                    // 画出对角线
                Imgproc.line(image, rankedCenterPoints.get(0), rankedCenterPoints.get(3), new Scalar(255, 0, 0));
                Imgproc.line(image, rankedCenterPoints.get(1), rankedCenterPoints.get(2), new Scalar(255, 0, 0));
                    // 画出形心坐标
                for(Point point : rankedCenterPoints){
                    Imgproc.circle(image, point, 3, new Scalar(255, 255, 0), 3);
                }
                }
                // 计算加权中心坐标，并绘制于图上
                Point centerPoint = CalculateCenterPointCoordinate(0.5, rankedCenterPoints);
                Imgproc.circle(image, centerPoint, 5, new Scalar(255, 0, 0), 5);
                // 转为bitmap并存在结果中
                Utils.matToBitmap(image, bitmap);
                Size Ratio = CalculateRatio(rankedCenterPoints);
                return new SecondStageResult(bitmap, centerPoint, Ratio);
                }else{
                // 如果椭圆中心坐标不足5个，返回默认值
                Utils.matToBitmap(image, bitmap);
                return new SecondStageResult(bitmap, new Point(0, 0), new Size(0, 0));
                }

        }else{
            // 如果轮廓数量不足5个，返回默认值
            Utils.matToBitmap(image, bitmap);
            return new SecondStageResult(bitmap, new Point(0, 0), new Size(0, 0));
        }

    }
}
