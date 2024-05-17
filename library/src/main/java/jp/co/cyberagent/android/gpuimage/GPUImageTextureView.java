package jp.co.cyberagent.android.gpuimage;

import android.content.Context;
import android.util.AttributeSet;

public class GPUImageTextureView extends GPUImageView<GLTextureView> {
    public GPUImageTextureView(Context context) {
        super(context);
    }

    public GPUImageTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected GLTextureView generateSurfaceView(Context context, AttributeSet attrs) {
        return new GPUImageGLTextureView(context, attrs);
    }
}
