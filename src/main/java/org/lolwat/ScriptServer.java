package org.lolwat;

import org.dreambot.api.script.ScriptManager;
import org.dreambot.core.Instance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ScriptServer implements Runnable {
    private final int port;
    private final ScriptManager manager;
    private String lastScriptName;
    private String[] lastScriptParams;

    public ScriptServer(int port) {
        this.port = port;
        this.manager = Instance.getInstance().getScriptManager();
    }

    public void setLastScript(String scriptName, String[] scriptParams) {
        this.lastScriptName = scriptName;
        this.lastScriptParams = scriptParams;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    String[] input = in.readLine().split(",", 2);
                    String command = input[0];

                    switch (command) {
                        case "Start":
                            if (!manager.isRunning()) {
                                manager.start(lastScriptName, lastScriptParams);
                            }
                            break;
                        case "Stop":
                            if (manager.isRunning()) {
                                manager.stop();
                            }
                            break;
                        case "ChangeScript":
                            if (manager.isRunning()) {
                                manager.stop();
                                try {
                                    Thread.sleep(2000); // wait for 2 seconds
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (input.length > 1) {
                                int lastCommaIndex = input[1].lastIndexOf(",");
                                String newScriptName = input[1].substring(0, lastCommaIndex).replace(",", " ");
                                String[] newScriptParams = input[1].substring(lastCommaIndex + 1).split(",");
                                manager.start(newScriptName.trim(), newScriptParams);
                                setLastScript(newScriptName.trim(), newScriptParams);
                            }
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}