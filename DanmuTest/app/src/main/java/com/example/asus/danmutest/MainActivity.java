package com.example.asus.danmutest;

import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakuIterator;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

public class MainActivity extends AppCompatActivity {

    final String Tag="MainActivity";


    String contents[]=new String[]{
            "你他妈是谁？",
            "你不是我糕！把我菜",
            "ban瞎子"
    };

    //添加弹幕的开关
    private boolean showDanmaku;

    //DanmakuView
    private DanmakuView danmakuView;

    //Danmuku的上下文，可以用于对弹幕的各种全局配置进行设定，如设置字体、设置最大显示行数等。
    // 这里并没有什么特殊的要求，因此一切都保持默认。
    private DanmakuContext danmakuContext;

    //构建解析器
    private BaseDanmakuParser parser=new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };



    /*
        实现界面的沉浸式
        只有在安卓4.4及以上的安卓版本中才会支持沉浸式
        主要参数
        View.SYSTEM_UI_FLAG_VISIBLE ：状态栏和Activity共存，Activity不全屏显示。也就是应用平常的显示画面
        View.SYSTEM_UI_FLAG_FULLSCREEN ：Activity全屏显示，且状态栏被覆盖掉
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN ：Activity全屏显示，但是状态栏不会被覆盖掉，而是正常显示，只是Activity顶端布   局会被覆盖住
        View.INVISIBLE ： Activity全屏显示，隐藏状态栏
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //首先判断sdk版本
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

//    getWindow().setStatusBarColor(Color.TRANSPARENT);       //设置状态栏为透明色
//    getWindow().setNavigationBarColor(Color.TRANSPARENT);       //设置导航栏为透明色
//    ActionBar actionBar=getSupportActionBar();
//    actionBar.hide();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(Tag,Environment.getExternalStorageDirectory().toString());
        View decorView=getWindow().getDecorView();
        final VideoView videoView=(VideoView)findViewById(R.id.video_view);
        videoView.setVideoPath(Environment.getExternalStorageDirectory()+
                "/netease/vopen/course_download/english.mp4");
        videoView.start();

        danmakuView=(DanmakuView)findViewById(R.id.danmaku_view);
        //提升绘制效率
        danmakuView.setDrawingCacheEnabled(true);
        Log.d(Tag,"成功获取实例");
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                showDanmaku=true;       //打开开关
                danmakuView.start();    //开启弹幕
                Log.d(Tag,"成功开启弹幕");
                generateSomeDanmaku();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });

        danmakuContext=DanmakuContext.create();
        Log.d(Tag,"成功获取上下文");
        //拥有解析器和上下文之后，就可以对DanmakuView进行操作
        //prepare的参数为解析器和上下文
        //调用prepare函数之后，就会回调setCallback的prepare方法
        danmakuView.prepare(parser,danmakuContext);


        /*
        用户添加弹幕
         */
        final LinearLayout linearLayout=(LinearLayout)findViewById(R.id.send_layout);
        final EditText editText=(EditText)findViewById(R.id.edit_text);
        final Button send=(Button)findViewById(R.id.send);
        danmakuView.setOnClickListener(new View.OnClickListener(){
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                if(linearLayout.getVisibility()==View.GONE)
                    linearLayout.setVisibility(View.VISIBLE);
                else if(linearLayout.getVisibility()==View.VISIBLE)
                    linearLayout.setVisibility(View.GONE);
            }
        });
        send.setOnClickListener(new View.OnClickListener(){
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                String content=editText.getText().toString();      //发送弹幕
                if(content.length()!=0){
                    addDanmaku(content,true);
                    editText.setText("");
                }
            }
        });

        /*
        系统输入法弹出的时候会导致焦点丢失，退出沉浸模式，这里对系统的UI进行监听，保证一直处于趁机模式
         */
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    onWindowFocusChanged(true);
                }
            }
        });
    }


    /*
     *向danmakuView中添加一条弹幕
     *
     * @para content
     *          弹幕的具体内容
     * @para withBorder
     *          弹幕是否具有边框
     */
    private void addDanmaku(String content,boolean withBorder){
        //一条弹幕的内容
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content;
        danmaku.padding = 5;
        danmaku.textColor = Color.YELLOW;
        danmaku.textSize=sp2px(20);
        //设置弹幕发送的时间为当前时间
        if (withBorder) {
            danmaku.borderColor = Color.GREEN;
        }
        danmaku.setTime(danmakuView.getCurrentTime());
        danmakuView.addDanmaku(danmaku);
    }


    /*
    *随机生成弹幕来测试
     */
    private void generateSomeDanmaku(){
        new Thread(new Runnable() {
            //建立一个新的线程进行
            @Override
            public void run() {
                while(showDanmaku){
                    int num=new Random().nextInt(300);      //产生0-299的整数并转换成字符串
                    String content=""+num+num;
                    addDanmaku(content,false);
                    try{
                        Thread.sleep(num);      //产生间隔时间
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * sp转px的方法。
     */
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(danmakuView!=null&&danmakuView.isPrepared())     //活动pause的时候，弹幕暂停
            danmakuView.pause();
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {         //活动重新载入，弹幕重新载入
        super.onResume();
        if(danmakuView!=null&&danmakuView.isPrepared()&&danmakuView.isPaused())
            danmakuView.resume();
    }


    @Override
    protected void onDestroy() {        //活动销毁时，停止刷新弹幕，释放弹幕资源
        super.onDestroy();
        showDanmaku=false;      //关闭开关
        if(danmakuView!=null){
            danmakuView.release();
            //这里使danmakuView等于null是为了加快回收速度，不加java的垃圾回收机制也会将其回收
            danmakuView=null;
        }
    }
}
