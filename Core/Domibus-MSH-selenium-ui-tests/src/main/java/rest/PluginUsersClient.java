package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PluginUsersClient extends BaseRestClient {

	public PluginUsersClient(String username, String password) {
		super(username, password);
	}

	public void createPluginUser(String username, String role, String pass, String domain) throws Exception {
		String payload = provider.createPluginUserObj(username, role, pass);

		switchDomain(domain);
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.PLUGIN_USERS), payload);
		if (response.getStatus() != 204) {
			throw new DomibusRestException("Could not create plugin user", response);
		}
	}

	public void createCertPluginUser(String username, String role, String domain) throws Exception {
		String payload = provider.createCertPluginUserObj(username, role);

		switchDomain(domain);
		ClientResponse response = jsonPUT(resource.path(RestServicePaths.PLUGIN_USERS), payload);
		if (response.getStatus() != 204) {
			throw new DomibusRestException("Could not create plugin user", response);
		}
	}

	public void deletePluginUser(String username, String domain) throws Exception {

		switchDomain(domain);

		String getResponse = requestGET(resource.path(RestServicePaths.PLUGIN_USERS), null).getEntity(String.class);

		JSONArray pusers = new JSONObject(sanitizeResponse(getResponse)).getJSONArray("entries");
		JSONArray toDelete = new JSONArray();


		for (int i = 0; i < pusers.length(); i++) {
			JSONObject puser = pusers.getJSONObject(i);
			if (!puser.has("userName") || puser.isNull("userName")) {
				if (StringUtils.equalsIgnoreCase(puser.getString("certificateId"), username)) {
					puser.put("status", "REMOVED");
					toDelete.put(puser);
				}
			} else {
				if (StringUtils.equalsIgnoreCase(puser.getString("userName"), username)) {
					puser.put("status", "REMOVED");
					toDelete.put(puser);
				}
			}
		}

		ClientResponse response = jsonPUT(resource.path(RestServicePaths.PLUGIN_USERS), toDelete.toString());
		if (response.getStatus() != 204) {
			throw new DomibusRestException("Could not delete plugin user", response);
		}
	}

	public void deletePluginUser(String username, String type, String domain) throws Exception {

		switchDomain(domain);

		JSONArray pusers = getPluginUsers(domain, type);
		JSONArray toDelete = new JSONArray();

		for (int i = 0; i < pusers.length(); i++) {
			JSONObject puser = pusers.getJSONObject(i);
			if (!puser.has("userName") || puser.isNull("userName")) {
				continue;
			}
			if (StringUtils.equalsIgnoreCase(puser.getString("userName"), username)) {
				puser.put("status", "REMOVED");
				toDelete.put(puser);
			}
		}

		ClientResponse response = jsonPUT(resource.path(RestServicePaths.PLUGIN_USERS), toDelete.toString());
		if (response.getStatus() != 204) {
			throw new DomibusRestException("Could not delete plugin user", response);
		}
	}

	// ----------------------------------------- Plugin Users ----------------------------------------------------------
	public JSONArray getPluginUsers(String domain, String authType) throws Exception {

		switchDomain(domain);

		HashMap<String, String> params = new HashMap<>();
		params.put("authType", authType);
		params.put("page", "0");
		params.put("pageSize", "10000");

		ClientResponse response = requestGET(resource.path(RestServicePaths.PLUGIN_USERS), params);
		if (response.getStatus() != 200) {
			throw new DomibusRestException("Could not get plugin users ", response);
		}

		try {
			String rawResp = response.getEntity(String.class);
			return new JSONObject(sanitizeResponse(rawResp)).getJSONArray("entries");
		} catch (JSONException e) {
			log.error("EXCEPTION: ", e);
		}
		return null;
	}

	public ArrayList<String> getPluginUsernameList(String authType, String domain) throws Exception {
		JSONArray users = getPluginUsers(domain, authType);
		ArrayList<String> usernames = new ArrayList<>();

		for (int i = 0; i < users.length(); i++) {
			JSONObject user = users.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(authType, "BASIC")) {
				usernames.add(user.getString("userName"));
			} else {
				usernames.add(user.getString("certificateId"));
			}
		}
		return usernames;
	}


	public ClientResponse searchPluginUsers(String domain, String authType, String role, String username, String originalUser, String page, String pageSize) throws Exception {

		switchDomain(domain);

		HashMap<String, String> params = new HashMap<>();
		HashMap<String, String> paramsNotEmpty = new HashMap<>();
		params.put("authType", authType);
		params.put("authRole", role);
		params.put("originalUser", originalUser);
		params.put("username", username);
		params.put("authType", authType);
		params.put("page", page);
		params.put("pageSize", pageSize);

		params.keySet().removeIf(k -> StringUtils.isEmpty(params.get(k)));

		ClientResponse response = requestGET(resource.path(RestServicePaths.PLUGIN_USERS), params);
		return response;
	}

	public ClientResponse updatePluginUserList(JSONArray array, String domain) throws Exception {
		switchDomain(domain);
		return jsonPUT(resource.path(RestServicePaths.PLUGIN_USERS), array.toString());
	}

	public void deactivateBasicUser(String username, String domainCode) throws Exception {
		JSONArray array = getPluginUsers(domainCode, "BASIC");

		JSONObject plu = null;

		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			if (StringUtils.equalsIgnoreCase(obj.getString("userName"), username)) {
				plu = obj;
			}
		}

		if (null == plu) {
			throw new Exception("Plugin user not found");
		}

		plu.put("status", "UPDATED");
		plu.put("active", "false");
		JSONArray toUpdate = new JSONArray();
		toUpdate.put(plu);

		ClientResponse response = updatePluginUserList(toUpdate, domainCode);
		if (response.getStatus() > 250) {
			throw new DomibusRestException("Could not deactivate plugin user", response);
		}

	}
}
