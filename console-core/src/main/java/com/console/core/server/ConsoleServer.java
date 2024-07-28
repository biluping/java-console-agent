package com.console.core.server;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;

public class ConsoleServer {

    private static ServerSocket serverSocket;

    public static void start(Instrumentation instrumentation) {
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
                        String command = new String(buffer, 0, len);
                        System.out.println(command);
                        printStream.print("ok");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }, "java-console-handler").start();

    }
}