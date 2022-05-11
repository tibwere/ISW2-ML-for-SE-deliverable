package it.uniroma2.isw2.deliverable2.exceptions;

public class MaximumRequestToGithubAPIException extends Exception {
    public MaximumRequestToGithubAPIException() {
        super("The maximum Github REST API request limit has been reached");
    }
}
