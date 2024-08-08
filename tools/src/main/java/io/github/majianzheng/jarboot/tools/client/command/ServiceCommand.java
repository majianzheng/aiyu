package io.github.majianzheng.jarboot.tools.client.command;

import io.github.majianzheng.jarboot.api.cmd.annotation.*;
import io.github.majianzheng.jarboot.api.pojo.ServiceInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * list command
 * @author majianzheng
 */
@Name("service")
@Summary("service operation. ig. service start demo-service")
@Description("Example:\n" +
        "  service -h 127.0.0.1:9899 start demo-server\n" +
        "  service stop demo-service\n" +
        "  service restart demo-service\n")
public class ServiceCommand extends AbstractClientCommand {
    private static final String ACTION_START = "start";
    private static final String ACTION_STOP = "stop";
    private static final String ACTION_RESTART = "restart";

    private String action;
    private String serviceName;
    private String host;

    @Option(shortName = "h", longName = "host")
    @Description("Cluster host")
    public void setHost(String host) {
        this.host = host;
    }

    @Argument(argName = "action", index = 0)
    @Description("action, input start stop restart or get.")
    public void setAction(String action) {
        this.action = action;
    }

    @Argument(argName = "name", index = 1)
    @Description("service name")
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    @Override
    public void run() {
        List<ServiceInstance> service = new ArrayList<>();
        ServiceInstance instance = new ServiceInstance();
        instance.setHost(host);
        instance.setName(serviceName);
        service.add(instance);
        switch (action) {
            case ACTION_START:
                client.startService(service);
                break;
            case ACTION_STOP:
                client.stopService(service);
                break;
            case ACTION_RESTART:
                client.restartService(service);
                break;
            default:
                printHelp();
        }
    }

    @Override
    public void cancel() {

    }
}
