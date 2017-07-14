package com.pearson.equella.support.oauthtester.servlets;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.pearson.equella.support.oauthtester.util.Config;
import com.pearson.equella.support.oauthtester.util.History;
import com.pearson.equella.support.oauthtester.util.UrlCaller;

public class PrimaryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(final HttpServletRequest req,
			final HttpServletResponse res) throws ServletException, IOException {
		JSONObject historyMsg = new JSONObject();
		if(Config.getDelay() > 0) {
			try {
				Thread.sleep(Config.getDelay());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		String tempCode = req.getParameter("code");
		//Resolve temporary code
		JSONObject access = UrlCaller.handleTemporaryCode(tempCode);
		historyMsg.put("date", (new Date()).toString());
		if(tempCode == null) {
			tempCode = "";
		}
		historyMsg.put("temp_code", tempCode);
		historyMsg.put("status_to_retrieve_access_token", access.getString("status"));
		historyMsg.put("access_token", access.getString("payload"));
		
		//Make REST call
		JSONObject listedCollections = UrlCaller.makeRestCall(historyMsg.getString("access_token"));
		historyMsg.put("status_to_make_rest_call", listedCollections.getString("status"));
		historyMsg.put("rest_call_results", listedCollections.getString("payload"));
		
		//Add History
		History.add(historyMsg);
		
		//Display
		String retryUrl = String.format("%soauth/authorise?response_type=code&client_id=%s&redirect_uri=%s",
				Config.getEndpoint(), Config.getClientId(), Config.getRedirectUrl());
		String style = "table, th, td {" +
				"border: 1px solid black;" +
				"border-collapse: collapse;" +
				"}" +
				"th, td {" +
				"padding: 5px;" +
				"}";
		res.getWriter()
				.append(String.format("<html><style>%s</style><body><h2>EQUELLA Support OAuth Tester v2</h2>" +
						"<h4>Note:  To check the OAuth flow worked, this test app lists the collections (Assuming LIST_COLLECTION is granted to only specific users / groups / roles).</h4>", style))
				.append(String.format("<div>Date = [%s]</div>", historyMsg.getString("date")))
				.append(String.format("<div>Temp code = [%s]</div>", historyMsg.getString("temp_code")))
				.append(String.format("<div>Endpoint status to retrieve access_token = [%s]</div>", historyMsg.get("status_to_retrieve_access_token")))
				.append(String.format("<div>access_token = [%s]</div>", historyMsg.get("access_token")))
				.append(String.format("<div>Endpoint status to retrieve collections = [%s]</div>", historyMsg.get("status_to_make_rest_call")))
				.append(String.format("<div>Collection names = %s</div>", historyMsg.get("rest_call_results")))
				.append(String.format("<br/><p>Click <a href=\"%s\">here</a> to try again.  This will navigate to [%s]</p></body></html>", retryUrl, retryUrl))
				.append(String.format("<br/><p>History:</p>%s</body></html>", History.display()))
				.append("</body></html>");
		}
}