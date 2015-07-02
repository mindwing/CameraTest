package kr.mindwing.cameratest;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 이미지가 preview 를 가려도 동영상 녹화가 가능한지 테스트.
 *
 */
public class MainActivity extends Activity {
	public static final String TAG = "CameraTest";

	private static String EXTERNAL_STORAGE_PATH = "";
	private static String RECORDED_FILE = "video_recorded";
	private static int fileIndex = 0;

	MediaPlayer player;
	MediaRecorder recorder;

	private Camera camera = null;
	SurfaceHolder holder;

	private ImageView curtain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// create a SurfaceView instance and add it to the layout
		SurfaceView surface = new SurfaceView(this);
		holder = surface.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		FrameLayout frame = (FrameLayout) findViewById(R.id.videoLayout);
		frame.addView(surface);

		Button btRecord = (Button) findViewById(R.id.recordBtn);
		Button btRecordStop = (Button) findViewById(R.id.recordStopBtn);
		Button btPlay = (Button) findViewById(R.id.playBtn);
		Button btPlayStop = (Button) findViewById(R.id.playStopBtn);

		curtain = (ImageView) findViewById(R.id.curtain);

		btRecord.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					if (recorder == null) {
						recorder = new MediaRecorder();
					}

					curtain.setVisibility(View.VISIBLE);

					recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

					recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

					recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
					recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

					recorder.setOutputFile(getVideoFileName());

					recorder.setPreviewDisplay(holder.getSurface());
					recorder.prepare();
					recorder.start();

				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);

					recorder.release();
					recorder = null;
				}
			}
		});

		btRecordStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				curtain.setVisibility(View.GONE);

				if (recorder == null) {
					return;
				}

				recorder.stop();
				recorder.reset();
				recorder.release();
				recorder = null;

				ContentValues values = new ContentValues(10);

				values.put(MediaStore.MediaColumns.TITLE, "VideoTest");
				values.put(MediaStore.Audio.Media.DISPLAY_NAME, "VideoTest");
				values.put(MediaStore.Audio.Media.ALBUM, "VideoTest");
				values.put(MediaStore.MediaColumns.DATE_ADDED,
						System.currentTimeMillis() / 1000);
				values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
				values.put(MediaStore.Audio.Media.DATA, getVideoFileName());

				Uri videoUri = getContentResolver().insert(
						MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
				if (videoUri == null) {
					Log.d(TAG, "Video creation problem.");
					return;
				}

				// sendBroadcast(new
				// Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				// videoUri));
			}
		});

		btPlay.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				curtain.setVisibility(View.GONE);

				if (player == null) {
					player = new MediaPlayer();
				}

				try {
					player.setDataSource(getVideoFileName());
					player.setDisplay(holder);

					player.prepare();
					player.start();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		});

		btPlayStop.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				curtain.setVisibility(View.GONE);

				if (player == null)
					return;

				player.stop();
				player.release();
				player = null;
			}
		});
	}

	protected void onPause() {
		super.onPause();

		if (camera != null) {
			camera.release();
			camera = null;
		}

		if (recorder != null) {
			recorder.release();
			recorder = null;
		}

		if (player != null) {
			player.release();
			player = null;
		}
	}

	private String getVideoFileName() {
		return "/sdcard/mytest.mp4";
	}
}