package com.hp.cdsplus.web.exceptionmapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.hp.cdsplus.web.exception.ApplicationException;

/**
 * @author reddypm
 *
 */
@Provider
public class ApplicationExceptionHandler implements
		ExceptionMapper<ApplicationException> {
	@Override
	public Response toResponse(ApplicationException ex) {

		String msg = ex.getMessage();

		return Response.status(Response.Status.NOT_FOUND)
				.entity(msg).type(MediaType.TEXT_PLAIN).build();
	}
}