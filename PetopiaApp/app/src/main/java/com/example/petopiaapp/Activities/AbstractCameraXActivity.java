package com.example.petopiaapp.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.example.petopiaapp.Activities.BaseModuleActivity;
import com.example.petopiaapp.Activities.StatusBarUtils;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class AbstractCameraXActivity<R> extends BaseModuleActivity {
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 200;
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private long mLastAnalysisResultTime;

    protected abstract int getContentViewLayoutId();
    protected abstract PreviewView getCameraPreviewView();

    protected ImageCapture imageCapture;
    protected Executor executor;
    protected ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setStatusBarOverlay(getWindow(), true);
        setContentView(getContentViewLayoutId());
        startBackgroundThread();
        executor = ContextCompat.getMainExecutor(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    REQUEST_CODE_CAMERA_PERMISSION);
        } else {
            setupCameraX();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(
                        this,
                        "You can't use image classification example without granting CAMERA permission",
                        Toast.LENGTH_LONG)
                        .show();
                finish();
            } else {
                setupCameraX();
            }
        }
    }
    private void bindAll(@NonNull ProcessCameraProvider cameraProvider) {
        final CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        final Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(getCameraPreviewView().getSurfaceProvider());

        final ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(416, 416))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
        imageAnalysis.setAnalyzer(executor, (image) -> {
            Log.e("petopia", "runninggg");

            final R result = analyzeImage(image);
            image.close();
            if (result != null) {
                runOnUiThread(() -> applyToUiAnalyzeImageResult(result));
            }
        });

        imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(this.getDisplay().getRotation())
                        .build();
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);
    }

    private void setupCameraX() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindAll(cameraProvider);
            } catch (InterruptedException | ExecutionException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, executor);
    }

    @WorkerThread
    @Nullable
    protected abstract R analyzeImage(ImageProxy image);

    @UiThread
    protected abstract void applyToUiAnalyzeImageResult(R result);
}
