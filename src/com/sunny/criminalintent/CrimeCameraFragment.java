package com.sunny.criminalintent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class CrimeCameraFragment extends Fragment {
	
	private static final String TAG = "CrimeCameraFragment";
	
	public static final String EXTRA_PHOTO_FILENAME = 
			"com.sunny.criminalintent.photo_filename";
	
	private Camera mCamera;
	
	private SurfaceView mCrimeSv;
	private View mProgressContainerFl;
	
	private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
		
		@Override
		public void onShutter() {
			// Display the progress indicator
			mProgressContainerFl.setVisibility(View.VISIBLE);
		}
	};
	
	private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// Create a filename
			String filename = UUID.randomUUID().toString() + ".jpg";
			// Save the jpeg data to disk
			FileOutputStream os = null;
			boolean success = true;
			
			try {
				os =  getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
				os.write(data);
			} catch (Exception e) {
				Log.e(TAG, "Error writing to file " + filename, e);
				success = false;
			} finally {
				try {
					if (os != null)
						os.close();
				} catch (Exception e) {
					Log.e(TAG, "Error closing file " + filename, e);
					success = false;
				}
			}
			
			// Set the photo filename on the result intent
			if (success) {
				Intent intent = new Intent();
				intent.putExtra(EXTRA_PHOTO_FILENAME, filename);
				getActivity().setResult(Activity.RESULT_OK, intent);
			} else {
				getActivity().setResult(Activity.RESULT_CANCELED);
			}
			
			getActivity().finish();
		}
	};

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_crime_camera, container, false);
		
		mProgressContainerFl = view.findViewById(R.id.fl_crime_camera_progressContainer);
		mProgressContainerFl.setVisibility(View.INVISIBLE);
		
		Button mTakeBtn = (Button) view.findViewById(R.id.btn_crime_camera_take_picture);
		mTakeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mCamera != null) {
					mCamera.takePicture(mShutterCallback, null, mJpegCallback);
				}
			}
		});
		
		mCrimeSv = (SurfaceView) view.findViewById(R.id.sv_crime_camera);
		SurfaceHolder holder = mCrimeSv.getHolder();
		// setType() and SURFACE_TYPE_PUSH_BUFFERS are both deprecated, 
		// but are required for Camera preview to work on pre-3.0 devices.
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		holder.addCallback(new SurfaceHolder.Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// We can no longer display on this surface, so stop the preview 
				if (mCamera != null) {
					mCamera.stopPreview();
				}
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// Tell the camera to use this surface as its preview area
				try {
					if (mCamera != null) {
						mCamera.setPreviewDisplay(holder);
					}
				} catch (IOException e) {
					Log.e(TAG, "Error setting up preview display", e);
				}
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {
				if (mCamera == null) {
					return;
				}
				
				// The surface has changed size; update the camera preview size
				Camera.Parameters parameters = mCamera.getParameters();
				Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), width, height);
				parameters.setPreviewSize(s.width, s.height);
				s = getBestSupportedSize(parameters.getSupportedPictureSizes(), width, height);
				parameters.setPictureSize(s.width, s.height);
				mCamera.setParameters(parameters);
				
				try {
					mCamera.startPreview();
				} catch (Exception e) {
					Log.e(TAG, "Could not start preview", e);
					mCamera.release();
					mCamera = null;
				}
			}
		});
		
		return view;
	}
	
	/**
	 * A simple algorithm to get the largest size available.
	 * For a more robust version, see CameraPreview.java int the
	 * ApiDemos sample app from Android.
	 * 
	 * @param sizes
	 * @param width
	 * @param height
	 * @return
	 */
	private Size getBestSupportedSize(List<Size> sizes, int width, int height) {
		Size bestSize = sizes.get(0);
		int largestArea = bestSize.width * bestSize.height;
		for (Size size : sizes) {
			int area = size.width * size.height;
			if (area > largestArea) {
				bestSize = size;
				largestArea = area;
			}
		}
		return bestSize;
	}

	@TargetApi(9)
	@Override
	public void onResume() {
		super.onResume();
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mCamera = Camera.open(0);
		} else {
			mCamera = Camera.open();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

}
