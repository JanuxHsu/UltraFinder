package com.server.servlets;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Future;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

	@POST
	@Path("/setConfig")
	@Produces(MediaType.APPLICATION_JSON)
	public String setConfig(String configStr) {

		try {
			System.out.println(configStr);
			UltraFinderService.configJson = (JsonObject) jsonParser.parse(configStr);
			return gson.toJson(UltraFinderService.configJson);
		} catch (Exception e) {
			JsonObject res = new JsonObject();
			res.addProperty("status", "fail");
			res.addProperty("desciption", e.getMessage());
			return gsonPretty.toJson(res);
		}

	}

	@GET
	@Path("/getConfig")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConfig() {
		System.out.println(gsonPretty.toJson(UltraFinderService.configJson));

		if (UltraFinderService.configJson == null) {
			return gsonPretty.toJson(gson.toJsonTree(new UltraFinderConfig()));
		}
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
			try {
				UltraFinderConfig config = null;
				try {
					JsonObject configJSON = gson.fromJson(UltraFinderService.configJson, JsonObject.class);
					config = gson.fromJson(configJSON, UltraFinderConfig.class);

				} catch (Exception e) {
					e.printStackTrace();
					res.addProperty("status", "fail");
					res.addProperty("desciption", e.getStackTrace().toString());
					return gsonPretty.toJson(res);
				}

				if (config != null) {
					Future<?> job = controller.submitJob(config);

					res.addProperty("status", "success");
					res.addProperty("desciption", job.hashCode());
					return gsonPretty.toJson(res);
				} else {
					res.addProperty("status", "fail");
					res.addProperty("desciption", "unknown error");
					return gsonPretty.toJson(res);
				}

			} catch (Exception e) {
				e.printStackTrace();
				res.addProperty("status", "fail");
				res.addProperty("desciption", e.getMessage());
				return gsonPretty.toJson(res);
			}

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
			Map<String, Object> progressPackage = controller.finder.getProgress();
			res.add("detail", gson.toJsonTree(progressPackage));
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
	public Response getResultFile() {
		File file = new File(FilenameUtils.concat(System.getProperty("user.dir"), "Result.txt"));

		javax.ws.rs.core.Response res = null;

		if (file.exists()) {

			ResponseBuilder response = javax.ws.rs.core.Response.ok((Object) file);
			response.header("Content-Disposition", "attachment; filename=" + file.getName());
			res = response.build();
		}

		return res;

	}

	@GET
	@Path("/checkResultJson")
	// @Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResultJson() {
		File file = new File(FilenameUtils.concat(System.getProperty("user.dir"), "Result.txt"));

		javax.ws.rs.core.Response res = null;

		if (file.exists()) {

			ResponseBuilder response = javax.ws.rs.core.Response.ok((Object) file);

			res = response.build();
		}

		return res;

	}
}
