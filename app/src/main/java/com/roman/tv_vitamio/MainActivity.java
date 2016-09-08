package com.roman.tv_vitamio;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class MainActivity extends Activity implements MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {

    VideoView videoView;
    MediaController mediaController;
    private int layout = VideoView.VIDEO_LAYOUT_ZOOM;
    private ProgressBar pb;
    private TextView downloadRateView, loadRateView;

    private GestureDetector gestureDetector;
    private AudioManager audioManager;
    /**
     * 最大声音
     */
    private int maxVolume;
    /**
     * 当前声音
     */
    private int volume = -1;
    /**
     * 当前亮度
     */
    private float brightness = -1f;
    private View volumeBrightnessLayout;
    private ImageView operationBg;
    private ImageView operationPercent;
    private TextView operationText;
    private PopupWindow popupWindow;
    private ListView listView;
    private List<Map<String, Object>> chanel;
    private String[] chanels = {"CCTV1", "CCTV5", "CCTV6", "CCTV5+", "北京体育", "北京卫视", "江苏卫视", "浙江卫视", "东方卫视", "CHC", "湖南卫视", "TEST"};
    private String[] chanelsURIs = {"http://tv6.byr.cn/hls/cctv1hd.m3u8",
            "http://tv6.byr.cn/hls/cctv5hd.m3u8", "http://tv6.byr.cn/hls/cctv6hd.m3u8",
            "http://tv6.byr.cn/hls/cctv5phd.m3u8", "http://tv6.byr.cn/hls/btv6hd.m3u8",
            "http://tv6.byr.cn/hls/btv1hd.m3u8", "http://tv6.byr.cn/hls/jshd.m3u8",
            "http://tv6.byr.cn/hls/zjhd.m3u8", "http://tv6.byr.cn/hls/dfhd.m3u8",
            "http://tv6.byr.cn/hls/chchd.m3u8", "http://tv6.byr.cn/hls/hunanhd.m3u8",
            "http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8"};
    /**
     * 定时隐藏
     */
    private Handler mDismissHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            volumeBrightnessLayout.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chanel = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < chanels.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("chanel", chanels[i]);
            chanel.add(item);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_main);
        View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.window, null);
        popupWindow = new PopupWindow(contentView);
        listView = (ListView) contentView.findViewById(R.id.listView);

        videoView = (VideoView) findViewById(R.id.surface_view);
        pb = (ProgressBar) findViewById(R.id.probar);
        downloadRateView = (TextView) findViewById(R.id.download_rate);
        loadRateView = (TextView) findViewById(R.id.load_rate);

        volumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
        operationBg = (ImageView) findViewById(R.id.operation_bg);
        //operationPercent = (ImageView) findViewById(R.id.operation_percent);
        operationText = (TextView) findViewById(R.id.operation_text);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        videoView.setVideoURI(Uri.parse(chanelsURIs[0]));
        mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.requestFocus();
        videoView.setOnInfoListener(this);
        videoView.setOnBufferingUpdateListener(this);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setPlaybackSpeed(1.0f);
            }
        });

        gestureDetector = new GestureDetector(this, new MyGestureListener());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (videoView != null)
            videoView.setVideoLayout(layout, 0);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (videoView.isPlaying()) {
                    videoView.pause();
                    pb.setVisibility(View.VISIBLE);
                    downloadRateView.setText("");
                    loadRateView.setText("");
                    downloadRateView.setVisibility(View.VISIBLE);
                    loadRateView.setVisibility(View.VISIBLE);

                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                videoView.start();
                pb.setVisibility(View.GONE);
                downloadRateView.setVisibility(View.GONE);
                loadRateView.setVisibility(View.GONE);
                break;
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                downloadRateView.setText("" + extra + "kb/s" + "  ");
                break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        loadRateView.setText(percent + "%");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event))
            return true;

        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        volume = -1;
        brightness = -1f;

        // 隐藏
        mDismissHandler.removeMessages(0);
        mDismissHandler.sendEmptyMessageDelayed(0, 500);
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;

            // 显示
            operationBg.setImageResource(R.drawable.video_volumn_bg);
            volumeBrightnessLayout.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * maxVolume) + volume;
        if (index > maxVolume)
            index = maxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

//        // 变更进度条
//        ViewGroup.LayoutParams lp = operationPercent.getLayoutParams();
//        lp.width = findViewById(R.id.operation_full).getLayoutParams().width
//                * index / maxVolume;
//        operationPercent.setLayoutParams(lp);

        operationText.setText(index * 20 / 3 + "");
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (brightness < 0) {
            brightness = getWindow().getAttributes().screenBrightness;
            if (brightness <= 0.00f)
                brightness = 0.50f;
            if (brightness < 0.01f)
                brightness = 0.01f;

            // 显示
            operationBg.setImageResource(R.drawable.video_brightness_bg);
            volumeBrightnessLayout.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        getWindow().setAttributes(lpa);

//        ViewGroup.LayoutParams lp = operationPercent.getLayoutParams();
//        lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
//        operationPercent.setLayoutParams(lp);

        operationText.setText((int) (lpa.screenBrightness * 100) + "");
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (layout == VideoView.VIDEO_LAYOUT_ZOOM)
                layout = VideoView.VIDEO_LAYOUT_ORIGIN;
            else
                layout++;
            if (videoView != null)
                videoView.setVideoLayout(layout, 0);
            return true;
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float oldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();
            int x = (int) e2.getRawX();
            Display disp = getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();

            if (oldX > windowWidth * 4.0 / 5)// 右边滑动
                onVolumeSlide((mOldY - y) / windowHeight);
            else if (oldX < windowWidth * 2.0 / 5 & oldX > windowWidth / 5.0)// 左边滑动
                onBrightnessSlide((mOldY - y) / windowHeight);
            else if (oldX < windowWidth / 10.0 & (x - oldX) > windowWidth / 10.0)//左边向右滑动
            {
                popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popupWindow.setFocusable(true);
                popupWindow.showAtLocation(videoView, Gravity.LEFT, 0, 0);
                listView.setAdapter(new SimpleAdapter(MainActivity.this, chanel, R.layout.list_item,
                        new String[]{"chanel"}, new int[]{R.id.chanel}));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        System.out.print(chanelsURIs[position]);
                        videoView.setVideoURI(Uri.parse(chanelsURIs[position]));
                    }
                });
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

    }

}
