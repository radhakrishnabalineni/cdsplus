package com.hp.cdsplus.web.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.hp.cdsplus.web.exception.ResourceNotFoundException;

/**
 * @author reddypm
 * 
 */
@Provider
public class ResourceNotFoundExceptionHandler implements
		ExceptionMapper<ResourceNotFoundException> {

	@Override
	public Response toResponse(ResourceNotFoundException ex)

	{
		
		return Response.status(Response.Status.NOT_FOUND).build();

	}
}