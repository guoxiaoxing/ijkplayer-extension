package com.guoxiaoxing.ijkplayer.extension.demo.video;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.guoxiaoxing.ijkplayer.extension.demo.R;
import com.guoxiaoxing.ijkplayer.extension.demo.model.SwitchVideoModel;
import com.guoxiaoxing.ijkplayer.extension.demo.widget.SwitchVideoTypeDialog;
import com.guoxiaoxing.ijkplayer.extension.GSYVideoManager;
import com.guoxiaoxing.ijkplayer.extension.GSYVideoPlayer;
import com.guoxiaoxing.ijkplayer.extension.utils.GSYVideoType;
import com.guoxiaoxing.ijkplayer.extension.video.StandardGSYVideoPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ExtensionVideoView extends StandardGSYVideoPlayer {

    private TextView mMoreScale;

    private TextView mSwitchSize;

    private TextView mChangeRotate;

    private List<SwitchVideoModel> mUrlList = new ArrayList<>();

    //记住切换数据源类型
    private int mType = 0;

    //数据源
    private int mSourcePosition = 0;

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public ExtensionVideoView(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public ExtensionVideoView(Context context) {
        super(context);
    }

    public ExtensionVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        initView();
    }

    private void initView() {
        mMoreScale = (TextView) findViewById(R.id.moreScale);
        mSwitchSize = (TextView) findViewById(R.id.switchSize);
        mChangeRotate = (TextView) findViewById(R.id.change_rotate);

        //切换清晰度
        mMoreScale.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mType == 0) {
                    mType = 1;
                    mMoreScale.setText("16:9");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9);
                    if (mTextureView != null)
                        mTextureView.requestLayout();
                } else if (mType == 1) {
                    mType = 2;
                    mMoreScale.setText("4:3");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3);
                    if (mTextureView != null)
                        mTextureView.requestLayout();
                } else if (mType == 2) {
                    mType = 0;
                    mMoreScale.setText("默认比例");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);
                    if (mTextureView != null)
                        mTextureView.requestLayout();
                }
            }
        });

        //切换视频清晰度
        mSwitchSize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showSwitchDialog();
            }
        });

        //旋转播放角度
        mChangeRotate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mTextureView.getRotation() - mRotate) == 270) {
                    mTextureView.setRotation(mRotate);
                    mTextureView.requestLayout();
                    mCoverImageView.setRotation(mRotate);
                    mCoverImageView.requestLayout();
                } else {
                    mTextureView.setRotation(mTextureView.getRotation() + 90);
                    mTextureView.requestLayout();
                    mCoverImageView.setRotation(mCoverImageView.getRotation() + 90);
                    mCoverImageView.requestLayout();
                }
            }
        });

    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param objects       object[0]目前为title
     * @return
     */
    public boolean setupUrl(List<SwitchVideoModel> url, boolean cacheWithPlay, Object... objects) {
        mUrlList = url;
        return setUp(url.get(0).getUrl(), cacheWithPlay, objects);
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param objects       object[0]目前为title
     * @return
     */
    public boolean setupUrl(List<SwitchVideoModel> url, boolean cacheWithPlay, File cachePath, Object... objects) {
        mUrlList = url;
        return setUp(url.get(0).getUrl(), cacheWithPlay, cachePath, objects);
    }

    @Override
    public int getLayoutId() {
        return R.layout.sample_video;
    }

    /**
     * 弹出切换清晰度
     */
    private void showSwitchDialog() {
        SwitchVideoTypeDialog switchVideoTypeDialog = new SwitchVideoTypeDialog(getContext());
        switchVideoTypeDialog.initList(mUrlList, new SwitchVideoTypeDialog.OnListItemClickListener() {
            @Override
            public void onItemClick(int position) {
                final String name = mUrlList.get(position).getName();
                if (mSourcePosition != position) {
                    if ((mCurrentState == GSYVideoPlayer.CURRENT_STATE_PLAYING
                            || mCurrentState == GSYVideoPlayer.CURRENT_STATE_PAUSE)
                            && GSYVideoManager.instance().getMediaPlayer() != null) {
                        final String url = mUrlList.get(position).getUrl();
                        onVideoPause();
                        final long currentPosition = mCurrentPosition;
                        GSYVideoManager.instance().releaseMediaPlayer();
                        cancelProgressTimer();
                        hideAllWidget();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setUp(url, mCache, mCachePath, mObjects);
                                setSeekOnStart(currentPosition);
                                startPlayLogic();
                                cancelProgressTimer();
                                hideAllWidget();
                            }
                        }, 500);
                        mSwitchSize.setText(name);
                        mSourcePosition = position;
                    }
                } else {
                    Toast.makeText(getContext(), "已经是 " + name, Toast.LENGTH_LONG).show();
                }
            }
        });
        switchVideoTypeDialog.show();
    }
}
