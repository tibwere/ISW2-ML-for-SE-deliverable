package it.uniroma2.isw2.deliverable2.exceptions;

public class MissingGithubTokenException extends Exception {
    public MissingGithubTokenException() {
        super("Cannot find the Github token needed to query the REST server. Contact the developer");
    }
}
