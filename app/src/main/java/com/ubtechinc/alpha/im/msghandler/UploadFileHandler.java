package com.ubtechinc.alpha.im.msghandler;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import com.ubtech.utilcode.utils.LogUtils;
import com.ubtech.utilcode.utils.Utils;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.robotinfo.RobotState;
import com.ubtechinc.alpha.utils.QiNiuUploadUtil;
import com.ubtechinc.alpha.utils.ZipUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;

/**
 * Created by riley.zhang on 2018/6/14.
 */

public class UploadFileHandler implements IMsgHandler {
    private static final String TAG = UploadFileHandler.class.getSimpleName();
    private static final String FILE_DIR = "ZIP";
    private static final String ZIP_FILE_PATH = "test.zip";
    private String mCurrentLogPath;
    private HashMap<String, String> mLogFileMap = new HashMap<>();

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        LogUtils.i(TAG, "UploadFileHandler requestCmdId = " + requestCmdId + " responseCmdId = " + responseCmdId);
        String logFileBytes = logFilePath();
        //此处不用判断fileBytes为空，如果数据为空的话七牛直接返回失败
        Date now = new Date();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now);
        String uploadFileName = date + "/" + RobotState.get().getSid() + "_robot";
        QiNiuUploadUtil.getInstance().uploadFilePath(logFileBytes, uploadFileName, new QiNiuUploadUtil.UploadFileListener() {
            @Override
            public void onSuccess() {
                deleteAllFiles();
                LogUtils.i(TAG, "qiniu uploadFilePath success");

            }

            @Override
            public void onFail() {
                deleteAllFiles();
                LogUtils.i(TAG, "qiniu uploadFilePath fail");

            }
        });
    }

    private void deleteAllFiles() {

        deleteFile(mCurrentLogPath + File.separator + ZIP_FILE_PATH);
        deleteDirectory(mCurrentLogPath + File.separator + FILE_DIR);
    }

    private boolean deleteFile(String filePath) {
        File file = new File(filePath);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + filePath + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + filePath + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + filePath + "不存在！");
            return false;
        }
    }

    private boolean deleteDirectory(String dirPath) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dirPath.endsWith(File.separator))
            dirPath = dirPath + File.separator;
        File dirFile = new File(dirPath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            System.out.println("删除目录失败：" + dirPath + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            } else if (files[i].isDirectory()) {// 删除子目录(目前这个是没有子目录的，里面只有各个应用的log文件)
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            System.out.println("删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            System.out.println("删除目录" + dirPath + "成功！");
            return true;
        } else {
            return false;
        }
    }

    //返回的文件的地址跟LogUtils 类里面写log到对应的文件保持一致
    private String logFilePath() {
        String fullPath;
        getAllUbtPackages();
        copyAllLogFile();
        fullPath = mCurrentLogPath + File.separator + ZIP_FILE_PATH;
        Log.i(TAG, "logFilePath fullPath = " + fullPath);
        return fullPath;
    }

    private void getAllUbtPackages() {
        String dir;

        if("mounted".equals(Environment.getExternalStorageState())) {
            dir = Utils.getContext().getExternalCacheDir().getPath() + File.separator + "ulog";
        } else {
            dir = Utils.getContext().getCacheDir().getPath() + File.separator + "ulog";
        }

        Date now = new Date();
        String date = (new SimpleDateFormat("MM-dd", Locale.getDefault())).format(now);
        String currentPackageName = Utils.getContext().getPackageName();
        PackageManager packageManager = Utils.getContext().getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        if (packageInfoList == null) {
            return;
        }
        for (int i = 0 ; i < packageInfoList.size(); i++) {

            if (packageInfoList.get(i).packageName.contains("ubt")) {
                String packageName = packageInfoList.get(i).packageName;
                String packageFile = dir.replace(currentPackageName, packageName);
                File file = new File(packageFile);
                Log.i(TAG, "getAllUbtPackages file.exists() = " + file.exists() + " packageFile = " + packageFile);
                if (!file.exists()) {
                    continue;
                }
                File[] files = file.listFiles();
                Log.i(TAG, "getAllUbtPackages files = " + files);
                if (files == null) {
                    continue;
                }
                for(File childFile:files) {
                    if (childFile.isDirectory()) {
                        packageFile = childFile.getAbsolutePath();
                        Log.i(TAG, "getAllUbtPackages 11111 packageFile = " + packageFile);
                    }
                }
                if (packageName.equalsIgnoreCase(currentPackageName)) {
                    mCurrentLogPath = packageFile;
                }
                packageFile = packageFile + File.separator + date + ".txt";
                mLogFileMap.put(packageName, packageFile);
                Log.i(TAG, "getAllUbtPackages 222222 packageFile = " + packageFile);
            }
        }
    }

    private void copyAllLogFile() {
        if (mLogFileMap == null || mLogFileMap.size() == 0) {
            return;
        }
        File file = new File(mCurrentLogPath + File.separator + FILE_DIR);
        if (!file.exists()) {
            boolean isSuccess = file.mkdir();
            Log.i(TAG, "isSuccess = " + isSuccess);
        }

        Iterator iterator = mLogFileMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (java.util.Map.Entry)iterator.next();
            copySingleLogFile(entry.getValue().toString(), entry.getKey().toString());
        }
        try{
            ZipUtil.zip(mCurrentLogPath + File.separator + FILE_DIR, mCurrentLogPath + File.separator + ZIP_FILE_PATH);
        }catch (IOException i) {
            i.printStackTrace();
        }

    }

    private void copySingleLogFile(String sourceFilePath, String destFilePath) {
        copyFile(sourceFilePath, mCurrentLogPath + File.separator + FILE_DIR + File.separator + destFilePath);
    }

    private void copyFile(String oldPath, String newPath) {
        Log.i(TAG, "copyFile oldPath = " + oldPath + " newPath = " + newPath);
        try {
            int bytesum = 0;
            int byteread;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];

                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (IOException e) {
            LogUtils.i(TAG, "copyFile fail");
            e.printStackTrace();
        }
    }

    private byte[] getLogFileBytes() {
        byte[] fileBytes = null;
        Source source = null;
        BufferedSource bSource = null;
        try{
            String filePath = logFilePath();
            if (filePath == null) {
                return fileBytes;
            }
            LogUtils.i(TAG, "getLogFileBytes filePath = " + filePath);
            File file = new File(filePath);
            //读文件
            source = Okio.source(file);
            //通过source拿到 bufferedSource
            bSource = Okio.buffer(source);
            fileBytes = bSource.readByteArray();
            LogUtils.i(TAG, "fileBytes = " + fileBytes + " bSource = " + bSource);
        } catch (IOException e){
            e.printStackTrace();
            LogUtils.i(TAG, "read file fail !!!!!");
        } finally {
            try{
                if (null != bSource) {
                    bSource.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return fileBytes;
    }


}
