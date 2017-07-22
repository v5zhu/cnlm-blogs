package me.cnlm.springboot.quartz.restful.service.impl;

import me.cnlm.springboot.quartz.restful.service.TaskTest;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

public class TaskTestImpl implements TaskTest {
    public final Logger log = Logger.getLogger(this.getClass());

    @Override
    public void run() {
        for (int i = 0; i < 1; i++) {
            log.info(i + " run......................................" + (new Date()));
        }

    }

    @Override
    public void run1() {

//        String url;
//        String method;
//        HttpClient httpClient = new HttpClient()

        for (int i = 0; i < 1; i++) {
            log.info(i + " run1......................................" + (new Date()));
        }
    }

    public static void main(String[] args) {
        String c = null;
        Map<String, Charset> charsets = Charset.availableCharsets();
        for (Map.Entry<String, Charset> entry : charsets.entrySet()) {
            System.out.println(entry.getKey());
        }
    }
}
