package com.ubtechinc.alpha.upload.log;

import android.text.TextUtils;

import com.ubtech.utilcode.utils.FileUtils;
import com.ubtechinc.alpha.upload.IUploadResultListener;
import com.ubtechinc.alpha.upload.UploadCBHandler;
import com.ubtechinc.alpha.upload.UploadType;
import com.ubtechinc.alpha.upload.qiniu.QiniuUploader;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 通过语音命令或者手机命令来抓取上传log
 *
 * @author wangzhengtian
 * @Date 2017-03-03
 */

public class LogUploadByCommand implements IUploadResultListener {
    /** 最大上传尝试次数 **/
    private static final int MAX_UPLOAD_TRY = 3;
    private static final int INTERVAL_TIME = 5*60*1000;
    private int mUploadCount = 0;
    private String mFileName = "";

    /** 是否在抓log，相邻两次要间隔5分钟以上 **/
    private boolean isGettingLog = false;
    private Timer mTimer;

    private static LogUploadByCommand sLogUploadByCommand;

    /** 上一次成功抓取的时间 **/
    private long startTime;

    private LogUploadByCommand() {
        mTimer = new Timer();
    }

    public static LogUploadByCommand getInstance() {
        if(sLogUploadByCommand == null) {
            sLogUploadByCommand = new LogUploadByCommand();
        }

        return sLogUploadByCommand;
    }

    public int start() {
        if(isGettingLog) {
            /** 距离上次抓取不足5分钟 **/
            return getSurplusTime();
        }

        LogSave2File logSave2File = LogSave2File.getInstance();
        String fileName = logSave2File.save();
        if(!TextUtils.isEmpty(fileName)) {
            isGettingLog = true;
            startTime = System.currentTimeMillis();

            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isGettingLog = false;
                }
            },INTERVAL_TIME);

            mUploadCount = mUploadCount + 1;
            mFileName = fileName;
            UploadCBHandler handler = new UploadCBHandler();
            handler.filePath = FileUtils.getSDCardPath() + File.separator + LogSave2File.LOG_DIRECTORY +   File.separator + fileName;
            handler.type = UploadType.TYPE_LOG;
            QiniuUploader.get().upload(handler,  this);
        }

        return 10;
    }


    private int getSurplusTime() {
        int intervalTime = (int) (System.currentTimeMillis() - startTime);
        int minute = 5 - (intervalTime/(1000*60));
        return minute;
    }

    @Override
    public void onUploadSuccess(String url, UploadCBHandler uploadCBHandler) {

    }

    @Override
    public void onUploadFail(String respInfo, UploadCBHandler uploadCBHandler) {
        if( mUploadCount < MAX_UPLOAD_TRY  && !TextUtils.isEmpty(mFileName)) {
            /** 再次尝试上传 **/
            mUploadCount = mUploadCount + 1;
            UploadCBHandler handler = new UploadCBHandler();
            handler.filePath = FileUtils.getSDCardPath() + File.separator + LogSave2File.LOG_DIRECTORY +   File.separator + mFileName;
            handler.type = UploadType.TYPE_LOG;
            QiniuUploader.get().upload(handler,  this);
        }
    }
}
