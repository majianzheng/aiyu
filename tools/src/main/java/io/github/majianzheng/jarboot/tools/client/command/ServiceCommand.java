package io.github.majianzheng.jarboot.tools.client.command;

import io.github.majianzheng.jarboot.api.cmd.annotation.Argument;
import io.github.majianzheng.jarboot.api.cmd.annotation.Description;
import io.github.majianzheng.jarboot.api.cmd.annotation.Name;
import io.github.majianzheng.jarboot.api.cmd.annotation.Summary;
import io.github.majianzheng.jarboot.api.pojo.ServiceInstance;
import io.github.majianzheng.jarboot.text.util.RenderUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * list command
 * @author majianzheng
 */
@Name("service")
@Summary("service operation.")
@Description("Example:\n" +
        "  service get demo-server\n" +
        "  service start demo-server\n" +
        "  service stop demo-service\n" +
        "  service restart demo-service p\n")
public class ServiceCommand extends AbstractClientCommand {
    private static final String ACTION_GET = "get";
    private static final String ACTION_START = "start";
    private static final String ACTION_STOP = "stop";
    private static final String ACTION_RESTART = "restart";

    private String action;
    private String serviceName;

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
        switch (action) {
            case ACTION_GET:
                printService();
                break;
            case ACTION_START:
                client.startService(Collections.singletonList(serviceName));
                print(String.format("Service %s start success.\n", serviceName));
                break;
            case ACTION_STOP:
                client.stopService(Collections.singletonList(serviceName));
                print(String.format("Service %s stop success.\n", serviceName));
                break;
            case ACTION_RESTART:
                client.restartService(Collections.singletonList(serviceName));
                print(String.format("Service %s restart success.\n", serviceName));
                break;
            default:
                printHelp();
        }
    }

    @Override
    public void cancel() {

    }

    private void printService() {
        ServiceInstance service = client.getService(serviceName);
        List<String> header = Arrays.asList("SID", "NAME", "GROUP", "STATUS", "HOST");
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                service.getSid(),
                service.getName(),
                withDefault(service.getGroup()),
                withDefault(service.getStatus()),
                withDefault(service.getHostName(), service.getHost())));
        String out = RenderUtil.renderTable(header, rows, terminal.getWidth(), null);
        print(out);
    }
}
