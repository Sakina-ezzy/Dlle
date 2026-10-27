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

@WebServlet("/teacherDashboardStats")
public class TeacherDashboardStatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        // Check teacher login
        if (session == null
                || session.getAttribute("teacherLoggedIn") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().print(
                    "{\"status\":\"UNAUTHORIZED\"}"
            );

            return;
        }

        String eventsSql =
                "SELECT COUNT(*) AS total_events FROM events";

        String registrationsSql =
                "SELECT COUNT(*) AS total_registrations "
                + "FROM registrations";

        String attendanceSql =
                "SELECT COUNT(*) AS total_attendance "
                + "FROM attendance";

        try (Connection con = DBConnection.getConnection()) {

            int totalEvents = 0;
            int totalRegistrations = 0;
            int totalAttendance = 0;

            // Total activities
            try (PreparedStatement ps =
                         con.prepareStatement(eventsSql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    totalEvents = rs.getInt("total_events");
                }
            }

            // Total registrations
            try (PreparedStatement ps =
                         con.prepareStatement(registrationsSql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    totalRegistrations =
                            rs.getInt("total_registrations");
                }
            }

            // Total attendance
            try (PreparedStatement ps =
                         con.prepareStatement(attendanceSql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    totalAttendance =
                            rs.getInt("total_attendance");
                }
            }

            response.getWriter().print(
                    "{"
                    + "\"status\":\"SUCCESS\","
                    + "\"totalEvents\":" + totalEvents + ","
                    + "\"totalRegistrations\":" + totalRegistrations + ","
                    + "\"totalAttendance\":" + totalAttendance
                    + "}"
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
}