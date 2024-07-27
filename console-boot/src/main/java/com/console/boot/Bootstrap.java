package com.console.boot;

import java.io.File;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

public class Bootstrap {

    private static final File JAVA_CONSOLE_LIB_DIR = new File(System.getProperty("user.home") + File.separator + ".java-console" + File.separator + "lib");
    static {
        // agent、core jar 文件复制到 ～/.java-console/lib 目录下
        JAVA_CONSOLE_LIB_DIR.mkdirs();
//        List<String> jarNameList = new ArrayList<>();
//        jarNameList.add("java-console-agent-jar-with-dependencies.jar");
//        jarNameList.add("java-console-core-jar-with-dependencies.jar");
//
//        for (String jarName : jarNameList) {
//            try(InputStream agentJarInputStream = Bootstrap.class.getResourceAsStream("/" + jarName)){
//                if (agentJarInputStream == null) {
//                    System.out.println("Resource not found: " + jarName);
//                    System.exit(1);
//                }
//                Path targetPath = new File(JAVA_CONSOLE_LIB_DIR, jarName.replace("-jar-with-dependencies", "")).toPath();
//                Files.copy(agentJarInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
//            } catch (Exception e) {
//                System.out.println("Bootstrap.class.getResourceAsStream fail: " + e.getMessage());
//                System.exit(1);
//            }
//        }

        try {
            File file = new File("/Users/rabbit/my/project/java/code_source/java-console-agent/console-core/target/java-console-core-jar-with-dependencies.jar");
            File target = new File(JAVA_CONSOLE_LIB_DIR, "java-console-core.jar");
            Files.copy(file.toPath(), target.toPath(),  StandardCopyOption.REPLACE_EXISTING);

            file = new File("/Users/rabbit/my/project/java/code_source/java-console-agent/console-agent/target/java-console-agent-jar-with-dependencies.jar");
            target = new File(JAVA_CONSOLE_LIB_DIR, "java-console-agent.jar");
            Files.copy(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        long pid = -1;
        try {
            pid = ProcessUtils.select(false);
        } catch (InputMismatchException e) {
            System.out.println("Please input an integer to select pid.");
            System.exit(1);
        }
        if (pid < 0) {
            System.out.println("Please select an available pid.");
            System.exit(1);
        }

        List<String> attachArgs = new ArrayList<String>();
        attachArgs.add("-jar");
        attachArgs.add(new File(JAVA_CONSOLE_LIB_DIR, "java-console-core.jar").getAbsolutePath());
        attachArgs.add("" + pid);
        attachArgs.add(JAVA_CONSOLE_LIB_DIR.getAbsolutePath() + File.separator + "java-console-agent.jar");
        ProcessUtils.startConsoleCore(pid, attachArgs);

    }
}
