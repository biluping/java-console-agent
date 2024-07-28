package com.console.agent;

import java.io.File;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.net.URL;

public class JavaConsoleAgent {

    private static Instrumentation instrumentationCache;
    private static volatile ClassLoader consoleClassLoader;
    private static final File JAVA_CONSOLE_LIB_DIR = new File(System.getProperty("user.home") + File.separator + ".java-console" + File.separator + "lib");
    private static final PrintStream ps = System.err;


    public static void agentmain(String args, Instrumentation instrumentation) throws Exception {
        if (instrumentationCache != null) {
            ps.println("JavaConsoleAgent agent has been attached before");
            ps.flush();
            return;
        }

        File consoleCoreJarFile = new File(JAVA_CONSOLE_LIB_DIR, "java-console-core.jar");
        if (!consoleCoreJarFile.exists()) {
            ps.println("Can not find java-console-core.jar file from agent jar directory: " + JAVA_CONSOLE_LIB_DIR.getAbsolutePath());
            return;
        }

        Thread bindingThread = getStartServerThread(consoleCoreJarFile, instrumentation);
        bindingThread.start();
        bindingThread.join();

    }

    /**
     * 获取开启 server 的线程
     */
    private static Thread getStartServerThread(File consoleCoreJarFile, Instrumentation instrumentation) throws Exception {
        ClassLoader consoleClassloader = getConsoleClassLoader(consoleCoreJarFile);

        Thread bindingThread = new Thread(() -> {
            try {
                Thread.currentThread().setContextClassLoader(consoleClassloader);
                Class<?> serverClass = consoleClassloader.loadClass("com.console.core.server.ConsoleServer");
                serverClass.getMethod("start", Instrumentation.class).invoke(null, instrumentation);
                ps.println("console server start success");
                instrumentationCache = instrumentation;
            } catch (Throwable throwable) {
                throwable.printStackTrace(ps);
            }
        });

        bindingThread.setName("console-binding-thread");
        return bindingThread;
    }

    /**
     * 获取类加载器，保证单例，多次 attach 不会重复创建
     */
    private static ClassLoader getConsoleClassLoader(File consoleCoreJarFile) throws Exception {
        if (consoleClassLoader != null) {
            return consoleClassLoader;
        }
        consoleClassLoader = new ConsoleClassloader(new URL[]{consoleCoreJarFile.toURI().toURL()});
        return consoleClassLoader;
    }

}
