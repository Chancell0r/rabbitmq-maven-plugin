package com.github.iesen.rabbitmq.plugin;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

/**
 */
public class RabbitMQConstants {

    private static final String RABBIT_MQ_PLUGIN_DIR = ".rabbitmq_maven_plugin";

    private static final String RABBITMQ_PARENT_DIR = SystemUtils.getUserHome() + File.separator + RABBIT_MQ_PLUGIN_DIR;

    public static final String RABBITMQ_DEFAULT_PORT = "5672";

    public static final String RABBITMQ_DEFAULT_VERSION = "3.6.9";

    public static final String ERLANG_HOME_WIN = System.getenv("ProgramFiles") + File.separator + "erl8.2";
    public static final String ERLANG_INSTALLER_URL = "http://erlang.org/download/otp_win64_19.2.exe";
    public static final String ERLANG_INSTALLER_FILE_NAME = "otp_win64_19.2.exe";

    private static RabbitMQConstants rabbitMqConstants;

    private final String rabbitMqVersion;

    private final String rabbitMqParentDir;

    private final String rabbitMqHome;

    private RabbitMQConstants(String rabbitMqVersion, String rabbitMqParentDir, String rabbitMqHome) {
        this.rabbitMqVersion = rabbitMqVersion;
        this.rabbitMqParentDir = rabbitMqParentDir;
        this.rabbitMqHome = rabbitMqHome;
    }

    public static void createInstance( String rabbitMqVersion, String rabbitMqParentDir){
        if(rabbitMqConstants == null){
            String rabbitMqParentDirActual = StringUtils.isEmpty(rabbitMqParentDir) ? RABBITMQ_PARENT_DIR : rabbitMqParentDir + RABBIT_MQ_PLUGIN_DIR;
            String rabbitMqHome = rabbitMqParentDirActual + File.separator + "rabbitmq_server-" + rabbitMqVersion;
            rabbitMqConstants = new RabbitMQConstants(rabbitMqVersion, rabbitMqParentDirActual, rabbitMqHome);
        }
    }

    public static RabbitMQConstants getInstance(){
        return rabbitMqConstants;
    }

    public String getRabbitMqVersion() {
        return rabbitMqVersion;
    }

    public String getRabbitMqParentDir() {
        return rabbitMqParentDir;
    }

    public String getRabbitMqHome() {
        return rabbitMqHome;
    }
}
