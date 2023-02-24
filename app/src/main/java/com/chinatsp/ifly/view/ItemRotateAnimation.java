package com.chinatsp.ifly.view;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class ItemRotateAnimation extends Animation {


    public static  final long durationMillis=1000*7/24;//7帧内7/24

    private int centerX, centerY;

    private Camera camera = new Camera();

    private  float degX= - 180;
    private  float degY=0;



    public void setDeg(float degX,float degY){
       this.degX=degX;
       this.degY=degX;
   }


    /**

     * 获取坐标，定义动画时间

     * @param width

     * @param height

     * @param parentWidth

     * @param parentHeight

     */

    @Override

    public void initialize(int width, int height, int parentWidth, int parentHeight) {

        super.initialize(width, height, parentWidth, parentHeight);

        //获得中心点坐标

        centerX = width / 2;

        centerY = height / 2;

        //动画执行时间 自行定义

        setInterpolator(new LinearInterpolator());
        setDuration(durationMillis);
        setRepeatCount(0);
    }



    /**

     * 旋转的角度设置

     * @param interpolatedTime

     * @param t

     */



    @Override

    protected void applyTransformation(float interpolatedTime, Transformation t) {

        final Matrix matrix = t.getMatrix();

        camera.save();

        //设置camera的位置
       camera.setLocation(0,0,180);
       camera.rotateY(degY * (1 - interpolatedTime));
       camera.rotateX(degX * interpolatedTime);

        //把我们的摄像头加在变换矩阵上

        camera.getMatrix(matrix);

        //设置翻转中心点

        matrix.preTranslate(-centerX, -centerY);

        matrix.postTranslate(centerX,centerY);

        camera.restore();

    }



}

