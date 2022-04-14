package cn.katoumegumi.java.code.generator.utils;

import cn.katoumegumi.java.code.generator.Generator;
import cn.katoumegumi.java.common.WsStringUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FreeMarkerUtils {

    private final String templatePath;

    private final Configuration configuration;

    private final Map<String, Template> templateMap = new ConcurrentHashMap<>();


    public static void main(String[] args) {
        String path = WsStringUtils.decodeUnicode(Generator.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        System.out.println(path);
    }

    public FreeMarkerUtils(String templatePath) {
        this.templatePath = templatePath;
        this.configuration = createConfiguration(templatePath);

    }

    private static Configuration createConfiguration(String templatePath) {
        Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setClassForTemplateLoading(Generator.class, "/" + templatePath);
        return configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Template getTemplate(String templateName) {
        return templateMap.computeIfAbsent(templateName, name -> {
            try {
                return configuration.getTemplate(name);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

}
