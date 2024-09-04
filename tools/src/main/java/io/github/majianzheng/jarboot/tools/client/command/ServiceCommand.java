package io.github.majianzheng.jarboot.tools.client.command;

import io.github.majianzheng.jarboot.api.cmd.annotation.*;
import io.github.majianzheng.jarboot.api.constant.TaskLifecycle;
import io.github.majianzheng.jarboot.api.event.JarbootEvent;
import io.github.majianzheng.jarboot.api.event.Subscriber;
import io.github.majianzheng.jarboot.api.event.TaskLifecycleEvent;
import io.github.majianzheng.jarboot.api.pojo.ServiceInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
public class ServiceCommand extends AbstractClientCommand implements Subscriber<TaskLifecycleEvent> {
    private static final String ACTION_START = "start";
    private static final String ACTION_STOP = "stop";
    private static final String ACTION_RESTART = "restart";

    private String action;
    private String serviceName;
    private String host;
    private CountDownLatch latch;

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
        client.registerSubscriber(this);
        final String tips = "Service " + serviceName + " " + action;
        try {
            latch = new CountDownLatch(1);
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
                    return;
            }
            final long timeout = 30;
            if (latch.await(timeout, java.util.concurrent.TimeUnit.SECONDS)) {
                println(tips + " success.");
            } else {
                println(tips + " timeout.");
            }
        } catch (InterruptedException e) {
            println(tips + " interrupted.");
            Thread.currentThread().interrupt();
        } finally {
            client.deregisterSubscriber(this);
        }
    }

    @Override
    public void cancel() {  // default implementation ignored
    }

    @Override
    public void onEvent(TaskLifecycleEvent event) {
        synchronized (this) {
            switch (action) {
                case ACTION_START:
                case ACTION_RESTART:
                    if (event.getLifecycle() == TaskLifecycle.FINISHED ||
                            event.getLifecycle() == TaskLifecycle.AFTER_STARTED ||
                            event.getLifecycle() == TaskLifecycle.START_FAILED ||
                            event.getLifecycle() == TaskLifecycle.SCHEDULING) {
                        latch.countDown();
                    }
                    break;
                case ACTION_STOP:
                    if (event.getLifecycle() == TaskLifecycle.AFTER_STOPPED ||
                            event.getLifecycle() == TaskLifecycle.STOP_FAILED) {
                        latch.countDown();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Class<? extends JarbootEvent> subscribeType() {
        return TaskLifecycleEvent.class;
    }
}
