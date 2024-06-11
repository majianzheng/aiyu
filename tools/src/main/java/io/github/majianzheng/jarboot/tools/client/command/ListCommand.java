package io.github.majianzheng.jarboot.tools.client.command;

import io.github.majianzheng.jarboot.api.cmd.annotation.Argument;
import io.github.majianzheng.jarboot.api.cmd.annotation.Description;
import io.github.majianzheng.jarboot.api.cmd.annotation.Name;
import io.github.majianzheng.jarboot.api.cmd.annotation.Summary;
import io.github.majianzheng.jarboot.api.pojo.JvmProcess;
import io.github.majianzheng.jarboot.api.pojo.ServiceInstance;
import io.github.majianzheng.jarboot.text.util.RenderUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * list command
 * @author majianzheng
 */
@Name("list")
@Summary("Display service or java process info.")
@Description("Example:\n" +
        "  list service\n" +
        "  list process\n" +
        "  list s\n" +
        "  list p\n")
public class ListCommand extends AbstractClientCommand {

    private String option;
    @Argument(argName = "option", index = 0, required = false)
    @Description("list type, input service or process")
    public void setOption(String option) {
        this.option = option;
    }
    @Override
    public void run() {
        final String service = "service";
        final String process = "process";
        final String s = "s";
        final String p = "p";
        if (service.equalsIgnoreCase(option) || s.equalsIgnoreCase(option)) {
            echoServices();
        } else if (process.equalsIgnoreCase(option) || p.equalsIgnoreCase(option)) {
            echoProcess();
        } else {
            printHelp();
        }
    }

    private void echoProcess() {
        List<JvmProcess> process = this.client.getJvmProcesses();
        List<String> header = Arrays.asList("PID", "NAME", "STATUS", "HOST");
        List<List<String>> rows = new ArrayList<>();
        process.forEach(service ->  rows.add(Arrays.asList(
                service.getPid(),
                service.getName(),
                withDefault(service.getStatus()),
                withDefault(service.getHostName(), service.getHost()))));
        String out = RenderUtil.renderTable(header, rows, terminal.getWidth(), null);
        print(out);
    }

    private void echoServices() {
        List<ServiceInstance> list = this.client.getServiceList();
        List<String> header = Arrays.asList("SID", "NAME", "GROUP", "STATUS", "HOST");
        List<List<String>> rows = new ArrayList<>();
        list.forEach(service -> rows.add(Arrays.asList(
                    service.getSid(),
                    service.getName(),
                    withDefault(service.getGroup()),
                    withDefault(service.getStatus()),
                    withDefault(service.getHostName(), service.getHost()))));
        String out = RenderUtil.renderTable(header, rows, terminal.getWidth(), null);
        print(out);
    }

    @Override
    public void cancel() {

    }
}
