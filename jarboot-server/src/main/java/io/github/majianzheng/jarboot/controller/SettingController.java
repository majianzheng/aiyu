package io.github.majianzheng.jarboot.controller;

import io.github.majianzheng.jarboot.api.constant.CommonConst;
import io.github.majianzheng.jarboot.api.pojo.SystemSetting;
import io.github.majianzheng.jarboot.api.pojo.ServiceSetting;
import io.github.majianzheng.jarboot.base.AgentManager;
import io.github.majianzheng.jarboot.common.pojo.ResponseVo;
import io.github.majianzheng.jarboot.common.pojo.ResponseSimple;
import io.github.majianzheng.jarboot.api.service.SettingService;
import io.github.majianzheng.jarboot.common.utils.HttpResponseUtils;
import io.github.majianzheng.jarboot.utils.SettingUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Set;

/**
 * 系统配置
 * @author majianzheng
 */
@RequestMapping(value = CommonConst.SETTING_CONTEXT)
@RestController
public class SettingController {
    @Resource
    private SettingService settingService;

    /**
     * 获取服务
     * @param serviceName 服务路径
     * @return 服务配置
     */
    @GetMapping(value="/serviceSetting")
    public ResponseVo<ServiceSetting> getServerSetting(String serviceName) {
        ServiceSetting results = settingService.getServiceSetting(serviceName);
        return HttpResponseUtils.success(results);
    }

    /**
     * 提交服务配置
     * @param setting 服务配置
     */
    @PostMapping(value="/serviceSetting")
    public ResponseSimple submitServerSetting(@RequestBody ServiceSetting setting) {
        settingService.submitServiceSetting(setting);
        return HttpResponseUtils.success();
    }

    /**
     * 获取全局配置
     * @return 全局配置
     */
    @GetMapping(value="/globalSetting")
    public ResponseVo<SystemSetting> getGlobalSetting() {
        SystemSetting results = settingService.getSystemSetting();
        return HttpResponseUtils.success(results);
    }

    /**
     * 提交全局配置
     * @param setting 全局配置
     * @return 提交结果
     */
    @PostMapping(value="/globalSetting")
    public ResponseSimple submitGlobalSetting(@RequestBody SystemSetting setting) {
        settingService.saveSetting(setting);
        return HttpResponseUtils.success();
    }

    /**
     * 获取服务的VM配置
     * @param serviceName 服务路径
     * @param file vm文件路径
     * @return vm配置
     */
    @GetMapping(value="/vmoptions")
    public ResponseVo<String> getVmOptions(String serviceName, String file) {
        String results = settingService.getVmOptions(serviceName, file);
        return HttpResponseUtils.success(results);
    }

    /**
     * 保存服务的JVM配置
     * @param serviceName 服务路径
     * @param file vm配置文件路径
     * @param content vm配置文件内容
     * @return 执行结果
     */
    @PostMapping(value="/vmoptions")
    public ResponseSimple saveVmOptions(String serviceName, String file, String content) {
        settingService.saveVmOptions(serviceName, file, content);
        return HttpResponseUtils.success();
    }

    /**
     * 增加信任主机
     * @param host 主机
     * @return
     */
    @PostMapping(value="/trustedHost")
    public ResponseSimple addTrustedHost(String host) {
        SettingUtils.addTrustedHost(host);
        AgentManager.getInstance().addTrustedHost(host);
        return HttpResponseUtils.success();
    }

    /**
     * 移除信任主机
     * @param host 主机
     * @return
     * @throws IOException
     */
    @DeleteMapping(value="/trustedHost")
    public ResponseSimple removeTrustedHost(String host) throws IOException {
        SettingUtils.removeTrustedHost(host);
        return HttpResponseUtils.success();
    }

    /**
     * 获取信任主机
     * @return 信任主机列表
     */
    @GetMapping(value="/trustedHost")
    public ResponseVo<Set<String>> getTrustedHosts() {
        return HttpResponseUtils.success(SettingUtils.getTrustedHosts());
    }
}
