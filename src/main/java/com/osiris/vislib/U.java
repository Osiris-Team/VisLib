package com.osiris.vislib;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.osiris.autoplug.core.json.exceptions.HttpErrorException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Utils.
 */
public class U {

    /**
     * Returns the json-element. This can be a json-array or a json-object.
     *
     * @param url The url which leads to the json file.
     * @return JsonElement
     * @throws Exception When status code other than 200.
     */
    public static JsonElement getGithubJsonElement(String url) throws IOException, HttpErrorException {
        HttpURLConnection con = null;
        JsonElement element;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.addRequestProperty("User-Agent", "VisLib - https://github.com/Osiris-Team/VisLib");
            con.setConnectTimeout(1000);
            con.connect();

            if (con.getResponseCode() == 200) {
                try (InputStreamReader inr = new InputStreamReader(con.getInputStream())) {
                    element = JsonParser.parseReader(inr);
                }
            } else {
                throw new HttpErrorException(con.getResponseCode(), con.getResponseMessage(), "Couldn't get the json file from: " + url);
            }
        } catch (IOException | HttpErrorException e) {
            if (con != null) con.disconnect();
            throw e;
        } finally {
            if (con != null) con.disconnect();
        }
        return element;
    }

    public static void writeJsonToFile(JsonElement el, File file) throws IOException {
        // Done bc regular toJson(writer) lets the file only half written.
        Files.write(file.toPath(), new Gson().toJson(el).getBytes(StandardCharsets.UTF_8));
    }

}
