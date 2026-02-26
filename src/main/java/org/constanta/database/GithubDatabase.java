package org.constanta.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.constanta.models.UserData;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GithubDatabase {

    private final Map<String, UserData> users = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Настройки GitHub - нужно будет добавить в config.properties
    private final String repoOwner;
    private final String repoName;
    private final String branch;
    private final String githubToken;
    private final String filePath = "userdata.json";

    private String currentSha = null; // Для обновления файла

    public GithubDatabase(String repoOwner, String repoName, String branch, String githubToken) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.branch = branch;
        this.githubToken = githubToken;

        loadFromGitHub();
    }

    private void loadFromGitHub() {
        try {
            String urlString = String.format(
                    "https://api.github.com/repos/%s/%s/contents/%s?ref=%s",
                    repoOwner, repoName, filePath, branch
            );

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + githubToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Map<String, Object> responseMap = gson.fromJson(
                        response.toString(),
                        new TypeToken<Map<String, Object>>(){}.getType()
                );

                currentSha = (String) responseMap.get("sha");

                String contentBase64 = (String) responseMap.get("content");
                String content = new String(
                        Base64.getDecoder().decode(contentBase64.replace("\n", "")),
                        StandardCharsets.UTF_8
                );

                Type type = new TypeToken<Map<String, UserData>>(){}.getType();
                Map<String, UserData> loaded = gson.fromJson(content, type);

                if (loaded != null) {
                    users.clear();
                    users.putAll(loaded);
                    System.out.println("✅ Данные загружены с GitHub. Пользователей: " + users.size());
                }

            } else if (responseCode == 404) {
                System.out.println("📁 Файл данных не найден на GitHub, будет создан при сохранении");
            } else {
                System.err.println("❌ Ошибка загрузки с GitHub. Код: " + responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("❌ Ошибка при загрузке с GitHub: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveToGitHub(String commitMessage) {
        try {
            String jsonContent = gson.toJson(users);

            String encodedContent = Base64.getEncoder().encodeToString(
                    jsonContent.getBytes(StandardCharsets.UTF_8)
            );

            String urlString = String.format(
                    "https://api.github.com/repos/%s/%s/contents/%s",
                    repoOwner, repoName, filePath
            );

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Authorization", "token " + githubToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("message", commitMessage);
            requestBody.put("content", encodedContent);
            requestBody.put("branch", branch);

            if (currentSha != null) {
                requestBody.put("sha", currentSha); // Для обновления существующего файла
            }

            String requestJson = gson.toJson(requestBody);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestJson.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == 200 || responseCode == 201) {
                System.out.println("✅ Данные сохранены на GitHub");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Map<String, Object> responseMap = gson.fromJson(
                        response.toString(),
                        new TypeToken<Map<String, Object>>(){}.getType()
                );

                if (responseMap.containsKey("content")) {
                    Map<String, Object> content = (Map<String, Object>) responseMap.get("content");
                    currentSha = (String) content.get("sha");
                }

            } else {
                System.err.println("❌ Ошибка сохранения на GitHub. Код: " + responseCode);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                System.err.println("Ответ: " + response.toString());
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("❌ Ошибка при сохранении на GitHub: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public UserData getUserData(String userId) {
        return users.computeIfAbsent(userId, UserData::new);
    }

    public int getBalance(String userId) {
        return getUserData(userId).getBalance();
    }

    public void addBalance(String userId, int amount) {
        getUserData(userId).addBalance(amount);
        saveToGitHub("Update balance for user " + userId);
    }

    public boolean hasClaimedToday(String userId) {
        String today = java.time.LocalDate.now().toString();
        UserData user = users.get(userId);
        return user != null && today.equals(user.getLastFantikDate());
    }

    public void setLastClaimedToday(String userId) {
        String today = java.time.LocalDate.now().toString();
        getUserData(userId).setLastFantikDate(today);
        saveToGitHub("Update daily claim for user " + userId);
    }

    public void forceSave() {
        saveToGitHub("Auto-save at " + java.time.LocalDateTime.now());
    }
}