package com.github.iesen.rabbitmq.plugin.manager;

import static com.github.iesen.rabbitmq.plugin.RabbitMQConstants.ERLANG_HOME_WIN;
import static com.github.iesen.rabbitmq.plugin.RabbitMQConstants.ERLANG_INSTALLER_FILE_NAME;
import static com.github.iesen.rabbitmq.plugin.RabbitMQConstants.ERLANG_INSTALLER_URL;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.github.iesen.rabbitmq.plugin.RabbitMQConstants;
import com.google.common.collect.Lists;

/**
 *
 */
public class WindowsRabbitManager implements RabbitManager {

    private final Log log;

    public WindowsRabbitManager(Log log) {
        this.log = log;
    }

    @Override
    public boolean rabbitExtracted() {
        File rabbitHome = new File(RabbitMQConstants.getInstance().getRabbitMqHome());
        return rabbitHome.exists();
    }

    @Override
    public void extractServer() throws MojoExecutionException {
        try {
            // Erlang install required
            boolean erlangInstalled = isErlangInstalled();
            if (!erlangInstalled) {
                installErlang();
            }
            RabbitMQConstants rabbitMQConstants = RabbitMQConstants.getInstance();
            String rabbitDownloadUrl = "https://www.rabbitmq.com/releases/rabbitmq-server/v" + rabbitMQConstants.getRabbitMqVersion() + "/rabbitmq-server-windows-" + rabbitMQConstants.getRabbitMqVersion() + ".zip";
            log.debug("Downloading rabbitmq from " + rabbitDownloadUrl);
            FileUtils.download(rabbitDownloadUrl, rabbitMQConstants.getRabbitMqParentDir() + File.separator + "rabbitmq-server-windows-" + rabbitMQConstants.getRabbitMqVersion() + ".zip");
            log.debug("Extracting downloaded files");
            FileUtils.extractZip(rabbitMQConstants.getRabbitMqParentDir() + File.separator + "rabbitmq-server-windows-" + rabbitMQConstants.getRabbitMqVersion() + ".zip", rabbitMQConstants.getRabbitMqParentDir());
            // Make folder invisible
            ProcessBuilder invisibleCommand = new ProcessBuilder("attrib", "+h", rabbitMQConstants.getRabbitMqParentDir());
            invisibleCommand.start();
            // Enable management
            ProcessBuilder managementEnabler = new ProcessBuilder(rabbitMQConstants.getRabbitMqHome() + File.separator + "sbin" + File.separator + "rabbitmq-plugins.bat", "enable", "rabbitmq_management");
            managementEnabler.environment().put("ERLANG_HOME", ERLANG_HOME_WIN);
            log.debug("Enable management" + managementEnabler.command());
            Process managementProcess = managementEnabler.start();
            String statusLine;
            StringBuilder statusResult = new StringBuilder();
            BufferedReader inStatus = new BufferedReader(new InputStreamReader(managementProcess.getInputStream()));
            while ((statusLine = inStatus.readLine()) != null) {
                log.debug(statusLine);
                statusResult.append(statusLine).append('\n');
            }
            inStatus.close();
            log.debug("Management enable result : " + statusResult.toString());
        } catch (IOException e) {
            throw new MojoExecutionException("Error extracting server", e);
        }

    }

    @Override
    public void removeRabbitMq() throws MojoExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void installErlang() throws MojoExecutionException {
        try {
            RabbitMQConstants rabbitMQConstants = RabbitMQConstants.getInstance();
            File installer = new File(rabbitMQConstants.getRabbitMqParentDir() + File.separator + ERLANG_INSTALLER_FILE_NAME);
            if (!installer.exists()) {
                installer.getParentFile().mkdirs();
                FileUtils.download(ERLANG_INSTALLER_URL, rabbitMQConstants.getRabbitMqParentDir() + File.separator + ERLANG_INSTALLER_FILE_NAME);
            }
            log.info("Executing erlang installer : " + "cmd /C start /B /BELOWNORMAL /WAIT " + ERLANG_INSTALLER_FILE_NAME + " /S");
            // Call installer
            ProcessBuilder erlangInstallCommand = new ProcessBuilder("cmd", "/C", "start", "/B", "/BELOWNORMAL", "/WAIT", ERLANG_INSTALLER_FILE_NAME, "/S");
            erlangInstallCommand.directory(new File(rabbitMQConstants.getRabbitMqParentDir()));
            Process installProcess = erlangInstallCommand.start();
            int installResult = installProcess.waitFor();
            log.info("Erlang install result: " + installResult);
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Error installing erlang: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isErlangInstalled() {
        File erlangInstallFolder = new File(ERLANG_HOME_WIN + File.separator + "README");
        log.info("Checking erlang installation on " + ERLANG_HOME_WIN);
        return erlangInstallFolder.exists();
    }

    @Override
    public boolean isRabbitRunning() throws MojoExecutionException {
        try {
            RabbitMQConstants rabbitMQConstants = RabbitMQConstants.getInstance();
            String rabbitctlPath = rabbitMQConstants.getRabbitMqHome() + File.separator + "sbin" + File.separator + "rabbitmqctl.bat";
            if (!(new File(rabbitctlPath).exists())) {
                throw new MojoExecutionException("RabbitMQ has not started yet");
            }
            ProcessBuilder statusProcessBuilder = new ProcessBuilder(rabbitMQConstants.getRabbitMqHome() + File.separator + "sbin" + File.separator + "rabbitmqctl.bat", "status");
            statusProcessBuilder.environment().put("ERLANG_HOME", ERLANG_HOME_WIN);
            Process statusProcess = statusProcessBuilder.start();
            String statusLine;
            StringBuilder statusResult = new StringBuilder();
            BufferedReader inStatus = new BufferedReader(new InputStreamReader(statusProcess.getInputStream()));
            while ((statusLine = inStatus.readLine()) != null) {
                log.debug(statusLine);
                statusResult.append(statusLine).append('\n');
            }
            inStatus.close();
            return statusResult.toString().contains("pid");
        } catch (IOException e) {
            throw new MojoExecutionException("Error checking server status via rabbitmqctl", e);
        }

    }

    @Override
    public void stop() throws MojoExecutionException {
        try {
            RabbitMQConstants rabbitMQConstants = RabbitMQConstants.getInstance();
            String rabbitctlPath = rabbitMQConstants.getRabbitMqHome() + File.separator + "sbin" + File.separator + "rabbitmqctl.bat";
            if (!(new File(rabbitctlPath).exists())) {
                throw new MojoExecutionException("RabbitMQ is not started");
            }
            ProcessBuilder statusProcessBuilder = new ProcessBuilder(rabbitctlPath, "stop");
            statusProcessBuilder.environment().put("ERLANG_HOME", ERLANG_HOME_WIN);
            Process statusProcess = statusProcessBuilder.start();
            String line;
            BufferedReader inStop = new BufferedReader(new InputStreamReader(statusProcess.getInputStream()));
            while ((line = inStop.readLine()) != null) {
                log.debug(line);
            }
            inStop.close();
            statusProcess.waitFor();
        } catch (IOException e) {
            throw new MojoExecutionException("Error stopping server via rabbitmqctl", e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Error waiting server stop", e);
        }

    }

    @Override
    public void start(String port, boolean detached) throws MojoExecutionException {
        try {
            RabbitMQConstants rabbitMQConstants = RabbitMQConstants.getInstance();
            List<String> startCommand = Lists.newArrayList(rabbitMQConstants.getRabbitMqHome() + File.separator + "sbin" + File.separatorChar + "rabbitmq-server.bat");
            if (detached) {
                startCommand.add("-detached");
            }
            ProcessBuilder processBuilder = new ProcessBuilder(startCommand);
            log.info("Erlang home: " + ERLANG_HOME_WIN);
            processBuilder.environment().put("ERLANG_HOME", ERLANG_HOME_WIN);
            processBuilder.environment().put("RABBITMQ_NODE_PORT", port);
            Process start = processBuilder.start();
            String line;
            BufferedReader inStop = new BufferedReader(new InputStreamReader(start.getInputStream()));
            while ((line = inStop.readLine()) != null) {
                log.info(line);
            }
            inStop.close();
            log.info("RabbitMQ Running on: " + port);
        } catch (IOException e) {
            throw new MojoExecutionException("Error starting server", e);
        }

    }


}
