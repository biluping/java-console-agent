package com.console.core.server;

import com.console.core.command.OgnlCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConsoleServer {

    private static ServerSocket serverSocket;

    public synchronized static void start(Instrumentation instrumentation) {

        if (serverSocket != null) {
            return;
        }

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(10101);
                serverSocket.setReuseAddress(true);
                System.err.println("Console Server is running on port 10101...");
                Socket clientSocket = serverSocket.accept();
                System.err.println("Client connect success!");

                Thread hook = new Thread(() -> {
                    try {
                        if (serverSocket != null) {
                            serverSocket.close();
                        }
                    } catch (IOException ignore) {
                    }
                });
                Runtime.getRuntime().addShutdownHook(hook);

                try (PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
                     BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    printStream.println("connect successful");
                    printStream.print("[console]$ ");

                    char[] buffer = new char[1024];
                    int len;
                    while ((len = bufferedReader.read(buffer)) != -1) {
                        String expression = new String(buffer, 0, len).trim();
                        if (expression.isEmpty()) {
                            printStream.print("[console]$ ");
                            continue;
                        }
                        if ("exit".equals(expression) || "quit".equals(expression)) {
                            clientSocket.close();
                            serverSocket.close();
                            serverSocket = null;
                            Runtime.getRuntime().removeShutdownHook(hook);
                            System.err.println("client exit");
                            break;
                        }
                        if ("help".equals(expression)) {
                            List<String> list = new ArrayList<>();
                            list.add("#spring: 获取 ApplicationContext 对象");
                            list.add("bean[myBean]");
                            list.add("let aaa = 'bbb' : 存储变量和值");
                            list.add("aaa: 取出变量值");
                            list.add("@java.lang.System@out.println('12345')");
                            printStream.println(String.join("\n", list));
                            printStream.print("[console]$ ");
                            continue;
                        }
                        OgnlCommand ognlCommand = OgnlCommand.getInstance(instrumentation);
                        String response = ognlCommand.exec(expression);
                        printStream.println(null == response ? "ok" : response);
                        printStream.print("[console]$ ");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }, "java-console-handler").start();

    }
}