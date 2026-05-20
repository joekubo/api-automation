package com.tolclin.qa.api_automation.services;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class LiteBeamConfigurator {

    public static void main(String[] args) {
        //// CHANGE PASSWORD ////
        // LiteBeamConfigurator liteBeamConfigurator = new LiteBeamConfigurator();
        // String host = "192.168.1.20";
        // String currentUsername = "root";
        // String currentPassword = "1234567Admin";
        // liteBeamConfigurator.litebeamACredChange(host, currentUsername,
        //// currentPassword);

        try {
            String verificationOutput = configureToAccessPoint(
                    "192.168.1.20",
                    "root",
                    "12345678Adm!n",
                    "My_LiteBeam_AP_TESTING",
                    "SecureWiFiKey123");

            System.out.println("Device Verification Logs:\n" + verificationOutput);
        } catch (Exception e) {
            System.err.println("Failed to provision Access Point: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public static String configureToAccessPoint(String host, String user, String password, String newSsid,
            String newWpaKey) throws Exception {
        int port = 22;
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(password);

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("server_host_key",
                "ssh-rsa,ssh-dss,ssh-ed25519,ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521,rsa-sha2-512,rsa-sha2-256");
        session.setConfig(config);

        System.out.println("Connecting to LiteBeam AC Gen 2 at " + host + "...");
        session.connect(10000);
        System.out.println("Connected successfully. Verifying parameter file blocks...");

        // BULLETPROOF AIRMAX PIPELINE STRATEGY:
        // 1. Force the radio operational key state directly into 'ap' (Access Point
        // Mode)
        // 2. Cleanly purge any older existing wireless.1 configuration tracking
        // references
        // 3. Inject explicit new target SSID and WPA keys into both file system trees
        // 4. Force a write action to burn parameters onto the system flash memory
        // layout
        String command = String.format(
                "sed -i 's!radio.1.mode=.*!radio.1.mode=ap!' /tmp/system.cfg && " +
                        "sed -i 's!radio.1.mode=.*!radio.1.mode=ap!' /var/tmp/system.cfg && " +
                        "sed -i '/radio.1.ssid=/d' /tmp/system.cfg /var/tmp/system.cfg; " +
                        "sed -i '/radio.1.wpa.key=/d' /tmp/system.cfg /var/tmp/system.cfg; " +
                        "echo 'radio.1.ssid=%s' >> /tmp/system.cfg && " +
                        "echo 'radio.1.ssid=%s' >> /var/tmp/system.cfg && " +
                        "echo 'radio.1.wpa.key=%s' >> /tmp/system.cfg && " +
                        "echo 'radio.1.wpa.key=%s' >> /var/tmp/system.cfg && " +
                        "/usr/etc/rc.d/rc.softrestart save && " +
                        "grep -E 'radio.1.mode|radio.1.ssid' /tmp/system.cfg",
                newSsid, newSsid, newWpaKey, newWpaKey);

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);

        InputStream input = channel.getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        channel.connect();

        byte[] buffer = new byte[1024]; // Safe dimension size initialized
        int length;
        while ((length = input.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }

        channel.disconnect();
        session.disconnect();
        return output.toString("UTF-8");
    }

    public void litebeamACredChange(String host, String currentUsername, String currentPassword) {

        int port = 22;
        String newUsername = "root";
        String newPassword = "12345678Adm!n";

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(currentUsername, host, port);
            session.setPassword(currentPassword);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("server_host_key",
                    "ssh-rsa,ssh-dss,ssh-ed25519,ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521,rsa-sha2-512,rsa-sha2-256");
            session.setConfig(config);

            System.out.println("Connecting to LiteBeam AC Gen 2 at " + host + "...");
            session.connect(10000);
            System.out.println("Connected successfully.");

            String command = String.format(
                    "printf '%s\\n%s\\n' | passwd && " +
                            "NEW_HASH=$(cat /etc/passwd | grep '^%s:' | cut -d: -f2) && " +
                            "sed -i \"s!users.1.name=.*!users.1.name=%s!\" /tmp/system.cfg && " +
                            "sed -i \"s!users.1.password=.*!users.1.password=$NEW_HASH!\" /tmp/system.cfg && " +
                            "sed -i \"s!users.1.name=.*!users.1.name=%s!\" /var/tmp/system.cfg && " +
                            "sed -i \"s!users.1.password=.*!users.1.password=$NEW_HASH!\" /var/tmp/system.cfg && " +
                            "/usr/etc/rc.d/rc.softrestart save && " +
                            "grep -E 'users.1.name|users.1.password' /tmp/system.cfg",
                    newPassword, newPassword, currentUsername, newUsername, newUsername);

            System.out.println("Deploying interactive kernel parameter alignment sequence...");
            String commandOutput = executeSshCommand(session, command);
            System.out.println("Device Output Verification Block: \n" + commandOutput);

            System.out.println("Process execution complete.");
            session.disconnect();

        } catch (Exception e) {
            System.err.println("An execution error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, Object> litebeamAcLogin(String username, String password) {
        Map<String, Object> response = new HashMap<>();
        return response;
    }

    private static String executeSshCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);

        InputStream input = channel.getInputStream();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        channel.connect();

        byte[] buffer = new byte[1024]; // Standard size initialization layout
        int length;
        while ((length = input.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }

        channel.disconnect();
        return output.toString("UTF-8");
    }
}
