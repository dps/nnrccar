package org.davidsingleton.nnrccar;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Part of NNRCCar - a self driving radio controlled car.
 * 
 * This activity implements an app which grabs camera preview frames (at 176x144 resolution)
 * and streams them via a TCP connection to a server app running on port 6666 on the IP address
 * provided by the user at start up.
 */
public class NNRCCarActivity extends Activity implements SensorEventListener {

	private FeatureStreamingCameraPreview mPreview;
	Camera mCamera;
	int numberOfCameras;
	int cameraCurrentlyLocked;

	// The first rear facing camera
	int defaultCameraId;

	private static final String PREFS_NAME = "org.davidsingleton.NNRCCar";

	private FeatureStreamer fs = new FeatureStreamer();
	private SensorManager sensorManager;
	private float[] gravity = new float[3];
	private float[] linear_acceleration = new float[3];
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mPreview = new FeatureStreamingCameraPreview(this, fs);
		setContentView(mPreview);
		numberOfCameras = Camera.getNumberOfCameras();

		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				defaultCameraId = i;
			}
		}

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		AlertDialog.Builder addressAlert = new AlertDialog.Builder(this);
		addressAlert.setTitle("Connect to...");
		addressAlert.setMessage("IP address:");
		final EditText input = new EditText(this);
		final Context activityContext = (Context) this;
		input.setText(loadIPAddressPref(this));
		addressAlert.setView(input);
		addressAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String addr = input.getText().toString();
				saveIPAddressPref(activityContext, addr);
				fs.connect(addr, 6666);
			}
		});
		addressAlert.show();
	}

	static String loadIPAddressPref(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		String prefix = prefs.getString("ipaddr", null);
		if (prefix != null) {
			return prefix;
		} else {
			return "";
		}
	}

	static void saveIPAddressPref(Context context, String text) {
		SharedPreferences.Editor prefs = context
		    .getSharedPreferences(PREFS_NAME, 0).edit();
		prefs.putString("ipaddr", text);
		prefs.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();

		sensorManager.registerListener(this,
		    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		    SensorManager.SENSOR_DELAY_FASTEST);

		mCamera = Camera.open();
		cameraCurrentlyLocked = defaultCameraId;
		mPreview.setCamera(mCamera);
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);

		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// no op
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		final float alpha = 0.8f;

		gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
		gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
		gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

		linear_acceleration[0] = event.values[0] - gravity[0];
		linear_acceleration[1] = event.values[1] - gravity[1];
		linear_acceleration[2] = event.values[2] - gravity[2];
		
		mPreview.updateAccelerometerFeatures(linear_acceleration);
	}

}

class FeatureStreamingCameraPreview extends ViewGroup implements SurfaceHolder.Callback,
    PreviewCallback {
	private final String TAG = "FSPreview";

	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	Camera mCamera;

	private byte[] pixels = null;
	private float[] accelerometerFeatures = new float[3];
	private FeatureStreamer fs;

	FeatureStreamingCameraPreview(Context context, FeatureStreamer fs) {
		super(context);
		this.fs = fs;
		mSurfaceView = new SurfaceView(context);
		addView(mSurfaceView);

		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void updateAccelerometerFeatures(float[] features) {
		synchronized (this) {
		  accelerometerFeatures = features;
		}
	}
	
	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters()
			    .getSupportedPreviewSizes();
			requestLayout();
		}
	}

	public void switchCamera(Camera camera) {
		setCamera(camera);
		try {
			if (true)
				camera.setPreviewDisplay(mHolder);
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		requestLayout();

		camera.setParameters(parameters);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(),
		    heightMeasureSpec);

		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getMinimumPreviewSize(mSupportedPreviewSizes, width,
			    height);
			setMeasuredDimension(mPreviewSize.width, mPreviewSize.height);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && getChildCount() > 0) {
			final View child = getChildAt(0);

			final int width = r - l;
			final int height = b - t;

			int previewWidth = width;
			int previewHeight = height;
			if (mPreviewSize != null) {
				previewWidth = mPreviewSize.width;
				previewHeight = mPreviewSize.height;
			}

			// Center the child SurfaceView within the parent.
			if (width * previewHeight > height * previewWidth) {
				final int scaledChildWidth = previewWidth * height / previewHeight;
				child.layout((width - scaledChildWidth) / 2, 0,
				    (width + scaledChildWidth) / 2, height);
			} else {
				final int scaledChildHeight = previewHeight * width / previewWidth;
				child.layout(0, (height - scaledChildHeight) / 2, width,
				    (height + scaledChildHeight) / 2);
			}
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			if (mCamera != null) {
				if (true)
					mCamera.setPreviewDisplay(holder);
				mCamera.setPreviewCallback(this);
			}
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		if (mCamera != null) {
			mCamera.stopPreview();
		}
	}

	private Size getMinimumPreviewSize(List<Size> sizes, int w, int h) {
		if (sizes == null)
			return null;
		int minWidth = Integer.MAX_VALUE;

		Size optimalSize = null;
		// Try to find the min size
		for (Size size : sizes) {
			if (size.width < minWidth) {
				optimalSize = size;
				minWidth = size.width;
			}
		}

		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

		pixels = new byte[mPreviewSize.width * mPreviewSize.height];
		requestLayout();

		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

		if (data.length >= mPreviewSize.width * mPreviewSize.height) {
			decodeYUV420SPGrayscale(pixels, data, mPreviewSize.width,
			    mPreviewSize.height);
			synchronized (this) {
			  fs.sendFeatures(mPreviewSize.width, mPreviewSize.height, pixels, accelerometerFeatures);
			}
		}

	}

	static public void decodeYUV420SPGrayscale(byte[] rgb, byte[] yuv420sp,
	    int width, int height) {
		final int frameSize = width * height;

		for (int pix = 0; pix < frameSize; pix++) {
			int pixVal = (0xff & ((int) yuv420sp[pix])) - 16;
			if (pixVal < 0)
				pixVal = 0;
			if (pixVal > 255)
				pixVal = 255;
			rgb[pix] = (byte) pixVal;
		}
	}


}
