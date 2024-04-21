package io.github.majianzheng.jarboot.tools.client.command;

import io.github.majianzheng.jarboot.api.cmd.annotation.Argument;
import io.github.majianzheng.jarboot.api.cmd.annotation.Description;
import io.github.majianzheng.jarboot.api.cmd.annotation.Name;
import io.github.majianzheng.jarboot.api.cmd.annotation.Summary;
import io.github.majianzheng.jarboot.api.constant.TaskLifecycle;
import io.github.majianzheng.jarboot.api.event.JarbootEvent;
import io.github.majianzheng.jarboot.api.event.Subscriber;
import io.github.majianzheng.jarboot.api.event.TaskLifecycleEvent;
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
public class ServiceCommand extends AbstractClientCommand implements Subscriber<TaskLifecycleEvent> {
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
                client.registerSubscriber(serviceName, TaskLifecycle.PRE_START, this);
                client.registerSubscriber(serviceName, TaskLifecycle.AFTER_STARTED, this);
                client.registerSubscriber(serviceName, TaskLifecycle.START_FAILED, this);
                client.startService(Collections.singletonList(serviceName));
                break;
            case ACTION_STOP:
                client.registerSubscriber(serviceName, TaskLifecycle.PRE_STOP, this);
                client.registerSubscriber(serviceName, TaskLifecycle.AFTER_STOPPED, this);
                client.registerSubscriber(serviceName, TaskLifecycle.STOP_FAILED, this);
                client.stopService(Collections.singletonList(serviceName));
                break;
            case ACTION_RESTART:
                client.registerSubscriber(serviceName, TaskLifecycle.PRE_STOP, this);
                client.registerSubscriber(serviceName, TaskLifecycle.PRE_START, this);
                client.registerSubscriber(serviceName, TaskLifecycle.AFTER_STARTED, this);
                client.registerSubscriber(serviceName, TaskLifecycle.AFTER_STOPPED, this);
                client.registerSubscriber(serviceName, TaskLifecycle.START_FAILED, this);
                client.registerSubscriber(serviceName, TaskLifecycle.STOP_FAILED, this);
                client.restartService(Collections.singletonList(serviceName));
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

    @Override
    public void onEvent(TaskLifecycleEvent event) {
        switch (event.getLifecycle()) {
            case PRE_START:
                client.deregisterSubscriber(serviceName, TaskLifecycle.PRE_START, this);
                print(String.format("Service %s start...\n", serviceName));
                break;
            case AFTER_STARTED:
                client.deregisterSubscriber(serviceName, TaskLifecycle.AFTER_STARTED, this);
                print(String.format("Service %s start success.\n", serviceName));
                break;
            case START_FAILED:
                client.deregisterSubscriber(serviceName, TaskLifecycle.START_FAILED, this);
                print(String.format("Service %s start failed.\n", serviceName));
                break;
            case PRE_STOP:
                client.deregisterSubscriber(serviceName, TaskLifecycle.PRE_STOP, this);
                print(String.format("Service %s stop...\n", serviceName));
                break;
            case AFTER_STOPPED:
                client.deregisterSubscriber(serviceName, TaskLifecycle.AFTER_STOPPED, this);
                print(String.format("Service %s stop success.\n", serviceName));
                break;
            case STOP_FAILED:
                client.deregisterSubscriber(serviceName, TaskLifecycle.STOP_FAILED, this);
                print(String.format("Service %s stop failed.\n", serviceName));
                break;
            default:
                break;
        }
    }

    @Override
    public Class<? extends JarbootEvent> subscribeType() {
        return TaskLifecycleEvent.class;
    }
}
