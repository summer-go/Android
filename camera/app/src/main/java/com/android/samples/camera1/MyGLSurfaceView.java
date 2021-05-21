package com.android.samples.camera1;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * @author xiatian05
 * @date 2021/5/20
 * @ver 11.20.0
 */
class MyGLSurfaceView extends GLSurfaceView {
    public static String TAG = "OpenglActivity";
    private final Context mContext;
    private MyGLRenderer renderer;

    public MyGLSurfaceView(Context context) {
        super(context);
        this.mContext = context;
        // create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new MyGLRenderer();

        // set the renderer for drawing on the GLSurfaceView
        setRenderer(renderer);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
//        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }

    private final float TOUCH_SCALE_FACTOR = 180f / 320;
    private float previousX;
    private float previousY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                float dx = x - previousX;
                float dy = y - previousY;
                // reverse direction of rotation above the mid-line
                if(y > getHeight() / 2){
                    dx = dx * -1;
                }

                // reverse direction of rotation to left of the mid-line
                if(x < getWidth() / 2){
                    dy = dy * -1;
                }

                float ret = (renderer.getAngle() + ((dx + dy) * TOUCH_SCALE_FACTOR));
                renderer.setAngle(ret);
                requestRender();
                break;
        }
        previousX = x;
        previousY = y;
        return true;
    }

    public static class MyGLRenderer implements GLSurfaceView.Renderer {

        private Triangle mTriangle;
        private Square mSquare;
        private final float[] vPMatrix = new float[16];
        private final float[] projectionMatrix = new float[16];
        private final float[] viewMatrix = new float[16];
        private float[] rotationMatrix = new float[16];

        public volatile float mAngle;
        public float getAngle(){
            return mAngle;
        }

        public void setAngle(float angle){
            mAngle = angle;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            // initialize a triangle
            mTriangle = new Triangle();
            // initialize a square
            mSquare = new Square();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(TAG, "onSurfaceChanged width: " + width + " height: " + height);
            GLES20.glViewport(0, 0, width, height);
            float ratio = (float) width / height;

            // this projection matrix is applied to object coordinates
            // in the onDrawFrame() method
            Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            float[] scratch = new float[16];
            //create a rotation transformation for the triangle
            long time = SystemClock.uptimeMillis() % 4000L;
            float angle = 0.090f * ((int) time);

            // 运行结果是逆时针旋转，为什么？
            // 按照右手定则，大拇指指向-z方向，四指并拢指向顺时针方向，但是三角形是逆时针，这是因为LookAtM的eyeZ是-3，即位于三角形后面观察，正好是反向
            // 修改下面LoogAtM eyeZ = 3,就能得到顺时针动画
            Matrix.setRotateM(rotationMatrix, 0, mAngle, 0, 0, -1.0f);

            // set the camera position (View matrix)
            // opengl是右手坐标系
            Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            // calculate the projection and view transformation
            Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

            Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);
            mTriangle.draw(scratch);
        }


        public static int loadShader(int type, String shaderCode) {
            // create a vertex shader type(GLES20.GL_VERTEX_SHADER)
            // or a fragment shader tye(GLES20.GL_FRAGMENT_SHADER)
            int shader = GLES20.glCreateShader(type);

            // add the source code to the shader and compile it
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader);

            return shader;
        }
    }
}
