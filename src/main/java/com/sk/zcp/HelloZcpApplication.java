package com.sk.zcp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication(scanBasePackages = {"com.sk.zcp"})
public class HelloZcpApplication {

    private static final Logger logger = LoggerFactory.getLogger(HelloZcpApplication.class);

    @Autowired
    private HelloZcpService service;

    public static void main(String[] args) {
        SpringApplication.run(HelloZcpApplication.class, args);
    }

    @GetMapping("/home")
    public String home() {
        logger.info("--------- home start ----------");
        return "Hello ZCP v2";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @GetMapping("/info")
    public String info(@RequestHeader Map header, HttpServletRequest req) {
        logger.info("--------- info start ----------");
        StringBuffer sb = new StringBuffer();
        sb.append("<b>HTTP HEADER</b><br/>");
        header.forEach((key, value) -> {
            sb.append(String.format("%s : %s <br/>", key, value));
        });
        sb.append("<b>Remote</b><br/>");
        sb.append("getServerName : ").append(req.getServerName()).append("<br/>");
        sb.append("getServerPort : ").append(req.getServerPort()).append("<br/>");
        sb.append("getContextPath : ").append(req.getContextPath()).append("<br/>");
        sb.append("getServletPath : ").append(req.getServletPath()).append("<br/>");
        sb.append("getRequestURL : ").append(req.getRequestURL().toString()).append("<br/>");
        sb.append("getRemoteAddr : ").append(req.getRemoteAddr()).append("<br/>");
        sb.append("getRemoteHost : ").append(req.getRemoteHost()).append("<br/>");
        sb.append("getRemoteUser : ").append(req.getRemoteUser()).append("<br/>");
        sb.append("getRemotePort : ").append(req.getRemotePort()).append("<br/>");

        return sb.toString();
    }

    @GetMapping("/printErrorLog")
    public String error() throws Exception {
        logger.info("--------- printErrorLog start ----------");
        Exception exception = new Exception("Error service");
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        PrintWriter pw = new PrintWriter(bw);

        exception.printStackTrace(pw);
        pw.flush();
        String stacktrace = sw.toString();
        stacktrace = stacktrace.replaceAll("\n", "<br/>");
        stacktrace = stacktrace.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        pw.close();
        logger.error("print stacktrace", exception);

        System.err.println(exception.getMessage());
        return "log --------<br>" + stacktrace;
    }

    @GetMapping("/404")
    public void notFound(HttpServletResponse httpResponse) throws Exception {
        logger.info("---------- 404 start ------------");
        httpResponse.sendRedirect("/");
    }

    @GetMapping("/mdc")
    public void mdcTest1(HttpServletResponse httpResponse) throws Exception {
        logger.info("---------- mdc start ------------");
        service.method1();
        service.method2();
        service.method3();
        httpResponse.sendRedirect("/");
    }

    @SuppressWarnings("static-access")
    @GetMapping("/timeout")
    public String timeout(@RequestParam(defaultValue = "60") String timeout) {
        long start = System.currentTimeMillis();

        int sec = Integer.parseInt(timeout);
        logger.info("/timeout: start (" + sec + " sec)");
        try {
            Thread.currentThread().sleep(sec * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        long timeElapsed = end - start;
        logger.info("/timeout: finished. elapsed " + timeElapsed + " ms");

        return timeElapsed + " ms";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @GetMapping("/bigJson")
    public Map bigJson(@RequestParam(defaultValue = "1000") String amount) {
        logger.info("--------- bigJson start ----------");
        long t1 = System.nanoTime();
        Map map = new HashMap();
        map.put("dataAmount", amount);
        Integer intAmount = Integer.parseInt(amount);
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < intAmount; i++) {
            Map submap = new HashMap();
            submap.put("key", "value " + i);
            list.add(submap);
        }
        map.put("data", list);
        long t2 = System.nanoTime();
        map.put("processTime", t2 - t1);
        return map;
    }

    @GetMapping("/time")
    public String getServerTime() {
        logger.info("--------- time start ----------");
        Date d = Calendar.getInstance().getTime();
        logger.debug(d.toString());
        return d.toString();
    }

    @GetMapping("/fileread")
    public String fileread(@RequestParam String filepath) {
        logger.info("--------- fileread start ----------");

        String s = filepath;
        Path path = Paths.get(s);
        logger.info("file : " + filepath);
        try {
            Files.lines(path).forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Done. Check Log.";
    }

    @GetMapping("/nslookup")
    public ResponseEntity<Object> nslookup(@RequestParam String host) throws UnknownHostException {
        logger.info("Nslookup for - {}", host);
        InetAddress[] all = InetAddress.getAllByName(host);

        List<String> addresses = new ArrayList<>();
        for (int i = 0; i < all.length; i++) {
            InetAddress inet = all[i];
            addresses.add(inet.getHostAddress());
            logger.info("address for - {}", host);
        }

        return ResponseEntity.ok(addresses);
    }

}
