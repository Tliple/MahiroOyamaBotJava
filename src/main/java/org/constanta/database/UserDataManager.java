package org.constanta.database;

import org.constanta.models.UserData;

import org.constanta.models.UserData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserDataManager {

    private final Map<String, UserData> users = new ConcurrentHashMap<>();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String dataFile = "userdata.json";

    public UserDataManager() {
        loadData();
    }

    public UserData getUserData(String userId) {
        return users.computeIfAbsent(userId, UserData::new);
    }

    public int getBalance(String userId) {
        return getUserData(userId).getBalance();
    }

    public void addBalance(String userId, int amount) {
        getUserData(userId).addBalance(amount);
        saveData(); // Сохраняем после изменения
    }

    public boolean hasClaimedToday(String userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        UserData user = users.get(userId);
        return user != null && today.equals(user.getLastFantikDate());
    }

    public void setLastClaimedToday(String userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        getUserData(userId).setLastFantikDate(today);
        saveData();
    }

    private void loadData() {
        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, UserData>>(){}.getType();
            Map<String, UserData> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                users.clear();
                users.putAll(loaded);
            }
            System.out.println("Data loaded: " + dataFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        } catch (Exception e) {
            System.err.println("Data loading error: " + e.getMessage());
        }
    }

    // Сохранить данные в файл
    private void saveData() {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(users, writer);
            System.out.println("Data saved: " + dataFile);
        } catch (Exception e) {
            System.err.println("Data save error: " + e.getMessage());
        }
    }
}
