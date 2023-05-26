package com.mz.jarboot.controller;

import com.mz.jarboot.api.constant.CommonConst;
import com.mz.jarboot.auth.annotation.Permission;
import com.mz.jarboot.api.pojo.PluginInfo;
import com.mz.jarboot.common.pojo.ResponseVo;
import com.mz.jarboot.common.utils.HttpResponseUtils;
import com.mz.jarboot.service.PluginsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 插件管理
 * @author majianzheng
 */
@RequestMapping(value = CommonConst.PLUGINS_CONTEXT)
@RestController
@Permission
public class PluginsController {
    @Autowired
    private PluginsService pluginsService;

    /**
     * 上传插件文件
     * @param file 文件
     * @param type 类型
     * @return 执行结果
     */
    @PostMapping
    @ResponseBody
    @Permission("Add plugin")
    public ResponseVo<String> uploadPlugin(@RequestParam("file") MultipartFile file,
                                           @RequestParam("type") String type) {
        pluginsService.uploadPlugin(file, type);
        return HttpResponseUtils.success();
    }

    /**
     * 获取插件列表
     * @return 执行结果
     */
    @GetMapping
    @ResponseBody
    public ResponseVo<List<PluginInfo>> getAgentPlugins() {
        return HttpResponseUtils.success(pluginsService.getAgentPlugins());
    }

    /**
     * 移除插件
     * @param type 插件路径
     * @param filename 文件名
     * @return 执行结果
     */
    @DeleteMapping
    @ResponseBody
    @Permission("Remove plugin")
    public ResponseVo<String> removePlugin(String type, String filename) {
        pluginsService.removePlugin(type, filename);
        return HttpResponseUtils.success();
    }
}
