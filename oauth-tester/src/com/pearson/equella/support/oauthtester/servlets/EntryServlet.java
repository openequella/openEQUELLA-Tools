package com.pearson.equella.support.oauthtester.servlets;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pearson.equella.support.oauthtester.util.Config;

public class EntryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(final HttpServletRequest req,
			final HttpServletResponse res) throws ServletException, IOException {
		String url = String.format("%soauth/authorise?response_type=code&client_id=%s&redirect_uri=%s",
				Config.getEndpoint(), Config.getClientId(), Config.getRedirectUrl());
				
		res.getWriter()
				.append("<html><body><h2>EQUELLA Support OAuth Tester Entry Point v2</h2>")
				.append(String.format("<p>Click <a href=\"%s\">here</a> to begin.  </p><p>This will navigate to [%s]</p></body></html>", url, url));
	}
}