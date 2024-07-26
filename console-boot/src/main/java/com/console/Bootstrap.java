package com.console;

import java.util.InputMismatchException;

public class Bootstrap {

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
    }
}
