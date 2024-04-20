package io.github.majianzheng.jarboot.tools.client;

import io.github.majianzheng.jarboot.api.cmd.annotation.Description;
import io.github.majianzheng.jarboot.api.cmd.annotation.Option;
import io.github.majianzheng.jarboot.api.constant.CommonConst;
import io.github.majianzheng.jarboot.api.constant.TaskLifecycle;
import io.github.majianzheng.jarboot.api.event.JarbootEvent;
import io.github.majianzheng.jarboot.api.event.Subscriber;
import io.github.majianzheng.jarboot.api.event.TaskLifecycleEvent;
import io.github.majianzheng.jarboot.api.pojo.ServiceInstance;
import io.github.majianzheng.jarboot.api.service.ServiceManager;
import io.github.majianzheng.jarboot.api.service.SettingService;
import io.github.majianzheng.jarboot.client.ClientProxy;
import io.github.majianzheng.jarboot.client.ServiceManagerClient;
import io.github.majianzheng.jarboot.client.SettingClient;
import io.github.majianzheng.jarboot.client.command.CommandExecutorFactory;
import io.github.majianzheng.jarboot.client.command.CommandExecutorService;
import io.github.majianzheng.jarboot.client.command.CommandResult;
import io.github.majianzheng.jarboot.common.AnsiLog;
import io.github.majianzheng.jarboot.common.utils.BannerUtils;
import io.github.majianzheng.jarboot.common.utils.CommandCliParser;
import io.github.majianzheng.jarboot.common.utils.StringUtils;
import io.github.majianzheng.jarboot.tools.client.command.AbstractClientCommand;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Future;

/**
 * 客户端命令行工具
 * @author jianzhengma
 */
public class JarbootClientCli {
    private String host;
    private String username;
    private String password;
    private Terminal terminal;
    private LineReader lineReader;

    @Option(shortName = "h", longName = "host")
    @Description("The Jarboot host. ig: 127.0.0.1:9899")
    public void setHost(String host) {
        this.host = host;
    }

    @Option(shortName = "u", longName = "user")
    @Description("The Jarboot username")
    public void setUsername(String username) {
        this.username = username;
    }

    @Option(shortName = "p", longName = "password")
    @Description("The Jarboot password")
    public void setPassword(String password) {
        this.password = password;
    }

    public static void main(String[] args) throws IOException {
        BannerUtils.print();
        JarbootClientCli clientCli = new JarbootClientCli();
        CommandCliParser commandCliParser = new CommandCliParser(args, clientCli);
        commandCliParser.postConstruct();
        if (StringUtils.isEmpty(clientCli.host)) {
            clientCli.host = System.getenv(CommonConst.JARBOOT_HOST_ENV);
            if (StringUtils.isEmpty(clientCli.host)) {
                clientCli.host = "127.0.0.1:9899";
            }
        }
        //登录
        clientCli.login();
        //开始执行
        clientCli.run();
    }

    private void login() throws IOException {
        AnsiLog.println("Login to Jarboot server: {}", this.host);
        if (StringUtils.isEmpty(username) && null != System.console()) {
            username = System.console().readLine("username:");
        }
        if (StringUtils.isEmpty(password) && null != System.console()) {
            password = new String(System.console().readPassword("password:"));
        }
        //登录认证
        String version = ClientProxy
                .Factory
                .createClientProxy(host, username, password)
                .getVersion();
        AnsiLog.println("Login success, jarboot server version: {}", version);
        terminal = TerminalBuilder
                .builder()
                .name("jarboot client terminal")
                .streams(System.in, System.out)
                .encoding(StandardCharsets.UTF_8)
                .color(true)
                .build();

        lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .option(LineReader.Option.ERASE_LINE_ON_FINISH, true)
                .build();
    }

    protected void run() {
        //test
        ServiceManager client = new ServiceManagerClient(this.host, null, null);
        final String prefix = ">>> ";
        final char NULL_MASK = 0;
        for (;;) {
            String inputLine = lineReader.readLine(prefix, NULL_MASK);
            if ("q".equals(inputLine) || "quit".equals(inputLine) || "exit".equals(inputLine) || "bye".equals(inputLine)) {
                break;
            }
            if (StringUtils.isEmpty(inputLine)) {
                continue;
            }
            // 打印出用户输入的内容
            AbstractClientCommand command = ClientCommandBuilder.build(inputLine, client, terminal);
            if (null != command) {
                command.run();
            }
        }
    }
}
