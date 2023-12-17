package cn.lank8s.springboot.helper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadHelper {

    public static final ExecutorService executorService = Executors.newFixedThreadPool(3);
}
