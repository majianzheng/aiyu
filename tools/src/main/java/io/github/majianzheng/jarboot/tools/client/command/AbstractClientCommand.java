package io.github.majianzheng.jarboot.tools.client.command;

import io.github.majianzheng.jarboot.api.cmd.annotation.Description;
import io.github.majianzheng.jarboot.api.cmd.annotation.Name;
import io.github.majianzheng.jarboot.api.cmd.annotation.Summary;
import io.github.majianzheng.jarboot.api.service.ServiceManager;
import io.github.majianzheng.jarboot.common.AnsiLog;
import org.jline.terminal.Terminal;

/**
 * 客户端命令
 * @author majianzheng
 */
public abstract class AbstractClientCommand {
    protected String name;
    protected ServiceManager client;
    protected Terminal terminal;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setClient(ServiceManager client) {
        this.client = client;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    /**
     * 执行命令
     */
    public abstract void run();

    /**
     * 取消命令
     */
    public abstract void cancel();

    public void printHelp() {
        this.printHelp(this.getClass());
    }

    protected String withDefault(String str) {
        return null == str ? "-" : str;
    }

    protected String withDefault(String str, String defaultStr) {
        if (null == defaultStr) {
            defaultStr = "-";
        }
        return null == str ? defaultStr : str;
    }

    protected void print(String str) {
        System.out.print(str);
    }

    protected void printHelp(Class<?> cls) {
        AnsiLog.println("Usage:");
        Name cmd = cls.getAnnotation(Name.class);
        if (null != cmd) {
            AnsiLog.println("Command: " + cmd.value());
        }
        Summary summary = cls.getAnnotation(Summary.class);
        if (null != summary) {
            AnsiLog.println(summary.value());
        }
        Description description = cls.getAnnotation(Description.class);
        if (null != description) {
            AnsiLog.println(description.value());
        }
    }
}
