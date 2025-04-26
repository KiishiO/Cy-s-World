package onetoone;
// Import Local classes
import onetoone.BusSystem.Bus;
import onetoone.BusSystem.BusController;
import onetoone.BusSystem.busRepository;
import onetoone.Login.LoginRepository;
import onetoone.Persons.PersonRepository;
import onetoone.Signup.SignupRepository;
import onetoone.DiningHall.DiningHallRepository;
import onetoone.TestingCenter.TestingCenterRepository;
import onetoone.TestingCenter.ExamInfoRepository;

// Import Java libraries
import java.util.List;

// Import RestAssured and JUnit
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class JaydenSystemTest {

	@LocalServerPort
	int port;

	@Before
	public void setUp() {
		RestAssured.port = port;
		RestAssured.baseURI = "http://localhost";
	}

	@Test
	public void testGetAllBuses() {
		// Send request and receive response
		Response response;
		try {
			response = RestAssured.given()
					.header("Content-Type", "application/json")
					.when()
					.get("/busOpt/all");

			// Check status code
			int statusCode = response.getStatusCode();

			// If first attempt fails, try alternative endpoint
			if (statusCode != 200) {
				System.out.println("First endpoint failed, trying alternative...");
				response = RestAssured.given()
						.header("Content-Type", "application/json")
						.when()
						.get("/busOpt/all");
				statusCode = response.getStatusCode();
			}

			assertEquals(200, statusCode);

			// Check response body for correct response
			String returnString = response.getBody().asString();
			JSONArray busArray = new JSONArray(returnString);

			// Verify that buses are returned (may be empty if no buses in db)
			System.out.println("Found " + busArray.length() + " buses in the response");

			// Check specific properties of buses if any are returned
			if (busArray.length() > 0) {
				JSONObject firstBus = busArray.getJSONObject(0);
				assertTrue(firstBus.has("busName"), "Bus should have a busName property");
				assertTrue(firstBus.has("busNum"), "Bus should have a busNum property");
			}
		} catch (JSONException e) {
			System.err.println("JSON parsing error: " + e.getMessage());
			e.printStackTrace();
			throw new AssertionError("JSON parsing failed: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Test error: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testGetBusByNumber() {
		// First add a bus to ensure we have data to test with
		String busData = "{\"busNum\":1,\"busName\":\"Test Bus One\",\"busRating\":\"A\",\"stopLocations\":[\"Campus Center\",\"Library\"]}";

		// Try to add a test bus first
		try {
			RestAssured.given()
					.header("Content-Type", "application/json")
					.body(busData)
					.when()
					.post("/busOpt/add");
		} catch (Exception e) {
			System.out.println("Could not add test bus: " + e.getMessage());
			// Continue with test anyway
		}

		// Define bus number to test
		int busNumToTest = 1;

		try {
			// Try first endpoint format
			Response response = RestAssured.given()
					.header("Content-Type", "application/json")
					.when()
					.get("/busOpt/" + busNumToTest);

			int statusCode = response.getStatusCode();

			// If first attempt fails, try alternative endpoint
			if (statusCode != 200) {
				System.out.println("First endpoint failed, trying alternative...");
				response = RestAssured.given()
						.header("Content-Type", "application/json")
						.when()
						.get("/busOpt/" + busNumToTest);
				statusCode = response.getStatusCode();
			}

			assertEquals(200, statusCode);

			// Check response body
			String returnString = response.getBody().asString();
			JSONObject busObject = new JSONObject(returnString);

			// Verify expected fields are present
			assertTrue(busObject.has("busNum"), "Bus should have a busNum property");
			assertTrue(busObject.has("busName"), "Bus should have a busName property");

		} catch (JSONException e) {
			System.err.println("JSON parsing error: " + e.getMessage());
			e.printStackTrace();
			throw new AssertionError("JSON parsing failed: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Test error: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testUpdateBusLocation() {
		// First add a bus to ensure we have data to test with
		String busData = "{\"busNum\":2,\"busName\":\"Location Test Bus\",\"busRating\":\"B\",\"stopLocations\":[\"Stop A\",\"Stop B\"],\"currentStopLocation\":\"Stop A\"}";

		// Try to add a test bus first
		try {
			RestAssured.given()
					.header("Content-Type", "application/json")
					.body(busData)
					.when()
					.post("/busOpt/add");
		} catch (Exception e) {
			System.out.println("Could not add test bus: " + e.getMessage());
			// Continue with test anyway
		}

		// Define bus number and new location
		int busNumToUpdate = 2;
		String newLocation = "Test Stop";

		try {
			// Create the request body according to what the controller expects
			String requestBody = "{\"stopLocation\":\"" + newLocation + "\"}";

			// Send the PUT request to the correct endpoint with the request body
			Response response = RestAssured.given()
					.header("Content-Type", "application/json")
					.body(requestBody)
					.when()
					.put("/busOpt/" + busNumToUpdate + "/updateStop");

			// Check status code
			int statusCode = response.getStatusCode();
			assertEquals(200, statusCode, "Expected status code 200 but got " + statusCode);

			// Check that the response contains the expected text
			String responseBody = response.getBody().asString();
			assertTrue(responseBody.contains("Stop location updated to: " + newLocation),
					"Response should contain confirmation of update");

			// Verify the bus location was actually updated by getting the bus details
			Response getResponse = RestAssured.given()
					.header("Content-Type", "application/json")
					.when()
					.get("/busOpt/" + busNumToUpdate);

			if (getResponse.getStatusCode() == 200) {
				String returnString = getResponse.getBody().asString();
				JSONObject busObject = new JSONObject(returnString);

				if (busObject.has("currentStopLocation")) {
					String actualLocation = busObject.getString("currentStopLocation");
					assertEquals(newLocation, actualLocation, "Bus location should have been updated to: " + newLocation);
				}
			}
		} catch (JSONException e) {
			System.err.println("JSON parsing error: " + e.getMessage());
			e.printStackTrace();
			throw new AssertionError("JSON parsing failed: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Test error: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testAddNewBus() {
		// Create new bus data
		String busData = "{\"busNum\":99,\"busName\":\"Test Express\",\"busRating\":\"A\",\"stopLocations\":[\"Campus Center\",\"Library\",\"Dorms\"]}";

		// Send request and receive response
		Response response = RestAssured.given()
				.header("Content-Type", "application/json")
				.body(busData)
				.when()
				.post("/busOpt/add");

		// Check status code
		int statusCode = response.getStatusCode();
		assertEquals(200, statusCode);

		// Verify the bus was added by getting all buses
		Response getAllResponse = RestAssured.given()
				.header("Content-Type", "application/json")
				.when()
				.get("/busOpt/all");

		String returnString = getAllResponse.getBody().asString();
		try {
			JSONArray busArray = new JSONArray(returnString);
			boolean foundNewBus = false;

			for (int i = 0; i < busArray.length(); i++) {
				JSONObject bus = busArray.getJSONObject(i);
				if (bus.getInt("busNum") == 99) {
					foundNewBus = true;
					assertEquals("Test Express", bus.getString("busName"));
					break;
				}
			}

			assertEquals(true, foundNewBus, "New bus should be found in the list");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testBusDeletion(){
		// Create new bus data
		String busData = "{\"busNum\":99,\"busName\":\"Test Express\",\"busRating\":\"A\",\"stopLocations\":[\"Campus Center\",\"Library\",\"Dorms\"]}";
		int busNum = 99;

		// Send request and receive response
		Response response = RestAssured.given()
				.header("Content-Type", "application/json")
				.body(busData)
				.when()
				.delete("/busOpt/" + busNum);

		// Check status code
		int statusCode = response.getStatusCode();
		assertEquals(200, statusCode);

		// Verify the bus was deleted by getting all buses
		Response getResponse = RestAssured.given().header("Content-Type", "application/json")
				.when().get("/bsuOpt/all");

		String returnString = getResponse.getBody().asString();
		try {
			JSONArray busArray = new JSONArray(returnString);
			boolean oldBusDeleted = false;

			for (int i = 0; i < busArray.length(); i++) {
				JSONObject bus = busArray.getJSONObject(i);
				if (bus.getInt("busNum") != 99) {
					oldBusDeleted = true;
					assertEquals("Test Express", bus.getString("busName"));
					break;
				}
			}

            assertTrue(oldBusDeleted, "BusNum:" + busNum + " has been deleted from the list");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


}