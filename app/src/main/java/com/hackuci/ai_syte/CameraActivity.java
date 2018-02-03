package com.hackuci.ai_syte;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CameraActivity extends AppCompatActivity {

    private static final int PIXEL_WIDTH = 1440;
    private static final int PIXEL_HEIGHT = 2560;

    //Check state orientation of output image
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private SlidingUpPanelLayout mLayout;
    private static final String TAG = "MainAcitvity";
    List<String> array_list;
    TextView textView ;
    ListView listview;

    private TextureView textureView;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
    //Save to FILE
    private File file;
    private Handler mBackgroundHandler;
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
        }
    };
    private HandlerThread mBackgroundThread;

    public CameraActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* HIDES STATUS BAR */
        ActionBar actionBar;
        if((actionBar = getActionBar()) != null)
            actionBar.hide();
        /* HIDES STATUS BAR */

        textureView = findViewById(R.id.textureView);
        //From Java 1.4 , you can use keyword 'assert' to check expression true or false
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        textureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        init();    // call init method
        setListview();
        panelListener();
    }

    /**
     * Initialization of the textview and SlidingUpPanelLayout
     */
    public void init(){

        mLayout = findViewById(R.id.sliding_layout);
        textView = findViewById(R.id.list_main);
        listview = findViewById(R.id.list);
    }
    public List<String> array_list(){
        array_list = Arrays.asList(
                "This",
                "Is",
                "An",
                "Example",
                "ListView",
                "That",
                "You",
                "Can",
                "Scroll",
                ".",
                "It",
                "Shows",
                "How",
                "Any",
                "Scrollable",
                "View",
                "Can",
                "Be",
                "Included",
                "As",
                "A",
                "Child",
                "Of",
                "SlidingUpPanelLayout"
        );
        return array_list;
    }
    /**
     *  Set array adapter to display a list of items.
     *  Called a callback setOnItemClickListener method,
     *  It calls when user click on the list of item.
     */
    public void setListview(){

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {

                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                textView.setText(array_list().get(position));
                Toast.makeText(CameraActivity.this, "onItemClick" , Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * This is array adapter, it takes context of the activity as a first parameter,
         * layout of the listview as a second parameter and array as a third parameter.
         */
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                array_list());

        listview.setAdapter(arrayAdapter);

    }
    /**
     * Call setPanelSlidelistener method to listen open and close of slide panel
     **/
    public void panelListener(){
        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            // During the transition of expand and collapse onPanelSlide function will be called.
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

































                Log.e(TAG, "onPanelSlide, offset " + slideOffset);
            }

            // Called when secondary layout is dragged up by user
            @Override
            public void onPanelExpanded(View panel) {

                Log.e(TAG, "onPanelExpanded");
            }

            // Called when secondary layout is dragged down by user
            @Override
            public void onPanelCollapsed(View panel) {

                Log.e(TAG, "onPanelCollapsed");
            }

            @Override
            public void onPanelAnchored(View panel) {

                Log.e(TAG, "onPanelAnchored");
            }

            @Override
            public void onPanelHidden(View panel) {

                Log.e(TAG, "onPanelHidden");
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // HIDES NOTIFICATION BAR
        View view = getWindow().getDecorView();
        if (hasFocus && view != null) {
            view.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        // HIDES NOTIFICATION BAR

    }

    private void takePicture() {
        if (cameraDevice == null)
            return;
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            assert manager != null;
            manager.getCameraCharacteristics(cameraDevice.getId());

            //Capture image with custom size
            final ImageReader reader = ImageReader.newInstance(PIXEL_WIDTH, PIXEL_HEIGHT, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //Check orientation base on device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            file = new File(Environment.getExternalStorageDirectory() + "/" + UUID.randomUUID().toString() + ".jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        {
                            if (image != null)
                                image.close();
                        }
                    }
                }

                @SuppressLint("StaticFieldLeak")
                private void save(byte[] bytes) throws IOException {
                    OutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    } finally {
                        if (outputStream != null)
                            outputStream.close();
                    }

                    com.google.api.services.vision.v1.model.Image base64EncodedImage =
                            new com.google.api.services.vision.v1.model.Image();
                    base64EncodedImage.encodeContent(bytes);


                    Feature feature = new Feature();
                    feature.setType("LANDMARK_DETECTION");
                    feature.setMaxResults(10);

                    final List<Feature> featureList = new ArrayList<>();
                    featureList.add(feature);
                    final List<AnnotateImageRequest> annotateImageRequests = new ArrayList<>();


                    AnnotateImageRequest annotateImageReq = new AnnotateImageRequest();
                    annotateImageReq.setFeatures(featureList);
                    annotateImageReq.setImage(base64EncodedImage);
                    annotateImageRequests.add(annotateImageReq);







                    new AsyncTask<Object, Void, String>() {
                        @Override
                        protected String doInBackground(Object... params) {
                            try {

                                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                                VisionRequestInitializer requestInitializer = new VisionRequestInitializer("AIzaSyBVoIgIwQ5dWN0ZTTA4cnZHOUJws5CBZW0");

                                Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                                builder.setVisionRequestInitializer(requestInitializer);

                                Vision vision = builder.build();

                                BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                                batchAnnotateImagesRequest.setRequests(annotateImageRequests);

                                Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                                annotateRequest.setDisableGZipContent(true);
                                BatchAnnotateImagesResponse response = annotateRequest.execute();

                                return convertResponseToString(response);
                            } catch (GoogleJsonResponseException e) {
                                Log.d(TAG, "failed to make API request because " + e.getContent());
                            } catch (IOException e) {
                                Log.d(TAG, "failed to make API request because of other IOException " + e.getMessage());
                            }
                            return "Cloud Vision API request failed. Check logs for details.";
                        }

                        protected void onPostExecute(String result) {

                            System.out.println(result);


//                            visionAPIData.setText(result);
//                            imageUploadProgress.setVisibility(View.INVISIBLE);
                        }
                    }.execute();

















                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(CameraActivity.this, "Saved " + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        AnnotateImageResponse imageResponses = response.getResponses().get(0);

        List<EntityAnnotation> entityAnnotations;

        entityAnnotations = imageResponses.getLabelAnnotations();
        return formatAnnotation(entityAnnotations);
    }

    private String formatAnnotation(List<EntityAnnotation> entityAnnotation) {
        String message = "";

        if (entityAnnotation != null) {
            for (EntityAnnotation entity : entityAnnotation) {
                message = message + "    " + entity.getDescription() + " " + entity.getScore();
                message += "\n";
            }
        } else {
            message = "Nothing Found";
        }
        return message;
    }


    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            //Check realtime permission if run higher API 23
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Collapsed layout, when user press back button
     **/
    @Override
    public void onBackPressed() {

        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED
                        || mLayout.getPanelState() ==
                        SlidingUpPanelLayout.PanelState.ANCHORED)) {

            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        } else {

            super.onBackPressed();

            startActivity(new Intent(CameraActivity.this,
                    ChooseActivity.class));
        }
    }

}
