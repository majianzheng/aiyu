package io.github.majianzheng.jarboot.client;

/**
 * 文件操作客户端
 * @author majianzheng
 */
public class FileOperator {
    private final ClientProxy clientProxy;

    /**
     * 服务管理客户端构造
     * @param host 服务地址
     * @param user 用户名
     * @param password 登录密码
     */
    public FileOperator(String host, String user, String password) {
        if (null == user || null == password) {
            this.clientProxy = ClientProxy.Factory.createClientProxy(host);
        } else {
            this.clientProxy = ClientProxy.Factory.createClientProxy(host, user, password);
        }
    }

    /**
     * 服务管理客户端构造
     * @param proxy 客户端代理类
     */
    public FileOperator(ClientProxy proxy) {
        this.clientProxy = proxy;
    }
}
