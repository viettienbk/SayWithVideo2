package datn.bkdn.com.saywithvideo.activity;

import android.app.Activity;
import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.utilities.Base64;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.custom.MarkerView;
import datn.bkdn.com.saywithvideo.custom.VisualizerView;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Sound;
import datn.bkdn.com.saywithvideo.firebase.FirebaseAudio;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.firebase.FirebaseUser;
import datn.bkdn.com.saywithvideo.soundfile.SoundFile;
import datn.bkdn.com.saywithvideo.utils.AppTools;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Tools;
import datn.bkdn.com.saywithvideo.utils.Utils;

public class EditAudioActivity extends Activity implements MarkerView.CustomListener,
        MediaPlayer.OnCompletionListener, View.OnClickListener {

    private static final int MIN_SECOND = 1;
    private static final int MAX_SECOND = 10;
    private MarkerView mMarkerLeft;
    private MarkerView mMarkerRight;
    private VisualizerView mVisualizerView;
    private ImageView mImgPlay;
    private float mPixelPerSecond;
    private String mFilePath;
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    private int mWidth;
    private String mOutputPath;
    private Firebase mFirebase;
    private String mType;
    private TextView mTvStart;
    private TextView mTvEnd;
    private int mDuration;
    private String idSound;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_edit_audio);
        if (datn.bkdn.com.saywithvideo.network.Tools.isOnline(this)) {
            Firebase.setAndroidContext(this);
            mFirebase = new Firebase(FirebaseConstant.BASE_URL);
        }

        mFilePath = getIntent().getStringExtra("FileName");
        mType = getIntent().getStringExtra("Type");

        if (mFilePath == null) {
            Toast.makeText(getBaseContext(), "Audio file error", Toast.LENGTH_SHORT).show();
            finish();
        }

        init();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mFilePath);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnCompletionListener(this);
            mPixelPerSecond = (float) mVisualizerView.getWidth() / mMediaPlayer.getDuration();
            Log.d("mPixelPerSecond", "" + mPixelPerSecond);
            mDuration = mMediaPlayer.getDuration();
            mTvEnd.setText(mDuration / 1000 + "." + mDuration / 100 % 10);
        } catch (IOException e) {
            e.printStackTrace();
            mMediaPlayer = null;
        }

        mWidth = getResources().getDisplayMetrics().widthPixels;
    }

    private void init() {
        mMarkerLeft = (MarkerView) findViewById(R.id.markerLeft);
        mMarkerRight = (MarkerView) findViewById(R.id.markerRight);
        RelativeLayout mRlBack = (RelativeLayout) findViewById(R.id.rlBack);
        mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);
        TextView mTvNext = (TextView) findViewById(R.id.tvNext);
        mImgPlay = (ImageView) findViewById(R.id.imgPlay);
        mTvStart = (TextView) findViewById(R.id.tvStart);
        mTvEnd = (TextView) findViewById(R.id.tvEnd);

        mMarkerLeft.setListener(this);
        mMarkerRight.setListener(this);
        mRlBack.setOnClickListener(this);
        mTvNext.setOnClickListener(this);
        mImgPlay.setOnClickListener(this);
    }

    private void setupVisualizerFxAndUI() {
        mVisualizerView.start();
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        mVisualizerView.setDuration(mMediaPlayer.getDuration());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                        int current;
                        try {
                            current = mMediaPlayer.getCurrentPosition();
                            mVisualizerView.updateVisualizer(bytes, current);
                        } catch (Exception e) {
                            visualizer.setEnabled(false);
                        }

                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }

    @Override
    public void markerDraw() {

    }

    @Override
    public void markerTouchStart(MarkerView customImageView) {
        mPixelPerSecond = (float) mVisualizerView.getWidth() / mMediaPlayer.getDuration();
    }

    @Override
    public void markerTouchEnd(MarkerView customImageView, float x) {

    }

    @Override
    public void markerMove(MarkerView customImageView, float x) {
        float min_pixel = MIN_SECOND * mPixelPerSecond * 1000;

        if (customImageView.getId() == R.id.markerLeft) {
            if (mMarkerRight.getX() - x < min_pixel) {
                return;
            }
            float temp = x / mPixelPerSecond;
            mTvStart.setText((int) temp / 1000 + "." + (int) temp / 100 % 10);
        } else {
            int xr = (int) x + mMarkerRight.getWidth();
            if (xr > mWidth) {
                return;
            }
            if (x - mMarkerLeft.getX() < min_pixel) {
                return;
            }
            float temp = x / mPixelPerSecond;
            mTvEnd.setText((int) temp / 1000 + "." + (int) temp / 100 % 10);
        }
        customImageView.setX(x);
    }

    @Override
    public void onClick(View v) {
        float left;
        float right;
        switch (v.getId()) {
            case R.id.imgPlay:
                if (mVisualizer != null && mVisualizer.getEnabled()) {
                    mVisualizer.setEnabled(false);
                }

                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }
                    try {
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(mFilePath);
                        mMediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setupVisualizerFxAndUI();
                    mVisualizer.setEnabled(true);
                    if (mPixelPerSecond == 0) {
                        mPixelPerSecond = mVisualizerView.getWidth() * 1.0f / mMediaPlayer.getDuration();
                    }
                    left = (mMarkerLeft.getX() + mMarkerLeft.getWidth() / 2 - mVisualizerView.getLeft()) / mPixelPerSecond;
                    right = (mMarkerRight.getX() + mMarkerRight.getWidth() / 2 - mVisualizerView.getLeft()) / mPixelPerSecond;
                    mVisualizerView.setStartPosition(left);
                    mVisualizerView.setEndPosition(right);
                    start((long) left, (long) (right - left));
                }
                break;
            case R.id.tvNext:
                float start = Float.parseFloat(mTvStart.getText().toString());
                float end = Float.parseFloat(mTvEnd.getText().toString());
                if (end - start > MAX_SECOND) {
                    Toast.makeText(getBaseContext(), "The length of audio over 10 seconds", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    createDialog();
                }
                break;
            case R.id.rlBack:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                finish();

        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        complete();
    }

    private void complete() {
        mImgPlay.setImageResource(R.mipmap.ic_play);
        mVisualizer.setEnabled(false);
        mVisualizerView.reset();
    }

    public void start(long mStart, long mDuration) {
        CountDownTimer c = new CountDownTimer(mDuration, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                complete();
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
            }
        };
        mMediaPlayer.seekTo((int) mStart);
        mMediaPlayer.start();
        c.start();
        mImgPlay.setImageResource(R.mipmap.ic_pause);
    }

    private void createSound(String name) {
        idSound = UUID.randomUUID().toString();
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
        final String id = Utils.getCurrentUserID(EditAudioActivity.this);
        Sound sound = new Sound(idSound, name, Utils.getCurrentUserName(EditAudioActivity.this), mOutputPath, mOutputPath, ft.format(date).toString());
        sound.setIdUser(id);
        RealmUtils.getRealmUtils(EditAudioActivity.this).addNewSound(EditAudioActivity.this, sound);

        //  send to server
        FirebaseAudio mAudio = new FirebaseAudio(name, id, ft.format(date), 0);
        Firebase firebase = mFirebase.child(FirebaseConstant.AUDIO_URL).push();

        firebase.setValue(mAudio, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Toast.makeText(getBaseContext(), "Audio cound not saved", Toast.LENGTH_SHORT).show();
                } else {
                    String audio_id = firebase.getKey();
                    try {
                        String audioContent = Base64.encodeFromFile(mOutputPath);
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put("content", audioContent);
                        mFirebase.child(FirebaseConstant.AUDIO_CONTENT_URL).child(audio_id).setValue(hashMap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        FirebaseUser f = AppTools.getInfoUser(id);
        mFirebase.child(FirebaseConstant.USER_URL).child(id).child("no_sound").setValue((f.getNo_sound() + 1) + "");
    }

    public void createDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_name_audio);
        dialog.setTitle("Pick a name");

        TextView tvOK = (TextView) dialog.findViewById(R.id.tvOK);
        TextView tvCancel = (TextView) dialog.findViewById(R.id.tvCancel);
        final EditText edtName = (EditText) dialog.findViewById(R.id.edtnewName);

        tvOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                new EditAudio().execute();
                createSound(edtName.getText().toString());
                finish();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (mType.equals("Record")) {
            File file = new File(mFilePath);
            file.delete();
        }
        super.onBackPressed();
    }

    private class EditAudio extends AsyncTask<Void, Void, Void> {

        private float start;
        private float end;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            start = Float.parseFloat(mTvStart.getText().toString());
            end = Float.parseFloat(mTvEnd.getText().toString());
        }

        @Override
        protected Void doInBackground(Void... params) {
            String folderPath = Constant.DIRECTORY_PATH + Constant.AUDIO;
            Tools.createFolder(folderPath);
            mOutputPath = folderPath + "AUDIO_" + AppTools.getDate() + ".m4a";
            try {
                SoundFile soundFile = SoundFile.create(mFilePath, null);
                soundFile.WriteFile(new File(mOutputPath), start, end);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SoundFile.InvalidInputException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mType.equals("Record")) {
                File file = new File(mFilePath);
                file.delete();
            }
        }
    }
}
