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

@WebServlet("/registerEvent")
public class RegisterEventServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.sendRedirect("student-login.html");
            return;
        }

        String studentId = (String) session.getAttribute("studentId");

        String eventIdText = request.getParameter("eventId");

        if (eventIdText == null || eventIdText.isEmpty()) {
            response.getWriter().println("Invalid event.");
            return;
        }

        int eventId;

        try {
            eventId = Integer.parseInt(eventIdText);
        } catch (NumberFormatException e) {
            response.getWriter().println("Invalid event ID.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            // Get the student's academic year
            String studentSql =
                    "SELECT year FROM students WHERE student_id = ?";

            String year = null;

            try (PreparedStatement ps = con.prepareStatement(studentSql)) {

                ps.setString(1, studentId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        year = rs.getString("year");
                    }
                }
            }

            if (year == null) {
                response.getWriter().println("Student details not found.");
                return;
            }


            // Check whether the student is already registered
            String checkSql =
                    "SELECT registration_id FROM registrations "
                    + "WHERE event_id = ? AND student_id = ?";

            try (PreparedStatement ps = con.prepareStatement(checkSql)) {

                ps.setInt(1, eventId);
                ps.setString(2, studentId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        response.setContentType("text/html");

                        response.getWriter().println(
                                "<h2>Already Registered</h2>"
                        );

                        response.getWriter().println(
                                "<p>You are already registered for this activity.</p>"
                        );

                        return;
                    }
                }
            }


            // Save registration
            String insertSql =
                    "INSERT INTO registrations "
                    + "(event_id, student_id, year) "
                    + "VALUES (?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(insertSql)) {

                ps.setInt(1, eventId);
                ps.setString(2, studentId);
                ps.setString(3, year);

                ps.executeUpdate();
            }


            response.setContentType("text/html");

            response.getWriter().println(
                    "<h2>Registration successful!</h2>"
            );

            response.getWriter().println(
                    "<p>You have successfully registered for the activity.</p>"
            );

            response.getWriter().println(
                    "<p><a href='available-activities.html'>Back to Available Activities</a></p>"
            );


        } catch (Exception e) {

            response.setContentType("text/html");

            response.getWriter().println(
                    "<h2>Registration failed!</h2>"
            );

            response.getWriter().println(
                    "<p>" + e.getMessage() + "</p>"
            );

            e.printStackTrace(response.getWriter());
        }
    }
}