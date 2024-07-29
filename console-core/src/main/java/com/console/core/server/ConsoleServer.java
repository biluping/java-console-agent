package com.console.core.server;

import com.console.core.command.Command;
import com.console.core.command.OgnlCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;

public class ConsoleServer {

    private static ServerSocket serverSocket;

    public synchronized static void start(Instrumentation instrumentation) {

        if (serverSocket != null) {
            return;
        }

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(10101);
                System.err.println("Console Server is running on port 10101...");
                Socket clientSocket = serverSocket.accept();
                System.err.println("Client connect success!");

                try (PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
                     BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    printStream.print("connect successful");

                    char[] buffer = new char[1024];
                    int len;
                    while ((len = bufferedReader.read(buffer)) != -1) {
                        String expression = new String(buffer, 0, len).trim();
                        if ("exit".equals(expression) || "quit".equals(expression)) {
                            clientSocket.close();
                            serverSocket.close();
                            serverSocket = null;
                            System.err.println("client exit");
                            break;
                        }
                        Command command = new OgnlCommand();
                        String response = command.execute(expression, instrumentation);
                        printStream.print(null == response ? "ok" : response);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }, "java-console-handler").start();

    }
}