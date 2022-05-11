package it.uniroma2.isw2.deliverable2.exceptions;

public class MaximumRequestToGithubAPIException extends Exception {
    public MaximumRequestToGithubAPIException() {
        super("The maximum Github REST API request limit has been reached" +
                "For more information please visit:\n" +
                "https://docs.github.com/en/developers/apps/building-github-apps/rate-limits-for-github-apps#default-user-to-server-rate-limits-for-githubcom ");
    }
}
