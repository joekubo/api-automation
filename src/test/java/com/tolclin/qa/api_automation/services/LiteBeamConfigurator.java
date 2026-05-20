package com.tolclin.qa.api_automation.services;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class LiteBeamConfigurator {

    public static void main(String[] args) {
        String host = "192.168.1.20"; 
        int port = 22;
        String currentUsername = "ubnt";     // Revert targets back to airOS default following hardware reset
        String currentPassword = "1234567Admin";     // Revert targets back to airOS default following hardware reset

        String newUsername = "root";
        String newPassword = "NewSecurePassword456";

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(currentUsername, host, port);
            session.setPassword(currentPassword);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("server_host_key", "ssh-rsa,ssh-dss,ssh-ed25519,ecdsa-sha2-nistp256,ecdsa-sha2-nistp384,ecdsa-sha2-nistp521,rsa-sha2-512,rsa-sha2-256");
            session.setConfig(config);

            System.out.println("Connecting to LiteBeam AC Gen 2 at " + host + "...");
            session.connect(10000); 
            System.out.println("Connected successfully.");

            // THE BULLETPROOF AIROS 8 PIPELINE EXECUTION STRATEGY:
            // 1. Pipe the password change interactively into the native passwd executable utility loop
            // 2. Isolate the freshly created kernel signature directly from the local etc layer
            // 3. Inject the keys cleanly across both system profile target variants
            // 4. Force a write block and initialize the softrestart mechanism to commit
            String command = String.format(
                "printf '%s\\n%s\\n' | passwd && " +
                "NEW_HASH=$(cat /etc/passwd | grep '^%s:' | cut -d: -f2) && " +
                "sed -i \"s!users.1.name=.*!users.1.name=%s!\" /tmp/system.cfg && " +
                "sed -i \"s!users.1.password=.*!users.1.password=$NEW_HASH!\" /tmp/system.cfg && " +
                "sed -i \"s!users.1.name=.*!users.1.name=%s!\" /var/tmp/system.cfg && " +
                "sed -i \"s!users.1.password=.*!users.1.password=$NEW_HASH!\" /var/tmp/system.cfg && " +
                "/usr/etc/rc.d/rc.softrestart save && " +
                "grep -E 'users.1.name|users.1.password' /tmp/system.cfg",
                newPassword, newPassword, currentUsername, newUsername, newUsername
            );

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
