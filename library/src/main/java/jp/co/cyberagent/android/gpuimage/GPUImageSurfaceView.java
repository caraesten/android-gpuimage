package jp.co.cyberagent.android.gpuimage;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GPUImageSurfaceView extends GPUImageView<GLSurfaceView> {
    public GPUImageSurfaceView(Context context) {
        super(context);
    }

    public GPUImageSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected GLSurfaceView generateSurfaceView(Context context, AttributeSet attrs) {
        return new GPUImageGLSurfaceView(context, attrs);
    }
}
