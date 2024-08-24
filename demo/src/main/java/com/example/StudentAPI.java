package com.example;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONObject;

import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;

public class StudentAPI {

    public static void main(String[] args) {
        // Установите порт для сервера
        port(4567);

        enableCORS("*", "*", "*");

        // Подключение к базе данных
        Connection conn = connectToDB();

        if (conn == null) {
            System.err.println("Failed to connect to the database.");
            return;
        }

        // Добавить студента
        post("/students", (request, response) -> {
            JSONObject data = new JSONObject(request.body());
            String firstName = data.getString("first_name");
            String lastName = data.getString("last_name");
            String middleName = data.optString("middle_name", "");
            String birthDate = data.getString("birth_date");
            String studentGroup = data.getString("student_group");

            String query = "INSERT INTO students (first_name, last_name, middle_name, birth_date, student_group) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, middleName);
                stmt.setDate(4, Date.valueOf(birthDate));
                stmt.setString(5, studentGroup);
                stmt.executeUpdate();
                response.status(201);
            } catch (SQLException e) {
                e.printStackTrace();
                response.status(500);
            }

            return "Student added";
        });

        // Удалить студента
        delete("/students/:id", (request, response) -> {
            int id = Integer.parseInt(request.params("id"));
            String query = "DELETE FROM students WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    response.status(404);
                    return "Student not found";
                }
                response.status(200);
            } catch (SQLException e) {
                response.status(500);
            }

            return "Student deleted";
        });

        // Вывод списка студентов
        get("/students", (request, response) -> {
            String query = "SELECT * FROM students";
            StringBuilder result = new StringBuilder();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    result.append("ID: ").append(rs.getInt("id")).append(", ")
                          .append("First Name: ").append(rs.getString("first_name")).append(", ")
                          .append("Last Name: ").append(rs.getString("last_name")).append(", ")
                          .append("Middle Name: ").append(rs.getString("middle_name")).append(", ")
                          .append("Birth Date: ").append(rs.getDate("birth_date")).append(", ")
                          .append("Group: ").append(rs.getString("student_group")).append("\n");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.status(500);
            }
            return result.toString();
        });
    }

    private static Connection connectToDB() {
        try {
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/student_db", "postgres", "1111");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Метод для разрешения CORS
    private static void enableCORS(final String origin, final String methods, final String headers) {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Allow-Methods", methods);
            response.header("Access-Control-Allow-Headers", headers);
            // Данный заголовок для предотвращения кэширования CORS-запросов
            response.header("Access-Control-Allow-Credentials", "true");
        });
    }
    
}
