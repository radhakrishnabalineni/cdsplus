package com.hp.cdsplus.web.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;





public class ErrFilter implements Filter, Blacklist {

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException  
	{
		HttpServletRequest request = (HttpServletRequest) req;
		
		String queryParam = request.getQueryString();
		String uri = request.getRequestURI();
		if(validate(uri) && validate(queryParam))
			chain.doFilter(req, res);		
		else
		{
			res.reset();
			HttpServletResponse response = (HttpServletResponse) res;
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid url found");
			return;
		}
	}
		
	
	
	

	private boolean validate(String input) 
	{
		if(!StringUtils.isEmpty(input))
		{
			for(String bl : Blacklist.blackListCharacters )
			{
				if(input.contains(bl))
					return false;
			}
		}
		return true;
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
