package io.github.majianzheng.jarboot.tools.common;

import io.github.majianzheng.jarboot.api.constant.CommonConst;
import io.github.majianzheng.jarboot.common.JarbootException;
import io.github.majianzheng.jarboot.common.utils.StringUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.security.CodeSource;

/**
 * @author majianzheng
 */
public class Utils {
    public static String getJarbootHome() {
        String jarbootHome = System.getenv(CommonConst.JARBOOT_HOME);
        if (StringUtils.isEmpty(jarbootHome)) {
            CodeSource codeSource = Utils.class.getProtectionDomain().getCodeSource();
            try {
                File agentJarFile = new File(codeSource.getLocation().toURI().getSchemeSpecificPart());
                jarbootHome = agentJarFile.getParentFile().getParentFile().getPath();
            } catch (Exception e) {
                throw new JarbootException("Get current path failed!" + e.getMessage(), e);
            }
        }
        if (StringUtils.isEmpty(jarbootHome)) {
            throw new JarbootException(CommonConst.JARBOOT_HOME + " env is not set!");
        }
        File home = FileUtils.getFile(jarbootHome);
        if (home.isDirectory()) {
            String homePath = home.getAbsolutePath();
            String cache = homePath + File.separator + ".cache";
            System.setProperty("pty4j.tmpdir", cache);
            System.setProperty("java.io.tmpdir", cache);
            System.setProperty("java.tmp.dir", cache);
            jarbootHome = homePath;
            System.setProperty(CommonConst.JARBOOT_HOME, jarbootHome);
        }
        return jarbootHome;
    }

    private Utils() {}
}
