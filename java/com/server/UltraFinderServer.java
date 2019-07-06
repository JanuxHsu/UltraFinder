package com.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class UltraFinderServer {

	int port = 8080;

	ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 10, 3600, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>());

	public UltraFinderServer(int servicePort) {
		this.port = servicePort;
	}

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
		Options options = new Options();

		Option portParam = new Option("p", "port", true, "Service Port");
		portParam.setRequired(true);
		options.addOption(portParam);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("--", options);

			System.exit(1);
		}

		String serverPort = cmd.getOptionValue("p");

		int servicePort = Integer.valueOf(serverPort);

		UltraFinderServer ultraFinderServer = new UltraFinderServer(servicePort);
		ultraFinderServer.startServer();
	}
}
