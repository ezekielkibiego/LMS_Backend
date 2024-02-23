package com.zeraki.co.ke.lms;
import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.zeraki.co.ke.lms.course.CourseHandler;
import com.zeraki.co.ke.lms.institution.InstitutionHandler;
import com.zeraki.co.ke.lms.student.StudentHandler;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/institutions", new InstitutionHandler());
        server.createContext("/courses", new CourseHandler());
        server.createContext("/students", new StudentHandler());
        server.setExecutor(null);
        server.start();
    }


}
