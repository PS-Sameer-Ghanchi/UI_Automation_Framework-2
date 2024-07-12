package utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

import org.testng.annotations.AfterSuite;

public class ZephyrScaleIntegration {

	private static long testPlanId;
	private static String testPlanKey;
	private static long cycleId;
	private static long projectId;
	private static String cycleKey;
	private static long statusId;

	@AfterSuite
	public static void publishZephyrResults() {
		createTestPlan();
		uploadTestResults(".\\target\\cucumber-results.xml");
		readStatusIdAndProjectId();
		updateTestCycle("Regression");
		linkTestCycleToTestPlan();
	}

	private static void uploadTestResults(String filePath) {
		System.out.println("Second");
		String boundary = Long.toHexString(System.currentTimeMillis());
		String CRLF = "\r\n"; // Line separator required by multipart/form-data.

		try {
			URL url = new URL(
					"https://api.zephyrscale.smartbear.com/v2/automations/executions/junit?projectKey=ZDP&autoCreateTestCases=true");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			con.setRequestProperty("Authorization",
					"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjb250ZXh0Ijp7ImJhc2VVcmwiOiJodHRwczovL2VyZHN1cHBvcnQtc2FuZGJveC0yMjYuYXRsYXNzaWFuLm5ldCIsInVzZXIiOnsiYWNjb3VudElkIjoiNjM2MWNmYjY3ZDQ2NDVhZjRmMDNhOGEzIn19LCJpc3MiOiJjb20ua2Fub2FoLnRlc3QtbWFuYWdlciIsInN1YiI6Ijc3NzNjYjk1LTJlZTYtM2IwMC04YzI0LTQ4ZmJmYjg4ODBiNyIsImV4cCI6MTc0NTMxMjk2NywiaWF0IjoxNzEzNzc2OTY3fQ.XfJTXHUdmB2nWvFUeZ1e7fbQ_l6seoFWBTHwdEyK9lw");

			try (OutputStream output = con.getOutputStream();
					PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8),
							true)) {

				// Send file part.
				writer.append("--" + boundary).append(CRLF);
				writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + filePath + "\"")
						.append(CRLF);
				writer.append("Content-Type: application/xml").append(CRLF); // Adjust the content type if necessary.
				writer.append(CRLF).flush();

				try (InputStream fileStream = new FileInputStream(filePath)) {
					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = fileStream.read(buffer)) != -1) {
						output.write(buffer, 0, bytesRead);
					}
					output.flush(); // Important! Output cannot be closed. Close of writer will close output as
									// well.
				}

				writer.append(CRLF).flush(); // CRLF is important! It denotes the end of the boundary.
				writer.append("--" + boundary + "--").append(CRLF).flush();
			}

			StringBuilder response = new StringBuilder();
			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
			}

			System.out.println("Response: " + response.toString());

			JSONObject jsonResponse = new JSONObject(response.toString());
			JSONObject testCycle = jsonResponse.getJSONObject("testCycle");
			cycleId = testCycle.getLong("id");
			cycleKey = testCycle.getString("key");

			System.out.println("ID: " + cycleId + ", Key: " + cycleKey);
			System.out.println("/Second");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void createTestPlan() {
		try {
			System.out.println("First");
			URL url = new URL("https://api.zephyrscale.smartbear.com/v2/testplans");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization",
					"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjb250ZXh0Ijp7ImJhc2VVcmwiOiJodHRwczovL2VyZHN1cHBvcnQtc2FuZGJveC0yMjYuYXRsYXNzaWFuLm5ldCIsInVzZXIiOnsiYWNjb3VudElkIjoiNjM2MWNmYjY3ZDQ2NDVhZjRmMDNhOGEzIn19LCJpc3MiOiJjb20ua2Fub2FoLnRlc3QtbWFuYWdlciIsInN1YiI6Ijc3NzNjYjk1LTJlZTYtM2IwMC04YzI0LTQ4ZmJmYjg4ODBiNyIsImV4cCI6MTc0NTMxMjk2NywiaWF0IjoxNzEzNzc2OTY3fQ.XfJTXHUdmB2nWvFUeZ1e7fbQ_l6seoFWBTHwdEyK9lw");

			String jsonInputString = "{" + "\"projectKey\": \"ZDP\"," + "\"name\": \"Dummy Test plan\","
					+ "\"labels\": [" + "    \"Regression\"," + "    \"Automated\"" + "]," + "\"Category\": ["
					+ "    \"Regression\"" + "]," + "\"Tester\": \"Sunil Kumar Thakur\"" + "}";

			try (PrintWriter writer = new PrintWriter(
					new OutputStreamWriter(con.getOutputStream(), StandardCharsets.UTF_8), true)) {
				writer.print(jsonInputString);
			}

			StringBuilder response = new StringBuilder();
			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
			}

			System.out.println("Response: " + response.toString());

			JSONObject jsonResponse = new JSONObject(response.toString());
			testPlanId = jsonResponse.getLong("id");
			testPlanKey = jsonResponse.getString("key");

			System.out.println("ID: " + testPlanId + ", Key: " + testPlanKey);

			System.out.println("/First");
			// Handle the response...
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void readStatusIdAndProjectId() {
		try {
			System.out.println("Third");
			URL url = new URL("https://api.zephyrscale.smartbear.com/v2/testcycles/"+cycleKey);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization",
					"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjb250ZXh0Ijp7ImJhc2VVcmwiOiJodHRwczovL2VyZHN1cHBvcnQtc2FuZGJveC0yMjYuYXRsYXNzaWFuLm5ldCIsInVzZXIiOnsiYWNjb3VudElkIjoiNjM2MWNmYjY3ZDQ2NDVhZjRmMDNhOGEzIn19LCJpc3MiOiJjb20ua2Fub2FoLnRlc3QtbWFuYWdlciIsInN1YiI6Ijc3NzNjYjk1LTJlZTYtM2IwMC04YzI0LTQ4ZmJmYjg4ODBiNyIsImV4cCI6MTc0NTMxMjk2NywiaWF0IjoxNzEzNzc2OTY3fQ.XfJTXHUdmB2nWvFUeZ1e7fbQ_l6seoFWBTHwdEyK9lw");

			StringBuilder response = new StringBuilder();
			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
				String responseLine = null;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}
			}

			JSONObject jsonResponse = new JSONObject(response.toString());
			projectId = jsonResponse.getJSONObject("project").getLong("id"); // Navigating through nested object to get
			statusId = 	jsonResponse.getJSONObject("status").getLong("id");																// projectId

			System.out.println("Project ID: " + projectId + "Status ID: " + statusId);
			System.out.println("/Third");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void updateTestCycle(String name) {
        try {
        	System.out.println("Fourth");
            URL url = new URL("https://api.zephyrscale.smartbear.com/v2/testcycles/" + cycleKey); // Assuming `key` is used in the URL
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjb250ZXh0Ijp7ImJhc2VVcmwiOiJodHRwczovL2VyZHN1cHBvcnQtc2FuZGJveC0yMjYuYXRsYXNzaWFuLm5ldCIsInVzZXIiOnsiYWNjb3VudElkIjoiNjM2MWNmYjY3ZDQ2NDVhZjRmMDNhOGEzIn19LCJpc3MiOiJjb20ua2Fub2FoLnRlc3QtbWFuYWdlciIsInN1YiI6Ijc3NzNjYjk1LTJlZTYtM2IwMC04YzI0LTQ4ZmJmYjg4ODBiNyIsImV4cCI6MTc0NTMxMjk2NywiaWF0IjoxNzEzNzc2OTY3fQ.XfJTXHUdmB2nWvFUeZ1e7fbQ_l6seoFWBTHwdEyK9lw");
            con.setDoOutput(true);

            String jsonInputString = String.format("{"
                    + "\"id\": \"%s\","
                    + "\"key\": \"%s\","
                    + "\"name\": \"%s\","
                    + "\"project\": {"
                    + "    \"id\": \"%s\""
                    + "},"
                    + "\"jiraProjectVersion\": {"
                    + "    \"id\": \"%s\""
                    + "},"
                    + "\"status\": {"
                    + "    \"id\": \"%s\""
                    + "}"
                    + "}", cycleId, cycleKey, name, projectId, statusId, statusId);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            System.out.println("/Fourth");
            // Handle the response...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public static void linkTestCycleToTestPlan() {
        try {
        	System.out.println("Fifth");
            URL url = new URL("https://api.zephyrscale.smartbear.com/v2/testplans/" + testPlanId + "/links/testcycles?testPlanIdOrKey=" + testPlanId);
            
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjb250ZXh0Ijp7ImJhc2VVcmwiOiJodHRwczovL2VyZHN1cHBvcnQtc2FuZGJveC0yMjYuYXRsYXNzaWFuLm5ldCIsInVzZXIiOnsiYWNjb3VudElkIjoiNjM2MWNmYjY3ZDQ2NDVhZjRmMDNhOGEzIn19LCJpc3MiOiJjb20ua2Fub2FoLnRlc3QtbWFuYWdlciIsInN1YiI6Ijc3NzNjYjk1LTJlZTYtM2IwMC04YzI0LTQ4ZmJmYjg4ODBiNyIsImV4cCI6MTc0NTMxMjk2NywiaWF0IjoxNzEzNzc2OTY3fQ.XfJTXHUdmB2nWvFUeZ1e7fbQ_l6seoFWBTHwdEyK9lw");
            con.setDoOutput(true); // Allows us to write a body for our POST request

            String jsonInputString = String.format("{"
                    + "\"testCycleIdOrKey\": \"%s\""
                    + "}", cycleId);
            
            
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            System.out.println("/Fifth");
            // Optionally, read the response from the server...

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
