package io.github.majianzheng.jarboot.tools.client;

import io.github.majianzheng.jarboot.api.service.ServiceManager;
import io.github.majianzheng.jarboot.common.AnsiLog;
import io.github.majianzheng.jarboot.common.utils.CommandCliParser;
import io.github.majianzheng.jarboot.common.utils.StringUtils;
import io.github.majianzheng.jarboot.tools.client.command.AbstractClientCommand;
import io.github.majianzheng.jarboot.tools.client.command.ListCommand;
import io.github.majianzheng.jarboot.tools.client.command.ServiceCommand;
import org.jline.terminal.Terminal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 命令构建器
 * @author majianzheng
 */
public class ClientCommandBuilder {
    private static final Map<String, Class<? extends AbstractClientCommand>> CMDS = new ConcurrentHashMap<>(32);
    static {
        CMDS.put("list", ListCommand.class);
        CMDS.put("service", ServiceCommand.class);
    }

    public static AbstractClientCommand build(String commandLine, ServiceManager client, Terminal terminal) {
        int p = commandLine.indexOf(' ');
        String name;
        String args;
        if (-1 == p) {
            name = commandLine;
            args = StringUtils.EMPTY;
        } else {
            name = commandLine.substring(0, p);
            args = commandLine.substring(p + 1);
        }
        name = name.toLowerCase();
        AbstractClientCommand command = null;
        Class<? extends AbstractClientCommand> cls = CMDS.getOrDefault(name, null);
        if (null == cls) {
            AnsiLog.error("Command not found: {}", name);
            return null;
        }

        try {
            command = cls.getConstructor().newInstance();
            command.setClient(client);
            command.setTerminal(terminal);
            //设置命令名
            command.setName(name);
            //处理命令参数
            CommandCliParser parser = new CommandCliParser(args, command);
            parser.postConstruct();
            return command;
        } catch (Exception e) {
            AnsiLog.error(e);
            return null;
        }
    }
}
