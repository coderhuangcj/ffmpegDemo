package com.coder.hcj.ffmpeg;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coder.hcj.ffmpeg.ffm.CommandExecutor;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FFmpegActivity extends AppCompatActivity {

    public static final int CUT = 111;
    public static final int REMOVE_VOICE = 112;
    public static final int WATER_MARK = 113;
    public static final int CHOICE_IMAGE = 114;
    private String videoPath;//被移除音频流文件的地址
    private long videoDuration;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut_video);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
            LocalMedia media = selectList.get(0);
            String path = media.getPath();
            Log.i("TAG", "path=" + path);
            switch (requestCode) {
                case CUT://录制视频返回，执行裁剪开始的两秒的操作
                    CommandExecutor.cutVideo(path, 0, 2000, (savePath) -> {
                        showToast("裁剪视频结束");
                    });

                    break;
                case REMOVE_VOICE://录制视频返回，执行移除音频流的操作
                    videoDuration = media.getDuration();
                    CommandExecutor.videoRemoveVoice(path, (savePath) -> {
                        showToast("移除音频流结束");
                        videoPath = savePath;
                    });
                    break;
                case WATER_MARK://添加图片水印
                    CommandExecutor.waterMark(path, imagePath, 50, 50, (savePath) -> {
                        showToast("成功！");
                    });
                    break;
                case CHOICE_IMAGE://选择图片后录像  添加图片水印
                    imagePath = path;
                    PictureSelector.create(this)
                            .openCamera(PictureMimeType.ofVideo())
                            .forResult(WATER_MARK);
                    break;
            }
        }
    }


    /**
     * 录制视频  并裁剪开始的两秒视频
     *
     * @param view
     */
    public void cutVideo(View view) {
        PictureSelector.create(this)
                .openCamera(PictureMimeType.ofVideo())
                .forResult(CUT);
    }

    /**
     * 打开摄像头 录制视频   录制结束后  移除视频中的音频流
     *
     * @param view
     */
    public void removeAudio(View view) {
        PictureSelector.create(this)
                .openCamera(PictureMimeType.ofVideo())
                .forResult(REMOVE_VOICE);
    }

    /**
     * 合并音视频
     * 合并前先根据录制的视频长度，裁剪等长的音频文件，默认认为音频文件比视频文件长 需要裁剪
     *
     * @param view
     */
    public void merge(View view) {
        if (TextUtils.isEmpty(videoPath)) {
            showToast("请先录制移除音频流的视频");
        } else {
            File f = new File(videoPath);
            if (!f.exists()) {
                showToast("视频已被删除，请重新录制");
                return;
            }
        }
        String mp3Src = CommandExecutor.getDirectoryPath() + "/audio/demo.mp3";
        int duration = (int) (videoDuration / 1000);
        CommandExecutor.cutAudio(mp3Src, duration, (mp3Path) -> {//先根据视频长度来裁剪等长的音频 ，默认认为音频比视频长
            showToast("音频裁剪成" + duration + "秒结束");
            CommandExecutor.avMerge(videoPath, mp3Path, (path) -> showToast("合并音视频结束"));
        });
    }

    public void waterMark(View view) {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .maxSelectNum(1)// 最大图片选择数量 int
                .minSelectNum(1)// 最小选择数量 int
                .imageSpanCount(4)// 每行显示个数 int
                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                .previewImage(true)// 是否可预览图片 true or false
                .isCamera(false)// 是否显示拍照按钮 true or false
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
//                .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                .setOutputCameraPath("/CustomPath")// 自定义拍照保存路径,可不填
                .enableCrop(false)// 是否裁剪 true or false
                .compress(true)// 是否压缩 true or false
//                .withAspectRatio(1, 1)// int 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                .forResult(CHOICE_IMAGE);//结果回调onActivityResult code
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    public void textWaterMark(View v) {
        String path = CommandExecutor.getDirectoryPath() + "/image.png";
        textToPicture(path, new String[]{"家乐淘科技", "打个字", "哈哈哈"}, 100, Color.RED);
    }


    /**
     * 文字生成图片
     *
     * @param filePath filePath
     * @return 生成图片是否成功
     */
    public void textToPicture(String filePath, String[] arrText, int textSize, int textColor) {
        Bitmap bitmap = textToBitmap(arrText, textSize, textColor);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.flush();
            imagePath = filePath;

            PictureSelector.create(this)
                    .openCamera(PictureMimeType.ofVideo())
                    .forResult(WATER_MARK);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文本转成Bitmap
     *
     * @return 图片的bitmap
     */
    private static Bitmap textToBitmap(String[] arrText, int textSize, int textColor) {
        int width = getBitmapWidth(arrText, textSize);
        int height = getBitmapHeight(arrText, textSize);
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bm.setHasAlpha(true);
        Canvas canvas = new Canvas(bm);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Paint p = new Paint();
        Typeface font = Typeface.create(Typeface.DEFAULT_BOLD , Typeface.BOLD);
        p.setTypeface( font );
        p.setColor(textColor);
        p.setTextSize(textSize);
        for (int i = 0; i < arrText.length; i++) {
            canvas.drawText(arrText[i], 0, (i * textSize) + textSize, p);
        }
        return bm;
    }

    private static int getBitmapHeight(String[] arrText, int textSize) {
        return arrText.length * textSize + textSize / 2;
    }

    private static int getBitmapWidth(String[] arrText, int textSize) {
        int arrLength[] = new int[arrText.length];
        for (int i = 0; i < arrText.length; i++) {
            arrLength[i] = arrText[i].length();
        }
        Arrays.sort(arrLength);
        int maxLength = arrLength[arrLength.length - 1];
        int width = maxLength * textSize;
        return width;
    }


}
