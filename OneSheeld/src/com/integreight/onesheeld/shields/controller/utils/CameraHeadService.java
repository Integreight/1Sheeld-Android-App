package com.integreight.onesheeld.shields.controller.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import com.integreight.onesheeld.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

public class CameraHeadService extends Service implements
		SurfaceHolder.Callback {

	// Camera variables
	// a surface holder
	// a variable to control the camera
	private Camera mCamera;
	// the camera parameters
	private Parameters parameters;
	private Bitmap bmp;
	FileOutputStream fo;
	private String FLASH_MODE;
	private int QUALITY_MODE = 0;
	private boolean isFrontCamRequest = false;
	private Camera.Size pictureSize;
	SurfaceView sv;
	private SurfaceHolder sHolder;
	private WindowManager windowManager;
	WindowManager.LayoutParams params;
	public Intent cameraIntent;
	SharedPreferences pref;
	Editor editor;
	int width = 0, height = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate() {
		super.onCreate();

	}

	private Camera openFrontFacingCameraGingerbread() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
		}
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					cam = Camera.open(camIdx);
				} catch (RuntimeException e) {
					Log.e("Camera",
							"Camera failed to open: " + e.getLocalizedMessage());
					/*
					 * Toast.makeText(getApplicationContext(),
					 * "Front Camera failed to open", Toast.LENGTH_LONG)
					 * .show();
					 */
				}
			}
		}
		return cam;
	}

	private void setBesttPictureResolution() {
		// get biggest picture size
		width = pref.getInt("Picture_Width", 0);
		height = pref.getInt("Picture_height", 0);

		if (width == 0 | height == 0) {
			pictureSize = getBiggesttPictureSize(parameters);
			if (pictureSize != null)
				parameters
						.setPictureSize(pictureSize.width, pictureSize.height);
			// save width and height in sharedprefrences
			width = pictureSize.width;
			height = pictureSize.height;
			editor.putInt("Picture_Width", width);
			editor.putInt("Picture_height", height);
			editor.commit();

		} else {
			// if (pictureSize != null)
			parameters.setPictureSize(width, height);
		}
	}

	private Camera.Size getBiggesttPictureSize(Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPictureSizes()) {
			if (result == null) {
				result = size;
			} else {
				int resultArea = result.width * result.height;
				int newArea = size.width * size.height;

				if (newArea > resultArea) {
					result = size;
				}
			}
		}

		return (result);
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	/** Check if this device has front camera */
	private boolean checkFrontCamera(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_FRONT)) {
			// this device has front camera
			return true;
		} else {
			// no front camera on this device
			return false;
		}
	}

	Handler handler = new Handler();

	private class TakeImage extends AsyncTask<Intent, Void, Void> {

		@Override
		protected Void doInBackground(Intent... params) {
			takeImage(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
		}
	}

	private synchronized void takeImage(Intent intent) {

		if (checkCameraHardware(getApplicationContext())) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				String flash_mode = extras.getString("FLASH");
				FLASH_MODE = flash_mode;

				boolean front_cam_req = extras.getBoolean("Front_Request");
				isFrontCamRequest = front_cam_req;

				int quality_mode = extras.getInt("Quality_Mode");
				QUALITY_MODE = quality_mode;
			}

			if (isFrontCamRequest) {

				// set flash 0ff
				FLASH_MODE = "off";
				// only for gingerbread and newer versions
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {

					mCamera = openFrontFacingCameraGingerbread();
					if (mCamera != null) {

						try {
							mCamera.setPreviewDisplay(sv.getHolder());
						} catch (IOException e) {
							handler.post(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(getApplicationContext(),
											"API dosen't support front camera",
											Toast.LENGTH_LONG).show();
								}
							});

							stopSelf();
						}
						parameters = mCamera.getParameters();
						parameters.setFlashMode(FLASH_MODE);
						// set biggest picture
						setBesttPictureResolution();

						// set camera parameters
						mCamera.setParameters(parameters);
						mCamera.startPreview();
						mCamera.takePicture(null, null, mCall);

						// return 4;

					} else {
						mCamera = null;
						handler.post(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(
										getApplicationContext(),
										"Your Device dosen't have Front Camera !",
										Toast.LENGTH_LONG).show();
							}
						});

						stopSelf();
					}
					/*
					 * sHolder = sv.getHolder(); // tells Android that this
					 * surface will have its data // constantly // replaced if
					 * (Build.VERSION.SDK_INT < 11)
					 * 
					 * sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
					 */
				} else {
					if (checkFrontCamera(getApplicationContext())) {
						mCamera = openFrontFacingCameraGingerbread();

						if (mCamera != null) {

							try {
								mCamera.setPreviewDisplay(sv.getHolder());
							} catch (IOException e) {
								handler.post(new Runnable() {

									@Override
									public void run() {
										Toast.makeText(
												getApplicationContext(),
												"API dosen't support front camera",
												Toast.LENGTH_LONG).show();
									}
								});

								stopSelf();
							}
							parameters = mCamera.getParameters();
							parameters.setFlashMode(FLASH_MODE);

							// set biggest picture
							setBesttPictureResolution();

							// set camera parameters
							mCamera.setParameters(parameters);
							mCamera.startPreview();
							mCamera.takePicture(null, null, mCall);
							// return 4;

						} else {
							mCamera = null;
							/*
							 * Toast.makeText(getApplicationContext(),
							 * "API dosen't support front camera",
							 * Toast.LENGTH_LONG).show();
							 */
							handler.post(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(
											getApplicationContext(),
											"Your Device dosen't have Front Camera !",
											Toast.LENGTH_LONG).show();

								}
							});

							stopSelf();

						}
						// Get a surface
						/*
						 * sHolder = sv.getHolder(); // tells Android that this
						 * surface will have its data // constantly // replaced
						 * if (Build.VERSION.SDK_INT < 11)
						 * 
						 * sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS
						 * );
						 */
					}

				}

			} else {

				if (mCamera != null) {
					mCamera.stopPreview();
					mCamera.release();
					mCamera = Camera.open();
				} else
					mCamera = getCameraInstance();

				try {
					if (mCamera != null) {
						mCamera.setPreviewDisplay(sv.getHolder());
						parameters = mCamera.getParameters();
						if (FLASH_MODE == null || FLASH_MODE.isEmpty()) {
							FLASH_MODE = "auto";
						}
						parameters.setFlashMode(FLASH_MODE);
						// set biggest picture
						setBesttPictureResolution();
						// log quality and image format
						Log.d("Qaulity", parameters.getJpegQuality() + "");
						Log.d("Format", parameters.getPictureFormat() + "");

						// set camera parameters
						mCamera.setParameters(parameters);
						mCamera.startPreview();
						Log.d("ImageTakin", "OnTake()");
						mCamera.takePicture(null, null, mCall);
					} else {
						handler.post(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(getApplicationContext(),
										"Camera is unavailable !",
										Toast.LENGTH_LONG).show();
							}
						});

					}
					// return 4;

				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e("TAG", "CmaraHeadService()::takePicture", e);
				}
				// Get a surface
				/*
				 * sHolder = sv.getHolder(); // tells Android that this surface
				 * will have its data constantly // replaced if
				 * (Build.VERSION.SDK_INT < 11)
				 * 
				 * sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				 */

			}

		} else {
			// display in long period of time
			/*
			 * Toast.makeText(getApplicationContext(),
			 * "Your Device dosen't have a Camera !", Toast.LENGTH_LONG)
			 * .show();
			 */
			handler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							"Your Device dosen't have a Camera !",
							Toast.LENGTH_LONG).show();
				}
			});
			stopSelf();
		}

		// return super.onStartCommand(intent, flags, startId);

	}

	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// sv = new SurfaceView(getApplicationContext());
		cameraIntent = intent;
		Log.d("ImageTakin", "StartCommand()");
		pref = getApplicationContext().getSharedPreferences("MyPref", 0);
		editor = pref.edit();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.width = 1;
		params.height = 1;
		params.x = 0;
		params.y = 0;
		sv = new SurfaceView(getApplicationContext());

		windowManager.addView(sv, params);
		sHolder = sv.getHolder();
		sHolder.addCallback(this);

		// tells Android that this surface will have its data constantly
		// replaced
		if (Build.VERSION.SDK_INT < 11)
			sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		return 1;
	}

	Camera.PictureCallback mCall = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// decode the data obtained by the camera into a Bitmap
			Log.d("ImageTakin", "Done");
			if (bmp != null)
				bmp.recycle();
			System.gc();
			bmp = decodeBitmap(data);
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			if (bmp != null && QUALITY_MODE == 0)
				bmp.compress(Bitmap.CompressFormat.JPEG, 70, bytes);
			else if (bmp != null && QUALITY_MODE != 0)
				bmp.compress(Bitmap.CompressFormat.JPEG, QUALITY_MODE, bytes);

			File imagesFolder = new File(
					Environment.getExternalStorageDirectory(), "OneSheeld");
			if (!imagesFolder.exists())
				imagesFolder.mkdirs(); // <----
			File image = new File(imagesFolder, System.currentTimeMillis()
					+ ".jpg");

			// write the bytes in file
			try {
				fo = new FileOutputStream(image);
			} catch (FileNotFoundException e) {
				Log.e("TAG", "FileNotFoundException", e);
				// TODO Auto-generated catch block
			}
			try {
				fo.write(bytes.toByteArray());
			} catch (IOException e) {
				Log.e("TAG", "fo.write::PictureTaken", e);
				// TODO Auto-generated catch block
			}

			// remember close de FileOutput
			try {
				fo.close();
				if (Build.VERSION.SDK_INT < 19)
					sendBroadcast(new Intent(
							Intent.ACTION_MEDIA_MOUNTED,
							Uri.parse("file://"
									+ Environment.getExternalStorageDirectory())));
				else {
					MediaScannerConnection
							.scanFile(
									getApplicationContext(),
									new String[] { image.toString() },
									null,
									new MediaScannerConnection.OnScanCompletedListener() {
										public void onScanCompleted(
												String path, Uri uri) {
											Log.i("ExternalStorage", "Scanned "
													+ path + ":");
											Log.i("ExternalStorage", "-> uri="
													+ uri);
										}
									});
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
			/*
			 * Toast.makeText(getApplicationContext(),
			 * "Your Picture has been taken !", Toast.LENGTH_LONG).show();
			 */
			com.integreight.onesheeld.Log.d("Camera", "Image Taken !");
			if (bmp != null) {
				bmp.recycle();
				bmp = null;
				System.gc();
			}
			mCamera = null;
			handler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(getApplicationContext(),
							"Your Picture has been taken !", Toast.LENGTH_SHORT)
							.show();
				}
			});
			stopSelf();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	public void onDestroy() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		if (sv != null)
			windowManager.removeView(sv);
		Intent intent = new Intent("custom-event-name");
		// You can also include some extra data.
		intent.putExtra("message", "This is my message!");
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

		super.onDestroy();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (cameraIntent != null)
			new TakeImage().execute(cameraIntent);

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	public static Bitmap decodeBitmap(byte[] data) {

		Bitmap bitmap = null;
		BitmapFactory.Options bfOptions = new BitmapFactory.Options();
		bfOptions.inDither = false; // Disable Dithering mode
		bfOptions.inPurgeable = true; // Tell to gc that whether it needs free
										// memory, the Bitmap can be cleared
		bfOptions.inInputShareable = true; // Which kind of reference will be
											// used to recover the Bitmap data
											// after being clear, when it will
											// be used in the future
		bfOptions.inTempStorage = new byte[32 * 1024];

		if (data != null)
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
					bfOptions);

		return bitmap;
	}

}
