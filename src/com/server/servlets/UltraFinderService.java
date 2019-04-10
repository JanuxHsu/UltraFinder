package com.server.servlets;

import java.io.File;
import java.util.concurrent.Future;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.ws.ResponseWrapper;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.server.UltraFinderController;
import com.server.UltraFinderHelper;

import model.UltraFinderConfig;

@Path("/server")
public class UltraFinderService {

	Gson gson = new Gson();
	Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
	JsonParser jsonParser = new JsonParser();
	static char seperator = File.separatorChar;

	static JsonObject configJson = null;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String doGetAllClients() {

		JsonElement res = null;

		JsonArray resArray = new JsonArray();

		res = resArray;

		return gsonPretty.toJson(res);

	}

	@GET
	@Path("/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ResponseWrapper()
	public javax.ws.rs.core.Response download() {
		File file = new File(FilenameUtils.concat(System.getProperty("user.dir"), "hydraClient.jar"));

		javax.ws.rs.core.Response res = null;
		if (file.exists()) {
			ResponseBuilder response = javax.ws.rs.core.Response.ok((Object) file);
			response.header("Content-Disposition", "attachment; filename=" + file.getName());

			res = response.build();
		}

		return res;

	}

	@POST
	@Path("/setConfig")
	@Produces(MediaType.APPLICATION_JSON)
	public String setConfig(String configStr) {
		System.out.println(configStr);
		UltraFinderService.configJson = (JsonObject) jsonParser.parse(configStr);

		return gson.toJson(UltraFinderService.configJson);
	}

	@GET
	@Path("/getConfig")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConfig() {
		System.out.println(gsonPretty.toJson(UltraFinderService.configJson));
		return gsonPretty.toJson(UltraFinderService.configJson);

	}

	@GET
	@Path("/startFinder")
	@Produces(MediaType.APPLICATION_JSON)
	public String startFinder() {

		UltraFinderController controller = UltraFinderHelper.getController();
		JsonObject res = new JsonObject();

		if (controller.getActiveCount() > 0) {
			res.addProperty("status", "fail");
			res.addProperty("desciption", "job is running.");
		} else {
			JsonObject configJSON = gson.fromJson(UltraFinderService.configJson, JsonObject.class);

			UltraFinderConfig config = gson.fromJson(configJSON, UltraFinderConfig.class);

			Future<?> job = controller.submitJob(config);

			res.addProperty("status", "success");
			res.addProperty("desciption", job.toString());

		}

		return gsonPretty.toJson(res);
	}

	@GET
	@Path("/checkStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public String checkStatus() {

		UltraFinderController controller = UltraFinderHelper.getController();
		JsonObject res = new JsonObject();

		if (controller.getActiveCount() > 0) {
			res.addProperty("status", "running");
			res.addProperty("desciption", "job is running.");
		} else {

			res.addProperty("status", "idle");
			res.addProperty("desciption", "Ready for new Job.");

		}

		return gsonPretty.toJson(res);
	}

	@GET
	@Path("/checkResult")
	// @Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getClientVersion() {
		File file = new File(FilenameUtils.concat(System.getProperty("user.dir"), "Result.txt"));

		javax.ws.rs.core.Response res = null;

		if (file.exists()) {

			ResponseBuilder response = javax.ws.rs.core.Response.ok((Object) file);
			response.header("Content-Disposition", "attachment; filename=" + file.getName());
			res = response.build();
		}

		return res;

	}
}
