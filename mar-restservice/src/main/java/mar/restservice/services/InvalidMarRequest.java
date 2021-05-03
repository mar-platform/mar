package mar.restservice.services;

import spark.Request;

@SuppressWarnings("serial")
public class InvalidMarRequest extends Exception {

	private final Request request;

	public InvalidMarRequest(Request req, String msg) {
		super(msg);
		this.request = req;
	}

	public Request getRequest() {
		return request;
	}
}
