package com.coder.hcj.ffmpeg.ffm;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandExecutor {
    private static Handler mHandler;
    private static ExecutorService fixedThreadPool;

    static {
        System.loadLibrary("ffmpeg_demo");
        mHandler = new Handler(Looper.getMainLooper());
        fixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * 执行native的 ffmpeg的cmd命令
     *
     * @param length
     * @param arrCmd
     * @return
     */
    private static native int runCommand(int length, String[] arrCmd);


    /**
     * 裁剪视频
     *
     * @param videoPath 视频源地址
     * @param startTime 裁剪开始的时间戳 毫秒
     * @param endTime   裁剪结束的时间戳 毫秒
     */
    public static void cutVideo(String videoPath, long startTime, long endTime, final CallBack callBack) {
        String savePath = getVideoSavePath("裁剪视频");
        long duration = endTime - startTime;
        String command = "ffmpeg -ss %d -t %d -accurate_seek -i %s -codec copy %s";
        command = String.format(command, startTime / 1000, duration / 1000, videoPath, savePath);
        String cmds[] = command.split(" ");
        fixedThreadPool.execute(() -> {
            runCommand(cmds.length, cmds);
            call(callBack, savePath);
        });
    }


    /**
     * 裁剪音频
     *
     * @param audioPath 音频源地址
     * @param duration  裁剪的持续时长 秒
     */
    public static void cutAudio(String audioPath, int duration, final CallBack callBack) {
        String savePath = getAudioSavePath("裁剪音频");
        String command = "ffmpeg -ss %d -t %d -accurate_seek -i %s -codec copy %s";
        command = String.format(command, 0, duration, audioPath, savePath);
        String cmds[] = command.split(" ");
        fixedThreadPool.execute(() -> {
            runCommand(cmds.length, cmds);
            call(callBack, savePath);
        });
    }


    /**
     * 移除视频的音频流
     *
     * @param videoPath
     */
    public static void videoRemoveVoice(String videoPath, CallBack callBack) {
        String savePath = getVideoSavePath("移除音频流");
        String command = "ffmpeg -i %s -c:v copy -an %s";
        command = String.format(command, videoPath, savePath);
        String cmds[] = command.split(" ");
        fixedThreadPool.execute(() -> {
            runCommand(cmds.length, cmds);
            call(callBack, savePath);
        });
    }


    /**
     * 音视频合并
     *
     * @param videoPath 视频源
     * @param audioPath 音频源
     */
    public static void avMerge(String videoPath, String audioPath, CallBack callBack) {
        String savePath = getVideoSavePath("音视频合并");
        String command = "ffmpeg -i %s -i %s -c copy %s";
        command = String.format(command, videoPath, audioPath, savePath);
        String cmds[] = command.split(" ");
        fixedThreadPool.execute(() -> {
            long t1 = System.currentTimeMillis();
            Log.i("TAG", "start=" + t1);
            runCommand(cmds.length, cmds);
            long t2 = System.currentTimeMillis();
            Log.i("TAG", "end=" + t2 + "  total=" + (t2 - t1));
            call(callBack, savePath);
        });
    }

    /**
     * 给视频添加图片水印  执行耗时较长
     *
     * @param videoPath
     * @param waterMarkPath
     * @param x
     * @param y
     * @param callBack
     */
    public static void waterMark(String videoPath, String waterMarkPath, int x, int y, CallBack callBack) {
        String savePath = getVideoSavePath("视频水印");
        String command = "ffmpeg -i %s -i %s -filter_complex overlay=%d:%d -y %s";
        command = String.format(command, videoPath, waterMarkPath, x, y, savePath);
        String cmds[] = command.split(" ");


        fixedThreadPool.execute(() -> {
            runCommand(cmds.length, cmds);
            call(callBack, savePath);
        });
    }

    /**
     * 视频压缩(只简单控制了码率)
     *
     * @param videoPath
     */
    public static void compressVideo(String videoPath, CallBack callBack) {
        String savePath = getVideoSavePath("视频压缩");
        String command = "ffmpeg -i %s -b:v 2000k %s";
        command = String.format(command, videoPath, savePath);
        String cmds[] = command.split(" ");
        fixedThreadPool.execute(() -> {
            runCommand(cmds.length, cmds);
            call(callBack, savePath);
        });
    }

    private static String getVideoSavePath(String name) {
        return getDirectoryPath() + System.currentTimeMillis() + name + ".mp4";
    }

    private static String getAudioSavePath(String name) {
        return getDirectoryPath() + System.currentTimeMillis() + name + ".mp3";
    }

    public static String getDirectoryPath() {
        String savePath = Environment.getExternalStorageDirectory().getPath() + "/aaaFfmpegDemo/";
        File file = new File(savePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return savePath;
    }

    private static void call(CallBack callBack, String path) {
        if (callBack != null) {
            mHandler.post(() -> callBack.finish(path));
        }
    }

    public interface CallBack {
        void finish(String path);
    }
}
