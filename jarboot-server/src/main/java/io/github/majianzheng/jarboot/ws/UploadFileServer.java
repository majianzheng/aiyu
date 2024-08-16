package io.github.majianzheng.jarboot.ws;

import io.github.majianzheng.jarboot.common.utils.AesUtils;
import io.github.majianzheng.jarboot.common.utils.JsonUtils;
import io.github.majianzheng.jarboot.common.utils.StringUtils;
import io.github.majianzheng.jarboot.dao.FileUploadProgressDao;
import io.github.majianzheng.jarboot.entity.FileUploadProgress;
import io.github.majianzheng.jarboot.service.ServerRuntimeService;
import io.github.majianzheng.jarboot.utils.CommonUtils;
import io.github.majianzheng.jarboot.utils.SettingUtils;
import io.github.majianzheng.jarboot.utils.TaskUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 文件上传服务
 * @author mazheng
 */
@ServerEndpoint("/jarboot/upload/ws")
@RestController
@SuppressWarnings({"java:S3776", "java:S2696"})
public class UploadFileServer {
    private static final Logger logger = LoggerFactory.getLogger(UploadFileServer.class);
    private static final long INTERVAL_UPDATE_THRESHOLD = 160L * 1024 * 1024;
    private static final String EVENT_STATUS = "status";
    private static final String EVENT_PROGRESS = "progress";
    private static final String EVENT_SEND = "send";
    private static final Map<String, SessionProxy> SESSION_PROXY_MAP = new ConcurrentHashMap<>(16);
    private static FileUploadProgressDao fileUploadProgressDao;
    private static ServerRuntimeService serverRuntimeService;
    private FileUploadProgress fileUploadProgress;
    private FileOutputStream outputStream;
    private final AtomicBoolean scheduling = new AtomicBoolean(false);
    private boolean pause = false;
    private boolean importService = false;
    private File dstFile;
    private long writeTimes = 0;
    private long lastUpdateTime = 0;
    private long sendCountOnce;
    private boolean intervalUpdate = false;

    @Autowired
    public void setFileUploadProgressDao(FileUploadProgressDao dao) {
        fileUploadProgressDao = dao;
    }
    @Autowired
    public void setServerRuntimeService(ServerRuntimeService s) {
        serverRuntimeService = s;
    }
    @OnOpen
    public void onOpen(Session session) {
        String clusterHost = CommonUtils.getSessionClusterHost(session);
        if (CommonUtils.needProxy(clusterHost)) {
            SESSION_PROXY_MAP.put(clusterHost, new SessionProxy(session, clusterHost));
            return;
        }
        if (StringUtils.isEmpty(clusterHost)) {
            clusterHost = "localhost";
        }
        String filename = CommonUtils.getSessionParam("filename", session);
        String relativePath = CommonUtils.getSessionParam("relativePath", session);
        long totalSize = Long.parseLong(CommonUtils.getSessionParam("totalSize", session));
        String baseDir = CommonUtils.getSessionParam("baseDir", session);
        String dstPath = CommonUtils.getSessionParam("dstPath", session);
        String sendCountOnceStr = CommonUtils.getSessionParam("sendCountOnce", session);
        this.sendCountOnce = Long.parseLong(sendCountOnceStr);
        this.intervalUpdate = totalSize < INTERVAL_UPDATE_THRESHOLD;
        // home、service、 workspace
        String uploadMode = CommonUtils.getSessionParam("uploadMode", session);
        switch (uploadMode) {
            case "service":
                baseDir = SettingUtils.getHomePath();
                this.importService = true;
                break;
            case "workspace":
                baseDir = SettingUtils.getWorkspace();
                break;
            case "home":
                baseDir = SettingUtils.getHomePath();
                break;
            default:
                baseDir = StringUtils.isEmpty(baseDir) ? SettingUtils.getHomePath() : AesUtils.decrypt(baseDir);
        }
        boolean append = true;
        dstFile = FileUtils.getFile(baseDir, dstPath);
        fileUploadProgress = fileUploadProgressDao.getByClusterHostAndAbsolutePath(clusterHost, dstFile.getAbsolutePath());
        if (null == fileUploadProgress) {
            FileUploadProgress temp = new FileUploadProgress();
            temp.setClusterHost(clusterHost);
            temp.setDstPath(dstPath);
            temp.setAbsolutePath(dstFile.getAbsolutePath());
            temp.setFilename(filename);
            temp.setRelativePath(relativePath);
            temp.setTotalSize(totalSize);
            temp.setUploadSize(0L);
            fileUploadProgress = fileUploadProgressDao.save(temp);
            if (dstFile.isFile() && dstFile.exists()) {
                // 已经存在的文件，覆盖
                logger.info("文件({})已经存在，将覆盖当前文件", dstFile.getAbsolutePath());
                append = false;
            }
        } else {
            if (dstFile.isFile() && dstFile.exists()) {
                logger.info("文件({})已经存在，当前文件大小：{}, 已上传大小：{}，总大小：{}",
                        dstFile.getAbsolutePath(),
                        dstFile.length(),
                        fileUploadProgress.getUploadSize(),
                        fileUploadProgress.getTotalSize());
                if (!Objects.equals(dstFile.length(), fileUploadProgress.getUploadSize())) {
                    append = false;
                }
            } else {
                append = false;
            }
        }
        if (!append) {
            fileUploadProgress.setUploadSize(0L);
            fileUploadProgress.setTotalSize(totalSize);
            fileUploadProgressDao.save(fileUploadProgress);
        }
        try {
            outputStream = FileUtils.openOutputStream(dstFile, append);
            logger.info("开始上传文件：{}，append：{}, total: {}，uploadSize：{}", filename, append, totalSize, fileUploadProgress.getUploadSize());
            this.lastUpdateTime = System.currentTimeMillis();
            this.updateProgress(session, EVENT_PROGRESS);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            this.fileUploadProgress.setErrorMsg(e.getMessage());
            this.updateProgress(session, EVENT_STATUS);
        }
    }
    @OnClose
    public void onClose(Session session) {
        String clusterHost = CommonUtils.getSessionClusterHost(session);
        if (CommonUtils.needProxy(clusterHost)) {
            SessionProxy sessionProxy = SESSION_PROXY_MAP.remove(clusterHost);
            if (null != sessionProxy) {
                logger.info("代理关闭，代理clusterHost: {}", clusterHost);
                sessionProxy.proxyOnClose();
            }
            return;
        }
        logger.debug("关闭上传文件：{}", session.getId());
        try {
            if (null != fileUploadProgress && null != fileUploadProgress.getId()) {
                fileUploadProgressDao.save(fileUploadProgress);
                fileUploadProgressDao.deleteFinished();
            }
            if (this.importService && null != fileUploadProgress && Objects.equals(fileUploadProgress.getUploadSize(), fileUploadProgress.getTotalSize())) {
                // 导入服务处理
                serverRuntimeService.recoverService(session.getUserPrincipal().getName(), dstFile);
            }
            if (null == outputStream) {
                return;
            }
            outputStream.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            outputStream = null;
        }
    }
    @OnError
    public void onError(Throwable error, Session session) {
        logger.debug(error.getMessage(), error);
        String clusterHost = CommonUtils.getSessionClusterHost(session);
        SessionProxy sessionProxy = SESSION_PROXY_MAP.remove(clusterHost);
        if (null != sessionProxy) {
            logger.warn("代理上传异常，clusterHost: {}", clusterHost, error);
            sessionProxy.proxyOnClose();
        } else {
            logger.warn("上传异常，sessionId: {}", session.getId(), error);
        }
        this.onClose(session);
    }

    @OnMessage
    public void onBinaryMessage(byte[] message, Session session) {
        String clusterHost = CommonUtils.getSessionClusterHost(session);
        if (CommonUtils.needProxy(clusterHost)) {
            SESSION_PROXY_MAP.get(clusterHost).proxyBinary(message);
            return;
        }
        if (null == outputStream || message.length == 0) {
            logger.info("已经传输完成，message size: {}", message.length);
            return;
        }
        if (pause) {
            logger.info("已经发生了异常，暂停");
            return;
        }
        try {
            outputStream.write(message);
            writeTimes++;
            this.fileUploadProgress.setUploadSize(this.fileUploadProgress.getUploadSize() + message.length);
            if (fileUploadProgress.getUploadSize() >= fileUploadProgress.getTotalSize()) {
                // 传输完成
                logger.info("上传文件完成：{}", fileUploadProgress.getFilename());
                outputStream.flush();
                this.updateProgress(session, EVENT_PROGRESS);
                TaskUtils.getTaskExecutor().schedule(() -> {
                    try {
                        if (session.isOpen()) {
                            session.close();
                        }
                    } catch (Exception e) {
                        // ignore
                    } finally {
                        fileUploadProgressDao.deleteFinished();
                    }
                }, 5, TimeUnit.SECONDS);
            } else {
                if (writeTimes % this.sendCountOnce == 0) {
                    this.updateProgress(session, EVENT_SEND);
                    return;
                }
                final long interval = 200;
                if (this.intervalUpdate && writeTimes % interval == 0) {
                    long current = System.currentTimeMillis();
                    final long maxTimeout = 500;
                    if (current - this.lastUpdateTime > maxTimeout) {
                        this.lastUpdateTime = current;
                        this.updateProgress(session, EVENT_PROGRESS);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            this.fileUploadProgress.setErrorMsg(e.getMessage());
            this.pause = true;
            this.updateProgress(session, EVENT_STATUS);
        }
    }
    private void updateProgress(Session session, String event) {
        if (scheduling.compareAndSet(false, true)) {
            try {
                if (null != outputStream) {
                    if (Objects.equals(fileUploadProgress.getUploadSize(), fileUploadProgress.getTotalSize())) {
                        outputStream.close();
                        outputStream = null;
                    } else {
                        outputStream.flush();
                    }
                }
                this.fileUploadProgress.setEvent(event);
                session.getBasicRemote().sendText(JsonUtils.toJsonString(this.fileUploadProgress));
                this.fileUploadProgress.setEvent(StringUtils.EMPTY);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                scheduling.set(false);
            }
        }
    }
}
