package com.osiris.vislib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.osiris.autoplug.core.json.exceptions.HttpErrorException;
import com.osiris.dyml.exceptions.YamlWriterException;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class Github {

    /**
     * Repos that updated within the last 24h hours, sorted by starts.
     */
    public static JsonArray freshRepos; // TODO combine search by api and lib

    static {
        try {
            File freshReposJson = new File(System.getProperty("user.dir") + "/fresh-repos.json");
            freshReposJson.createNewFile();
            freshRepos = new Gson().fromJson(new JsonReader(new BufferedReader(new FileReader(freshReposJson))), JsonArray.class);
            fetchFreshRepos(freshReposJson);
            new Thread(() -> {
                try {
                    while (true) {
                        fetchFreshRepos(freshReposJson);
                        Thread.sleep(600000);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (YamlWriterException e) {
            e.printStackTrace();
        } catch (HttpErrorException e) {
            e.printStackTrace();
        }
    }

    public static void fetchFreshRepos(File json) throws IOException, HttpErrorException, YamlWriterException {
        if (freshRepos == null || freshRepos.isEmpty() || Config.getLastFetchFreshReposTime() > Instant.now().minus(Duration.ofDays(1)).toEpochMilli()) {
            freshRepos = U.getGithubJsonElement("https://api.github.com/search/repositories?q=lib+pushed:>" +
                            DateFormatUtils.format(new Date(System.currentTimeMillis() - 86400000), "yyyy-MM-dd")
                            + "+language:c&sort=stars&order=desc")
                    .getAsJsonObject()
                    .get("items").getAsJsonArray();
            Config.setLastFetchFreshReposTime(System.currentTimeMillis());
            U.writeJsonToFile(freshRepos, json);
        }
    }

}
