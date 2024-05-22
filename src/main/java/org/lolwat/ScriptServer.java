package org.lolwat;

import org.dreambot.api.script.ScriptManager;
import org.dreambot.core.Instance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
                            }
                            if (input.length > 1) {
                                String[] scriptAndParams = input[1].split(" ", 2);
                                String newScriptName = scriptAndParams[0];
                                String[] newScriptParams = scriptAndParams.length > 1 ? scriptAndParams[1].split(" ") : new String[0];
                                manager.start(newScriptName, newScriptParams);
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