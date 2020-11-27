package com.example.petopiaapp.Activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageInfo;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.petopiaapp.R;

public class CameraActivity extends AbstractCameraXActivity<CameraActivity.AnalysisResult> {

    public static final String INTENT_MODULE_ASSET_NAME = "INTENT_MODULE_ASSET_NAME";
    public static final String INTENT_INFO_VIEW_TYPE = "INTENT_INFO_VIEW_TYPE";

    private static final int INPUT_TENSOR_WIDTH = 416;
    private static final int INPUT_TENSOR_HEIGHT = 416;
    private static final int MOVING_AVG_PERIOD = 10;
    private static final String FORMAT_MS = "%dms";
    private static final String FORMAT_AVG_MS = "avg:%.0fms";

    private static final String FORMAT_FPS = "%.1fFPS";
    public static final String SCORES_FORMAT = "%.2f";

    static class AnalysisResult {
        private final long analysisDuration;
        private final long moduleForwardDuration;
        private final float[] scores;
        private final int len;
        public AnalysisResult(int len, float[] scores, long moduleForwardDuration, long analysisDuration) {
            this.moduleForwardDuration = moduleForwardDuration;
            this.analysisDuration = analysisDuration;
            this.scores = scores;
            this.len = len;
        }
    }

    private boolean mAnalyzeImageErrorState;
    private TextView mFpsText;
    private TextView mMsText;
    private TextView mMsAvgText;
    private TextView mlenText;
    private ImageView imgView;
    private Module mModule;
    private String mModuleAssetName;
    private FloatBuffer mInputTensorBuffer;
    private Tensor mInputTensor;
    private long mMovingAvgSum = 0;
    private Queue<Long> mMovingAvgQueue = new LinkedList<>();
    private ImageButton cameraButton;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_camera;
    }

    @Override
    protected PreviewView getCameraPreviewView() {
        return (PreviewView)findViewById(R.id.camera_preview_view);
    }
    private String getBatchDirectoryName() {

        String app_folder_path = "";
        app_folder_path = Environment.getExternalStorageDirectory().toString() + "/images";
        File dir = new File(app_folder_path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return app_folder_path;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFpsText = findViewById(R.id.camera_fps_text);
        mMsText = findViewById(R.id.camera_ms_text);
        mMsAvgText = findViewById(R.id.camera_ms_avg_text);
        mlenText = findViewById(R.id.camera_len);
        imgView = findViewById(R.id.imageView);
        cameraButton = (ImageButton)findViewById(R.id.imageButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date())+ ".jpg");
                ImageCapture.OutputFileOptions outputFileOptions =
                        new ImageCapture.OutputFileOptions.Builder(file).build();
                imageCapture.takePicture(outputFileOptions, executor,
                        new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                //Toast.makeText(getApplicationContext(), "이미지 저장 성공", Toast.LENGTH_LONG).show();
                                Log.e("petopia", "image saved");
                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                //Toast.makeText(getApplicationContext(), "이미지 저장 실패", Toast.LENGTH_LONG).show();
                                Log.e("petopia", "image saved error");
                            }
                        });
            }
        });
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void applyToUiAnalyzeImageResult(AnalysisResult result) {
        Log.e("petopia", "running");
        mMovingAvgSum += result.moduleForwardDuration;
        mMovingAvgQueue.add(result.moduleForwardDuration);
        if (mMovingAvgQueue.size() > MOVING_AVG_PERIOD) {
            mMovingAvgSum -= mMovingAvgQueue.remove();
        }
        mMsText.setText(String.format(Locale.US, FORMAT_MS, result.moduleForwardDuration));
        if (mMsText.getVisibility() != View.VISIBLE) {
            mMsText.setVisibility(View.VISIBLE);
        }
        mFpsText.setText(String.format(Locale.US, FORMAT_FPS, (1000.f / result.analysisDuration)));
        if (mFpsText.getVisibility() != View.VISIBLE) {
            mFpsText.setVisibility(View.VISIBLE);
        }
        mlenText.setText(String.format(Locale.US, "%d", result.len/5));
        if (mlenText.getVisibility() != View.VISIBLE) {
            mlenText.setVisibility(View.VISIBLE);
        }

        if (mMovingAvgQueue.size() == MOVING_AVG_PERIOD) {
            float avgMs = (float) mMovingAvgSum / MOVING_AVG_PERIOD;
            mMsAvgText.setText(String.format(Locale.US, FORMAT_AVG_MS, avgMs));
            if (mMsAvgText.getVisibility() != View.VISIBLE) {
                mMsAvgText.setVisibility(View.VISIBLE);
            }
        }

        Bitmap bitmap = ((PreviewView)findViewById(R.id.camera_preview_view)).getBitmap();
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        Bitmap overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        if(result.len >= 5) {

            for(int i = 0; i < result.len; i+=5) {
                Canvas canvas = new Canvas(overlay);
                canvas.drawRect((int)(result.scores[i] * (float)w/416.0), (int)(result.scores[i+1] * (float)w/416.0) + (h-w)/2, (int)(result.scores[i+2] * (float)w/416.0), (int)(result.scores[i+3] * (float)w/416.0) + (h-w)/2, paint);
            }

        }
        imgView.setImageDrawable(new BitmapDrawable(getResources(), overlay));
    }

    protected String getModuleAssetName() {
        if (!TextUtils.isEmpty(mModuleAssetName)) {
            return mModuleAssetName;
        }
        mModuleAssetName = "df_basedYolo_script.pt";

        return mModuleAssetName;
    }

    @Override
    protected String getInfoViewAdditionalText() {
        return getModuleAssetName();
    }

    public static String assetFilePath(Context context, String assetName) {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("petopia", "Error process asset " + assetName + " to file path");
        }
        return null;
    }

    @Override
    @WorkerThread
    @Nullable
    protected AnalysisResult analyzeImage(ImageProxy image) {
        Log.e("petopia", "analazing");
        int rotationDegrees = image.getImageInfo().getRotationDegrees();
        if (mAnalyzeImageErrorState) {
            return null;
        }

        try {
            if (mModule == null) {
                final String moduleFileAbsoluteFilePath = new File(
                        assetFilePath(this, getModuleAssetName())).getAbsolutePath();
                mModule = Module.load(moduleFileAbsoluteFilePath);

                mInputTensorBuffer =
                        Tensor.allocateFloatBuffer(3 * INPUT_TENSOR_WIDTH * INPUT_TENSOR_HEIGHT);
                mInputTensor = Tensor.fromBlob(mInputTensorBuffer, new long[]{1, 3, INPUT_TENSOR_HEIGHT, INPUT_TENSOR_WIDTH});
            }
            float[] mean = {0, 0, 0};
            float[] std = {1, 1, 1};
            final long startTime = SystemClock.elapsedRealtime();
            TensorImageUtils.imageYUV420CenterCropToFloatBuffer(
                    image.getImage(), rotationDegrees,
                    INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT,
                    mean,
                    std,
                    mInputTensorBuffer, 0);

            final long moduleForwardStartTime = SystemClock.elapsedRealtime();
            final Tensor outputTensor = mModule.forward(IValue.from(mInputTensor)).toTensor();
            final long moduleForwardDuration = SystemClock.elapsedRealtime() - moduleForwardStartTime;

            final float[] scores = outputTensor.getDataAsFloatArray();
            final long analysisDuration = SystemClock.elapsedRealtime() - startTime;
            return new AnalysisResult(scores.length, scores, moduleForwardDuration, analysisDuration);

        } catch (Exception e) {
            Log.e("petopia", "Error during image analysis", e);
            mAnalyzeImageErrorState = true;
            return null;
        }
    }

    @Override
    protected int getInfoViewCode() {
        return getIntent().getIntExtra(INTENT_INFO_VIEW_TYPE, -1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mModule != null) {
            mModule.destroy();
        }
    }
}
