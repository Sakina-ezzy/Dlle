package com.mycompany.dlle;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/myRegistrations")
public class MyRegistrationsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        if (session == null || session.getAttribute("studentId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.println("{\"error\":\"Student not logged in\"}");
            return;
        }

        String studentId = (String) session.getAttribute("studentId");

        String sql =
        "SELECT r.registration_id, r.event_id, r.registration_date, "
        + "e.title, e.description, e.event_date, e.hours, "
        + "e.attendance_status, "
        + "a.attendance_id, a.attendance_time "
        + "FROM registrations r "
        + "JOIN events e ON r.event_id = e.event_id "
        + "LEFT JOIN attendance a "
        + "ON r.event_id = a.event_id "
        + "AND r.student_id = a.student_id "
        + "WHERE r.student_id = ? "
        + "ORDER BY e.event_date ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {

                out.println("[");

                boolean first = true;

                while (rs.next()) {

                    if (!first) {
                        out.println(",");
                    }

                    out.println("{");
                    out.println("\"registrationId\":" + rs.getInt("registration_id") + ",");
                    out.println("\"eventId\":" + rs.getInt("event_id") + ",");
                    out.println("\"title\":\"" + escapeJson(rs.getString("title")) + "\",");
                    out.println("\"description\":\"" + escapeJson(rs.getString("description")) + "\",");
                    out.println("\"date\":\"" + rs.getDate("event_date") + "\",");
                    out.println("\"hours\":" + rs.getInt("hours") + ",");
                    out.println("\"attendanceStatus\":\""
                            + escapeJson(rs.getString("attendance_status")) + "\",");
                    boolean attendanceSubmitted =
        rs.getObject("attendance_id") != null;

out.println("\"attendanceSubmitted\":"
        + attendanceSubmitted + ",");

out.println("\"attendanceTime\":\""
        + escapeJson(rs.getString("attendance_time")) + "\",");
                    out.println("\"registrationDate\":\""
                            + escapeJson(rs.getString("registration_date")) + "\"");
                    out.println("}");

                    first = false;
                }

                out.println("]");

            }

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            out.println(
                    "{\"error\":\"Unable to load registrations\"}"
            );
        }
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}