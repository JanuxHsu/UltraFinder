package com.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class UltraFinderServer {

	int port = 8080;

	ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 10, 3600, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>());

	public void startServer() {
		Server server = new Server(port);

		ServletContextHandler servletContextHandler = new ServletContextHandler();

		servletContextHandler.setContextPath("/");
		server.setHandler(servletContextHandler);

		ServletHolder servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/api/*");
		servletHolder.setInitOrder(0);
		servletHolder.setInitParameter("jersey.config.server.provider.packages", "com.server.servlets");

		UltraFinderController ultraFinderController = com.server.UltraFinderController.getInstance();

		UltraFinderHelper.controller = ultraFinderController;

		try {
			server.start();
			server.join();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		UltraFinderServer ultraFinderServer = new UltraFinderServer();
		ultraFinderServer.startServer();
	}
}
