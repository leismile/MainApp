package com.ubtechinc.alpha.im.msghandler;

import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.QueryDownloadProgressProto;
import com.ubtechinc.alpha.appmanager.UpgradeClient;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;

/**
 * Created by logic on 18-4-12.
 *
 * @author logic
 */

public class QueryFirmwareDownloadProgressHandler implements IMsgHandler {
  @Override public void handleMsg(int requestCmdId, int responseCmdId,
      AlphaMessageOuterClass.AlphaMessage request, String peer) {
    final long requestSerial = request.getHeader().getSendSerial();
    final QueryDownloadProgressProto.QueryDownloadProgress.Builder downloadResponse =
        QueryDownloadProgressProto.QueryDownloadProgress.newBuilder();
    final UpgradeClient downloader = UpgradeClient.get();

    if (downloader.isDownloading()) {
      downloadResponse.setState(QueryDownloadProgressProto.DownloadState.DOWNLOADING);
    } else if (downloader.isCompleted()) {
      downloadResponse.setState(QueryDownloadProgressProto.DownloadState.COMPLETED);
    } else {
      downloadResponse.setState(QueryDownloadProgressProto.DownloadState.ERROR);
      String errMsg = downloader.getDownloadErrorMsg();
      errMsg = errMsg == null ? "未知错误" : errMsg;
      downloadResponse.setErrorMsg(errMsg);
    }
    downloadResponse.setProgress(QueryDownloadProgressProto.DownloadProgress.newBuilder()
        .setDownloadedBytes(downloader.downloadBytes())
        .setTotalBytes(downloader.totalBytes())
        .setSpeed(downloader.speed())
        .build());
    RobotPhoneCommuniteProxy.getInstance()
        .sendResponseMessage(responseCmdId, "1", requestSerial, downloadResponse.build(), peer,
            null);
  }
}
