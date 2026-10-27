package com.mycompany.dlle;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/teacherArchivedActivities")
public class TeacherArchivedActivitiesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute("teacherLoggedIn") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().print(
                    "{\"status\":\"UNAUTHORIZED\"}"
            );

            return;
        }

        String sql =
                "SELECT event_id, title, description, "
                + "event_date, hours, stream "
                + "FROM events "
                + "WHERE archived = TRUE "
                + "ORDER BY event_date DESC, event_id DESC";

        StringBuilder json =
                new StringBuilder();

        json.append("{");
        json.append("\"status\":\"SUCCESS\",");
        json.append("\"activities\":[");

        try (Connection con =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    json.append(",");
                }

                first = false;

                int eventId =
                        rs.getInt("event_id");

                String title =
                        rs.getString("title");

                String description =
                        rs.getString("description");

                String date =
                        rs.getString("event_date");

                int hours =
                        rs.getInt("hours");

                String stream =
                        rs.getString("stream");


                json.append("{");

                json.append("\"eventId\":")
                        .append(eventId)
                        .append(",");

                json.append("\"title\":\"")
                        .append(escapeJson(title))
                        .append("\",");

                json.append("\"description\":\"")
                        .append(escapeJson(description))
                        .append("\",");

                json.append("\"date\":\"")
                        .append(escapeJson(date))
                        .append("\",");

                json.append("\"hours\":")
                        .append(hours)
                        .append(",");

                json.append("\"stream\":\"")
                        .append(escapeJson(stream))
                        .append("\"");

                json.append("}");
            }

            json.append("]");
            json.append("}");

            response.getWriter().print(
                    json.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().print(
                    "{\"status\":\"ERROR\"}"
            );
        }
    }


    private String escapeJson(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}