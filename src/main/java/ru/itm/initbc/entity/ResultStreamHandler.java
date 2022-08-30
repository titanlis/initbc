package ru.itm.initbc.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class ResultStreamHandler implements Runnable {
    private InputStream inputStream;

    ResultStreamHandler(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void run() {

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            bufferedReader.lines().forEach(System.out::println);

        } catch (Throwable t) {
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
            }
        }
    }
}